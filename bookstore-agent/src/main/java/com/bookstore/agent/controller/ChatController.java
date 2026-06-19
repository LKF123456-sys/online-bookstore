package com.bookstore.agent.controller;

import com.bookstore.agent.agent.OrchestratorAgent;
import com.bookstore.agent.agent.CustomerServiceAgent;
import com.bookstore.agent.agent.ProductRecommendAgent;
import com.bookstore.agent.agent.ReviewAnalysisAgent;
import com.bookstore.agent.service.ChatMemoryService;
import com.bookstore.common.api.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 对话控制器 — 提供同步对话和 SSE 流式对话端点
 *
 * 设计说明：
 *   1. POST /api/agent/chat — 同步对话，等待完整回复后返回
 *   2. GET /api/agent/chat/stream — SSE 流式对话，逐 token 实时推送
 *   3. GET /api/agent/history — 获取会话历史
 *   4. DELETE /api/agent/history — 清空会话历史
 *
 * 面试亮点：
 *   1. SSE (Server-Sent Events) 实现流式对话，比 WebSocket 更轻量
 *   2. 前端通过 EventSource 接收实时 token，打字机效果
 *   3. 统一的 userId 从网关注入的 X-User-Id 请求头获取
 *   4. 会话管理：sessionId 自动生成，支持多会话切换
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class ChatController {

    private final OrchestratorAgent orchestratorAgent;
    private final CustomerServiceAgent customerServiceAgent;
    private final ProductRecommendAgent productRecommendAgent;
    private final ReviewAnalysisAgent reviewAnalysisAgent;
    private final ChatMemoryService chatMemoryService;

    /**
     * 同步对话 — 一次性返回完整回复
     *
     * 请求体：
     *   { "message": "我最近的订单有哪些", "sessionId": "xxx", "agentType": "auto" }
     *
     * agentType 可选值：
     *   - auto（默认）：Orchestrator 自动路由
     *   - customer_service：强制使用客服 Agent
     *   - product_recommend：强制使用推荐 Agent
     *   - review_analysis：强制使用评价分析 Agent
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", UUID.randomUUID().toString());
        String agentType = request.getOrDefault("agentType", "auto");

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }

        log.info("【Chat】同步对话: userId={}, sessionId={}, agentType={}, message={}",
                userId, sessionId, agentType, message);

        // 保存用户消息到记忆
        chatMemoryService.saveMessage(sessionId, userId, "user", message, null);

        // 调用 Agent 获取回复
        String reply;
        String agentName;
        switch (agentType) {
            case "customer_service":
                reply = customerServiceAgent.chat(userId, message);
                agentName = customerServiceAgent.getName();
                break;
            case "product_recommend":
                reply = productRecommendAgent.chat(userId, message);
                agentName = productRecommendAgent.getName();
                break;
            case "review_analysis":
                reply = reviewAnalysisAgent.chat(userId, message);
                agentName = reviewAnalysisAgent.getName();
                break;
            default: // "auto" — Orchestrator 自动路由
                reply = orchestratorAgent.chat(userId, message);
                agentName = orchestratorAgent.getRoutedAgentName(message);
                break;
        }

        // 保存助手回复到记忆
        chatMemoryService.saveMessage(sessionId, userId, "assistant", reply, agentName);

        Map<String, Object> response = Map.of(
                "reply", reply,
                "sessionId", sessionId,
                "agentName", agentName,
                "timestamp", System.currentTimeMillis()
        );
        return Result.success(response);
    }

    /**
     * SSE 流式对话 — 逐 token 实时推送
     *
     * 使用 Server-Sent Events 协议，前端通过 EventSource 接收数据流。
     * 每个 SSE 事件包含一个 token 片段，前端拼接后显示打字机效果。
     *
     * 事件格式：
     *   data: {"token": "你", "done": false}
     *   data: {"token": "好", "done": false}
     *   data: {"token": "", "done": true, "agentName": "客服助手"}
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("message") String message,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "agentType", defaultValue = "auto") String agentType) {

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        final String finalUserId = userId;
        final String finalSessionId = sessionId;

        log.info("【Chat】SSE 流式对话: userId={}, sessionId={}, message={}", userId, sessionId, message);

        // 保存用户消息
        chatMemoryService.saveMessage(finalSessionId, finalUserId, "user", message, null);

        // 构建完整的回复用于后续保存
        StringBuilder fullReply = new StringBuilder();
        final String agentName;
        final Flux<String> tokenStream;

        // 根据 agentType 路由到对应的 Agent
        switch (agentType) {
            case "customer_service":
                agentName = customerServiceAgent.getName();
                tokenStream = customerServiceAgent.chatStream(finalUserId, message);
                break;
            case "product_recommend":
                agentName = productRecommendAgent.getName();
                tokenStream = productRecommendAgent.chatStream(finalUserId, message);
                break;
            case "review_analysis":
                agentName = reviewAnalysisAgent.getName();
                tokenStream = reviewAnalysisAgent.chatStream(finalUserId, message);
                break;
            default: // "auto"
                agentName = orchestratorAgent.getRoutedAgentName(message);
                tokenStream = orchestratorAgent.chatStream(finalUserId, message);
                break;
        }

        return tokenStream
                .map(token -> {
                    fullReply.append(token);
                    return ServerSentEvent.<String>builder()
                            .data(String.format("{\"token\":\"%s\",\"done\":false,\"sessionId\":\"%s\"}",
                                    escapeJson(token), finalSessionId))
                            .build();
                })
                .concatWith(Flux.just(
                        // 流结束时发送完成标记
                        ServerSentEvent.<String>builder()
                                .data(String.format("{\"token\":\"\",\"done\":true,\"agentName\":\"%s\",\"sessionId\":\"%s\"}",
                                        agentName, finalSessionId))
                                .build()
                ))
                .doOnComplete(() -> {
                    // 流结束后保存完整回复
                    chatMemoryService.saveMessage(finalSessionId, finalUserId, "assistant",
                            fullReply.toString(), agentName);
                    log.debug("【Chat】SSE 流式对话完成: sessionId={}, replyLength={}",
                            finalSessionId, fullReply.length());
                })
                .onErrorResume(e -> {
                    log.error("SSE 流式对话异常: {}", e.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data(String.format("{\"token\":\"\",\"done\":true,\"error\":\"%s\"}",
                                    escapeJson(e.getMessage())))
                            .build());
                });
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("sessionId") String sessionId) {
        log.info("【Chat】获取历史: userId={}, sessionId={}", userId, sessionId);
        List<Map<String, Object>> history = chatMemoryService.getHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 清空会话历史
     */
    @DeleteMapping("/history")
    public Result<Void> clearHistory(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("sessionId") String sessionId) {
        log.info("【Chat】清空历史: userId={}, sessionId={}", userId, sessionId);
        chatMemoryService.clearHistory(sessionId);
        return Result.success(null);
    }

    /**
     * JSON 字符串转义 — 防止 token 中的特殊字符破坏 JSON 格式
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
