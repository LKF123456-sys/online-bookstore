package com.bookstore.promotion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.dto.CouponCreateDTO;
import com.bookstore.common.api.vo.CouponVO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.entity.Coupon;
import com.bookstore.common.entity.UserCoupon;
import com.bookstore.promotion.mapper.CouponMapper;
import com.bookstore.promotion.mapper.UserCouponMapper;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponMapper couponMapper;
    @Mock
    private UserCouponMapper userCouponMapper;

    @InjectMocks
    private CouponService couponService;

    private Coupon sampleCoupon;
    private UserCoupon sampleUserCoupon;

    @BeforeEach
    void setUp() {
        sampleCoupon = new Coupon();
        sampleCoupon.setId(1L);
        sampleCoupon.setName("满100减10");
        sampleCoupon.setType("满减");
        sampleCoupon.setThreshold(new BigDecimal("100.00"));
        sampleCoupon.setDiscount(new BigDecimal("10.00"));
        sampleCoupon.setTotalCount(100);
        sampleCoupon.setUsedCount(50);
        sampleCoupon.setStartTime(LocalDateTime.now().minusDays(1));
        sampleCoupon.setEndTime(LocalDateTime.now().plusDays(30));
        sampleCoupon.setStatus(1);
        sampleCoupon.setCreateTime(LocalDateTime.now());

        sampleUserCoupon = new UserCoupon();
        sampleUserCoupon.setId(1L);
        sampleUserCoupon.setUserId("user001");
        sampleUserCoupon.setCouponId(1L);
        sampleUserCoupon.setIsUsed(0);
        sampleUserCoupon.setGrantTime(LocalDateTime.now());
    }

    // ==================== 获取可用优惠券列表测试 ====================

    @Nested
    @DisplayName("获取可用优惠券列表")
    class GetCouponListTests {

        @Test
        @DisplayName("查询成功 — 返回分页的可用优惠券列表")
        void shouldReturnActiveCouponList() {
            Page<Coupon> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleCoupon));
            page.setTotal(1);

            when(couponMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<CouponVO> result = couponService.getCouponList(1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            verify(couponMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 无可用优惠券时返回空列表")
        void shouldReturnEmptyWhenNoActiveCoupons() {
            Page<Coupon> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(couponMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<CouponVO> result = couponService.getCouponList(1, 10);

            assertNotNull(result);
            assertEquals(0, result.getRecords().size());
            assertEquals(0L, result.getTotal());
        }
    }

    // ==================== 领取优惠券测试 ====================

    @Nested
    @DisplayName("领取优惠券")
    class ClaimCouponTests {

        @Test
        @DisplayName("领取成功 — 正常领取优惠券")
        void shouldClaimCouponSuccessfully() {
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);
            when(userCouponMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.claimCoupon("user001", 1L));

            // 验证插入用户优惠券记录
            ArgumentCaptor<UserCoupon> ucCaptor = ArgumentCaptor.forClass(UserCoupon.class);
            verify(userCouponMapper).insert(ucCaptor.capture());
            UserCoupon inserted = ucCaptor.getValue();
            assertEquals("user001", inserted.getUserId());
            assertEquals(1L, inserted.getCouponId());
            assertEquals(0, inserted.getIsUsed());

            // 验证已领取数量 +1
            ArgumentCaptor<Coupon> cCaptor = ArgumentCaptor.forClass(Coupon.class);
            verify(couponMapper).updateById(cCaptor.capture());
            assertEquals(51, cCaptor.getValue().getUsedCount());
        }

        @Test
        @DisplayName("领取失败 — 优惠券不存在时抛出异常")
        void shouldThrowWhenCouponNotFound() {
            when(couponMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 999L));
            assertTrue(ex.getMessage().contains("优惠券不存在"));
        }

        @Test
        @DisplayName("领取失败 — 优惠券已禁用时抛出异常")
        void shouldThrowWhenCouponDisabled() {
            sampleCoupon.setStatus(0);
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("优惠券已禁用"));
        }

        @Test
        @DisplayName("领取失败 — 优惠券未到领取时间时抛出异常")
        void shouldThrowWhenCouponNotStarted() {
            sampleCoupon.setStartTime(LocalDateTime.now().plusDays(1));
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("优惠券未到领取时间"));
        }

        @Test
        @DisplayName("领取失败 — 优惠券已过期时抛出异常")
        void shouldThrowWhenCouponExpired() {
            sampleCoupon.setEndTime(LocalDateTime.now().minusDays(1));
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("优惠券已过期"));
        }

        @Test
        @DisplayName("领取失败 — 优惠券已领完时抛出异常")
        void shouldThrowWhenCouponExhausted() {
            sampleCoupon.setUsedCount(100);
            sampleCoupon.setTotalCount(100);
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("优惠券已领完"));
        }

        @Test
        @DisplayName("领取失败 — 用户已领取过该优惠券时抛出异常")
        void shouldThrowWhenAlreadyClaimed() {
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);
            when(userCouponMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleUserCoupon);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.claimCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("已领取过该优惠券"));
        }
    }

    // ==================== 获取用户优惠券测试 ====================

    @Nested
    @DisplayName("获取用户优惠券")
    class GetUserCouponsTests {

        @Test
        @DisplayName("查询成功 — 返回用户未使用的优惠券列表")
        void shouldReturnUserUnusedCoupons() {
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleUserCoupon));
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);

            List<CouponVO> result = couponService.getUserCoupons("user001");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getUserStatus());
            verify(userCouponMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询成功 — 用户无未使用优惠券时返回空列表")
        void shouldReturnEmptyWhenNoUnusedCoupons() {
            when(userCouponMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<CouponVO> result = couponService.getUserCoupons("user001");

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    // ==================== 使用优惠券测试 ====================

    @Nested
    @DisplayName("使用优惠券")
    class UseCouponTests {

        @Test
        @DisplayName("使用成功 — 优惠券标记为已使用并记录使用时间")
        void shouldUseCouponSuccessfully() {
            when(userCouponMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleUserCoupon);
            when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.useCoupon("user001", 1L));

            assertEquals(1, sampleUserCoupon.getIsUsed());
            assertNotNull(sampleUserCoupon.getUseTime());
            verify(userCouponMapper).updateById(sampleUserCoupon);
        }

        @Test
        @DisplayName("使用失败 — 优惠券不存在或已使用时抛出异常")
        void shouldThrowWhenCouponNotFoundOrAlreadyUsed() {
            when(userCouponMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.useCoupon("user001", 1L));
            assertTrue(ex.getMessage().contains("优惠券不存在或已使用"));
        }
    }

    // ==================== 管理员获取所有优惠券测试 ====================

    @Nested
    @DisplayName("管理员获取所有优惠券")
    class GetAllCouponsTests {

        @Test
        @DisplayName("查询成功 — 返回所有状态的分页优惠券列表")
        void shouldReturnAllCoupons() {
            Page<Coupon> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleCoupon));
            page.setTotal(1);

            when(couponMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<CouponVO> result = couponService.getAllCoupons(1, 10);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getTotal());
            verify(couponMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    // ==================== 管理员创建优惠券测试 ====================

    @Nested
    @DisplayName("管理员创建优惠券")
    class CreateCouponTests {

        @Test
        @DisplayName("创建成功 — 默认状态为启用，已领取数量为0")
        void shouldCreateCouponWithDefaults() {
            CouponCreateDTO dto = new CouponCreateDTO();
            dto.setName("满200减20");
            dto.setType("满减");
            dto.setDiscount(new BigDecimal("20.00"));
            dto.setThreshold(new BigDecimal("200.00"));
            dto.setTotalCount(200);
            dto.setStartTime(LocalDateTime.now());
            dto.setEndTime(LocalDateTime.now().plusDays(30));

            when(couponMapper.insert(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.createCoupon(dto));

            ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
            verify(couponMapper).insert(captor.capture());
            Coupon inserted = captor.getValue();
            assertEquals(0, inserted.getUsedCount());
            assertEquals(1, inserted.getStatus());
            assertEquals("满200减20", inserted.getName());
            assertEquals("满减", inserted.getType());
        }
    }

    // ==================== 管理员更新优惠券测试 ====================

    @Nested
    @DisplayName("管理员更新优惠券")
    class UpdateCouponTests {

        @Test
        @DisplayName("更新成功 — 正常更新优惠券信息")
        void shouldUpdateCouponSuccessfully() {
            CouponCreateDTO dto = new CouponCreateDTO();
            dto.setName("满300减30");
            dto.setType("满减");
            dto.setDiscount(new BigDecimal("30.00"));
            dto.setThreshold(new BigDecimal("300.00"));
            dto.setTotalCount(500);
            dto.setStartTime(LocalDateTime.now());
            dto.setEndTime(LocalDateTime.now().plusDays(60));

            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.updateCoupon(1L, dto));

            verify(couponMapper).selectById(1L);
            verify(couponMapper).updateById(any(Coupon.class));
        }

        @Test
        @DisplayName("更新失败 — 优惠券不存在时抛出异常")
        void shouldThrowWhenCouponNotFound() {
            CouponCreateDTO dto = new CouponCreateDTO();
            when(couponMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.updateCoupon(999L, dto));
            assertTrue(ex.getMessage().contains("优惠券不存在"));
        }
    }

    // ==================== 管理员删除优惠券测试 ====================

    @Nested
    @DisplayName("管理员删除优惠券")
    class DeleteCouponTests {

        @Test
        @DisplayName("删除成功 — 物理删除优惠券记录")
        void shouldDeleteCouponSuccessfully() {
            when(couponMapper.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> couponService.deleteCoupon(1L));

            verify(couponMapper).deleteById(1L);
        }
    }

    // ==================== 管理员更新优惠券状态测试 ====================

    @Nested
    @DisplayName("管理员更新优惠券状态")
    class UpdateCouponStatusTests {

        @Test
        @DisplayName("启用成功 — 优惠券状态设为启用")
        void shouldEnableCouponSuccessfully() {
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.updateCouponStatus(1L, 1));

            assertEquals(1, sampleCoupon.getStatus());
            verify(couponMapper).updateById(sampleCoupon);
        }

        @Test
        @DisplayName("禁用成功 — 优惠券状态设为禁用")
        void shouldDisableCouponSuccessfully() {
            when(couponMapper.selectById(1L)).thenReturn(sampleCoupon);
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            assertDoesNotThrow(() -> couponService.updateCouponStatus(1L, 0));

            assertEquals(0, sampleCoupon.getStatus());
            verify(couponMapper).updateById(sampleCoupon);
        }

        @Test
        @DisplayName("更新失败 — 优惠券不存在时抛出异常")
        void shouldThrowWhenCouponNotFound() {
            when(couponMapper.selectById(999L)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> couponService.updateCouponStatus(999L, 1));
            assertTrue(ex.getMessage().contains("优惠券不存在"));
        }
    }
}
