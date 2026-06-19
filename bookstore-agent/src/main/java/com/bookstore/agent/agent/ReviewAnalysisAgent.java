package com.bookstore.agent.agent;

import com.bookstore.agent.tools.ReviewTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 评价分析 Agent — 处理商品评价的情感分析、摘要生成、优缺点提取
 *
 * 设计说明：
 *   1. 通过 ReviewTools 获取原始评价数据
 *   2. LLM 对评价进行二次分析：情感倾向、关键词提取、优缺点总结
 *   3. 帮助用户在购买前快速了解商品口碑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAnalysisAgent {

    private final ChatModel chatModel;
    private final ReviewTools reviewTools;

    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的评价分析师。你的职责是帮助用户了解图书的真实口碑。

            你的能力：
            1. 获取指定图书的用户评价
            2. 对评价进行情感分析（正面/中性/负面占比）
            3. 提取评价中提到的优点和缺点
            4. 生成评价摘要，帮助用户快速了解口碑

            工作规则：
            - 始终使用中文回复用户
            - 分析要客观、基于评价文本，不要添加臆测
            - 用结构化的方式呈现分析结果（如：优点列表、缺点列表、总体评价）
            - 如果评价数量较少，提醒用户参考样本有限
            - 关注评价中提到的具体方面：内容质量、印刷质量、性价比、物流速度等
            """;

    public String chat(String userId, String userMessage) {
        log.info("【ReviewAnalysisAgent】同步对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(reviewTools)
                .build();

        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    public Flux<String> chatStream(String userId, String userMessage) {
        log.info("【ReviewAnalysisAgent】流式对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(reviewTools)
                .build();

        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    public String getName() {
        return "评价分析师";
    }
}
