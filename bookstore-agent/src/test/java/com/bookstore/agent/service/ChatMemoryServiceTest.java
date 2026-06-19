package com.bookstore.agent.service;

import com.bookstore.agent.entity.ChatHistory;
import com.bookstore.agent.entity.ChatSession;
import com.bookstore.agent.mapper.ChatHistoryMapper;
import com.bookstore.agent.mapper.ChatSessionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatMemoryService 单元测试 — 验证双层存储（Redis + MySQL）逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMemoryService 对话记忆服务测试")
class ChatMemoryServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ChatHistoryMapper chatHistoryMapper;

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @InjectMocks
    private ChatMemoryService chatMemoryService;

    @Nested
    @DisplayName("保存消息")
    class SaveMessageTests {

        @Test
        @DisplayName("正常保存 — 同时写入 Redis 和 MySQL")
        void shouldSaveToBothRedisAndMySQL() {
            when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
            when(chatSessionMapper.selectById(anyString())).thenReturn(null); // session not exist
            when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1);
            when(chatHistoryMapper.insert(any(ChatHistory.class))).thenReturn(1);

            chatMemoryService.saveMessage("sess-001", "user-123", "user", "你好", null);

            // 验证 Redis 写入
            verify(listOperations).rightPush(eq("chat:history:sess-001"), anyString());
            verify(listOperations).trim(eq("chat:history:sess-001"), eq(-20L), eq(-1L));
            verify(stringRedisTemplate).expire(eq("chat:history:sess-001"), anyLong(), eq(TimeUnit.SECONDS));

            // 验证 MySQL 写入
            verify(chatHistoryMapper).insert(any(ChatHistory.class));
            verify(chatSessionMapper).insert(any(ChatSession.class));
        }

        @Test
        @DisplayName("MySQL 异常时不影响 Redis 写入 — 优雅降级")
        void shouldNotFailWhenMySQLDown() {
            when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
            when(chatSessionMapper.selectById(anyString())).thenThrow(new RuntimeException("DB connection refused"));

            assertDoesNotThrow(() ->
                chatMemoryService.saveMessage("sess-001", "user-123", "user", "测试", null)
            );

            // Redis 仍然正常写入
            verify(listOperations).rightPush(eq("chat:history:sess-001"), anyString());
        }

        @Test
        @DisplayName("已有会话时不重复创建 session 记录")
        void shouldUpdateExistingSession() {
            ChatSession existing = new ChatSession();
            existing.setSessionId("sess-001");

            when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
            when(chatSessionMapper.selectById("sess-001")).thenReturn(existing);
            when(chatSessionMapper.updateById(any())).thenReturn(1);
            when(chatHistoryMapper.insert(any())).thenReturn(1);

            chatMemoryService.saveMessage("sess-001", "user-123", "assistant", "你好！", "客服助手");

            // 更新而非创建
            verify(chatSessionMapper).updateById(any(ChatSession.class));
            verify(chatSessionMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("获取历史")
    class GetHistoryTests {

        @Test
        @DisplayName("正常获取 — 返回按时间排序的记录")
        void shouldReturnSortedHistory() {
            ChatHistory h1 = new ChatHistory();
            h1.setRole("user");
            h1.setContent("你好");
            ChatHistory h2 = new ChatHistory();
            h2.setRole("assistant");
            h2.setContent("你好！有什么可以帮你的？");

            when(chatHistoryMapper.selectList(any())).thenReturn(List.of(h1, h2));

            var result = chatMemoryService.getHistory("sess-001");

            assertEquals(2, result.size());
            assertEquals("user", result.get(0).get("role"));
            assertEquals("assistant", result.get(1).get("role"));
        }

        @Test
        @DisplayName("MySQL 异常时返回空列表")
        void shouldReturnEmptyListOnError() {
            when(chatHistoryMapper.selectList(any())).thenThrow(new RuntimeException("DB error"));

            var result = chatMemoryService.getHistory("sess-001");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("清空历史")
    class ClearHistoryTests {

        @Test
        @DisplayName("正常清空 — 同时清除 Redis 和 MySQL")
        void shouldClearBothRedisAndMySQL() {
            chatMemoryService.clearHistory("sess-001");

            verify(stringRedisTemplate).delete("chat:history:sess-001");
            verify(chatHistoryMapper).delete(any());
        }
    }
}
