package com.bookstore.agent.mcp; // mcp 包：存放 MCP (Model Context Protocol) 协议实现，将 Agent 工具暴露给外部客户端

// Lombok @AllArgsConstructor — 生成全参构造函数
import lombok.AllArgsConstructor;
// Lombok @Data — 生成 getter/setter/toString/equals/hashCode
import lombok.Data;
// Lombok @NoArgsConstructor — 生成无参构造函数（JSON 反序列化需要）
import lombok.NoArgsConstructor;

// Java Map 接口
import java.util.Map;

/**
 * MCP (Model Context Protocol) JSON-RPC 2.0 消息协议定义
 *
 * 什么是 MCP？
 *   MCP 是 Anthropic 提出的 AI 模型上下文协议，定义了 LLM 客户端与工具服务端之间的
 *   标准化通信格式。基于 JSON-RPC 2.0，支持 stdio 和 SSE 两种传输方式。
 *   本项目使用 SSE 传输方式，使得 Claude Desktop 等 MCP 客户端可直接连接 BookVerse。
 *
 * JSON-RPC 2.0 基础协议格式：
 *   请求：{ "jsonrpc": "2.0", "id": "1", "method": "tools/list", "params": {} }
 *   响应：{ "jsonrpc": "2.0", "id": "1", "result": {...} }
 *   错误：{ "jsonrpc": "2.0", "id": "1", "error": {"code": -32601, "message": "..."} }
 *
 * MCP 协议中的核心方法：
 *   ┌──────────────┬──────────────────────────────────────────────────────┐
 *   │ 方法         │ 说明                                                 │
 *   ├──────────────┼──────────────────────────────────────────────────────┤
 *   │ initialize   │ 客户端握手：交换协议版本和能力声明                     │
 *   │ tools/list   │ 列出服务端提供的所有工具及其参数 Schema                │
 *   │ tools/call   │ 调用指定工具并获取结果                               │
 *   │ ping         │ 心跳检测                                             │
 *   └──────────────┴──────────────────────────────────────────────────────┘
 *
 * 本类结构：
 *   - JsonRpcRequest / JsonRpcResponse / JsonRpcError：JSON-RPC 2.0 基础结构
 *   - McpToolDefinition / McpToolCallResult / McpContent：MCP 工具调用相关结构
 *   - McpInitializeResult / McpCapabilities / McpServerInfo：MCP 初始化响应结构
 *
 * 面试亮点：
 *   手写 MCP 协议实现（非依赖 Spring AI MCP Starter），展示对协议底层机制的深入理解
 */
public class McpProtocol { // MCP 协议消息定义容器类（不实例化，只包含静态内部类）

    // ===== JSON-RPC 2.0 基础消息结构 =====

    /**
     * JSON-RPC 2.0 请求对象
     * jsonrpc：固定 "2.0"
     * id：请求唯一标识（用于匹配响应），MCP 中为字符串或数字
     * method：要调用的方法名（如 tools/list、tools/call）
     * params：方法参数，Map<String, Object> 通用格式
     */
    @Data // Lombok：getter/setter/toString
    @NoArgsConstructor // Jackson 反序列化需要无参构造
    @AllArgsConstructor // 便于手动创建实例
    public static class JsonRpcRequest { // JSON-RPC 请求
        private String jsonrpc = "2.0"; // JSON-RPC 版本号，固定 2.0
        private String id; // 请求 ID，用于关联响应
        private String method; // 调用的方法名
        private Map<String, Object> params; // 方法参数（可选）
    }

    /**
     * JSON-RPC 2.0 响应对象
     * result 和 error 互斥：成功时 result 有值 error=null，失败时相反。
     * 提供 success() 和 error() 静态工厂方法，便于创建响应对象。
     */
    @Data // Lombok
    @NoArgsConstructor // 无参构造
    @AllArgsConstructor // 全参构造
    public static class JsonRpcResponse { // JSON-RPC 响应
        private String jsonrpc = "2.0"; // 版本号
        private String id; // 与请求对应的 ID
        private Object result; // 成功时的返回数据
        private JsonRpcError error; // 失败时的错误信息

