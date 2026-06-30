package com.bookstore.agent.mcp; // mcp 包：MCP 协议实现

// 导入三个工具类，MCP Server 将它们的公开方法注册为 MCP 工具
import com.bookstore.agent.tools.OrderTools;
import com.bookstore.agent.tools.ProductTools;
import com.bookstore.agent.tools.ReviewTools;
// 导入 RAG 语义搜索工具
import com.bookstore.agent.rag.RagSearchTool;
// Jackson ObjectMapper — JSON 序列化/反序列化
import com.fasterxml.jackson.databind.ObjectMapper;
// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Spring @Service 注解
import org.springframework.stereotype.Service;

// Jakarta @PostConstruct — Bean 初始化后回调
import jakarta.annotation.PostConstruct;
// Java 集合
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // 线程安全的 HashMap，用于工具注册表

/**
 * MCP Server 核心服务 — 管理工具注册、请求分发和调用执行
 *
 * 架构设计 — 工具注册表模式（Tool Registry Pattern）：
 *   1. 启动时通过 @PostConstruct 扫描所有 @Tool 方法，注册到 toolRegistry
 *   2. 每个工具包含：名称、描述、JSON Schema 参数定义、执行器（Lambda）
 *   3. 运行时通过 handleRequest() 根据 method 分发到对应处理器
 *   4. tools/call 通过工具名在注册表中查找并执行
 *
 * 与 Spring AI Tool Calling 的关系：
 *   Spring AI Tool Calling：Agent → LLM 自动调用 → 框架拦截 → 执行 → 结果注入
 *   MCP Tool Calling：  外部客户端 → SSE 请求 → handleRequest → 查找注册表 → 执行
 *   两者的不同：Spring AI 是 LLM 驱动（AI 决定调用时机），MCP 是客户端驱动（用户/程序决定）
 *   两者的相同：底层调用相同的 Tool 实现（OrderTools / ProductTools 等），代码复用
 *
 * 工具注册清单（10 个工具）：
 *   ┌───────────────────────┬─────────────────┬─────────────────────────────────┐
 *   │ 工具名                │ 来源            │ 功能                            │
 *   ├───────────────────────┼─────────────────┼─────────────────────────────────┤
 *   │ search_products       │ ProductTools    │ 关键词搜索图书                   │
 *   │ get_product_detail    │ ProductTools    │ 获取商品详情                     │
 *   │ get_recommend_products│ ProductTools    │ 获取推荐图书                     │
 *   │ get_hot_products      │ ProductTools    │ 获取热销排行                     │
 *   │ query_order_detail    │ OrderTools      │ 查询订单详情                     │
 *   │ query_order_list      │ OrderTools      │ 查询订单列表                     │
 *   │ cancel_order          │ OrderTools      │ 取消订单                         │
 *   │ get_product_reviews   │ ReviewTools     │ 获取商品评价                     │
 *   │ semantic_search_books │ RagSearchTool   │ RAG 语义搜索                    │
 *   │ search_knowledge      │ RagSearchTool   │ 搜索领域知识库                   │
 *   └───────────────────────┴─────────────────┴─────────────────────────────────┘
 *
 * 面试亮点：
 *   1. 手写 MCP 协议实现，不依赖 Spring AI MCP Starter — 展示协议底层理解
 *   2. 工具注册表模式：启动时收集 → Map 存储 → 运行时 O(1) 查找
 *   3. 代码复用：Spring AI @Tool 和 MCP tools/call 共用同一套工具实现
 *   4. JSON Schema 参数定义：标准化参数验证，与函数调用兼容
 */
@Slf4j // Lombok：log
@Service // Spring：Service Bean
@RequiredArgsConstructor // Lombok：构造器注入
public class McpServerService { // MCP Server 核心服务

    private final OrderTools orderTools; // 订单工具集
    private final ProductTools productTools; // 商品工具集
    private final ReviewTools reviewTools; // 评价工具集
    private final RagSearchTool ragSearchTool; // RAG 语义搜索工具
    private final ObjectMapper objectMapper; // Jackson JSON 序列化（当前未直接使用，保留供扩展）

