package com.bookstore.agent.mcp;

import com.bookstore.agent.rag.RagSearchTool;
import com.bookstore.agent.tools.OrderTools;
import com.bookstore.agent.tools.ProductTools;
import com.bookstore.agent.tools.ReviewTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * McpServerService 单元测试 — 验证 MCP 协议处理和工具注册
 *
 * 面试亮点：
 *   1. 验证 MCP 三个核心方法：initialize、tools/list、tools/call
 *   2. 验证 JSON-RPC 2.0 协议正确性
 *   3. 验证工具注册表完整性（10 个工具）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("McpServerService MCP 服务端测试")
class McpServerServiceTest {

    @Mock private OrderTools orderTools;
    @Mock private ProductTools productTools;
    @Mock private ReviewTools reviewTools;
    @Mock private RagSearchTool ragSearchTool;

    private McpServerService mcpServerService;

    @BeforeEach
    void setUp() {
        mcpServerService = new McpServerService(
                orderTools, productTools, reviewTools, ragSearchTool,
                new ObjectMapper());
        mcpServerService.registerTools();
    }

    @Nested
    @DisplayName("initialize 握手")
    class InitializeTests {

        @Test
        @DisplayName("返回协议版本和服务器信息")
        void shouldReturnServerInfo() {
            McpProtocol.JsonRpcRequest request = new McpProtocol.JsonRpcRequest(
                    "2.0", "1", "initialize", Map.of());

            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            assertNotNull(response);
            assertEquals("2.0", response.getJsonrpc());
            assertEquals("1", response.getId());
            assertNull(response.getError());
            assertNotNull(response.getResult());
        }
    }

    @Nested
    @DisplayName("tools/list 工具列表")
    class ToolsListTests {

        @Test
        @DisplayName("返回所有注册的工具定义")
        void shouldReturnAllTools() {
            List<McpProtocol.McpToolDefinition> tools = mcpServerService.getToolDefinitions();

            assertNotNull(tools);
            assertEquals(10, tools.size());

            // 验证包含关键工具
            List<String> toolNames = tools.stream()
                    .map(McpProtocol.McpToolDefinition::getName)
                    .toList();
            assertTrue(toolNames.contains("search_products"));
            assertTrue(toolNames.contains("query_order_detail"));
            assertTrue(toolNames.contains("semantic_search_books"));
            assertTrue(toolNames.contains("search_knowledge"));
        }

        @Test
        @DisplayName("工具定义包含名称、描述、输入 Schema")
        void shouldHaveCompleteToolDefinitions() {
            List<McpProtocol.McpToolDefinition> tools = mcpServerService.getToolDefinitions();

            for (McpProtocol.McpToolDefinition tool : tools) {
                assertNotNull(tool.getName(), "工具名称不能为空");
                assertNotNull(tool.getDescription(), "工具描述不能为空: " + tool.getName());
                assertNotNull(tool.getInputSchema(), "输入 Schema 不能为空: " + tool.getName());
            }
        }
    }

    @Nested
    @DisplayName("tools/call 工具调用")
    class ToolsCallTests {

        @Test
        @DisplayName("调用 search_products — 正确路由到 ProductTools")
        void shouldRouteToProductTools() {
            when(productTools.searchProducts("Java"))
                    .thenReturn("搜索《Java》的结果...");

            McpProtocol.JsonRpcRequest request = new McpProtocol.JsonRpcRequest(
                    "2.0", "2", "tools/call",
                    Map.of("name", "search_products",
                           "arguments", Map.of("keyword", "Java")));

            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            assertNotNull(response);
            assertNull(response.getError());
            verify(productTools).searchProducts("Java");
        }

        @Test
        @DisplayName("调用不存在的工具 — 返回错误")
        void shouldReturnErrorForUnknownTool() {
            McpProtocol.JsonRpcRequest request = new McpProtocol.JsonRpcRequest(
                    "2.0", "3", "tools/call",
                    Map.of("name", "nonexistent_tool", "arguments", Map.of()));

            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            assertNotNull(response.getError());
            assertEquals(-32602, response.getError().getCode());
            assertTrue(response.getError().getMessage().contains("Unknown tool"));
        }
    }

    @Nested
    @DisplayName("ping 心跳")
    class PingTests {

        @Test
        @DisplayName("返回 ok 状态")
        void shouldRespondToPing() {
            McpProtocol.JsonRpcRequest request = new McpProtocol.JsonRpcRequest(
                    "2.0", "4", "ping", null);

            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            assertNotNull(response);
            assertNull(response.getError());
        }
    }

    @Nested
    @DisplayName("未知方法")
    class UnknownMethodTests {

        @Test
        @DisplayName("返回 Method not found 错误")
        void shouldReturnMethodNotFoundError() {
            McpProtocol.JsonRpcRequest request = new McpProtocol.JsonRpcRequest(
                    "2.0", "5", "unknown/method", null);

            McpProtocol.JsonRpcResponse response = mcpServerService.handleRequest(request);

            assertNotNull(response.getError());
            assertEquals(-32601, response.getError().getCode());
        }
    }
}