        /**
         * 创建成功响应的静态工厂方法
         * @param id 请求 ID
         * @param result 响应数据
         */
        public static JsonRpcResponse success(String id, Object result) { // 工厂方法：成功响应
            JsonRpcResponse resp = new JsonRpcResponse(); // 创建响应对象
            resp.setJsonrpc("2.0"); // 设置版本
            resp.setId(id); // 设置请求 ID
            resp.setResult(result); // 设置结果数据
            return resp; // 返回响应
        }

        /**
         * 创建错误响应的静态工厂方法
         * @param id 请求 ID
         * @param code JSON-RPC 错误码（-32700 解析错误 / -32601 方法不存在 / -32602 无效参数）
         * @param message 错误描述
         */
        public static JsonRpcResponse error(String id, int code, String message) { // 工厂方法：错误响应
            JsonRpcResponse resp = new JsonRpcResponse(); // 创建响应对象
            resp.setJsonrpc("2.0"); // 设置版本
            resp.setId(id); // 设置请求 ID
            resp.setError(new JsonRpcError(code, message)); // 创建错误对象并设置
            return resp; // 返回响应
        }
    }

    /**
     * JSON-RPC 2.0 错误对象
     * code：数字错误码
     *   -32700：解析错误
     *   -32601：方法不存在
     *   -32602：无效参数
     *   -32603：内部错误
     */
    @Data // Lombok
    @NoArgsConstructor // 无参构造
    @AllArgsConstructor // 全参构造
    public static class JsonRpcError { // JSON-RPC 错误
        private int code; // 错误码
        private String message; // 错误消息
    }

    // ===== MCP 工具相关结构 =====

    /**
     * MCP 工具定义 — 描述一个可用工具的元数据
     * name：工具唯一标识（如 search_products）
     * description：工具功能描述（供 LLM 理解何时调用）
     * inputSchema：JSON Schema 格式的参数定义
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolDefinition { // 工具定义
        private String name; // 工具名称
        private String description; // 工具描述
        private Map<String, Object> inputSchema;  // JSON Schema 格式的参数定义
    }

    /**
     * MCP 工具调用结果
     * content：返回内容列表（支持多类型：text、image、resource）
     * isError：是否因工具执行异常而返回
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolCallResult { // 工具调用结果
        private java.util.List<McpContent> content; // 内容列表
        private boolean isError; // 是否为错误结果
    }

    /**
     * MCP 内容项
     * type：内容类型 — "text" | "image" | "resource"
     * text：文本内容（当 type=text 时）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpContent { // 内容项
        private String type;  // 类型：text / image / resource
        private String text; // 文本内容
    }

    // ===== MCP 初始化响应结构 =====

    /**
     * MCP 初始化结果
     * 客户端发送 initialize 请求后，服务端返回此结构。
     * protocolVersion：MCP 协议版本（2024-11-05）
     * capabilities：服务端能力声明（如是否支持 tools）
     * serverInfo：服务端基本信息（名称、版本）
     */
    @Data
    public static class McpInitializeResult { // 初始化结果
        private String protocolVersion = "2024-11-05"; // MCP 协议版本
        private McpCapabilities capabilities = new McpCapabilities(); // 能力声明
        private McpServerInfo serverInfo = new McpServerInfo(); // 服务端信息
    }

    /**
     * MCP 能力声明
     * tools：声明工具相关能力，如 listChanged=false 表示工具列表不会动态变化
     */
    @Data
    public static class McpCapabilities { // 能力声明
        private Map<String, Object> tools = Map.of("listChanged", false); // 工具能力：列表不变
    }

    /**
     * MCP 服务端信息
     * name：服务名称，name/version 组合用于客户端识别和版本兼容检查
     */
    @Data
    public static class McpServerInfo { // 服务端信息
        private String name = "bookstore-agent"; // 服务名称
        private String version = "1.0.0"; // 服务版本
    }
}
