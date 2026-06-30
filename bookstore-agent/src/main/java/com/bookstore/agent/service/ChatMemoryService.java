package com.bookstore.agent.service; // service 包：存放业务服务层代码，编排 Entity/Mapper 及其他组件

// 导入聊天记录实体类 ChatHistory — 对应 chat_history 表
import com.bookstore.agent.entity.ChatHistory;
// 导入会话实体类 ChatSession — 对应 chat_session 表
import com.bookstore.agent.entity.ChatSession;
// 导入聊天记录 Mapper — 操作 chat_history 表的 DAO 接口
import com.bookstore.agent.mapper.ChatHistoryMapper;
// 导入会话 Mapper — 操作 chat_session 表的 DAO 接口
import com.bookstore.agent.mapper.ChatSessionMapper;
// MyBatis-Plus 的 LambdaQueryWrapper — 类型安全的条件构造器，避免手写字段名
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// Lombok 构造器注入
import lombok.RequiredArgsConstructor;
// Lombok 日志
import lombok.extern.slf4j.Slf4j;
// Spring @Value 注解 — 从配置中注入属性值
import org.springframework.beans.factory.annotation.Value;
// StringRedisTemplate — Redis 的字符串操作模板，支持 List、Value、Hash 等操作
import org.springframework.data.redis.core.StringRedisTemplate;
// Spring @Service 注解 — 标记为业务层 Bean
import org.springframework.stereotype.Service;

// Java 时间类
import java.time.LocalDateTime;
// Java 集合工具类
import java.util.*;
// Java 并发时间工具
import java.util.concurrent.TimeUnit;

/**
 * 对话记忆服务 — 双层存储架构：Redis 热数据 + MySQL 冷数据
 *
 * 为什么需要双层存储？
 *   ┌──────────┬────────────────────────────┬─────────────────────────────┐
 *   │ 存储层   │ 用途                        │ 特点                        │
 *   ├──────────┼────────────────────────────┼─────────────────────────────┤
 *   │ Redis    │ LLM 上下文注入（getRecentContext）│ 毫秒级读取，自动过期，内存级性能│
 *   │ MySQL    │ 历史记录持久化（getHistory）    │ 数据不丢失，支持复杂查询    │
 *   └──────────┴────────────────────────────┴─────────────────────────────┘
 *
 * Redis 滑动窗口设计：
 *   使用 Redis List 数据结构，LPUSH 新消息，LTRIM 保留最近 N 条。
 *   例如 maxHistory=20，则只保留最近 20 条消息在 Redis 中，
 *   超出的自动丢弃。这样控制 LLM 上下文长度，减少 Token 消耗。
 *
 * 会话 TTL 机制：
 *   每次保存消息时刷新 Redis Key 的过期时间（sessionTtl 秒）。
 *   如果用户长时间不对话，Key 自动过期，清理内存。
 *
 * 面试亮点：
 *   1. 双层存储架构：Redis 热数据（性能） + MySQL 冷数据（持久化）
 *   2. 滑动窗口：LRANGE + LTRIM 实现固定大小的上下文窗口，控制 Token 消耗
 *   3. 写分离：Redis 写入必然成功，MySQL 异常不影响对话流程（try-catch 包裹）
 *   4. 会话管理：ensureSession 自动创建 + 更新 updateTime，支持会话列表查询
 */
