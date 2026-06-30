package com.bookstore.agent.mcp; // mcp 包：MCP 协议实现

// Jackson ObjectMapper — JSON 序列化/反序列化
import com.fasterxml.jackson.databind.ObjectMapper;
// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Spring MediaType — 常量 TEXT_EVENT_STREAM_VALUE = "text/event-stream"
import org.springframework.http.MediaType;
// Spring MVC 注解
import org.springframework.web.bind.annotation.*;
// Spring MVC 的 SseEmitter — SSE 推送工具，封装了 HTTP 长连接的发送和关闭操作
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Java IO 异常
import java.io.IOException;
// Java 集合
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList; // 写时复制的线程安全 List，适合读多写少场景

/**
 * MCP SSE 传输层控制器 — 实现 MCP 协议的 SSE 双向通信通道
 *
 * MCP SSE 传输模式说明：
 *   与传统的"客户端请求→服务端响应"模式不同，MCP SSE 使用双向通道：
 *     通道A（入站/长连接）：客户端 GET /api/agent/mcp/sse → 服务端持续推送事件
 *     通道B（出站/短请求）：客户端 POST /api/agent/mcp/message → 服务端处理并推送到通道A
 *
 * 通信流程：
 *   1. 客户端建立 SSE 长连接 → GET /api/agent/mcp/sse?sessionId=xxx
 *   2. 服务端通过 SSE 推送 endpoint 事件（告知 POST /message 端点地址）
 *   3. 客户端通过 POST /api/agent/mcp/message?sessionId=xxx 发送 JSON-RPC 请求
 *   4. 服务端处理请求，通过 sessionId 找到对应 SSE 连接，推送响应
 *
 * Claude Desktop 配置示例：
 *   {
 *     "mcpServers": {
 *       "bookstore": {
 *         "url": "http://localhost:8089/api/agent/mcp/sse"
 *       }
 *     }
 *   }
 *
 * 连接管理：
 *   - activeConnections：所有活跃 SSE 连接（CopyOnWriteArrayList，线程安全）
 *   - sessionConnections：sessionId → SSE 连接的映射（ConcurrentHashMap）
 *   - 连接关闭/超时自动清理，防止内存泄漏
 *
 * 面试亮点：
 *   1. MCP 双向通道：SSE（入站）+ POST（出站）组合实现双向通信
 *   2. 连接管理：CopyOnWriteArrayList + ConcurrentHashMap 线程安全
 *   3. 协议兼容：endpoint 事件告知 POST 地址，遵循 MCP 2024-11-05 规范
 *   4. Claude Desktop 兼容：外部 AI 客户端可直接控制 BookVerse 全能力
 */
@Slf4j // Lombok：log
@RestController // Spring MVC：REST 控制器
@RequestMapping("/api/agent/mcp") // 统一路径前缀
@RequiredArgsConstructor // Lombok：构造器注入
public class McpSseController { // MCP SSE 传输层控制器

    private final McpServerService mcpServerService; // MCP Server 核心服务（请求分发 + 工具执行）
    private final ObjectMapper objectMapper; // Jackson JSON 序列化（用于请求反序列化和响应序列化）

    /**
     * 活跃 SSE 连接列表
     * CopyOnWriteArrayList：写操作时复制整个数组，保证读操作无锁且安全。
     * 适合连接列表这种"读多写少"的场景（频繁推送，偶尔增减连接）。
     */
    private final CopyOnWriteArrayList<SseEmitter> activeConnections = new CopyOnWriteArrayList<>(); // 活跃连接列表

    /**
     * 会话到 SSE 连接的映射
     * ConcurrentHashMap：分段锁，高并发下性能优于 HashTable。
     * 用于通过 sessionId 精确找到对应的 SSE 连接发送响应。
     */
    private final Map<String, SseEmitter> sessionConnections = new ConcurrentHashMap<>(); // sessionId → SSE 连接映射

