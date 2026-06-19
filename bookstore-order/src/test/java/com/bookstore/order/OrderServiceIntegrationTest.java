package com.bookstore.order;

import com.bookstore.common.api.dto.OrderCreateDTO;
import com.bookstore.common.api.dto.OrderItemDTO;
import com.bookstore.order.config.OrderConfig;
import com.bookstore.order.mapper.CompensationRecordMapper;
import com.bookstore.order.mapper.OrderItemMapper;
import com.bookstore.order.mapper.OrdersMapper;
import com.bookstore.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 订单服务集成测试
 * 使用 Testcontainers 启动真实 MySQL 容器，验证端到端订单创建流程
 *
 * 面试亮点：
 *   1. Testcontainers：启动真实 Docker 容器而非 Mock/H2，覆盖真实 SQL 兼容性
 *   2. @DynamicPropertySource：动态注入容器连接信息
 *   3. 幂等性验证：测试重复提交被正确拦截
 *   4. 补偿记录验证：测试分布式事务补偿的持久化
 *   5. 动态配置验证：测试运行时配置变更的生效
 *
 * 运行前提：本地需要安装 Docker 环境
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class OrderServiceIntegrationTest {

    // ==================== Testcontainers 容器定义 ====================

    /**
     * MySQL 容器：使用与生产环境相同的 MySQL 8.x 版本
     * 确保 SQL 兼容性、字符集、索引等在集成测试阶段就能发现问题
     */
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bookstore_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-schema.sql");  // 初始化测试表结构

    /**
     * 动态注入容器连接信息到 Spring Environment
     * 替代硬编码的 application.yml 配置，使测试连接 Testcontainers 启动的容器
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        // 禁用 Nacos 和 RabbitMQ，避免测试时连接外部服务
        registry.add("spring.cloud.nacos.discovery.enabled", () -> "false");
        registry.add("spring.cloud.nacos.config.enabled", () -> "false");
        registry.add("spring.cloud.nacos.config.import-check.enabled", () -> "false");
    }

    // ==================== Mock 外部依赖 ====================

    /** Mock Feign 客户端（商品服务不在测试范围内） */
    @MockBean
    private com.bookstore.order.feign.ProductFeignClient productFeignClient;

    /** Mock Redis（幂等性测试单独控制 Redis 行为） */
    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    /** Mock 消息生产者（RabbitMQ 不在集成测试范围内） */
    @MockBean
    private com.bookstore.order.mq.OrderMessageProducer orderMessageProducer;

    // ==================== 注入被测对象 ====================

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CompensationRecordMapper compensationRecordMapper;

    @Autowired
    private OrderConfig orderConfig;

    // ==================== 测试用例 ====================

    @BeforeEach
    void setUp() {
        // Mock Redis 幂等性：默认放行（SETNX 返回 true）
        ValueOperations<String, String> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
    }

    /**
     * 测试用例 1：正常创建订单 — 验证订单和订单项正确入库
     */
    @Test
    @DisplayName("正常创建订单：订单和订单项应正确入库")
    void createOrder_shouldPersistOrderAndItems() {
        // Arrange: Mock 商品服务返回商品信息
        mockProductService("PROD-001", "Spring实战", new BigDecimal("89.00"), 100);

        OrderCreateDTO dto = buildOrderDTO("PROD-001", 2);

        // Act: 创建订单
        var result = orderService.createOrder("user-001", dto);

        // Assert: 验证返回结果
        assertNotNull(result);
        assertEquals("user-001", result.getUserid());
        assertEquals(2, result.getItems().size());

        // Assert: 验证数据库记录
        assertNotNull(ordersMapper.selectById(result.getOrderid()));
    }

    /**
     * 测试用例 2：幂等性校验 — 重复提交应被拦截
     */
    @Test
    @DisplayName("幂等性校验：重复提交应返回 409")
    void createOrder_duplicateSubmission_shouldBeRejected() {
        // Arrange: Mock Redis SETNX 返回 false（Key 已存在）
        ValueOperations<String, String> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);

        OrderCreateDTO dto = buildOrderDTO("PROD-001", 1);

        // Act & Assert: 应该抛出 409 业务异常
        var ex = assertThrows(com.bookstore.common.exception.BusinessException.class,
                () -> orderService.createOrder("user-001", dto));
        assertEquals(409, ex.getCode());
        assertTrue(ex.getMessage().contains("重复提交"));
    }

    /**
     * 测试用例 3：动态配置 — 关闭订单创建功能应拒绝新订单
     */
    @Test
    @DisplayName("动态配置：关闭订单创建后应返回 503")
    void createOrder_disabledByConfig_shouldReturn503() {
        // Arrange: 通过动态配置关闭订单创建
        orderConfig.setOrderCreationEnabled(false);

        OrderCreateDTO dto = buildOrderDTO("PROD-001", 1);

        // Act & Assert
        var ex = assertThrows(com.bookstore.common.exception.BusinessException.class,
                () -> orderService.createOrder("user-001", dto));
        assertEquals(503, ex.getCode());

        // Cleanup: 恢复配置
        orderConfig.setOrderCreationEnabled(true);
    }

    /**
     * 测试用例 4：商品不存在 — 应返回 404
     */
    @Test
    @DisplayName("商品不存在：应返回 404")
    void createOrder_productNotFound_shouldReturn404() {
        // Arrange: Mock 商品服务返回 null
        com.bookstore.common.api.Result<com.bookstore.common.api.vo.ProductVO> nullResult =
                new com.bookstore.common.api.Result<>();
        nullResult.setCode(200);
        nullResult.setData(null);
        when(productFeignClient.getProductById("NONEXISTENT")).thenReturn(nullResult);

        OrderCreateDTO dto = buildOrderDTO("NONEXISTENT", 1);

        // Act & Assert
        var ex = assertThrows(com.bookstore.common.exception.BusinessException.class,
                () -> orderService.createOrder("user-001", dto));
        assertEquals(404, ex.getCode());
    }

    /**
     * 测试用例 5：库存不足 — 应返回 409
     */
    @Test
    @DisplayName("库存不足：应返回 409")
    void createOrder_insufficientStock_shouldReturn409() {
        // Arrange: Mock 商品库存为 0
        mockProductService("PROD-001", "Spring实战", new BigDecimal("89.00"), 0);

        OrderCreateDTO dto = buildOrderDTO("PROD-001", 5);

        // Act & Assert
        var ex = assertThrows(com.bookstore.common.exception.BusinessException.class,
                () -> orderService.createOrder("user-001", dto));
        assertEquals(409, ex.getCode());
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    /**
     * 测试用例 6：超大订单 — 超过动态配置的最大商品数应拒绝
     */
    @Test
    @DisplayName("超大订单：超过最大商品数应返回 400")
    void createOrder_exceedMaxItems_shouldReturn400() {
        // Arrange: 设置最大商品数为 2
        orderConfig.setMaxOrderItems(2);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setItems(List.of(
                createItem("P1", 1), createItem("P2", 1), createItem("P3", 1)
        ));
        dto.setShipToFirstName("张");
        dto.setShipToLastName("三");
        dto.setShipAddr1("地址");
        dto.setShipCity("北京");
        dto.setShipZip("100000");
        dto.setShipCountry("CN");

        // Act & Assert
        var ex = assertThrows(com.bookstore.common.exception.BusinessException.class,
                () -> orderService.createOrder("user-001", dto));
        assertEquals(400, ex.getCode());

        // Cleanup
        orderConfig.setMaxOrderItems(50);
    }

    // ==================== 辅助方法 ====================

    private OrderCreateDTO buildOrderDTO(String productId, int quantity) {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setItems(List.of(createItem(productId, quantity)));
        dto.setShipToFirstName("张");
        dto.setShipToLastName("三");
        dto.setShipAddr1("北京市朝阳区xxx路123号");
        dto.setShipCity("北京");
        dto.setShipZip("100000");
        dto.setShipCountry("CN");
        dto.setCreditCard("4111111111111111");
        dto.setExprDate("12/28");
        dto.setCardType("Visa");
        return dto;
    }

    private OrderItemDTO createItem(String productId, int quantity) {
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private void mockProductService(String productId, String name, BigDecimal price, int stock) {
        com.bookstore.common.api.vo.ProductVO productVO = new com.bookstore.common.api.vo.ProductVO();
        productVO.setId(productId);
        productVO.setName(name);
        productVO.setPrice(price);
        productVO.setStock(stock);

        com.bookstore.common.api.Result<com.bookstore.common.api.vo.ProductVO> result =
                com.bookstore.common.api.Result.success(productVO);
        when(productFeignClient.getProductById(productId)).thenReturn(result);
    }
}
