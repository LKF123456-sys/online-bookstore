package com.bookstore.agent.agent;

import com.bookstore.agent.tools.ProductTools;
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
 *   1. 通过 ProductTools 调用商品微服务获取数据
 *   2. LLM 根据用户偏好和搜索结果生成个性化推荐语
 *   3. 支持语义理解：用户说"适合睡前看的书"→ LLM 推理出应搜索轻文学/散文类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRecommendAgent {

    private final ChatModel chatModel;
    private final ProductTools productTools;

    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的图书推荐顾问。你的职责是帮助用户发现和了解图书。

            你的能力：
            1. 根据关键词搜索图书
            2. 查看图书详细信息
            3. 获取系统推荐图书
            4. 查看热销图书排行

            工作规则：
            - 始终使用中文回复用户
            - 回复要热情、专业，像一位资深书店店员
            - 根据用户的需求和偏好给出个性化推荐，不要只是罗列搜索结果
            - 推荐时说明推荐理由（比如"这本书适合喜欢科幻的读者"）
            - 如果用户描述了阅读偏好但没有具体书名，先搜索相关图书再给出推荐
            - 涉及价格时要有性价比方面的建议
            """;

    public String chat(String userId, String userMessage) {
        log.info("【ProductRecommendAgent】同步对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productTools)
                .build();

        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    public Flux<String> chatStream(String userId, String userMessage) {
        log.info("【ProductRecommendAgent】流式对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productTools)
                .build();

        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    public String getName() {
        return "图书推荐顾问";
    }
}
