package com.bookstore.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.dto.ReviewSubmitDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ReviewVO;
import com.bookstore.common.entity.BookReview;
import com.bookstore.promotion.mapper.BookReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookReviewServiceTest {

    @Mock
    private BookReviewMapper bookReviewMapper;

    @InjectMocks
    private BookReviewService bookReviewService;

    private BookReview sampleReview;

    @BeforeEach
    void setUp() {
        sampleReview = new BookReview();
        sampleReview.setId(1L);
        sampleReview.setOrderId("ORD001");
        sampleReview.setProductId("P001");
        sampleReview.setUserId("user001");
        sampleReview.setRating(5);
        sampleReview.setContent("非常好的书，推荐购买！");
        sampleReview.setImage("http://img.example.com/review1.jpg");
        sampleReview.setLikes(10);
        sampleReview.setIsTop(0);
        sampleReview.setReply(null);
        sampleReview.setCreateTime(LocalDateTime.now());
        sampleReview.setBlocked(0);
    }

    // ==================== 获取商品评价列表测试 ====================

    @Nested
    @DisplayName("获取商品评价列表")
    class GetProductReviewsTests {

        @Test
        @DisplayName("查询成功 — 返回未屏蔽的评价分页列表")
        void shouldReturnNonBlockedReviews() {
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleReview));
            page.setTotal(1);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getProductReviews("P001", 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            verify(bookReviewMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 无评价时返回空列表")
        void shouldReturnEmptyWhenNoReviews() {
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getProductReviews("P001", 1, 10);

            assertNotNull(result);
            assertEquals(0, result.getRecords().size());
            assertEquals(0L, result.getTotal());
        }
    }

    // ==================== 提交评价测试 ====================

    @Nested
    @DisplayName("提交评价")
    class SubmitReviewTests {

        @Test
        @DisplayName("提交成功 — 默认屏蔽=0，点赞=0，置顶=0")
        void shouldSubmitReviewWithDefaults() {
            ReviewSubmitDTO dto = new ReviewSubmitDTO();
            dto.setProductId("P001");
            dto.setRating(5);
            dto.setContent("非常好的书");
            dto.setImage("http://img.example.com/img.jpg");

            when(bookReviewMapper.insert(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.submitReview("user001", dto));

            ArgumentCaptor<BookReview> captor = ArgumentCaptor.forClass(BookReview.class);
            verify(bookReviewMapper).insert(captor.capture());
            BookReview inserted = captor.getValue();
            assertEquals("user001", inserted.getUserId());
            assertEquals("P001", inserted.getProductId());
            assertEquals(5, inserted.getRating());
            assertEquals("非常好的书", inserted.getContent());
            assertEquals("http://img.example.com/img.jpg", inserted.getImage());
            assertEquals(0, inserted.getBlocked());
            assertEquals(0, inserted.getLikes());
            assertEquals(0, inserted.getIsTop());
        }

        @Test
        @DisplayName("提交成功 — 不带图片时image为null")
        void shouldSubmitReviewWithoutImage() {
            ReviewSubmitDTO dto = new ReviewSubmitDTO();
            dto.setProductId("P002");
            dto.setRating(3);
            dto.setContent("一般般");
            dto.setImage(null);

            when(bookReviewMapper.insert(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.submitReview("user002", dto));

            ArgumentCaptor<BookReview> captor = ArgumentCaptor.forClass(BookReview.class);
            verify(bookReviewMapper).insert(captor.capture());
            BookReview inserted = captor.getValue();
            assertNull(inserted.getImage());
            assertEquals("user002", inserted.getUserId());
        }
    }

    // ==================== 获取用户评价列表测试 ====================

    @Nested
    @DisplayName("获取用户评价列表")
    class GetUserReviewsTests {

        @Test
        @DisplayName("查询成功 — 返回用户的评价分页列表")
        void shouldReturnUserReviews() {
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleReview));
            page.setTotal(1);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getUserReviews("user001", 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
            verify(bookReviewMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 用户无评价时返回空列表")
        void shouldReturnEmptyWhenNoUserReviews() {
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getUserReviews("user001", 1, 10);

            assertNotNull(result);
            assertEquals(0, result.getRecords().size());
            assertEquals(0L, result.getTotal());
        }
    }

    // ==================== 管理员获取所有评价测试 ====================

    @Nested
    @DisplayName("管理员获取所有评价")
    class GetAllReviewsTests {

        @Test
        @DisplayName("查询成功 — 不传blocked参数时返回全部评价")
        void shouldReturnAllReviewsWithoutFilter() {
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleReview));
            page.setTotal(1);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getAllReviews(1, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(bookReviewMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 按屏蔽状态筛选已屏蔽的评价")
        void shouldReturnBlockedReviewsWhenFilterApplied() {
            sampleReview.setBlocked(1);
            Page<BookReview> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleReview));
            page.setTotal(1);

            when(bookReviewMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<ReviewVO> result = bookReviewService.getAllReviews(1, 10, 1);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(bookReviewMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    // ==================== 管理员屏蔽评价测试 ====================

    @Nested
    @DisplayName("管理员屏蔽评价")
    class BlockReviewTests {

        @Test
        @DisplayName("屏蔽成功 — 评价标记为已屏蔽")
        void shouldBlockReviewSuccessfully() {
            when(bookReviewMapper.selectById(1L)).thenReturn(sampleReview);
            when(bookReviewMapper.updateById(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.blockReview(1L));

            assertEquals(1, sampleReview.getBlocked());
            verify(bookReviewMapper).updateById(sampleReview);
        }

        @Test
        @DisplayName("屏蔽失败 — 评价不存在时抛出异常")
        void shouldThrowWhenReviewNotFound() {
            when(bookReviewMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> bookReviewService.blockReview(999L));
            assertTrue(ex.getMessage().contains("评价不存在"));
        }
    }

    // ==================== 管理员取消屏蔽评价测试 ====================

    @Nested
    @DisplayName("管理员取消屏蔽评价")
    class UnblockReviewTests {

        @Test
        @DisplayName("取消屏蔽成功 — 评价恢复为未屏蔽")
        void shouldUnblockReviewSuccessfully() {
            sampleReview.setBlocked(1);
            when(bookReviewMapper.selectById(1L)).thenReturn(sampleReview);
            when(bookReviewMapper.updateById(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.unblockReview(1L));

            assertEquals(0, sampleReview.getBlocked());
            verify(bookReviewMapper).updateById(sampleReview);
        }

        @Test
        @DisplayName("取消屏蔽失败 — 评价不存在时抛出异常")
        void shouldThrowWhenReviewNotFound() {
            when(bookReviewMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> bookReviewService.unblockReview(999L));
            assertTrue(ex.getMessage().contains("评价不存在"));
        }
    }

    // ==================== 管理员置顶评价测试 ====================

    @Nested
    @DisplayName("管理员置顶评价")
    class TopReviewTests {

        @Test
        @DisplayName("置顶成功 — 评价标记为已置顶")
        void shouldTopReviewSuccessfully() {
            when(bookReviewMapper.selectById(1L)).thenReturn(sampleReview);
            when(bookReviewMapper.updateById(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.topReview(1L));

            assertEquals(1, sampleReview.getIsTop());
            verify(bookReviewMapper).updateById(sampleReview);
        }

        @Test
        @DisplayName("置顶失败 — 评价不存在时抛出异常")
        void shouldThrowWhenReviewNotFound() {
            when(bookReviewMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> bookReviewService.topReview(999L));
            assertTrue(ex.getMessage().contains("评价不存在"));
        }
    }

    // ==================== 管理员取消置顶评价测试 ====================

    @Nested
    @DisplayName("管理员取消置顶评价")
    class UntopReviewTests {

        @Test
        @DisplayName("取消置顶成功 — 评价恢复为未置顶")
        void shouldUntopReviewSuccessfully() {
            sampleReview.setIsTop(1);
            when(bookReviewMapper.selectById(1L)).thenReturn(sampleReview);
            when(bookReviewMapper.updateById(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.untopReview(1L));

            assertEquals(0, sampleReview.getIsTop());
            verify(bookReviewMapper).updateById(sampleReview);
        }

        @Test
        @DisplayName("取消置顶失败 — 评价不存在时抛出异常")
        void shouldThrowWhenReviewNotFound() {
            when(bookReviewMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> bookReviewService.untopReview(999L));
            assertTrue(ex.getMessage().contains("评价不存在"));
        }
    }

    // ==================== 管理员回复评价测试 ====================

    @Nested
    @DisplayName("管理员回复评价")
    class ReplyReviewTests {

        @Test
        @DisplayName("回复成功 — 评价添加管理员回复内容")
        void shouldReplyReviewSuccessfully() {
            when(bookReviewMapper.selectById(1L)).thenReturn(sampleReview);
            when(bookReviewMapper.updateById(any(BookReview.class))).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.replyReview(1L, "感谢您的评价！"));

            assertEquals("感谢您的评价！", sampleReview.getReply());
            verify(bookReviewMapper).updateById(sampleReview);
        }

        @Test
        @DisplayName("回复失败 — 评价不存在时抛出异常")
        void shouldThrowWhenReviewNotFound() {
            when(bookReviewMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> bookReviewService.replyReview(999L, "感谢"));
            assertTrue(ex.getMessage().contains("评价不存在"));
        }
    }

    // ==================== 管理员删除评价测试 ====================

    @Nested
    @DisplayName("管理员删除评价")
    class DeleteReviewTests {

        @Test
        @DisplayName("删除成功 — 物理删除评价记录")
        void shouldDeleteReviewSuccessfully() {
            when(bookReviewMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> bookReviewService.deleteReview(1L));

            verify(bookReviewMapper).deleteById(1L);
        }
    }
}
