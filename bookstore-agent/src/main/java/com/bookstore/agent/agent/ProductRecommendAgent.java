package com.bookstore.agent.agent;

import com.bookstore.agent.tools.ProductTools;
import com.bookstore.agent.rag.RagSearchTool;
import com.bookstore.agent.service.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 商品推荐 Agent — 处理图书搜索、推荐、热销排行等问题
 *
 * 设计说明：
 *   1. 通过 ProductTools 调用商品微服务获取数据（关键词搜索）
 *   2. 通过 RagSearchTool 做语义搜索（RAG 检索增强生成）
 *   3. 两种方式互补：精确关键词用 ProductTools，模糊意图用 RagSearchTool
 *   4. 支持语义理解：用户说"适合睡前看的书"→ RAG 语义检索匹配散文/轻小说
 *   5. ChatMemory 注入历史上下文，让对话更连贯
 *
 * 面试亮点：
 *   1. 关键词搜索 + 语义搜索双通道：精确匹配与意图理解互补
 *   2. RAG 流程：用户问题 → Embedding → VectorStore 检索 → 上下文注入 → LLM 生成
 *   3. 知识库覆盖商品描述 + 领域知识，推荐更有深度
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRecommendAgent {

    private final ChatModel chatModel;
    private final ProductTools productTools;
    private final RagSearchTool ragSearchTool;
    private final ChatMemoryService chatMemoryService;

    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的图书推荐顾问。你的职责是帮助用户发现和了解图书。

            你的能力：
            1. 根据关键词搜索图书（精确匹配书名、作者）
            2. 基于语义理解搜索图书（理解用户意图，如"轻松睡前读物"）
            3. 搜索图书领域知识库（分类指南、阅读建议等专业知识）
            4. 查看图书详细信息
            5. 获取系统推荐图书和热销排行

            工作规则：
            - 始终使用中文回复用户
            - 回复要热情、专业，像一位资深书店店员
            - 当用户描述模糊需求时（如"有没有好看的小说"），优先使用语义搜索工具
            - 当用户提到具体书名或作者时，使用关键词搜索工具
            - 推荐时说明推荐理由，结合知识库中的领域知识给出有深度的建议
            - 涉及价格时要有性价比方面的建议
            """;

    public String chat(String userId, String userMessage) {
        log.info("【ProductRecommendAgent】同步对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productTools, ragSearchTool)
                .build();

        // 注入历史对话上下文 + RAG 检索上下文
        String enrichedMessage = buildEnrichedMessage(userId, userMessage);

        return chatClient.prompt()
                .user(enrichedMessage)
                .call()
                .content();
    }

    public Flux<String> chatStream(String userId, String userMessage) {
        log.info("【ProductRecommendAgent】流式对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productTools, ragSearchTool)
                .build();

        String enrichedMessage = buildEnrichedMessage(userId, userMessage);

        return chatClient.prompt()
                .user(enrichedMessage)
                .stream()
                .content();
    }

    /**
     * 构建增强消息：注入历史对话上下文
     * ChatMemory 从 Redis 获取最近 N 条消息，拼接在用户消息前
     */
    private String buildEnrichedMessage(String userId, String userMessage) {
        StringBuilder sb = new StringBuilder();

        // 注入历史对话（Redis 短期记忆）
        String recentContext = chatMemoryService.getRecentContext(null);
        if (recentContext != null && !recentContext.isBlank()) {
            sb.append("[历史对话上下文]\n").append(recentContext).append("\n");
        }

        sb.append("[用户当前消息]\n").append(userMessage);
        return sb.toString();
    }

    public String getName() {
        return "图书推荐顾问";
    }
}
