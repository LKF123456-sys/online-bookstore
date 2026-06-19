package com.bookstore.agent.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MCP SSE 端点 — 实现 MCP 协议的 SSE 传输层
 *
 * 设计说明：
 *   1. 客户端通过 GET /api/agent/mcp/sse 建立 SSE 连接
 *   2. 服务端通过 SSE 推送 JSON-RPC 响应
 *   3. 客户端通过 POST /api/agent/mcp/message 发送 JSON-RPC 请求
 *   4. 这种"双向通道"模式是 MCP SSE 传输的标准实现
 *
 * 面试亮点：
 *   1. SSE 作为 MCP 传输层：比 WebSocket 更轻量，适合 HTTP 环境
 *   2. 连接管理：支持多客户端同时连接
 *   3. 协议兼容：遵循 MCP 2024-11-05 规范
 *   4. Claude Desktop 可以直接连接此端点使用 BookVerse 的全部能力
 *
 * Claude Desktop 配置示例：
 *   {
 *     "mcpServers": {
 *       "bookstore": {
 *         "url": "http://localhost:8089/api/agent/mcp/sse"
 *       }
 *     }
 *   }
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/mcp")
@RequiredArgsConstructor
public class McpSseController {

    private final McpServerService mcpServerService;
    private final ObjectMapper objectMapper;

    /** 活跃的 SSE 连接 */
    private final CopyOnWriteArrayList<SseEmitter> activeConnections = new CopyOnWriteArrayList<>();

    /** 会话到 SSE 连接的映射 */
    private final Map<String, SseEmitter> sessionConnections = new ConcurrentHashMap<>();

    /**
     * SSE 连接端点 — 客户端连接后保持长连接，接收服务端推送
     *
     * MCP 客户端（如 Claude Desktop）首先连接此端点建立 SSE 通道，
     * 然后通过 POST /message 发送 JSON-RPC 请求，响应通过此 SSE 通道返回。
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(value = "sessionId", required = false) String sessionId) {
        log.info("MCP SSE 连接建立: sessionId={}", sessionId);

        SseEmitter emitter = new SseEmitter(0L);  // 无限超时
        activeConnections.add(emitter);
        if (sessionId != null) {
            sessionConnections.put(sessionId, emitter);
        }

        // 连接建立后发送 endpoint 事件（MCP 规范要求）
        try {
            String messageEndpoint = "/api/agent/mcp/message";
            if (sessionId != null) {
                messageEndpoint += "?sessionId=" + sessionId;
            }
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data(messageEndpoint));
        } catch (IOException e) {
            log.warn("发送 endpoint 事件失败: {}", e.getMessage());
        }

        // 连接关闭时清理
        emitter.onCompletion(() -> {
            activeConnections.remove(emitter);
            if (sessionId != null) sessionConnections.remove(sessionId);
            log.info("MCP SSE 连接关闭: sessionId={}", sessionId);
        });
        emitter.onTimeout(() -> {
            activeConnections.remove(emitter);
            if (sessionId != null) sessionConnections.remove(sessionId);
        });

        return emitter;
    }

    /**
     * JSON-RPC 消息端点 — 接收客户端请求，通过 SSE 返回响应
     *
     * 支持的方法：
     *   - initialize: 握手
     *   - tools/list: 获取工具列表
     *   - tools/call: 调用工具
     *   - ping: 心跳
     */
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void handleMessage(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestBody String requestBody) {
        log.debug("MCP 消息: sessionId={}, body={}", sessionId, requestBody);

        try {
            // 解析 JSON-RPC 请求
            McpProtocol.JsonRpcRequest request = objectMapper.readValue(
                    requestBody, McpProtocol.JsonRpcRequest.class);

            // 处理请求
            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            // 通过 SSE 发送响应
            String responseJson = objectMapper.writeValueAsString(response);
            SseEmitter emitter = sessionId != null
                    ? sessionConnections.get(sessionId)
                    : (activeConnections.isEmpty() ? null : activeConnections.get(0));

            if (emitter != null) {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(responseJson));
            } else {
                log.warn("没有活跃的 SSE 连接来发送响应");
            }
        } catch (Exception e) {
            log.error("处理 MCP 消息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取 MCP Server 状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "status", "running",
                "protocolVersion", "2024-11-05",
                "serverName", "bookstore-agent",
                "toolCount", mcpServerService.getToolDefinitions().size(),
                "activeConnections", activeConnections.size(),
                "transport", "SSE"
        );
    }

    /**
     * 获取工具列表（REST 方式，非 MCP 协议，方便调试）
     */
    @GetMapping("/tools")
    public Object listTools() {
        return Map.of("tools", mcpServerService.getToolDefinitions());
    }
}
