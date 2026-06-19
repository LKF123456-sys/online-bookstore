package com.bookstore.agent.mcp;

import com.bookstore.agent.tools.OrderTools;
import com.bookstore.agent.tools.ProductTools;
import com.bookstore.agent.tools.ReviewTools;
import com.bookstore.agent.rag.RagSearchTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Server — 将 Agent 的 Tool Calling 能力通过 MCP 协议暴露给外部客户端
 *
 * 设计说明：
 *   1. 启动时收集所有 @Tool 方法，注册为 MCP Tool 定义
 *   2. 外部 MCP 客户端（如 Claude Desktop）可通过 SSE 连接并调用这些工具
 *   3. 支持 MCP 协议的三个核心方法：initialize、tools/list、tools/call
 *
 * 面试亮点：
 *   1. 手写 MCP 协议实现，展示对协议底层的理解（非依赖 Spring AI MCP starter）
 *   2. 工具注册表模式：动态收集 + 运行时查找
 *   3. 本地 @Tool 调用与远程 MCP 调用共享同一套工具实现
 *   4. SSE 传输：复用现有 SSE 基础设施
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServerService {

    private final OrderTools orderTools;
    private final ProductTools productTools;
    private final ReviewTools reviewTools;
    private final RagSearchTool ragSearchTool;
    private final ObjectMapper objectMapper;

    /** 工具注册表：name → ToolHandler */
    private final Map<String, ToolHandler> toolRegistry = new ConcurrentHashMap<>();

    /**
     * 启动时注册所有工具
     */
    @PostConstruct
    public void registerTools() {
        log.info("===== 注册 MCP Server 工具 =====");

        // 商品工具
        registerTool("search_products", "根据关键词搜索图书商品",
                Map.of("type", "object", "properties", Map.of(
                        "keyword", Map.of("type", "string", "description", "搜索关键词")
                ), "required", List.of("keyword")),
                params -> productTools.searchProducts((String) params.get("keyword")));

        registerTool("get_product_detail", "获取指定图书的详细信息",
                Map.of("type", "object", "properties", Map.of(
                        "productId", Map.of("type", "string", "description", "商品ID")
                ), "required", List.of("productId")),
                params -> productTools.getProductDetail((String) params.get("productId")));

        registerTool("get_recommend_products", "获取系统推荐的图书列表",
                Map.of("type", "object", "properties", Map.of()),
                params -> productTools.getRecommendProducts());

        registerTool("get_hot_products", "获取热销图书排行榜",
                Map.of("type", "object", "properties", Map.of()),
                params -> productTools.getHotProducts());

        // 订单工具
        registerTool("query_order_detail", "查询指定订单的详细信息",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "orderId", Map.of("type", "string", "description", "订单ID")
                ), "required", List.of("userId", "orderId")),
                params -> orderTools.queryOrderDetail(
                        (String) params.get("userId"), (String) params.get("orderId")));

        registerTool("query_order_list", "查询用户的订单列表",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "status", Map.of("type", "string", "description", "状态筛选"),
                        "pageNum", Map.of("type", "integer", "description", "页码")
                ), "required", List.of("userId")),
                params -> orderTools.queryOrderList(
                        (String) params.get("userId"),
                        (String) params.getOrDefault("status", null),
                        params.containsKey("pageNum") ? ((Number) params.get("pageNum")).intValue() : 1));

        registerTool("cancel_order", "取消指定订单",
                Map.of("type", "object", "properties", Map.of(
                        "userId", Map.of("type", "string", "description", "用户ID"),
                        "orderId", Map.of("type", "string", "description", "订单ID")
                ), "required", List.of("userId", "orderId")),
                params -> orderTools.cancelOrder(
                        (String) params.get("userId"), (String) params.get("orderId")));

        // 评价工具
        registerTool("get_product_reviews", "获取指定图书的用户评价",
                Map.of("type", "object", "properties", Map.of(
                        "productId", Map.of("type", "string", "description", "商品ID"),
                        "pageNum", Map.of("type", "integer", "description", "页码")
                ), "required", List.of("productId")),
                params -> reviewTools.getProductReviews(
                        (String) params.get("productId"),
                        params.containsKey("pageNum") ? ((Number) params.get("pageNum")).intValue() : 1));

        // RAG 语义搜索工具
        registerTool("semantic_search_books", "基于语义理解搜索图书（RAG）",
                Map.of("type", "object", "properties", Map.of(
                        "query", Map.of("type", "string", "description", "搜索意图描述"),
                        "topK", Map.of("type", "integer", "description", "返回数量，默认5")
                ), "required", List.of("query")),
                params -> ragSearchTool.semanticSearchBooks(
                        (String) params.get("query"),
                        params.containsKey("topK") ? ((Number) params.get("topK")).intValue() : 5));

        registerTool("search_knowledge", "搜索图书领域知识库（RAG）",
                Map.of("type", "object", "properties", Map.of(
                        "query", Map.of("type", "string", "description", "知识问题")
                ), "required", List.of("query")),
                params -> ragSearchTool.searchKnowledge((String) params.get("query")));

        log.info("MCP Server 工具注册完成: {} 个工具", toolRegistry.size());
    }

    /**
     * 处理 MCP 请求（JSON-RPC 2.0 分发）
     */
    public McpProtocol.JsonRpcResponse handleRequest(McpProtocol.JsonRpcRequest request) {
        String method = request.getMethod();
        String id = request.getId();
        log.debug("MCP 请求: method={}, id={}", method, id);

        return switch (method) {
            case "initialize" -> handleInitialize(id);
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(id, request.getParams());
            case "ping" -> McpProtocol.JsonRpcResponse.success(id, Map.of("status", "ok"));
            default -> McpProtocol.JsonRpcResponse.error(id, -32601, "Method not found: " + method);
        };
    }

    /**
     * 获取工具定义列表（MCP tools/list 响应）
     */
    public List<McpProtocol.McpToolDefinition> getToolDefinitions() {
        List<McpProtocol.McpToolDefinition> definitions = new ArrayList<>();
        for (var entry : toolRegistry.entrySet()) {
            definitions.add(new McpProtocol.McpToolDefinition(
                    entry.getKey(),
                    entry.getValue().description,
                    entry.getValue().inputSchema
            ));
        }
        return definitions;
    }

    // ===== 内部方法 =====

    private McpProtocol.JsonRpcResponse handleInitialize(String id) {
        McpProtocol.McpInitializeResult result = new McpProtocol.McpInitializeResult();
        return McpProtocol.JsonRpcResponse.success(id, result);
    }

    private McpProtocol.JsonRpcResponse handleToolsList(String id) {
        Map<String, Object> result = Map.of("tools", getToolDefinitions());
        return McpProtocol.JsonRpcResponse.success(id, result);
    }

    private McpProtocol.JsonRpcResponse handleToolsCall(String id, Map<String, Object> params) {
        if (params == null) {
            return McpProtocol.JsonRpcResponse.error(id, -32602, "Missing params");
        }

        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        ToolHandler handler = toolRegistry.get(toolName);
        if (handler == null) {
            return McpProtocol.JsonRpcResponse.error(id, -32602, "Unknown tool: " + toolName);
        }

        try {
            String result = handler.executor.execute(arguments);
            McpProtocol.McpToolCallResult callResult = new McpProtocol.McpToolCallResult(
                    List.of(new McpProtocol.McpContent("text", result)),
                    false
            );
            return McpProtocol.JsonRpcResponse.success(id, callResult);
        } catch (Exception e) {
            log.error("MCP 工具调用失败: tool={}, error={}", toolName, e.getMessage());
            McpProtocol.McpToolCallResult errorResult = new McpProtocol.McpToolCallResult(
                    List.of(new McpProtocol.McpContent("text", "工具调用失败: " + e.getMessage())),
                    true
            );
            return McpProtocol.JsonRpcResponse.success(id, errorResult);
        }
    }

    private void registerTool(String name, String description,
                              Map<String, Object> inputSchema, ToolExecutor executor) {
        toolRegistry.put(name, new ToolHandler(name, description, inputSchema, executor));
    }

    @FunctionalInterface
    interface ToolExecutor {
        String execute(Map<String, Object> params);
    }

    private record ToolHandler(
            String name,
            String description,
            Map<String, Object> inputSchema,
            ToolExecutor executor
    ) {}
}
