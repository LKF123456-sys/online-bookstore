package com.bookstore.agent.entity; // 声明当前类所在的包：entity 包专门存放数据表对应的实体类

// 导入 MyBatis-Plus 的主键生成策略枚举，AUTO 表示数据库自增
import com.baomidou.mybatisplus.annotation.IdType;
// 导入 MyBatis-Plus 的 @TableId 注解，用于标识实体类的主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// 导入 MyBatis-Plus 的 @TableName 注解，用于指定实体类对应的数据库表名
import com.baomidou.mybatisplus.annotation.TableName;
// 导入 Lombok 的 @Data 注解，自动生成 getter/setter/toString/equals/hashCode 方法
import lombok.Data;

// 导入 Java 8 的日期时间类
import java.time.LocalDateTime;

/**
 * 聊天记录实体类 — 映射到数据库的 chat_history 表
 *
 * 表设计用途：
 *   1. 永久存储每一次对话中的每一条消息（用户消息 + AI 回复）
 *   2. 支持按 sessionId 查询一次完整对话的所有消息
 *   3. 支持按时间排序，还原对话时间线
 *   4. 通过 agentName 字段可追溯每条 AI 回复是由哪个专家 Agent 生成的
 *
 * 与 ChatSession 的关系：
 *   一条 ChatSession 对应一次完整对话，包含多条 ChatHistory 记录。
 *   ChatHistory 是「消息级」记录，ChatSession 是「会话级」记录。
 *
 * 数据生命周期：
 *   写入时：每条消息实时写入（saveMessage 时同步写入）
 *   读取时：按 sessionId + createTime 升序查询，还原对话时间线
 *   删除时：用户清空历史时按 sessionId 批量删除
 */
@Data // Lombok 注解：编译时自动生成所有字段的 getter、setter 方法，以及 toString、equals、hashCode 方法
@TableName("chat_history") // MyBatis-Plus 注解：指定此实体类对应的数据库表名是 chat_history
public class ChatHistory { // 实体类声明，实现序列化接口（由 MyBatis-Plus 在内部处理）

    /**
     * 主键ID — 数据库自增
     * 使用 Long 类型而非 Integer，支持海量消息存储
     * @TableId(type = IdType.AUTO) 表示主键由数据库自动生成（AUTO_INCREMENT）
     */
    @TableId(type = IdType.AUTO) // 标识此字段为主键，ID 生成策略为数据库自增
    private Long id; // 每条消息的唯一标识，由 MySQL 自动分配

    /**
     * 会话ID — 标识一次完整对话
     * - 同一个 sessionId 的所有消息组成一次对话的完整记录
     * - 格式：UUID 随机字符串，如 "a1b2c3d4-e5f6-..."
     * - 在 Redis 中用作 key 的一部分：chat:history:{sessionId}
     * - 前端通过此 ID 加载历史对话
     */
    private String sessionId; // UUID 格式的会话标识符，关联 ChatSession 表的 sessionId 字段

    /**
     * 消息角色
     * 枚举值及含义：
     *   - "user"：用户发送的消息
     *   - "assistant"：AI 助手（Agent）生成的回复
     *   - "system"：系统级消息（如会话创建、超时提示等）
     * 用于前端区分消息样式（用户消息靠右、AI 消息靠左）
     * 遵循 OpenAI Chat Completion API 的 role 字段约定
     */
    private String role; // 角色标识符，取值为 user / assistant / system 之一

    /**
     * 消息正文内容
     * - 用户消息：用户输入的原始文本
     * - AI 回复：Agent 经过 Tool Calling + LLM 推理后生成的完整回复文本
     * - 系统消息：系统自动生成的提示信息
     * 使用 MySQL 的 TEXT 类型存储，支持长文本（如 AI 生成长篇书评推荐）
     */
    private String content; // 消息的完整文本内容，长度无明确限制（数据库对应 TEXT 类型）

    /**
     * 负责生成此消息的 Agent 名称
     * - 仅当 role = "assistant" 时有值
     * - 取值示例："客服助手" / "图书推荐顾问" / "评价分析师" / "通用助手"
     * - 为 null 时表示用户消息或系统消息
     * - 用于前端展示当前由哪个专家 Agent 提供服务
     */
    private String agentName; // Agent 的中文名称，仅 AI 回复消息有值，用于前端展示

    /**
     * 消息创建时间
     * - 记录消息产生的精确时间，精确到纳秒（取决于数据库精度）
     * - 用于按时间排序还原对话时间线
     * - 也用于判断会话是否过期（超过 TTL 的会话可被清理）
     */
    private LocalDateTime createTime; // 消息创建的时间戳，用于排序和过期判断
}
