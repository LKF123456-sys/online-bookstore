package com.bookstore.agent.controller; // controller 包：存放 REST API 控制器，处理 HTTP 请求并返回 JSON 响应

// 导入 OrchestratorAgent — Multi-Agent 系统的编排器，负责意图分类和路由分发
import com.bookstore.agent.agent.OrchestratorAgent;
// 导入 CustomerServiceAgent — 客服专家 Agent，处理订单相关请求
import com.bookstore.agent.agent.CustomerServiceAgent;
// 导入 ProductRecommendAgent — 图书推荐专家 Agent，处理搜索/推荐请求
import com.bookstore.agent.agent.ProductRecommendAgent;
// 导入 ReviewAnalysisAgent — 评价分析专家 Agent，处理评价分析请求
import com.bookstore.agent.agent.ReviewAnalysisAgent;
// 导入 ChatMemoryService — 对话记忆服务，负责 Redis + MySQL 双层消息存储
import com.bookstore.agent.service.ChatMemoryService;
// 导入公共模块的统一响应结果封装类 Result<T>，标准 API 返回格式
import com.bookstore.common.api.Result;
// Lombok 注解：编译时生成包含 final 字段的构造函数
import lombok.RequiredArgsConstructor;
// Lombok 注解：编译时生成 Logger
import lombok.extern.slf4j.Slf4j;
// Spring MediaType 常量类，TEXT_EVENT_STREAM_VALUE = "text/event-stream"
import org.springframework.http.MediaType;
// Spring WebFlux 的 ServerSentEvent — SSE 事件封装类，包含 id、event、data、comment 字段
import org.springframework.http.codec.ServerSentEvent;
// Spring MVC 注解集合：@RestController、@RequestMapping、@GetMapping、@PostMapping 等
import org.springframework.web.bind.annotation.*;
// Project Reactor 的 Flux — 响应式 0..N 元素流，用于 SSE 推送
import reactor.core.publisher.Flux;

// Java 标准库
import java.util.List; // List 集合接口
import java.util.Map; // Map 键值对接口，用于构建灵活的 JSON 响应
import java.util.UUID; // UUID 工具类，用于生成唯一的会话 ID

/**
 * AI 对话控制器 — 对外暴露 REST API，处理前端发来的所有对话请求
 *
 * 端点清单（4 个 API）：
 *   ┌──────────┬─────────────────────────┬───────────────────────────────────┐
 *   │ 方法     │ 路径                     │ 功能                              │
 *   ├──────────┼─────────────────────────┼───────────────────────────────────┤
 *   │ POST     │ /api/agent/chat         │ 同步对话：发送消息，等待完整回复   │
 *   │ GET      │ /api/agent/chat/stream  │ SSE 流式对话：逐 token 实时推送    │
 *   │ GET      │ /api/agent/history      │ 查询指定会话的历史消息列表         │
 *   │ DELETE   │ /api/agent/history      │ 清空指定会话的所有历史消息         │
 *   └──────────┴─────────────────────────┴───────────────────────────────────┘
 *
 * 请求头说明：
 *   X-User-Id：用户身份标识，由网关 AuthFilter 解析 JWT 后注入。
 *   内网信任此请求头，不再做二次认证。缺失时默认 anonymous。
 *
 * agentType 参数说明：
 *   - auto（默认）：走 Orchestrator 自动意图分类 + 路由
 *   - customer_service：跳过编排器，直接调用 CustomerServiceAgent
 *   - product_recommend：跳过编排器，直接调用 ProductRecommendAgent
 *   - review_analysis：跳过编排器，直接调用 ReviewAnalysisAgent
 *   - 直接指定 Agent 类型可避免意图分类的额外 LLM 调用，降低延迟
 *
 * 消息生命周期：
 *   1. 接收请求 → 提取 userId / sessionId / message / agentType
 *   2. 保存用户消息到 ChatMemoryService（Redis + MySQL 双层写入）
 *   3. 根据 agentType 路由到对应 Agent 生成回复
 *   4. 保存 AI 回复到 ChatMemoryService
 *   5. 返回响应给前端
 *
 * 面试亮点：
 *   1. SSE 流式对话：逐 token 实时推送，比 WebSocket 更轻量（单向推送）
 *   2. 支持 agentType 参数：允许前端直接指定 Agent，减少编排器 LLM 调用
 *   3. 双层存储：Redis 热数据 + MySQL 归档，兼顾性能和持久化
 *   4. SSE 异常容错：onErrorResume 优雅降级，返回错误事件而非中断流
 *   5. JSON 转义：escapeJson 防止 token 中特殊字符破坏 SSE 事件格式
 */