@Slf4j // Lombok：自动生成 log
@Service // Spring：标记为业务层 Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class ChatMemoryService { // 对话记忆服务

    private final StringRedisTemplate stringRedisTemplate; // Redis 字符串操作模板，用于 List 的 push/pop/trim/range
    private final ChatHistoryMapper chatHistoryMapper; // 聊天记录 DAO（MySQL）
    private final ChatSessionMapper chatSessionMapper; // 会话 DAO（MySQL）

    private static final String REDIS_KEY_PREFIX = "chat:history:"; // Redis Key 前缀，完整 Key 格式：chat:history:{sessionId}

    @Value("${agent.memory.max-history:20}") // 从配置注入滑动窗口大小，默认 20 条
    private int maxHistory; // Redis 中保留的最大历史消息条数

    @Value("${agent.memory.session-ttl:3600}") // 从配置注入会话过期时间，默认 3600 秒（1小时）
    private int sessionTtl; // Redis Key 的 TTL（秒）

    /**
     * 保存消息 — 双层写入（Redis + MySQL）
     *
     * 写入流程：
     *   1. Redis：RPUSH 追加消息 → LTRIM 截断窗口 → EXPIRE 刷新 TTL
     *   2. MySQL：ensureSession（确保会话存在）→ insert 消息记录
     *
     * 容错设计：
     *   Redis 写入失败时直接抛异常（依赖 Redis 高可用）。
     *   MySQL 写入失败时 catch 异常仅记录日志，不中断对话。
     *   这是有意为之：历史记录丢失不影响当前对话体验。
     *
     * @param sessionId 会话 ID，与 Redis Key 绑定
     * @param userId 用户 ID
     * @param role 消息角色："user" / "assistant" / "system"
     * @param content 消息完整文本内容
     * @param agentName Agent 名称（仅 assistant 消息有值，user 消息传 null）
     */
    public void saveMessage(String sessionId, String userId, String role, String content, String agentName) { // 保存消息方法
        // ==================== 1. 保存到 Redis 滑动窗口 ====================
        String redisKey = REDIS_KEY_PREFIX + sessionId; // 拼接 Redis Key：chat:history:{sessionId}
        // 将消息序列化为简易 JSON 格式（不使用 Jackson，减少序列化开销）
        String messageJson = String.format("{\"role\":\"%s\",\"content\":%s,\"agentName\":%s}", // JSON 模板
                role, toJsonString(content), // role 直接插入，content 需要 JSON 转义
                agentName != null ? "\"" + agentName + "\"" : "null"); // agentName 为 null 时输出 null（JSON 关键字）
        stringRedisTemplate.opsForList().rightPush(redisKey, messageJson); // RPUSH：追加到 List 尾部
        stringRedisTemplate.opsForList().trim(redisKey, -maxHistory, -1); // LTRIM：保留最后 maxHistory 条（负数索引从尾部计数）
        stringRedisTemplate.expire(redisKey, sessionTtl, TimeUnit.SECONDS); // EXPIRE：设置 TTL，每次写入都刷新

        // ==================== 2. 保存到 MySQL 长期归档 ====================
        try { // MySQL 写入失败不影响对话
            // 确保会话记录存在（不存在则创建，存在则更新 updateTime）
            ensureSession(sessionId, userId); // 调用 ensureSession 方法

            ChatHistory history = new ChatHistory(); // 创建 ChatHistory 实体对象
            history.setSessionId(sessionId); // 设置会话 ID
            history.setRole(role); // 设置消息角色
            history.setContent(content); // 设置消息内容
            history.setAgentName(agentName); // 设置 Agent 名称（null 表示用户或系统消息）
            history.setCreateTime(LocalDateTime.now()); // 设置创建时间为当前时间
            chatHistoryMapper.insert(history); // MyBatis-Plus 的 insert 方法，执行 INSERT INTO
        } catch (Exception e) { // 捕获 MySQL 写入异常
            log.warn("MySQL 保存聊天记录失败（不影响对话）: {}", e.getMessage()); // 仅记录警告，不向上抛异常
        }
    }

    /**
     * 获取会话历史 — 从 MySQL 读取完整记录
     *
     * 为什么从 MySQL 读取而非 Redis？
     *   Redis 只有最近 N 条消息（滑动窗口），不足以展示完整历史。
     *   MySQL 保存了会话的全部消息，按时间排序可还原完整对话。
     *
     * 返回格式：List<Map<String, Object>>，每条 Map 包含：
     *   - role: "user" / "assistant" / "system"
     *   - content: 消息内容
     *   - agentName: Agent 名称（可能为 null）
     *   - createTime: ISO 格式时间
     *
     * @param sessionId 会话 ID
     * @return 按时间升序排列的消息列表，查询失败返回空列表
     */
    public List<Map<String, Object>> getHistory(String sessionId) { // 获取会话历史方法
        try { // 异常捕获
            // 使用 LambdaQueryWrapper 构建类型安全的查询条件
            LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>(); // 创建条件构造器
            wrapper.eq(ChatHistory::getSessionId, sessionId) // WHERE session_id = ?
                    .orderByAsc(ChatHistory::getCreateTime); // ORDER BY create_time ASC
            List<ChatHistory> records = chatHistoryMapper.selectList(wrapper); // 执行查询，返回实体列表

            List<Map<String, Object>> result = new ArrayList<>(); // 构建响应列表
            for (ChatHistory h : records) { // 遍历实体列表
                Map<String, Object> map = new LinkedHashMap<>(); // LinkedHashMap 保持插入顺序
                map.put("role", h.getRole()); // 消息角色
                map.put("content", h.getContent()); // 消息内容
                map.put("agentName", h.getAgentName()); // Agent 名称
                map.put("createTime", h.getCreateTime()); // 创建时间
                result.add(map); // 添加到结果列表
            }
            return result; // 返回历史消息列表
        } catch (Exception e) { // 异常捕获
            log.warn("获取聊天历史失败: {}", e.getMessage()); // 记录警告
            return List.of(); // 返回空列表（Java 9+ 不可变空 List）
        }
    }

    /**
     * 清空会话历史 — 同时清理 Redis 和 MySQL
     *
     * @param sessionId 会话 ID
     */
    public void clearHistory(String sessionId) { // 清空历史方法
        // 清空 Redis（删除整个 List Key）
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + sessionId); // DEL chat:history:{sessionId}

        // 清空 MySQL（按 sessionId 删除所有消息记录）
        try { // MySQL 操作异常捕获
            LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>(); // 创建条件构造器
            wrapper.eq(ChatHistory::getSessionId, sessionId); // WHERE session_id = ?
            chatHistoryMapper.delete(wrapper); // DELETE FROM chat_history WHERE session_id = ?
        } catch (Exception e) { // 异常捕获
            log.warn("清空 MySQL 聊天记录失败: {}", e.getMessage()); // 记录警告
        }
    }

    /**
     * 获取 Redis 中的近期消息（用于注入 LLM 上下文）
     *
     * 这是 RAG 之外的另一类上下文注入：对话历史记忆。
     * 从 Redis 读取滑动窗口内的所有消息，拼接为格式化文本返回，
     * Agent 将此文本拼接到用户消息前（如 ProductRecommendAgent.buildEnrichedMessage）。
     *
     * 注意：sessionId 参数当前未使用，ChatMemoryService 通过 ThreadLocal 获取。
     * 实际调用时传入 null，方法从 ThreadLocal 中提取当前请求的 sessionId。
     *
     * @param sessionId 会话 ID（传 null，实测通过 ThreadLocal 获取）
     * @return 格式化的历史对话文本，如 "对话历史：\n{\"role\":\"user\"...}"
     */
    public String getRecentContext(String sessionId) { // 获取近期上下文字符串
        String redisKey = REDIS_KEY_PREFIX + sessionId; // 拼接 Redis Key
        List<String> messages = stringRedisTemplate.opsForList().range(redisKey, 0, -1); // LRANGE 获取 List 全部元素
        if (messages == null || messages.isEmpty()) { // 检查是否有消息
            return ""; // 无消息返回空字符串
        }
        StringBuilder sb = new StringBuilder("对话历史：\n"); // 历史上下文开头标记
        for (String msg : messages) { // 遍历 Redis 中的消息
            sb.append(msg).append("\n"); // 逐条追加（msg 已是 JSON 格式字符串）
        }
        return sb.toString(); // 返回拼接后的完整历史上下文
    }

    /**
     * 确保会话记录存在 — 不存在则创建，存在则更新 updateTime
     *
     * 每次保存消息时调用，实现会话的自动创建和活跃时间更新。
     * 如果会话不存在：创建一个新会话，标题默认 "新对话"。
     * 如果会话已存在：只更新 updateTime 字段，标记最后活跃时间。
     *
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     */
    private void ensureSession(String sessionId, String userId) { // 私有方法：确保会话存在
        ChatSession session = chatSessionMapper.selectById(sessionId); // 根据主键（sessionId）查询会话
        if (session == null) { // 会话不存在 — 创建新会话
            session = new ChatSession(); // 创建 ChatSession 实体
            session.setSessionId(sessionId); // 设置主键（UUID，由调用方生成）
            session.setUserId(userId); // 设置所属用户
            session.setTitle("新对话"); // 默认标题，后续可被第一条消息替换
            session.setCreateTime(LocalDateTime.now()); // 设置创建时间
            session.setUpdateTime(LocalDateTime.now()); // 设置更新时间
            chatSessionMapper.insert(session); // INSERT INTO chat_session
        } else { // 会话已存在 — 更新活跃时间
            session.setUpdateTime(LocalDateTime.now()); // 更新最后活跃时间
            chatSessionMapper.updateById(session); // UPDATE chat_session SET update_time = ? WHERE session_id = ?
        }
    }

    /**
     * 简单的 JSON 字符串转义
     * 将 Java 字符串转换为合法的 JSON 字符串值（加双引号 + 特殊字符转义）。
     * 用于手写 JSON 序列化（避免引入 Jackson ObjectMapper 的性能开销）。
     *
     * @param value 原始字符串
     * @return JSON 转义后的字符串，如 "Hello\nWorld"
     */
    private String toJsonString(String value) { // JSON 转义私有方法
        if (value == null) return "null"; // null → JSON null 关键字（不带引号）
        return "\"" + value.replace("\\", "\\\\") // 转义反斜杠 → \\
                .replace("\"", "\\\"") // 转义双引号 → \"
                .replace("\n", "\\n") // 转义换行 → \n
                .replace("\r", "\\r") // 转义回车 → \r
                .replace("\t", "\\t") + "\""; // 转义制表 → \t，末尾加闭合双引号
    }
}
