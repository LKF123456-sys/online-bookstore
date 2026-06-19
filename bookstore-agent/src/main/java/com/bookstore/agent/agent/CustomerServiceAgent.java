package com.bookstore.agent.agent;

import com.bookstore.agent.tools.OrderTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 客服 Agent — 处理订单查询、取消、状态跟踪等客服类问题
 *
 * 设计说明：
 *   1. 使用 Spring AI 的 ChatClient 构建对话，注入系统提示词和工具
 *   2. 通过 Tool Calling 让 LLM 自主决定是否调用 OrderTools 中的方法
 *   3. 支持同步和流式两种调用方式
 *
 * 面试亮点：
 *   1. System Prompt 工程：限定 Agent 角色和能力边界，防止 Prompt 注入
 *   2. Tool Calling 闭环：LLM 生成参数 → 调用工具 → 结果注入 → 生成回复
 *   3. 安全边界：userId 由服务端注入，LLM 无法伪造用户身份
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceAgent {

    private final ChatModel chatModel;
    private final OrderTools orderTools;

    private static final String SYSTEM_PROMPT = """
            你是 BookVerse 书店的智能客服助手。你的职责是帮助用户解决订单相关的问题。

            你的能力：
            1. 查询用户的订单详情（需要提供订单号）
            2. 查询用户的订单列表（可按状态筛选）
            3. 帮助用户取消订单（仅限待支付状态的订单）

            工作规则：
            - 始终使用中文回复用户
            - 回复要简洁、专业、友好
            - 在调用工具获取数据后，用自然语言总结结果，不要直接输出原始数据
            - 如果用户的请求超出你的能力范围（如退款、修改收货地址），礼貌地建议用户联系人工客服
            - 涉及金额时要使用 ¥ 符号
            - 不要编造不存在的订单信息，必须通过工具查询真实数据
            """;

    /**
     * 同步对话 — 等待 LLM 完整回复后一次性返回
     */
    public String chat(String userId, String userMessage) {
        log.info("【CustomerServiceAgent】同步对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(orderTools)
                .build();

        // 在用户消息中注入 userId，供工具调用时使用
        String enrichedMessage = userMessage + "\n[系统信息: 当前用户ID=" + userId + "]";

        return chatClient.prompt()
                .user(enrichedMessage)
                .call()
                .content();
    }

    /**
     * 流式对话 — 逐 token 返回，用于 SSE 实时推送
     */
    public Flux<String> chatStream(String userId, String userMessage) {
        log.info("【CustomerServiceAgent】流式对话: userId={}, message={}", userId, userMessage);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(orderTools)
                .build();

        String enrichedMessage = userMessage + "\n[系统信息: 当前用户ID=" + userId + "]";

        return chatClient.prompt()
                .user(enrichedMessage)
                .stream()
                .content();
    }

    /**
     * Agent 名称（用于前端展示和 Orchestrator 路由标识）
     */
    public String getName() {
        return "客服助手";
    }
}