    /**
     * GET /api/agent/mcp/sse — SSE 连接端点
     *
     * MCP 客户端（如 Claude Desktop）首先连接此端点建立 SSE 长连接通道。
     * 连接建立后：
     *   1. 立即推送 endpoint 事件，告知客户端 POST 消息端点地址
     *   2. 保持连接开放，等待后续推送（工具调用响应等）
     *   3. 注册 onCompletion/onTimeout 回调，连接关闭时自动清理
     *
     * @param sessionId 可选会话 ID，用于后续通过 POST /message 精确推送
     * @return SseEmitter 实例（Spring MVC 自动处理 SSE 协议细节）
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // GET SSE 端点
    public SseEmitter connect(@RequestParam(value = "sessionId", required = false) String sessionId) { // SSE 连接方法
        log.info("MCP SSE 连接建立: sessionId={}", sessionId); // 记录连接日志

        SseEmitter emitter = new SseEmitter(0L);  // 创建 SseEmitter，超时=0 表示无限超时（长连接）
        activeConnections.add(emitter); // 加入活跃连接列表
        if (sessionId != null) { // 如果有 sessionId
            sessionConnections.put(sessionId, emitter); // 存入 session 映射
        }

        // MCP 规范要求：连接建立后立即发送 endpoint 事件，告知客户端 POST 端点地址
        try { // 发送可能因客户端断开而失败
            String messageEndpoint = "/api/agent/mcp/message"; // POST 消息端点路径
            if (sessionId != null) { // 有 sessionId 时附加查询参数
                messageEndpoint += "?sessionId=" + sessionId; // 拼接 sessionId
            }
            emitter.send(SseEmitter.event() // 构建 SSE 事件
                    .name("endpoint") // 事件名：endpoint（MCP 规范）
                    .data(messageEndpoint)); // 事件数据：POST 端点 URL
        } catch (IOException e) { // 客户端可能已断开
            log.warn("发送 endpoint 事件失败: {}", e.getMessage()); // 记录警告
        }

        // 连接关闭时自动清理 — 防止内存泄漏
        emitter.onCompletion(() -> { // 正常关闭回调
            activeConnections.remove(emitter); // 从活跃列表移除
            if (sessionId != null) sessionConnections.remove(sessionId); // 从 session 映射移除
            log.info("MCP SSE 连接关闭: sessionId={}", sessionId); // 记录关闭日志
        });
        emitter.onTimeout(() -> { // 超时关闭回调
            activeConnections.remove(emitter); // 从活跃列表移除
            if (sessionId != null) sessionConnections.remove(sessionId); // 从 session 映射移除
        });

        return emitter; // 返回 SseEmitter，Spring MVC 将其注册为 SSE 长连接
    }

    /**
     * POST /api/agent/mcp/message — JSON-RPC 消息端点
     *
     * 客户端通过此端点发送 JSON-RPC 请求，服务端处理后通过 SSE 推送响应。
     * 处理流程：
     *   1. 接收 POST 请求体（JSON-RPC 格式的 JSON 字符串）
     *   2. 反序列化为 JsonRpcRequest 对象
     *   3. 调用 McpServerService.handleRequest 处理请求
     *   4. 序列化响应为 JSON 字符串
     *   5. 根据 sessionId 查找对应 SSE 连接并推送响应
     *
     * 支持的 MCP 方法：
     *   - initialize：握手
     *   - tools/list：获取工具列表
     *   - tools/call：调用工具
     *   - ping：心跳
     *
     * @param sessionId 会话 ID（可选），用于关联 SSE 连接
     * @param requestBody JSON-RPC 请求的原始 JSON 字符串
     */
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE) // POST 消息端点
    public void handleMessage( // 处理消息方法（void 返回：响应通过 SSE 推送而非 HTTP 响应）
            @RequestParam(value = "sessionId", required = false) String sessionId, // 会话 ID
            @RequestBody String requestBody) { // 原始请求体字符串（不直接反序列化，便于错误处理）
        log.debug("MCP 消息: sessionId={}, body={}", sessionId, requestBody); // DEBUG 日志

        try { // 整个处理过程异常捕获
            // Step 1: 解析 JSON-RPC 请求
            McpProtocol.JsonRpcRequest request = objectMapper.readValue( // Jackson 反序列化
                    requestBody, McpProtocol.JsonRpcRequest.class); // JSON → JsonRpcRequest 对象

            // Step 2: 处理 MCP 请求（分发到对应 handler）
            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request); // 调用核心服务

            // Step 3: 序列化响应为 JSON
            String responseJson = objectMapper.writeValueAsString(response); // JsonRpcResponse → JSON 字符串

            // Step 4: 查找目标 SSE 连接 — 优先根据 sessionId，无 sessionId 时取第一个活跃连接
            SseEmitter emitter = sessionId != null // 判断是否有 sessionId
                    ? sessionConnections.get(sessionId) // 有：从 session 映射精确查找
                    : (activeConnections.isEmpty() ? null : activeConnections.get(0)); // 无：取第一个活跃连接

            if (emitter != null) { // 找到连接
                emitter.send(SseEmitter.event() // 构建 SSE 事件
                        .name("message") // 事件名：message
                        .data(responseJson)); // 事件数据：JSON-RPC 响应 JSON
            } else { // 无活跃连接
                log.warn("没有活跃的 SSE 连接来发送响应"); // 记录警告
            }
        } catch (Exception e) { // 处理异常
            log.error("处理 MCP 消息失败: {}", e.getMessage()); // 记录错误
        }
    }

    /**
     * GET /api/agent/mcp/status — MCP Server 状态端点
     * 用于运维监控和健康检查，返回当前 MCP 服务的运行状态。
     *
     * @return 状态 Map：status、protocolVersion、serverName、toolCount、activeConnections、transport
     */
    @GetMapping("/status") // GET 状态端点
    public Map<String, Object> getStatus() { // 获取 MCP 状态
        return Map.of( // Java 9 Map.of 不可变 Map
                "status", "running", // 运行状态
                "protocolVersion", "2024-11-05", // MCP 协议版本
                "serverName", "bookstore-agent", // 服务名称
                "toolCount", mcpServerService.getToolDefinitions().size(), // 已注册工具数
                "activeConnections", activeConnections.size(), // 活跃 SSE 连接数
                "transport", "SSE" // 传输方式
        );
    }

    /**
     * GET /api/agent/mcp/tools — 工具列表端点（REST 方式）
     *
     * 绕过 MCP 协议，直接用 REST API 查看所有可用工具，方便调试和文档生成。
     * 返回格式：{ "tools": [...] }
     *
     * @return 工具定义列表
     */
    @GetMapping("/tools") // GET 工具列表端点
    public Object listTools() { // 获取工具列表
        return Map.of("tools", mcpServerService.getToolDefinitions()); // 返回工具定义
    }
}
