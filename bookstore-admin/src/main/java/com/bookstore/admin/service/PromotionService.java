package com.bookstore.admin.service;

import com.bookstore.admin.feign.PromotionFeignClient;
import com.bookstore.common.api.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 营销服务 — 封装 PromotionFeignClient，添加统一错误处理和日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionFeignClient promotionFeignClient;

    /** 提交评价 */
    public Result<Void> submitReview(String userId, Map<String, Object> reviewData) {
        log.info("PromotionService.submitReview: userId={}", userId);
        return promotionFeignClient.submitReview(userId, reviewData);
    }

    /** 用户评价列表 */
    public Result<Map<String, Object>> userReviews(String userId, int pageNum, int pageSize) {
        return promotionFeignClient.userReviews(userId, pageNum, pageSize);
    }

    /** 商品评价列表 */
    public Result<Map<String, Object>> productReviews(String productId, int pageNum, int pageSize) {
        return promotionFeignClient.productReviews(productId, pageNum, pageSize);
    }

    /** 删除评价 */
    public Result<Void> deleteReview(String userId, Long reviewId) {
        log.info("PromotionService.deleteReview: reviewId={}, userId={}", reviewId, userId);
        return promotionFeignClient.deleteReview(userId, reviewId);
    }

    /** 可用优惠券列表 */
    public Result<Map<String, Object>> couponList(int pageNum, int pageSize) {
        return promotionFeignClient.couponList(pageNum, pageSize);
    }

    /** 用户已领优惠券 */
    public Result<List<Map<String, Object>>> userCoupons(String userId) {
        return promotionFeignClient.userCoupons(userId);
    }

    /** 领取优惠券 */
    public Result<Void> claimCoupon(String userId, Long couponId) {
        return promotionFeignClient.claimCoupon(userId, couponId);
    }

    /** 公告列表 */
    public Result<Map<String, Object>> announcementList(int pageNum, int pageSize) {
        return promotionFeignClient.announcementList(pageNum, pageSize);
    }

    // ===== 管理后台 =====

    public Result<Map<String, Object>> adminReviewList(int pageNum, int pageSize, String keyword) {
        return promotionFeignClient.adminReviewList(pageNum, pageSize, keyword);
    }

    public Result<Void> adminDeleteReview(Long reviewId) {
        return promotionFeignClient.adminDeleteReview(reviewId);
    }

    public Result<Map<String, Object>> adminCouponList(int pageNum, int pageSize) {
        return promotionFeignClient.adminCouponList(pageNum, pageSize);
    }

    public Result<Void> createCoupon(Map<String, Object> couponData) {
        return promotionFeignClient.createCoupon(couponData);
    }

    public Result<Map<String, Object>> adminAnnouncementList(int pageNum, int pageSize) {
        return promotionFeignClient.adminAnnouncementList(pageNum, pageSize);
    }

    public Result<Void> createAnnouncement(Map<String, Object> announcementData) {
        return promotionFeignClient.createAnnouncement(announcementData);
    }
}
