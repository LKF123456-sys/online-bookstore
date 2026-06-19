package com.bookstore.agent.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 单元测试 — 验证 Agent 名称和基础配置
 *
 * 由于 ChatClient 的 fluent API 难以 mock（ChatClient.builder().prompt().user().call() 链式调用），
 * Agent 的核心对话逻辑通过集成测试验证。此单元测试聚焦于：
 *   1. Agent 名称标识（用于前端展示和 Orchestrator 路由）
 *   2. Agent 实例化正确性
 */
@DisplayName("Agent 基础测试")
class AgentNameTest {

    @Test
    @DisplayName("CustomerServiceAgent 名称应为 '客服助手'")
    void customerServiceAgentName() {
        var agent = new CustomerServiceAgent(null, null);
        assertEquals("客服助手", agent.getName());
    }

    @Test
    @DisplayName("ProductRecommendAgent 名称应为 '图书推荐顾问'")
    void productRecommendAgentName() {
        var agent = new ProductRecommendAgent(null, null);
        assertEquals("图书推荐顾问", agent.getName());
    }

    @Test
    @DisplayName("ReviewAnalysisAgent 名称应为 '评价分析师'")
    void reviewAnalysisAgentName() {
        var agent = new ReviewAnalysisAgent(null, null);
        assertEquals("评价分析师", agent.getName());
    }
}
