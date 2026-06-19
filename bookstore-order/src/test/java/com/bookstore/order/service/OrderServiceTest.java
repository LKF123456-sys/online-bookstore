package com.bookstore.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.OrderCreateDTO;
import com.bookstore.common.api.dto.OrderItemDTO;
import com.bookstore.common.api.vo.OrderVO;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ProductVO;
import com.bookstore.common.entity.OrderItem;
import com.bookstore.common.entity.Orders;
import com.bookstore.common.entity.OrderStatus;
import com.bookstore.common.util.SnowflakeIdGenerator;
import com.bookstore.order.feign.ProductFeignClient;
import com.bookstore.order.mapper.OrderItemMapper;
import com.bookstore.order.mapper.OrdersMapper;
import com.bookstore.order.mq.OrderMessageProducer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Mock
    private OrderMessageProducer orderMessageProducer;
    @Mock
    private MeterRegistry meterRegistry;

    private OrderService orderService;  // 手动构造，避免 @InjectMocks 在 setUp 前初始化

    private ProductVO sampleProduct;
    private Orders sampleOrder;
    private OrderItem sampleOrderItem;

    @BeforeEach
    void setUp() {
        // Mock MeterRegistry 返回 mock 指标对象（构造函数中使用）
        when(meterRegistry.counter(anyString())).thenReturn(mock(Counter.class));
        when(meterRegistry.summary(anyString())).thenReturn(mock(DistributionSummary.class));

        // 手动构造 OrderService，确保 meterRegistry mock 已配置
        orderService = new OrderService(ordersMapper, orderItemMapper, productFeignClient,
                snowflakeIdGenerator, orderMessageProducer, meterRegistry);

        sampleProduct = new ProductVO();
        sampleProduct.setId("P001");
        sampleProduct.setName("Java编程思想");
        sampleProduct.setPrice(new BigDecimal("89.00"));
        sampleProduct.setStock(100);
        sampleProduct.setImageUrl("http://img.example.com/java.jpg");

        sampleOrder = new Orders();
        sampleOrder.setOrderid("ORD001");
        sampleOrder.setUserid("user001");
        sampleOrder.setStatus(OrderStatus.PENDING_PAYMENT.getStatus());
        sampleOrder.setTotalprice(new BigDecimal("178.00"));
        sampleOrder.setOriginalprice(new BigDecimal("178.00"));
        sampleOrder.setDiscountamount(BigDecimal.ZERO);

        sampleOrderItem = new OrderItem();
        sampleOrderItem.setId(1L);
        sampleOrderItem.setOrderId("ORD001");
        sampleOrderItem.setProductId("P001");
        sampleOrderItem.setProductName("Java编程思想");
        sampleOrderItem.setPrice(new BigDecimal("89.00"));
        sampleOrderItem.setQuantity(2);
    }

    // ==================== 创建订单测试 ====================

    @Nested
    @DisplayName("创建订单")
    class CreateOrderTests {

        @Test
        @DisplayName("创建成功 — 正常下单，返回订单信息")
        void shouldCreateOrderSuccessfully() {
            OrderCreateDTO dto = new OrderCreateDTO();
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId("P001");
            itemDTO.setQuantity(2);
            dto.setItems(List.of(itemDTO));
            dto.setShipToFirstName("张");
            dto.setShipToLastName("三");
            dto.setShipAddr1("北京市朝阳区");

            when(snowflakeIdGenerator.nextOrderId("ORD")).thenReturn("ORD123456");
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(ordersMapper.insert(any(Orders.class))).thenReturn(1);
            // convertToVO 内部调用 orderItemMapper.selectList
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            OrderVO result = orderService.createOrder("user001", dto);

            assertNotNull(result);
            assertEquals("ORD123456", result.getOrderid());

            // 验证商品信息查询（createOrder + convertToVO 各调一次）
            verify(productFeignClient, atLeastOnce()).getProductById("P001");
            // 验证库存扣减
            verify(productFeignClient).updateStock("P001", 2);
            // 验证订单项插入
            verify(orderItemMapper).insert(any(OrderItem.class));
            // 验证订单插入
            verify(ordersMapper).insert(any(Orders.class));
        }

        @Test
        @DisplayName("创建失败 — 商品不存在时抛出异常")
        void shouldThrowWhenProductNotFound() {
            OrderCreateDTO dto = new OrderCreateDTO();
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId("INVALID");
            itemDTO.setQuantity(1);
            dto.setItems(List.of(itemDTO));

            when(snowflakeIdGenerator.nextOrderId("ORD")).thenReturn("ORD123456");
            when(productFeignClient.getProductById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.createOrder("user001", dto));
            assertTrue(ex.getMessage().contains("商品不存在"));
        }

        @Test
        @DisplayName("创建失败 — 库存不足时抛出异常")
        void shouldThrowWhenStockInsufficient() {
            OrderCreateDTO dto = new OrderCreateDTO();
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId("P001");
            itemDTO.setQuantity(999);
            dto.setItems(List.of(itemDTO));

            when(snowflakeIdGenerator.nextOrderId("ORD")).thenReturn("ORD123456");
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.createOrder("user001", dto));
            assertTrue(ex.getMessage().contains("库存不足"));
        }

        @Test
        @DisplayName("创建成功 — 商品列表为空时生成零元订单")
        void shouldCreateEmptyOrderWhenNoItems() {
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setItems(Collections.emptyList());

            when(snowflakeIdGenerator.nextOrderId("ORD")).thenReturn("ORD123456");
            when(ordersMapper.insert(any(Orders.class))).thenReturn(1);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            OrderVO result = orderService.createOrder("user001", dto);

            assertNotNull(result);
            verify(productFeignClient, never()).getProductById(anyString());
        }

        @Test
        @DisplayName("创建成功 — 使用优惠券时计算折扣后的总价")
        void shouldApplyDiscountWhenCouponUsed() {
            OrderCreateDTO dto = new OrderCreateDTO();
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId("P001");
            itemDTO.setQuantity(1);
            dto.setItems(List.of(itemDTO));
            dto.setCouponName("满50减10");
            dto.setDiscountAmount(new BigDecimal("10.00"));

            when(snowflakeIdGenerator.nextOrderId("ORD")).thenReturn("ORD123456");
            when(productFeignClient.getProductById("P001")).thenReturn(Result.success(sampleProduct));
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(ordersMapper.insert(any(Orders.class))).thenReturn(1);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            OrderVO result = orderService.createOrder("user001", dto);

            assertNotNull(result);

            ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
            verify(ordersMapper).insert(captor.capture());
            Orders savedOrder = captor.getValue();
            assertEquals(new BigDecimal("89.00"), savedOrder.getOriginalprice());
            assertEquals(new BigDecimal("79.00"), savedOrder.getTotalprice());
        }
    }

    // ==================== 订单支付测试 ====================

    @Nested
    @DisplayName("订单支付")
    class PayOrderTests {

        @Test
        @DisplayName("支付成功 — 待支付订单变为已支付")
        void shouldPayOrderSuccessfully() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);
            when(ordersMapper.updateById(any(Orders.class))).thenReturn(1);

            assertDoesNotThrow(() -> orderService.payOrder("user001", "ORD001"));

            assertEquals(OrderStatus.PAID.getStatus(), sampleOrder.getStatus());
            verify(ordersMapper).updateById(sampleOrder);
        }

        @Test
        @DisplayName("支付失败 — 订单不存在时抛出异常")
        void shouldThrowWhenOrderNotFound() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder("user001", "INVALID"));
            assertEquals("订单不存在", ex.getMessage());
        }

        @Test
        @DisplayName("支付失败 — 已支付订单不能重复支付")
        void shouldThrowWhenOrderAlreadyPaid() {
            sampleOrder.setStatus(OrderStatus.PAID.getStatus());
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder("user001", "ORD001"));
            assertEquals("订单状态异常", ex.getMessage());
            verify(ordersMapper, never()).updateById(any(Orders.class));
        }

        @Test
        @DisplayName("支付失败 — 已取消订单不能支付")
        void shouldThrowWhenOrderCancelled() {
            sampleOrder.setStatus(OrderStatus.CANCELLED.getStatus());
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder("user001", "ORD001"));
            assertEquals("订单状态异常", ex.getMessage());
        }
    }

    // ==================== 取消订单测试 ====================

    @Nested
    @DisplayName("取消订单")
    class CancelOrderTests {

        @Test
        @DisplayName("取消成功 — 待支付订单变为已取消，恢复库存")
        void shouldCancelOrderAndRestoreStock() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);
            when(ordersMapper.updateById(any(Orders.class))).thenReturn(1);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            assertDoesNotThrow(() -> orderService.cancelOrder("user001", "ORD001"));

            assertEquals(OrderStatus.CANCELLED.getStatus(), sampleOrder.getStatus());
            // 验证恢复库存（传入负数）
            verify(productFeignClient).updateStock("P001", -2);
        }

        @Test
        @DisplayName("取消失败 — 订单不存在时抛出异常")
        void shouldThrowWhenOrderNotFound() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.cancelOrder("user001", "INVALID"));
            assertEquals("订单不存在", ex.getMessage());
        }

        @Test
        @DisplayName("取消失败 — 只有待支付订单才能取消")
        void shouldThrowWhenOrderNotPendingPayment() {
            sampleOrder.setStatus(OrderStatus.SHIPPED.getStatus());
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.cancelOrder("user001", "ORD001"));
            assertEquals("只能取消待支付订单", ex.getMessage());
        }
    }

    // ==================== 确认收货测试 ====================

    @Nested
    @DisplayName("确认收货")
    class ConfirmReceiveTests {

        @Test
        @DisplayName("确认成功 — 已发货订单变为已完成")
        void shouldConfirmReceiveSuccessfully() {
            sampleOrder.setStatus(OrderStatus.SHIPPED.getStatus());
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);
            when(ordersMapper.updateById(any(Orders.class))).thenReturn(1);

            assertDoesNotThrow(() -> orderService.confirmReceive("user001", "ORD001"));

            assertEquals(OrderStatus.COMPLETED.getStatus(), sampleOrder.getStatus());
        }

        @Test
        @DisplayName("确认失败 — 待支付订单不能确认收货")
        void shouldThrowWhenOrderNotShipped() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.confirmReceive("user001", "ORD001"));
            assertEquals("订单状态异常", ex.getMessage());
        }
    }

    // ==================== 发货测试 ====================

    @Nested
    @DisplayName("管理员发货")
    class ShipOrderTests {

        @Test
        @DisplayName("发货成功 — 已支付订单变为已发货")
        void shouldShipOrderSuccessfully() {
            sampleOrder.setStatus(OrderStatus.PAID.getStatus());
            when(ordersMapper.selectById("ORD001")).thenReturn(sampleOrder);
            when(ordersMapper.updateById(any(Orders.class))).thenReturn(1);

            assertDoesNotThrow(() -> orderService.shipOrder("ORD001"));

            assertEquals(OrderStatus.SHIPPED.getStatus(), sampleOrder.getStatus());
        }

        @Test
        @DisplayName("发货失败 — 待支付订单不能发货")
        void shouldThrowWhenOrderNotPaid() {
            when(ordersMapper.selectById("ORD001")).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.shipOrder("ORD001"));
            assertEquals("订单状态异常", ex.getMessage());
        }

        @Test
        @DisplayName("发货失败 — 订单不存在")
        void shouldThrowWhenOrderNotFound() {
            when(ordersMapper.selectById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.shipOrder("INVALID"));
            assertEquals("订单不存在", ex.getMessage());
        }
    }

    // ==================== 订单查询测试 ====================

    @Nested
    @DisplayName("订单查询")
    class OrderQueryTests {

        @Test
        @DisplayName("查询订单详情 — 用户只能查自己的订单")
        void shouldGetOrderById() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            OrderVO result = orderService.getOrderById("user001", "ORD001");

            assertNotNull(result);
            assertEquals("ORD001", result.getOrderid());
            assertEquals("user001", result.getUserid());
        }

        @Test
        @DisplayName("查询订单详情 — 管理员可查任何订单（userId=null）")
        void shouldGetOrderByIdAsAdmin() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleOrder);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            OrderVO result = orderService.getOrderById(null, "ORD001");

            assertNotNull(result);
            assertEquals("ORD001", result.getOrderid());
        }

        @Test
        @DisplayName("查询订单详情 — 订单不存在时抛出异常")
        void shouldThrowWhenOrderNotFound() {
            when(ordersMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.getOrderById("user001", "INVALID"));
            assertEquals("订单不存在", ex.getMessage());
        }

        @Test
        @DisplayName("分页查询订单列表")
        void shouldGetOrderList() {
            Page<Orders> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleOrder));
            page.setTotal(1);

            when(ordersMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            PageResult<OrderVO> result = orderService.getOrderList("user001", 1, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("分页查询订单列表 — 按状态筛选")
        void shouldGetOrderListByStatus() {
            Page<Orders> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleOrder));
            page.setTotal(1);

            when(ordersMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleOrderItem));

            PageResult<OrderVO> result = orderService.getOrderList("user001", 1, 10, OrderStatus.PENDING_PAYMENT.getStatus());

            assertNotNull(result);
            verify(ordersMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }
}
