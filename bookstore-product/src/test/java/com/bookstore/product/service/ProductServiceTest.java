package com.bookstore.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.dto.ProductQueryDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ProductVO;
import com.bookstore.common.entity.Product;
import com.bookstore.common.entity.ProductSku;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.entity.ProductSpec;
import com.bookstore.product.mapper.ProductMapper;
import com.bookstore.product.mapper.ProductSkuMapper;
import com.bookstore.product.mapper.ProductSpecMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private ProductSpecMapper productSpecMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        // 使用 lenient 避免对不依赖 Redis/MeterRegistry 的测试触发 UnnecessaryStubbingException
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(mock(Counter.class));

        sampleProduct = new Product();
        sampleProduct.setProductid("P001");
        sampleProduct.setName("Java编程思想");
        sampleProduct.setDescn("经典Java教程");
        sampleProduct.setAuthor("Bruce Eckel");
        sampleProduct.setCategory("科技计算机");
        sampleProduct.setPrice(new BigDecimal("89.00"));
        sampleProduct.setImage("http://img.example.com/java.jpg");
        sampleProduct.setStock(100);
        sampleProduct.setSales(50);
        sampleProduct.setIsRecommend(1);
        sampleProduct.setStatus(1);
    }

    // ==================== 商品列表查询测试 ====================

    @Nested
    @DisplayName("商品列表查询")
    class GetProductListTests {

        @Test
        @DisplayName("查询成功 — 返回分页商品列表")
        void shouldReturnProductList() {
            ProductQueryDTO query = new ProductQueryDTO();
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Product> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleProduct));
            page.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ProductVO> result = productService.getProductList(query);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals("P001", result.getRecords().get(0).getId());
            assertEquals("Java编程思想", result.getRecords().get(0).getName());
        }

        @Test
        @DisplayName("查询成功 — 带关键词搜索")
        void shouldSearchByKeyword() {
            ProductQueryDTO query = new ProductQueryDTO();
            query.setKeyword("Java");
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Product> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleProduct));
            page.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ProductVO> result = productService.getProductList(query);

            assertNotNull(result);
            verify(productMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 按分类筛选")
        void shouldFilterByCategory() {
            ProductQueryDTO query = new ProductQueryDTO();
            query.setCategory("科技计算机");
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Product> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleProduct));
            page.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ProductVO> result = productService.getProductList(query);

            assertNotNull(result);
            assertEquals("科技计算机", result.getRecords().get(0).getCategory());
        }

        @Test
        @DisplayName("查询成功 — 按价格区间筛选")
        void shouldFilterByPriceRange() {
            ProductQueryDTO query = new ProductQueryDTO();
            query.setMinPrice(50.0);
            query.setMaxPrice(100.0);
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Product> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleProduct));
            page.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ProductVO> result = productService.getProductList(query);

            assertNotNull(result);
        }
    }

    // ==================== 商品详情查询测试 ====================

    @Nested
    @DisplayName("商品详情查询")
    class GetProductByIdTests {

        @Test
        @DisplayName("查询成功 — 缓存未命中时从数据库查询")
        void shouldReturnProductFromDatabase() {
            when(valueOperations.get("product:P001")).thenReturn(null);
            // mock 缓存重建的分布式锁
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);
            when(productSkuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            when(productSpecMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            ProductVO result = productService.getProductById("P001");

            assertNotNull(result);
            assertEquals("P001", result.getId());
            assertEquals("Java编程思想", result.getName());
            assertEquals(new BigDecimal("89.00"), result.getPrice());
            // 验证缓存写入（TTL 为 5 分钟 + 随机抖动，所以用 anyLong）
            verify(valueOperations).set(eq("product:P001"), any(ProductVO.class), anyLong(), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("查询成功 — 缓存命中时直接返回缓存数据")
        void shouldReturnProductFromCache() {
            ProductVO cachedVO = new ProductVO();
            cachedVO.setId("P001");
            cachedVO.setName("Java编程思想(缓存)");
            cachedVO.setPrice(new BigDecimal("89.00"));

            when(valueOperations.get("product:P001")).thenReturn(cachedVO);

            ProductVO result = productService.getProductById("P001");

            assertNotNull(result);
            assertEquals("Java编程思想(缓存)", result.getName());
            verify(productMapper, never()).selectById(anyString());
        }

        @Test
        @DisplayName("查询失败 — 商品不存在时抛出异常（含缓存穿透防护）")
        void shouldThrowWhenProductNotFound() {
            when(valueOperations.get("product:INVALID")).thenReturn(null);
            // mock 缓存重建的分布式锁
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> productService.getProductById("INVALID"));
            assertEquals("商品不存在", ex.getMessage());
        }

        @Test
        @DisplayName("缓存异常时 — 回退到数据库查询")
        void shouldFallbackToDatabaseWhenCacheFails() {
            // 第一次 get 抛异常（模拟 Redis 故障），第二次返回 null（锁内双重检查通过）
            when(valueOperations.get("product:P001"))
                    .thenThrow(new RuntimeException("Redis连接失败"))
                    .thenReturn(null);
            // mock 缓存重建的分布式锁
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);
            when(productSkuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
            when(productSpecMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            ProductVO result = productService.getProductById("P001");

            assertNotNull(result);
            assertEquals("P001", result.getId());
        }
    }

    // ==================== 推荐和热门商品测试 ====================

    @Nested
    @DisplayName("推荐和热门商品")
    class RecommendAndHotTests {

        @Test
        @DisplayName("获取推荐商品")
        void shouldGetRecommendProducts() {
            when(productMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleProduct));

            List<ProductVO> result = productService.getRecommendProducts(5);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Java编程思想", result.get(0).getName());
        }

        @Test
        @DisplayName("获取热门商品")
        void shouldGetHotProducts() {
            when(productMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleProduct));

            List<ProductVO> result = productService.getHotProducts(10);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("无推荐商品时返回空列表")
        void shouldReturnEmptyWhenNoRecommendations() {
            when(productMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<ProductVO> result = productService.getRecommendProducts(5);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== 库存扣减测试 ====================

    @Nested
    @DisplayName("库存扣减")
    class UpdateStockTests {

        @Test
        @DisplayName("扣减成功 — 库存和销量正确更新")
        void shouldDeductStockSuccessfully() {
            // mock 分布式锁获取成功
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.updateStock("P001", 10));

            assertEquals(90, sampleProduct.getStock());  // 100 - 10
            assertEquals(60, sampleProduct.getSales());   // 50 + 10
            verify(productMapper).updateById(sampleProduct);
            verify(redisTemplate).delete("product:P001");
        }

        @Test
        @DisplayName("扣减失败 — 库存不足时抛出异常")
        void shouldThrowWhenStockInsufficient() {
            // mock 分布式锁获取成功
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> productService.updateStock("P001", 200));
            assertEquals("库存不足", ex.getMessage());
            verify(productMapper, never()).updateById(any(Product.class));
        }

        @Test
        @DisplayName("扣减失败 — 商品不存在时抛出异常")
        void shouldThrowWhenProductNotFound() {
            // mock 分布式锁获取成功
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> productService.updateStock("INVALID", 1));
            assertEquals("商品不存在", ex.getMessage());
        }

        @Test
        @DisplayName("库存恢复 — 传入负数时增加库存")
        void shouldRestoreStockWithNegativeQuantity() {
            // mock 分布式锁获取成功
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.updateStock("P001", -5));

            assertEquals(105, sampleProduct.getStock());  // 100 - (-5) = 105
        }
    }

    // ==================== 商品增删改测试 ====================

    @Nested
    @DisplayName("商品增删改")
    class ProductCRUDTests {

        @Test
        @DisplayName("新增商品 — 默认上架状态")
        void shouldAddProduct() {
            ProductVO vo = new ProductVO();
            vo.setName("新书");
            vo.setPrice(new BigDecimal("59.00"));

            when(productMapper.insert(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.addProduct(vo));

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productMapper).insert(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("修改商品")
        void shouldUpdateProduct() {
            ProductVO vo = new ProductVO();
            vo.setId("P001");
            vo.setName("更新后的书名");

            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.updateProduct(vo));

            verify(productMapper).updateById(any(Product.class));
            verify(redisTemplate).delete("product:P001");
        }

        @Test
        @DisplayName("删除商品")
        void shouldDeleteProduct() {
            when(productMapper.deleteById("P001")).thenReturn(1);

            assertDoesNotThrow(() -> productService.deleteProduct("P001"));

            verify(productMapper).deleteById("P001");
            verify(redisTemplate).delete("product:P001");
        }

        @Test
        @DisplayName("更新商品状态 — 上架/下架")
        void shouldUpdateProductStatus() {
            when(productMapper.selectById("P001")).thenReturn(sampleProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            assertDoesNotThrow(() -> productService.updateProductStatus("P001", 0));

            assertEquals(0, sampleProduct.getStatus());
            verify(productMapper).updateById(sampleProduct);
        }

        @Test
        @DisplayName("更新商品状态 — 商品不存在时抛出异常")
        void shouldThrowWhenProductNotFound() {
            when(productMapper.selectById("INVALID")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> productService.updateProductStatus("INVALID", 0));
            assertEquals("商品不存在", ex.getMessage());
        }
    }
}