@Slf4j // Lombok：自动生成 log 对象
@RestController // Spring MVC：标记为 REST 控制器，所有方法返回值自动序列化为 JSON
@RequestMapping("/api/agent") // 统一路径前缀，所有端点均以 /api/agent 开头
@RequiredArgsConstructor // Lombok：生成包含所有 final 字段的构造函数
public class ChatController { // AI 对话 REST 控制器

    private final OrchestratorAgent orchestratorAgent; // 编排器 Agent（意图分类 + 路由）
    private final CustomerServiceAgent customerServiceAgent; // 客服 Agent（订单处理）
    private final ProductRecommendAgent productRecommendAgent; // 推荐 Agent（图书搜索）
    private final ReviewAnalysisAgent reviewAnalysisAgent; // 评价分析 Agent（评价分析）
    private final ChatMemoryService chatMemoryService; // 对话记忆服务（Redis + MySQL）

    /**
     * POST /api/agent/chat — 同步对话端点
     *
     * 适用场景：
     *   不需要打字机效果的场景，如管理后台的 AI 辅助功能、批量问答等。
     *   前端发送请求后阻塞等待，收到完整回复后一次性渲染。
     *
     * 请求体格式：
     *   {
     *     "message": "我最近的订单有哪些？",
     *     "sessionId": "a1b2c3d4-...",   // 可选，不传则自动生成
     *     "agentType": "auto"             // 可选，默认 auto
     *   }
     *
     * 响应格式：
     *   {
     *     "code": 200,
     *     "data": {
     *       "reply": "您最近有 3 个订单...",
     *       "sessionId": "a1b2c3d4-...",
     *       "agentName": "客服助手",
     *       "timestamp": 1719000000000
     *     }
     *   }
     *
     * @param userId 从 X-User-Id 请求头获取的用户 ID（网关注入）
     * @param request 请求体 JSON Map，包含 message、sessionId（可选）、agentType（可选）
     * @return Result<Map> 统一响应，data 中包含 reply、sessionId、agentName、timestamp
     */
    @PostMapping("/chat") // POST 映射：处理 /api/agent/chat 请求
    public Result<Map<String, Object>> chat( // 同步对话方法，返回包装后的响应
            @RequestHeader(value = "X-User-Id", required = false) String userId, // 从请求头提取用户 ID，允许为空
            @RequestBody Map<String, String> request) { // 从请求体解析 JSON 到 Map

        String message = request.get("message"); // 提取用户消息文本
        String sessionId = request.getOrDefault("sessionId", UUID.randomUUID().toString()); // 获取或生成会话 ID
        String agentType = request.getOrDefault("agentType", "auto"); // 获取 Agent 类型，默认 auto 自动路由

        if (userId == null || userId.isBlank()) { // 检查 userId 是否有效
            userId = "anonymous"; // 无用户身份时使用匿名标识
        }

        log.info("【Chat】同步对话: userId={}, sessionId={}, agentType={}, message={}", // 记录请求日志
                userId, sessionId, agentType, message);

        // Step 1: 保存用户消息到记忆系统（Redis + MySQL）
        chatMemoryService.saveMessage(sessionId, userId, "user", message, null); // role="user"，agentName=null

        // Step 2: 根据 agentType 调用对应 Agent 生成回复
        String reply; // 声明回复变量
        String agentName; // 声明 Agent 名称变量
        switch (agentType) { // 根据前端指定的 Agent 类型路由
            case "customer_service": // 指定客服 Agent
                reply = customerServiceAgent.chat(userId, message); // 调用客服 Agent 同步方法
                agentName = customerServiceAgent.getName(); // 获取 Agent 中文名"客服助手"
                break;
            case "product_recommend": // 指定推荐 Agent
                reply = productRecommendAgent.chat(userId, message); // 调用推荐 Agent
                agentName = productRecommendAgent.getName(); // 获取 Agent 中文名"图书推荐顾问"
                break;
            case "review_analysis": // 指定评价分析 Agent
                reply = reviewAnalysisAgent.chat(userId, message); // 调用评价分析 Agent
                agentName = reviewAnalysisAgent.getName(); // 获取 Agent 中文名"评价分析师"
                break;
            default: // "auto" — Orchestrator 自动意图分类 + 路由
                reply = orchestratorAgent.chat(userId, message); // 走编排器自动路由
                agentName = orchestratorAgent.getRoutedAgentName(message); // 根据分类结果获取 Agent 名称
                break;
        }

        // Step 3: 保存 AI 回复到记忆系统
        chatMemoryService.saveMessage(sessionId, userId, "assistant", reply, agentName); // role="assistant"

        // Step 4: 构建响应 Map
        Map<String, Object> response = Map.of( // Java 9+ 的不可变 Map 工厂方法
                "reply", reply, // AI 生成的回复文本
                "sessionId", sessionId, // 会话标识，前端用于后续请求
                "agentName", agentName, // 当前服务的 Agent 名称
                "timestamp", System.currentTimeMillis() // 响应时间戳
        );
        return Result.success(response); // 包装为统一响应格式 {"code":200, "data":{...}}
    }

