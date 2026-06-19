package com.bookstore.agent.tools;

import com.bookstore.agent.feign.ReviewFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ReviewVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 评价工具集单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewTools 评价工具集测试")
class ReviewToolsTest {

    @Mock
    private ReviewFeignClient reviewFeignClient;

    @InjectMocks
    private ReviewTools reviewTools;

    private ReviewVO sampleReview;

    @BeforeEach
    void setUp() {
        sampleReview = new ReviewVO();
        sampleReview.setId(1L);
        sampleReview.setProductId("PROD-001");
        sampleReview.setRating(5);
        sampleReview.setContent("非常好的书，内容深入浅出，强烈推荐！");
        sampleReview.setLikes(12);
        sampleReview.setReply("感谢您的好评！");
    }

    @Nested
    @DisplayName("获取商品评价")
    class GetProductReviewsTests {

        @Test
        @DisplayName("正常获取 — 返回评价列表含平均评分")
        void shouldReturnReviewsWithAverageRating() {
            PageResult<ReviewVO> page = new PageResult<>();
            page.setRecords(List.of(sampleReview));
            page.setTotal(1L);

            when(reviewFeignClient.getProductReviews("PROD-001", 1, 10))
                    .thenReturn(Result.success(page));

            String result = reviewTools.getProductReviews("PROD-001", 1);

            assertTrue(result.contains("1 条评价"));
            assertTrue(result.contains("5.0 星"));
            assertTrue(result.contains("非常好的书"));
            assertTrue(result.contains("商家回复"));
        }

        @Test
        @DisplayName("无评价 — 返回空提示")
        void shouldHandleNoReviews() {
            PageResult<ReviewVO> page = new PageResult<>();
            page.setRecords(List.of());
            page.setTotal(0L);

            when(reviewFeignClient.getProductReviews("PROD-001", 1, 10))
                    .thenReturn(Result.success(page));

            String result = reviewTools.getProductReviews("PROD-001", 1);

            assertTrue(result.contains("暂无用户评价"));
        }

        @Test
        @DisplayName("多条评价 — 正确计算平均分")
        void shouldCalculateAverageRating() {
            ReviewVO review2 = new ReviewVO();
            review2.setId(2L);
            review2.setRating(3);
            review2.setContent("一般般");
            review2.setLikes(2);

            PageResult<ReviewVO> page = new PageResult<>();
            page.setRecords(List.of(sampleReview, review2));
            page.setTotal(2L);

            when(reviewFeignClient.getProductReviews("PROD-001", 1, 10))
                    .thenReturn(Result.success(page));

            String result = reviewTools.getProductReviews("PROD-001", 1);

            // (5+3)/2 = 4.0
            assertTrue(result.contains("4.0 星"));
            assertTrue(result.contains("2 条评价"));
        }

        @Test
        @DisplayName("Feign 异常 — 优雅降级")
        void shouldHandleFeignException() {
            when(reviewFeignClient.getProductReviews("PROD-001", 1, 10))
                    .thenThrow(new RuntimeException("Service unavailable"));

            String result = reviewTools.getProductReviews("PROD-001", 1);

            assertTrue(result.contains("错误"));
            assertTrue(result.contains("Service unavailable"));
        }
    }
}