    /**
     * 工具注册表 — 线程安全的 name → ToolHandler 映射
     * ConcurrentHashMap 保证多线程（多 MCP 客户端）并发访问安全。
     */
    private final Map<String, ToolHandler> toolRegistry = new ConcurrentHashMap<>(); // 工具注册表

    /**
     * 启动时自动注册所有 MCP 工具
     * @PostConstruct 确保在依赖注入完成后执行，此时 orderTools 等已可用。
     *
     * 注册过程：对每个工具调用 registerTool()，提供：
     *   1. 工具名（snake_case，MCP 约定）
     *   2. 功能描述
     *   3. JSON Schema 参数定义（type + properties + required）
     *   4. Lambda 执行器：将 params Map 转为具体方法调用
     */
    @PostConstruct // Jakarta 注解：Bean 初始化后执行
    public void registerTools() { // 工具注册入口方法
        log.info("===== 注册 MCP Server 工具 ====="); // 启动标记

        // ===== 商品工具（4个）=====

        // search_products：关键词搜索，必需参数 keyword
        registerTool("search_products", "根据关键词搜索图书商品", // 工具名 + 描述
                Map.of("type", "object", "properties", Map.of( // JSON Schema：参数类型为 object
                        "keyword", Map.of("type", "string", "description", "搜索关键词") // keyword 参数：string 类型
                ), "required", List.of("keyword")), // 必需参数列表
                params -> productTools.searchProducts((String) params.get("keyword"))); // Lambda 执行器

        // get_product_detail：获取商品详情，必需参数 productId
        registerTool("get_product_detail", "获取指定图书的详细信息",
                Map.of("type", "object", "properties", Map.of(
                        "productId", Map.of("type", "string", "description", "商品ID")
                ), "required", List.of("productId")),
                params -> productTools.getProductDetail((String) params.get("productId")));

        // get_recommend_products：无参数
        registerTool("get_recommend_products", "获取系统推荐的图书列表",
                Map.of("type", "object", "properties", Map.of()), // 无参数工具
                params -> productTools.getRecommendProducts());

        // get_hot_products：无参数
        registerTool("get_hot_products", "获取热销图书排行榜",
                Map.of("type", "object", "properties", Map.of()),
                params -> productTools.getHotProducts());

        // ===== 订单工具（3个）=====

        // query_order_detail：必需参数 userId + orderId
        registerTool("query_order_detail", "查询指定订单的详细信息",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "orderId", Map.of("type", "string", "description", "订单ID")
                ), "required", List.of("userId", "orderId")), // 两个参数都必需
                params -> orderTools.queryOrderDetail( // Lambda 执行器
                        (String) params.get("userId"), (String) params.get("orderId"))); // 提取参数并调用

        // query_order_list：必需参数 userId，可选 status 和 pageNum
        registerTool("query_order_list", "查询用户的订单列表",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "status", Map.of("type", "string", "description", "状态筛选"), // 可选
                        "pageNum", Map.of("type", "integer", "description", "页码") // 可选
                ), "required", List.of("userId")), // 仅 userId 必需
                params -> orderTools.queryOrderList( // Lambda 执行器
                        (String) params.get("userId"), // userId
                        (String) params.getOrDefault("status", null), // status 默认为 null
                        params.containsKey("pageNum") ? ((Number) params.get("pageNum")).intValue() : 1)); // pageNum 默认 1

        // cancel_order：必需参数 userId + orderId
        registerTool("cancel_order", "取消指定订单",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "orderId", Map.of("type", "string", "description", "订单ID")
                ), "required", List.of("userId", "orderId")),
                params -> orderTools.cancelOrder(
                        (String) params.get("userId"), (String) params.get("orderId")));

        // ===== 评价工具（1个）=====

        // get_product_reviews：必需参数 productId，可选 pageNum
        registerTool("get_product_reviews", "获取指定图书的用户评价",
                Map.of("type", "object", "properties", Map.of(
                        "productId", Map.of("type", "string", "description", "商品ID"),
                        "pageNum", Map.of("type", "integer", "description", "页码")
                ), "required", List.of("productId")),
                params -> reviewTools.getProductReviews(
                        (String) params.get("productId"),
                        params.containsKey("pageNum") ? ((Number) params.get("pageNum")).intValue() : 1));

        // ===== RAG 语义搜索工具（2个）=====

        // semantic_search_books：必需参数 query，可选 topK
        registerTool("semantic_search_books", "基于语义理解搜索图书（RAG）",
                Map.of("type", "object", "properties", Map.of(
                        "query", Map.of("type", "string", "description", "搜索意图描述"),
                        "topK", Map.of("type", "integer", "description", "返回数量，默认5")
                ), "required", List.of("query")),
                params -> ragSearchTool.semanticSearchBooks(
                        (String) params.get("query"),
                        params.containsKey("topK") ? ((Number) params.get("topK")).intValue() : 5)); // topK 默认 5

        // search_knowledge：必需参数 query
        registerTool("search_knowledge", "搜索图书领域知识库（RAG）",
                Map.of("type", "object", "properties", Map.of(
                        "query", Map.of("type", "string", "description", "知识问题")
                ), "required", List.of("query")),
                params -> ragSearchTool.searchKnowledge((String) params.get("query")));

        log.info("MCP Server 工具注册完成: {} 个工具", toolRegistry.size()); // 输出注册统计
    }

    /**
     * MCP 请求分发器 — 根据 JSON-RPC method 路由到对应处理器
     *
     * 支持的 4 个核心方法：
     *   initialize：握手，返回能力声明和服务信息
     *   tools/list：列出所有可用工具（名称、描述、参数 Schema）
     *   tools/call：调用指定工具，传入参数，返回执行结果
     *   ping：心跳，返回 {"status": "ok"}
     *
     * @param request JSON-RPC 2.0 请求对象
     * @return JSON-RPC 2.0 响应对象
     */
    public McpProtocol.JsonRpcResponse handleRequest(McpProtocol.JsonRpcRequest request) { // MCP 请求分发
        String method = request.getMethod(); // 提取方法名
        String id = request.getId(); // 提取请求 ID
        log.debug("MCP 请求: method={}, id={}", method, id); // 记录日志

        return switch (method) { // Java 增强 switch 表达式路由
            case "initialize" -> handleInitialize(id); // 握手：返回服务能力和信息
            case "tools/list" -> handleToolsList(id); // 工具列表：返回所有注册工具
            case "tools/call" -> handleToolsCall(id, request.getParams()); // 工具调用：执行指定工具
            case "ping" -> McpProtocol.JsonRpcResponse.success(id, Map.of("status", "ok")); // 心跳：返回 ok
            default -> McpProtocol.JsonRpcResponse.error(id, -32601, "Method not found: " + method); // 未知方法：-32601
        };
    }

    /**
     * 获取工具定义列表 — 用于 tools/list 响应
     * 遍历 toolRegistry，将每个 ToolHandler 转换为 McpToolDefinition
     *
     * @return 所有已注册工具的 MCP 定义列表
     */
    public List<McpProtocol.McpToolDefinition> getToolDefinitions() { // 获取工具定义列表
        List<McpProtocol.McpToolDefinition> definitions = new ArrayList<>(); // 创建结果列表
        for (var entry : toolRegistry.entrySet()) { // 遍历注册表
            definitions.add(new McpProtocol.McpToolDefinition( // 创建工具定义
                    entry.getKey(), // 工具名
                    entry.getValue().description, // 工具描述
                    entry.getValue().inputSchema // JSON Schema 参数定义
            ));
        }
        return definitions; // 返回定义列表
    }

    // ===== 内部方法 =====

    /**
     * 处理 initialize 请求
     * 返回 McpInitializeResult，包含协议版本、能力声明和服务信息。
     */
    private McpProtocol.JsonRpcResponse handleInitialize(String id) { // 处理初始化
        McpProtocol.McpInitializeResult result = new McpProtocol.McpInitializeResult(); // 创建初始化结果（使用默认值）
        return McpProtocol.JsonRpcResponse.success(id, result); // 返回成功响应
    }

    /**
     * 处理 tools/list 请求
     * 返回 {"tools": [...]} 格式的工具列表。
     */
    private McpProtocol.JsonRpcResponse handleToolsList(String id) { // 处理工具列表请求
        Map<String, Object> result = Map.of("tools", getToolDefinitions()); // 构建 tools/list 响应
        return McpProtocol.JsonRpcResponse.success(id, result); // 返回成功响应
    }

    /**
     * 处理 tools/call 请求 — 核心方法
     *
     * 处理流程：
     *   1. 检查 params 是否存在
     *   2. 从 params 提取工具名（name）和参数（arguments）
     *   3. 在注册表中查找工具处理器
     *   4. 执行工具并捕获异常
     *   5. 返回 McpToolCallResult（content + isError）
     *
     * @param id 请求 ID
     * @param params 请求参数，包含 name 和 arguments
     * @return JSON-RPC 响应
     */
    private McpProtocol.JsonRpcResponse handleToolsCall(String id, Map<String, Object> params) { // 处理工具调用请求
        if (params == null) { // 参数缺失检查
            return McpProtocol.JsonRpcResponse.error(id, -32602, "Missing params"); // -32602：无效参数
        }

        String toolName = (String) params.get("name"); // 提取工具名
        @SuppressWarnings("unchecked") // 抑制未经检查的类型转换警告
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of()); // 提取工具参数，默认空 Map

        ToolHandler handler = toolRegistry.get(toolName); // O(1) 查找
        if (handler == null) { // 工具未注册
            return McpProtocol.JsonRpcResponse.error(id, -32602, "Unknown tool: " + toolName); // -32602
        }

        try { // 执行工具，捕获异常
            String result = handler.executor.execute(arguments); // 通过 Lambda 执行器调用工具方法
            McpProtocol.McpToolCallResult callResult = new McpProtocol.McpToolCallResult( // 构建调用结果
                    List.of(new McpProtocol.McpContent("text", result)), // 内容列表：包含一条 text 类型的结果
                    false // isError = false：工具执行成功
            );
            return McpProtocol.JsonRpcResponse.success(id, callResult); // 返回成功响应
        } catch (Exception e) { // 工具执行异常
            log.error("MCP 工具调用失败: tool={}, error={}", toolName, e.getMessage()); // 记录错误日志
            McpProtocol.McpToolCallResult errorResult = new McpProtocol.McpToolCallResult( // 构建错误结果
                    List.of(new McpProtocol.McpContent("text", "工具调用失败: " + e.getMessage())), // 错误描述文本
                    true // isError = true：标记为错误结果
            );
            return McpProtocol.JsonRpcResponse.success(id, errorResult); // 注意：仍返回 success（MCP 约定，工具异常在 content 中体现）
        }
    }

    /**
     * 注册工具到注册表
     *
     * @param name 工具名（snake_case 约定）
     * @param description 工具功能描述
     * @param inputSchema JSON Schema 参数定义
     * @param executor 工具执行器（Lambda 表达式）
     */
    private void registerTool(String name, String description, // 工具注册方法
                              Map<String, Object> inputSchema, ToolExecutor executor) { // 参数 Schema + 执行器
        toolRegistry.put(name, new ToolHandler(name, description, inputSchema, executor)); // 创建 ToolHandler 并存入 Map
    }

    /**
     * 工具执行器函数式接口
     * 接收参数 Map，返回执行结果字符串。
     * 使用 @FunctionalInterface 确保 Lambda 表达式兼容。
     */
    @FunctionalInterface // 函数式接口注解：确保接口只有一个抽象方法
    interface ToolExecutor { // 工具执行器接口
        String execute(Map<String, Object> params); // 执行工具，返回结果
    }

    /**
     * 工具处理器记录类（Java 16 record）
     * 不可变数据对象，包含工具的全套元数据 + Lambda 执行器。
     * record 自动生成 equals/hashCode/toString 和全参构造函数。
     */
    private record ToolHandler( // 工具处理器 record
            String name, // 工具名
            String description, // 工具描述
            Map<String, Object> inputSchema, // JSON Schema 参数定义
            ToolExecutor executor // Lambda 执行器
    ) {}
}
