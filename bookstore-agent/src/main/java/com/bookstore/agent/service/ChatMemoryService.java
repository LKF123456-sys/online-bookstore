package com.bookstore.agent.service;

import com.bookstore.agent.entity.ChatHistory;
import com.bookstore.agent.entity.ChatSession;
import com.bookstore.agent.mapper.ChatHistoryMapper;
import com.bookstore.agent.mapper.ChatSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 对话记忆服务 — Redis 短期窗口 + MySQL 长期归档
 *
 * 设计说明：
 *   1. Redis 存储最近 N 条消息（滑动窗口），用于 LLM 上下文注入
 *   2. MySQL 存储完整对话记录，用于历史查询和数据分析
 *   3. 会话有 TTL（空闲超时），过期后 Redis 缓存自动清理
 *
 * 面试亮点：
 *   1. 双层存储：热数据在 Redis（毫秒级读取），冷数据在 MySQL（持久化）
 *   2. 滑动窗口：只保留最近 N 条消息给 LLM，控制 Token 消耗
 *   3. 会话管理：session 维度的 TTL 控制，自动清理过期数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatSessionMapper chatSessionMapper;

    private static final String REDIS_KEY_PREFIX = "chat:history:";

    @Value("${agent.memory.max-history:20}")
    private int maxHistory;

    @Value("${agent.memory.session-ttl:3600}")
    private int sessionTtl;

    /**
     * 保存消息到 Redis + MySQL
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param role      角色：user / assistant / system
     * @param content   消息内容
     * @param agentName Agent 名称（仅 assistant 消息有值）
     */
    public void saveMessage(String sessionId, String userId, String role, String content, String agentName) {
        // 1. 保存到 Redis 滑动窗口
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String messageJson = String.format("{\"role\":\"%s\",\"content\":%s,\"agentName\":%s}",
                role, toJsonString(content),
                agentName != null ? "\"" + agentName + "\"" : "null");
        stringRedisTemplate.opsForList().rightPush(redisKey, messageJson);
        stringRedisTemplate.opsForList().trim(redisKey, -maxHistory, -1); // 保留最后 N 条
        stringRedisTemplate.expire(redisKey, sessionTtl, TimeUnit.SECONDS);

        // 2. 保存到 MySQL 长期归档
        try {
            // 确保会话存在
            ensureSession(sessionId, userId);

            ChatHistory history = new ChatHistory();
            history.setSessionId(sessionId);
            history.setRole(role);
            history.setContent(content);
            history.setAgentName(agentName);
            history.setCreateTime(LocalDateTime.now());
            chatHistoryMapper.insert(history);
        } catch (Exception e) {
            log.warn("MySQL 保存聊天记录失败（不影响对话）: {}", e.getMessage());
        }
    }

    /**
     * 获取会话历史（从 MySQL 读取完整记录）
     */
    public List<Map<String, Object>> getHistory(String sessionId) {
        try {
            LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatHistory::getSessionId, sessionId)
                    .orderByAsc(ChatHistory::getCreateTime);
            List<ChatHistory> records = chatHistoryMapper.selectList(wrapper);

            List<Map<String, Object>> result = new ArrayList<>();
            for (ChatHistory h : records) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("role", h.getRole());
                map.put("content", h.getContent());
                map.put("agentName", h.getAgentName());
                map.put("createTime", h.getCreateTime());
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            log.warn("获取聊天历史失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 清空会话历史（Redis + MySQL）
     */
    public void clearHistory(String sessionId) {
        // 清空 Redis
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + sessionId);

        // 清空 MySQL
        try {
            LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatHistory::getSessionId, sessionId);
            chatHistoryMapper.delete(wrapper);
        } catch (Exception e) {
            log.warn("清空 MySQL 聊天记录失败: {}", e.getMessage());
        }
    }

    /**
     * 获取 Redis 中的近期消息（用于注入 LLM 上下文）
     * 返回格式化的历史消息文本
     */
    public String getRecentContext(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        List<String> messages = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("对话历史：\n");
        for (String msg : messages) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }

    /**
     * 确保会话记录存在（不存在则创建）
     */
    private void ensureSession(String sessionId, String userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setTitle("新对话");
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        } else {
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.updateById(session);
        }
    }

    /**
     * 简单的 JSON 字符串转义
     */
    private String toJsonString(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
