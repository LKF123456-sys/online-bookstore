package com.bookstore.admin.feign;

import com.bookstore.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 营销服务 Feign 客户端 — 声明式调用 bookstore-promotion 微服务
 */
@FeignClient(name = "bookstore-promotion")
public interface PromotionFeignClient {

    // ===== 评价接口 =====

    /** 提交评价 */
    @PostMapping("/api/review")
    Result<Void> submitReview(@RequestHeader(value = "X-User-Id", required = false) String userId,
                               @RequestBody Map<String, Object> reviewData);

    /** 用户评价列表 */
    @GetMapping("/api/review/my")
    Result<Map<String, Object>> userReviews(@RequestHeader("X-User-Id") String userId,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize);

    /** 商品评价列表 */
    @GetMapping("/api/review/product/{productId}")
    Result<Map<String, Object>> productReviews(@PathVariable String productId,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize);

    /** 删除评价 */
    @DeleteMapping("/api/review/{reviewId}")
    Result<Void> deleteReview(@RequestHeader("X-User-Id") String userId,
                               @PathVariable Long reviewId);

    // ===== 优惠券接口 =====

    /** 可用优惠券列表 */
    @GetMapping("/api/coupon/list")
    Result<Map<String, Object>> couponList(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "20") int pageSize);

    /** 用户已领优惠券 */
    @GetMapping("/api/coupon/my")
    Result<List<Map<String, Object>>> userCoupons(@RequestHeader("X-User-Id") String userId);

    /** 领取优惠券 */
    @PostMapping("/api/coupon/{couponId}/claim")
    Result<Void> claimCoupon(@RequestHeader("X-User-Id") String userId,
                              @PathVariable Long couponId);

    // ===== 公告接口 =====

    /** 公告列表 */
    @GetMapping("/api/announcement/list")
    Result<Map<String, Object>> announcementList(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize);

    // ===== 管理后台接口 =====

    /** 管理后台评价列表 */
    @GetMapping("/admin/review/list")
    Result<Map<String, Object>> adminReviewList(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String keyword);

    /** 管理后台删除评价 */
    @DeleteMapping("/admin/review/{reviewId}")
    Result<Void> adminDeleteReview(@PathVariable Long reviewId);

    /** 管理后台优惠券列表 */
    @GetMapping("/admin/coupon/list")
    Result<Map<String, Object>> adminCouponList(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize);

    /** 管理后台创建优惠券 */
    @PostMapping("/admin/coupon")
    Result<Void> createCoupon(@RequestBody Map<String, Object> couponData);

    /** 管理后台公告列表 */
    @GetMapping("/admin/announcement/list")
    Result<Map<String, Object>> adminAnnouncementList(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize);

    /** 管理后台创建公告 */
    @PostMapping("/admin/announcement")
    Result<Void> createAnnouncement(@RequestBody Map<String, Object> announcementData);
}
