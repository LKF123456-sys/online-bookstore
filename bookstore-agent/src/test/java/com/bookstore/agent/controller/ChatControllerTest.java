package com.bookstore.agent.controller;

import com.bookstore.agent.agent.CustomerServiceAgent;
import com.bookstore.agent.agent.OrchestratorAgent;
import com.bookstore.agent.agent.ProductRecommendAgent;
import com.bookstore.agent.agent.ReviewAnalysisAgent;
import com.bookstore.agent.service.ChatMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatController 单元测试 — 验证 REST API 端点行为
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController 控制器测试")
class ChatControllerTest {

    @Mock private OrchestratorAgent orchestratorAgent;
    @Mock private CustomerServiceAgent customerServiceAgent;
    @Mock private ProductRecommendAgent productRecommendAgent;
    @Mock private ReviewAnalysisAgent reviewAnalysisAgent;
    @Mock private ChatMemoryService chatMemoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(
                orchestratorAgent, customerServiceAgent,
                productRecommendAgent, reviewAnalysisAgent, chatMemoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    @DisplayName("POST /api/agent/chat — 同步对话")
    class SyncChatTests {

        @Test
        @DisplayName("正常对话 — 返回 200 + 回复内容")
        void shouldReturnChatResponse() throws Exception {
            when(orchestratorAgent.chat(anyString(), anyString()))
                    .thenReturn("你好！我是 BookVerse 智能助手。");
            when(orchestratorAgent.getRoutedAgentName(anyString()))
                    .thenReturn("通用助手");

            mockMvc.perform(post("/api/agent/chat")
                    .header("X-User-Id", "user-123")
                    .contentType("application/json")
                    .content("{\"message\":\"你好\",\"sessionId\":\"sess-001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.reply").exists())
                    .andExpect(jsonPath("$.data.agentName").value("通用助手"));

            // 验证记忆保存：1次用户消息 + 1次助手消息
            verify(chatMemoryService, times(2)).saveMessage(
                    anyString(), anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("指定 agentType=customer_service — 路由到客服 Agent")
        void shouldRouteToCustomerServiceAgent() throws Exception {
            when(customerServiceAgent.chat(anyString(), anyString()))
                    .thenReturn("您的订单已发货。");
            when(customerServiceAgent.getName())
                    .thenReturn("客服助手");

            mockMvc.perform(post("/api/agent/chat")
                    .header("X-User-Id", "user-123")
                    .contentType("application/json")
                    .content("{\"message\":\"我的订单到哪了\",\"sessionId\":\"sess-001\",\"agentType\":\"customer_service\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.agentName").value("客服助手"));

            verify(customerServiceAgent).chat(anyString(), anyString());
            verify(orchestratorAgent, never()).chat(anyString(), anyString());
        }

        @Test
        @DisplayName("指定 agentType=product_recommend — 路由到推荐 Agent")
        void shouldRouteToProductRecommendAgent() throws Exception {
            when(productRecommendAgent.chat(anyString(), anyString()))
                    .thenReturn("为您推荐《深入理解Java虚拟机》。");
            when(productRecommendAgent.getName())
                    .thenReturn("图书推荐顾问");

            mockMvc.perform(post("/api/agent/chat")
                    .header("X-User-Id", "user-123")
                    .contentType("application/json")
                    .content("{\"message\":\"推荐一本好书\",\"sessionId\":\"sess-001\",\"agentType\":\"product_recommend\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.agentName").value("图书推荐顾问"));

            verify(productRecommendAgent).chat(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/agent/history — 获取历史")
    class GetHistoryTests {

        @Test
        @DisplayName("正常获取 — 返回历史消息列表")
        void shouldReturnHistory() throws Exception {
            when(chatMemoryService.getHistory("sess-001"))
                    .thenReturn(List.of(
                            Map.of("role", "user", "content", "你好"),
                            Map.of("role", "assistant", "content", "你好！")
                    ));

            mockMvc.perform(get("/api/agent/history")
                    .header("X-User-Id", "user-123")
                    .param("sessionId", "sess-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("DELETE /api/agent/history — 清空历史")
    class ClearHistoryTests {

        @Test
        @DisplayName("正常清空 — 返回 200")
        void shouldClearHistory() throws Exception {
            mockMvc.perform(delete("/api/agent/history")
                    .header("X-User-Id", "user-123")
                    .param("sessionId", "sess-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(chatMemoryService).clearHistory("sess-001");
        }
    }
}