    /**
     * GET /api/agent/chat/stream — SSE 流式对话端点
     *
     * 这是整个 AI 对话系统的核心端点，实现流式打字机效果。
     *
     * SSE（Server-Sent Events）原理：
     *   1. 客户端通过 EventSource API 发起 GET 请求
     *   2. 服务端保持连接打开，通过 Flux 异步流逐条推送事件
     *   3. 每个 SSE 事件格式：data: {JSON}\n\n
     *   4. 浏览器 EventSource 自动处理重连
     *
     * 事件流协议：
     *   正常流：
     *     data: {"token":"你","done":false,"sessionId":"xxx"}
     *     data: {"token":"好","done":false,"sessionId":"xxx"}
     *     data: {"token":"","done":true,"agentName":"客服助手","sessionId":"xxx"}
     *   错误流：
     *     data: {"token":"","done":true,"error":"服务异常"}
     *
     * 流处理流程：
     *   用户消息 → map(逐 token 构建 SSE 事件) → concatWith(末尾追加完成事件)
     *   → doOnComplete(流结束后保存完整回复) → onErrorResume(异常降级为错误事件)
     *
     * @param userId 用户 ID，从 X-User-Id 请求头获取
     * @param message 用户消息（Query 参数）
     * @param sessionId 会话 ID（Query 参数，可选）
     * @param agentType Agent 类型（Query 参数，默认 "auto"）
     * @return Flux<ServerSentEvent<String>> SSE 事件流
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // GET 映射，Content-Type: text/event-stream
    public Flux<ServerSentEvent<String>> chatStream( // 流式对话方法，返回 SSE 事件流
            @RequestHeader(value = "X-User-Id", required = false) String userId, // 从请求头提取用户 ID
            @RequestParam("message") String message, // 用户消息，必传
            @RequestParam(value = "sessionId", required = false) String sessionId, // 会话 ID，可选
            @RequestParam(value = "agentType", defaultValue = "auto") String agentType) { // Agent 类型，默认 auto

        if (userId == null || userId.isBlank()) { // userId 空值检查
            userId = "anonymous"; // 匿名用户
        }
        if (sessionId == null || sessionId.isBlank()) { // sessionId 空值检查
            sessionId = UUID.randomUUID().toString(); // 自动生成 UUID 会话 ID
        }

        // 用 final 变量捕获，因为 Lambda 表达式（map、doOnComplete 等）只能访问 effectively final 变量
        final String finalUserId = userId; // 捕获为 final，供 Lambda 使用
        final String finalSessionId = sessionId; // 捕获为 final，供 Lambda 使用

        log.info("【Chat】SSE 流式对话: userId={}, sessionId={}, message={}", userId, sessionId, message); // 记录流式请求日志

        // Step 1: 保存用户消息到记忆系统
        chatMemoryService.saveMessage(finalSessionId, finalUserId, "user", message, null); // 用户消息 role="user"

        // Step 2: 构建回复收集器和 Agent 名称
        StringBuilder fullReply = new StringBuilder(); // StringBuilder 收集所有 token，用于最终保存完整回复
        final String agentName; // 声明 final Agent 名称（Lambda 要求 effectively final）
        final Flux<String> tokenStream; // 声明 final Token 流（Flux<String> 是最终的 token 流）

        // Step 3: 根据 agentType 路由到对应 Agent 的流式方法
        switch (agentType) { // 路由判断
            case "customer_service": // 指定客服 Agent
                agentName = customerServiceAgent.getName(); // "客服助手"
                tokenStream = customerServiceAgent.chatStream(finalUserId, message); // 获取客服 token 流
                break;
            case "product_recommend": // 指定推荐 Agent
                agentName = productRecommendAgent.getName(); // "图书推荐顾问"
                tokenStream = productRecommendAgent.chatStream(finalUserId, message); // 获取推荐 token 流
                break;
            case "review_analysis": // 指定评价分析 Agent
                agentName = reviewAnalysisAgent.getName(); // "评价分析师"
                tokenStream = reviewAnalysisAgent.chatStream(finalUserId, message); // 获取评价分析 token 流
                break;
            default: // "auto" — 走编排器自动路由
                agentName = orchestratorAgent.getRoutedAgentName(message); // 获取路由到的 Agent 名称
                tokenStream = orchestratorAgent.chatStream(finalUserId, message); // 走编排器获取 token 流
                break;
        }

        // Step 4: 构建 SSE 事件流（核心管道操作）
        return tokenStream
                .map(token -> { // 对 Flux 中的每个 token 片段做转换：String → ServerSentEvent
                    fullReply.append(token); // 收集 token 到 StringBuilder，构建完整回复
                    return ServerSentEvent.<String>builder() // 构建 SSE 事件对象
                            .data(String.format("{\"token\":\"%s\",\"done\":false,\"sessionId\":\"%s\"}", // 事件数据 JSON
                                    escapeJson(token), finalSessionId)) // 转义 token 中的特殊字符，注入 sessionId
                            .build(); // 构建 SSE 事件
                })
                .concatWith(Flux.just( // concatWith：在原流结束后追加一个或多个元素
                        // 流结束时发送完成标记事件
                        ServerSentEvent.<String>builder() // 构建完成标记 SSE 事件
                                .data(String.format("{\"token\":\"\",\"done\":true,\"agentName\":\"%s\",\"sessionId\":\"%s\"}", // 完成事件 JSON
                                        agentName, finalSessionId)) // 注入 Agent 名称和 sessionId
                                .build() // 构建完成事件
                ))
                .doOnComplete(() -> { // doOnComplete：流正常完成时的回调（所有 token 发送完毕）
                    // Step 5: 流结束后保存完整 AI 回复到记忆系统
                    chatMemoryService.saveMessage(finalSessionId, finalUserId, "assistant", // role="assistant"
                            fullReply.toString(), agentName); // 保存拼接后的完整回复文本
                    log.debug("【Chat】SSE 流式对话完成: sessionId={}, replyLength={}", // 记录完成日志
                            finalSessionId, fullReply.length()); // 输出回复长度用于监控
                })
                .onErrorResume(e -> { // onErrorResume：流发生异常时的降级处理（恢复为错误事件流）
                    log.error("SSE 流式对话异常: {}", e.getMessage()); // 记录异常日志
                    return Flux.just(ServerSentEvent.<String>builder() // 返回包含错误信息的单元素 Flux
                            .data(String.format("{\"token\":\"\",\"done\":true,\"error\":\"%s\"}", // 错误事件 JSON
                                    escapeJson(e.getMessage()))) // 转义异常信息
                            .build()); // 构建错误事件
                });
    }

    /**
     * GET /api/agent/history — 获取会话历史消息
     *
     * 查询指定 sessionId 的所有历史消息，按时间升序排列，还原完整对话。
     * 数据来源：优先 Redis（热数据），回退 MySQL（冷数据/归档数据）。
     *
     * @param userId 用户 ID（从请求头提取，当前用于日志）
     * @param sessionId 会话 ID
     * @return 历史消息列表，每条包含 role、content、agentName、createTime
     */
    @GetMapping("/history") // GET 映射：/api/agent/history?sessionId=xxx
    public Result<List<Map<String, Object>>> getHistory( // 获取历史消息
            @RequestHeader(value = "X-User-Id", required = false) String userId, // 用户 ID
            @RequestParam("sessionId") String sessionId) { // 会话 ID，必传
        log.info("【Chat】获取历史: userId={}, sessionId={}", userId, sessionId); // 记录请求日志
        List<Map<String, Object>> history = chatMemoryService.getHistory(sessionId); // 调用 ChatMemoryService 获取历史
        return Result.success(history); // 包装为统一响应格式返回
    }

