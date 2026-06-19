package com.bookstore.agent.tools;

import com.bookstore.agent.feign.OrderFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.OrderItemVO;
import com.bookstore.common.api.vo.OrderVO;
import com.bookstore.common.api.vo.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 订单工具集单元测试 — 验证 @Tool 方法在各种场景下的行为
 *
 * 面试亮点：
 *   1. 覆盖正常路径 + 边界条件 + 异常降级
 *   2. Mock Feign 客户端，隔离外部服务依赖
 *   3. 验证返回文本的可读性（工具返回给 LLM 的文本质量直接影响回复质量）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderTools 订单工具集测试")
class OrderToolsTest {

    @Mock
    private OrderFeignClient orderFeignClient;

    @InjectMocks
    private OrderTools orderTools;

    private OrderVO sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new OrderVO();
        sampleOrder.setOrderid("ORD-001");
        sampleOrder.setUserid("user-123");
        sampleOrder.setStatus("PAID");
        sampleOrder.setTotalprice(new BigDecimal("199.00"));
        sampleOrder.setOriginalprice(new BigDecimal("249.00"));
        sampleOrder.setDiscountamount(new BigDecimal("50.00"));
        sampleOrder.setOrderdate(LocalDateTime.of(2025, 6, 1, 10, 30));
        sampleOrder.setShiptofirstname("张");
        sampleOrder.setShiptolastname("三");
        sampleOrder.setShipaddr1("北京市朝阳区xx路1号");
        sampleOrder.setShipcity("北京");

        OrderItemVO item = new OrderItemVO();
        item.setProductName("Spring实战");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("99.50"));
        sampleOrder.setItems(List.of(item));
    }

    @Nested
    @DisplayName("查询订单详情")
    class QueryOrderDetailTests {

        @Test
        @DisplayName("正常查询 — 返回格式化的订单详情")
        void shouldReturnFormattedOrderDetail() {
            when(orderFeignClient.getOrderById("user-123", "ORD-001"))
                    .thenReturn(Result.success(sampleOrder));

            String result = orderTools.queryOrderDetail("user-123", "ORD-001");

            assertNotNull(result);
            assertTrue(result.contains("ORD-001"));
            assertTrue(result.contains("PAID"));
            assertTrue(result.contains("199.00"));
            assertTrue(result.contains("张"));
            assertTrue(result.contains("Spring实战"));
            verify(orderFeignClient).getOrderById("user-123", "ORD-001");
        }

        @Test
        @DisplayName("订单不存在 — 返回友好提示")
        void shouldReturnNotFoundMessage() {
            when(orderFeignClient.getOrderById("user-123", "INVALID"))
                    .thenReturn(Result.error(404, "订单不存在"));

            String result = orderTools.queryOrderDetail("user-123", "INVALID");

            assertTrue(result.contains("未找到订单"));
        }

        @Test
        @DisplayName("Feign 调用异常 — 返回错误描述而非抛异常")
        void shouldHandleFeignException() {
            when(orderFeignClient.getOrderById("user-123", "ORD-001"))
                    .thenThrow(new RuntimeException("Connection refused"));

            String result = orderTools.queryOrderDetail("user-123", "ORD-001");

            assertTrue(result.contains("错误"));
            assertTrue(result.contains("Connection refused"));
        }
    }

    @Nested
    @DisplayName("查询订单列表")
    class QueryOrderListTests {

        @Test
        @DisplayName("正常查询 — 返回订单列表摘要")
        void shouldReturnOrderListSummary() {
            PageResult<OrderVO> page = new PageResult<>();
            page.setRecords(List.of(sampleOrder));
            page.setTotal(1L);
            page.setPageNum(1);

            when(orderFeignClient.listOrders("user-123", 1, 5, null))
                    .thenReturn(Result.success(page));

            String result = orderTools.queryOrderList("user-123", null, 1);

            assertTrue(result.contains("1 条订单"));
            assertTrue(result.contains("ORD-001"));
        }

        @Test
        @DisplayName("按状态筛选 — 传递状态参数")
        void shouldFilterByStatus() {
            PageResult<OrderVO> page = new PageResult<>();
            page.setRecords(List.of());
            page.setTotal(0L);

            when(orderFeignClient.listOrders("user-123", 1, 5, "PAID"))
                    .thenReturn(Result.success(page));

            String result = orderTools.queryOrderList("user-123", "PAID", 1);

            assertTrue(result.contains("没有符合条件的订单"));
            verify(orderFeignClient).listOrders("user-123", 1, 5, "PAID");
        }

        @Test
        @DisplayName("空结果 — 返回空列表提示")
        void shouldHandleEmptyList() {
            PageResult<OrderVO> page = new PageResult<>();
            page.setRecords(List.of());
            page.setTotal(0L);

            when(orderFeignClient.listOrders("user-123", 1, 5, null))
                    .thenReturn(Result.success(page));

            String result = orderTools.queryOrderList("user-123", null, 1);

            assertTrue(result.contains("没有符合条件的订单"));
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrderTests {

        @Test
        @DisplayName("正常取消 — 返回成功消息")
        void shouldCancelOrderSuccessfully() {
            when(orderFeignClient.cancelOrder("user-123", "ORD-001"))
                    .thenReturn(Result.success(null));

            String result = orderTools.cancelOrder("user-123", "ORD-001");

            assertTrue(result.contains("已成功取消"));
        }

        @Test
        @DisplayName("取消失败（状态不允许）— 返回错误信息")
        void shouldReturnErrorWhenCancelFails() {
            when(orderFeignClient.cancelOrder("user-123", "ORD-001"))
                    .thenReturn(Result.error(409, "订单已发货，无法取消"));

            String result = orderTools.cancelOrder("user-123", "ORD-001");

            assertTrue(result.contains("取消订单失败"));
        }
    }
}
