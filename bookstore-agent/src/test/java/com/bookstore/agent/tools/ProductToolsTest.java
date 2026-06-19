package com.bookstore.agent.tools;

import com.bookstore.agent.feign.ProductFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.ProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 商品工具集单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductTools 商品工具集测试")
class ProductToolsTest {

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private ProductTools productTools;

    private ProductVO sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new ProductVO();
        sampleProduct.setId("PROD-001");
        sampleProduct.setName("深入理解Java虚拟机");
        sampleProduct.setAuthor("周志明");
        sampleProduct.setPrice(new BigDecimal("129.00"));
        sampleProduct.setStock(50);
        sampleProduct.setSales(200);
        sampleProduct.setCategory("计算机");
        sampleProduct.setDescription("全面解析JVM原理与调优实践");
    }

    @Nested
    @DisplayName("搜索商品")
    class SearchProductsTests {

        @Test
        @DisplayName("关键词搜索 — 返回匹配结果")
        void shouldReturnSearchResults() {
            when(productFeignClient.searchProducts("Java", 1, 5))
                    .thenReturn(Result.success(List.of(sampleProduct)));

            String result = productTools.searchProducts("Java");

            assertTrue(result.contains("深入理解Java虚拟机"));
            assertTrue(result.contains("周志明"));
            assertTrue(result.contains("129.00"));
        }

        @Test
        @DisplayName("无匹配结果 — 返回空结果提示")
        void shouldHandleEmptySearchResults() {
            when(productFeignClient.searchProducts("不存在", 1, 5))
                    .thenReturn(Result.success(List.of()));

            String result = productTools.searchProducts("不存在");

            assertTrue(result.contains("没有找到"));
        }

        @Test
        @DisplayName("搜索异常 — 优雅降级")
        void shouldHandleSearchException() {
            when(productFeignClient.searchProducts("test", 1, 5))
                    .thenThrow(new RuntimeException("ES unavailable"));

            String result = productTools.searchProducts("test");

            assertTrue(result.contains("错误"));
        }
    }

    @Nested
    @DisplayName("获取商品详情")
    class GetProductDetailTests {

        @Test
        @DisplayName("正常查询 — 返回完整详情文本")
        void shouldReturnProductDetail() {
            when(productFeignClient.getProductById("PROD-001"))
                    .thenReturn(Result.success(sampleProduct));

            String result = productTools.getProductDetail("PROD-001");

            assertTrue(result.contains("深入理解Java虚拟机"));
            assertTrue(result.contains("周志明"));
            assertTrue(result.contains("50 本"));
            assertTrue(result.contains("JVM原理"));
        }
    }

    @Nested
    @DisplayName("获取推荐商品")
    class GetRecommendProductsTests {

        @Test
        @DisplayName("正常获取 — 返回推荐列表")
        void shouldReturnRecommendations() {
            when(productFeignClient.getRecommendProducts(5))
                    .thenReturn(Result.success(List.of(sampleProduct)));

            String result = productTools.getRecommendProducts();

            assertTrue(result.contains("推荐"));
            assertTrue(result.contains("深入理解Java虚拟机"));
        }

        @Test
        @DisplayName("空推荐 — 返回提示")
        void shouldHandleEmptyRecommendations() {
            when(productFeignClient.getRecommendProducts(5))
                    .thenReturn(Result.success(List.of()));

            String result = productTools.getRecommendProducts();

            assertTrue(result.contains("没有推荐商品"));
        }
    }

    @Nested
    @DisplayName("获取热销商品")
    class GetHotProductsTests {

        @Test
        @DisplayName("正常获取 — 返回热销排行")
        void shouldReturnHotProducts() {
            when(productFeignClient.getHotProducts(5))
                    .thenReturn(Result.success(List.of(sampleProduct)));

            String result = productTools.getHotProducts();

            assertTrue(result.contains("热销"));
            assertTrue(result.contains("已售 200 本"));
        }
    }
}