    /**
     * DELETE /api/agent/history — 清空会话历史
     *
     * 删除指定 sessionId 的所有消息记录。
     * 同时清理 Redis 缓存和 MySQL 数据库中的相关数据。
     * 适用于用户手动清空对话或管理后台清理过期会话。
     *
     * @param userId 用户 ID（从请求头提取，用于日志）
     * @param sessionId 会话 ID
     * @return Result<Void> 操作结果
     */
    @DeleteMapping("/history") // DELETE 映射：/api/agent/history?sessionId=xxx
    public Result<Void> clearHistory( // 清空历史消息
            @RequestHeader(value = "X-User-Id", required = false) String userId, // 用户 ID
            @RequestParam("sessionId") String sessionId) { // 会话 ID
        log.info("【Chat】清空历史: userId={}, sessionId={}", userId, sessionId); // 记录操作日志
        chatMemoryService.clearHistory(sessionId); // 调用 ChatMemoryService 清空
        return Result.success(null); // 返回成功（data 为 null）
    }

    /**
     * JSON 字符串转义工具方法
     *
     * 为什么需要转义？
     *   SSE 事件的 data 字段是 JSON 格式字符串。如果 LLM 生成的 token 中包含
     *   双引号、反斜杠、换行符等特殊字符，会破坏 JSON 结构导致前端 JSON.parse 失败。
     *   此方法将所有 JSON 特殊字符转义为合法的转义序列。
     *
     * 转义规则：
     *   \ → \\   （反斜杠转义）
     *   " → \"   （双引号转义）
     *   \n → \\n （换行符转义）
     *   \r → \\r （回车符转义）
     *   \t → \\t （制表符转义）
     *
     * @param text 需要转义的原始文本
     * @return JSON 安全的转义后文本
     */
    private String escapeJson(String text) { // 私有的 JSON 转义方法，供 SSE 事件构建使用
        if (text == null) return ""; // null 安全检查：返回空字符串
        return text.replace("\\", "\\\\") // 转义反斜杠（必须先处理，否则后续替换会出问题）
                .replace("\"", "\\\"") // 转义双引号
                .replace("\n", "\\n") // 转义换行符
                .replace("\r", "\\r") // 转义回车符
                .replace("\t", "\\t"); // 转义制表符
    }
}
