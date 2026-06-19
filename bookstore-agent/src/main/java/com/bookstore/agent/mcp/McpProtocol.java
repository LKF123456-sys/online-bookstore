package com.bookstore.agent.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP (Model Context Protocol) JSON-RPC 2.0 消息定义
 *
 * MCP 基于 JSON-RPC 2.0 协议，所有通信都是 request → response 模式。
 * 传输层可以是 stdio 或 SSE（本项目使用 SSE）。
 *
 * 协议方法：
 *   - initialize: 客户端握手，交换能力声明
 *   - tools/list: 列出服务端提供的所有工具
 *   - tools/call: 调用指定工具并获取结果
 *   - ping: 心跳检测
 *
 * 面试亮点：
 *   手写 MCP 协议实现（非依赖框架），展示对协议底层的理解
 */
public class McpProtocol {

    // ===== JSON-RPC 2.0 基础结构 =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonRpcRequest {
        private String jsonrpc = "2.0";
        private String id;
        private String method;
        private Map<String, Object> params;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonRpcResponse {
        private String jsonrpc = "2.0";
        private String id;
        private Object result;
        private JsonRpcError error;

        public static JsonRpcResponse success(String id, Object result) {
            JsonRpcResponse resp = new JsonRpcResponse();
            resp.setJsonrpc("2.0");
            resp.setId(id);
            resp.setResult(result);
            return resp;
        }

        public static JsonRpcResponse error(String id, int code, String message) {
            JsonRpcResponse resp = new JsonRpcResponse();
            resp.setJsonrpc("2.0");
            resp.setId(id);
            resp.setError(new JsonRpcError(code, message));
            return resp;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonRpcError {
        private int code;
        private String message;
    }

    // ===== MCP 工具定义 =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolDefinition {
        private String name;
        private String description;
        private Map<String, Object> inputSchema;  // JSON Schema 格式
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolCallResult {
        private java.util.List<McpContent> content;
        private boolean isError;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpContent {
        private String type;  // "text" | "image" | "resource"
        private String text;
    }

    // ===== MCP 初始化响应 =====

    @Data
    public static class McpInitializeResult {
        private String protocolVersion = "2024-11-05";
        private McpCapabilities capabilities = new McpCapabilities();
        private McpServerInfo serverInfo = new McpServerInfo();
    }

    @Data
    public static class McpCapabilities {
        private Map<String, Object> tools = Map.of("listChanged", false);
    }

    @Data
    public static class McpServerInfo {
        private String name = "bookstore-agent";
        private String version = "1.0.0";
    }
}
