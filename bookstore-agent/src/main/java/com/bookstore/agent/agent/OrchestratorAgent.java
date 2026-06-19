package com.bookstore.agent.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 编排 Agent（Orchestrator）— 意图识别 + 路由到专家 Agent
 *
 * 设计说明：
 *   1. 接收用户消息后，使用 LLM 进行意图分类
 *   2. 根据分类结果路由到对应的专家 Agent：
 *      - CUSTOMER_SERVICE → CustomerServiceAgent（订单相关）
 *      - PRODUCT_RECOMMEND → ProductRecommendAgent（图书搜索/推荐）
 *      - REVIEW_ANALYSIS → ReviewAnalysisAgent（评价分析）
 *      - GENERAL → 通用对话（无工具调用）
 *   3. 意图分类通过 Prompt 工程实现，不使用硬编码规则
 *
 * 面试亮点：
 *   1. Multi-Agent 架构：Orchestrator 模式实现关注点分离
 *   2. Prompt-based 意图分类：比规则引擎更灵活，支持模糊表达
 *   3. 可扩展性：新增 Agent 只需添加分类标签和路由分支
 *   4. Fallback 机制：无法分类时使用通用对话兜底
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorAgent {

    private final ChatModel chatModel;
    private final CustomerServiceAgent customerServiceAgent;
    private final ProductRecommendAgent productRecommendAgent;
    private final ReviewAnalysisAgent reviewAnalysisAgent;

    /** 意图分类提示词 */
    private static final String INTENT_CLASSIFICATION_PROMPT = """
            你是一个意图分类器。根据用户的消息，判断用户想要进行什么操作。

            请只返回以下分类标签之一（不要返回任何其他内容）：
            - CUSTOMER_SERVICE：与订单查询、订单取消、订单状态、支付、物流相关
            - PRODUCT_RECOMMEND：与图书搜索、图书推荐、找书、畅销书、新书相关
            - REVIEW_ANALYSIS：与评价、评分、口碑、用户反馈、好不好相关
            - GENERAL：通用对话、问候、闲聊、或不属于以上类别的问题

            用户消息：%s

            分类结果：""";

    /** 通用对话的系统提示词 */
    private static final String GENERAL_SYSTEM_PROMPT = """
            你是 BookVerse 书店的 AI 助手。你友好、专业，能够回答关于书店的一般性问题。

            你可以：
            - 回答关于 BookVerse 书店的通用问题
            - 进行友好的日常对话
            - 引导用户使用更专业的功能（订单查询、图书推荐、评价分析）

            始终使用中文回复。如果用户的问题需要专业功能，引导他们使用对应的功能。
            """;

    /**
     * 同步对话 — Orchestrator 分类意图后路由到对应 Agent
     */
    public String chat(String userId, String userMessage) {
        String intent = classifyIntent(userMessage);
        log.info("【Orchestrator】意图分类结果: intent={}, message={}", intent, userMessage);

        return switch (intent) {
            case "CUSTOMER_SERVICE" -> customerServiceAgent.chat(userId, userMessage);
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.chat(userId, userMessage);
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.chat(userId, userMessage);
            default -> generalChat(userMessage);
        };
    }

    /**
     * 流式对话 — Orchestrator 分类意图后路由到对应 Agent 的流式端点
     */
    public Flux<String> chatStream(String userId, String userMessage) {
        String intent = classifyIntent(userMessage);
        log.info("【Orchestrator】流式对话意图分类: intent={}, message={}", intent, userMessage);

        return switch (intent) {
            case "CUSTOMER_SERVICE" -> customerServiceAgent.chatStream(userId, userMessage);
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.chatStream(userId, userMessage);
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.chatStream(userId, userMessage);
            default -> generalChatStream(userMessage);
        };
    }

    /**
     * 获取被路由到的 Agent 名称（用于前端展示）
     */
    public String getRoutedAgentName(String userMessage) {
        String intent = classifyIntent(userMessage);
        return switch (intent) {
            case "CUSTOMER_SERVICE" -> customerServiceAgent.getName();
            case "PRODUCT_RECOMMEND" -> productRecommendAgent.getName();
            case "REVIEW_ANALYSIS" -> reviewAnalysisAgent.getName();
            default -> "通用助手";
        };
    }

    /**
     * 使用 LLM 进行意图分类
     * 调用 LLM 获取分类标签，包含容错逻辑确保分类结果有效
     */
    private String classifyIntent(String userMessage) {
        try {
            String prompt = String.format(INTENT_CLASSIFICATION_PROMPT, userMessage);
            ChatClient client = ChatClient.builder(chatModel).build();
            String result = client.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (result == null) return "GENERAL";
            result = result.trim().toUpperCase();

            // 验证分类结果是否合法
            if (result.contains("CUSTOMER_SERVICE")) return "CUSTOMER_SERVICE";
            if (result.contains("PRODUCT_RECOMMEND")) return "PRODUCT_RECOMMEND";
            if (result.contains("REVIEW_ANALYSIS")) return "REVIEW_ANALYSIS";
            return "GENERAL";
        } catch (Exception e) {
            log.warn("意图分类失败，降级为通用对话: {}", e.getMessage());
            return "GENERAL";
        }
    }

    /** 通用对话（同步） */
    private String generalChat(String userMessage) {
        ChatClient client = ChatClient.builder(chatModel)
                .defaultSystem(GENERAL_SYSTEM_PROMPT)
                .build();
        return client.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /** 通用对话（流式） */
    private Flux<String> generalChatStream(String userMessage) {
        ChatClient client = ChatClient.builder(chatModel)
                .defaultSystem(GENERAL_SYSTEM_PROMPT)
                .build();
        return client.prompt()
                .user(userMessage)
                .stream()
                .content();
    }
}
