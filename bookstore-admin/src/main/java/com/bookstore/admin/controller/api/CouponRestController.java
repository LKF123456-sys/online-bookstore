package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.PromotionService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公告 & 优惠券 REST API — 为 Vue 前端提供公告和优惠券接口
 */
@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponRestController {

    private final PromotionService promotionService;

    /** 可用优惠券列表 */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        return promotionService.couponList(pageNum, pageSize);
    }

    /** 用户已领优惠券 */
    @GetMapping("/my")
    public Result<?> myCoupons(HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return promotionService.userCoupons(resolveUserId(session, userId));
    }

    /** 领取优惠券 */
    @PostMapping("/{couponId}/claim")
    public Result<Void> claim(@PathVariable Long couponId,
                               HttpSession session,
                               @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return promotionService.claimCoupon(resolveUserId(session, userId), couponId);
    }

    /** 公告列表 */
    @GetMapping("/announcements")
    public Result<Map<String, Object>> announcements(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return promotionService.announcementList(pageNum, pageSize);
    }

    private String resolveUserId(HttpSession session, String headerUserId) {
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user != null) return String.valueOf(user.get("userid"));
        throw new BusinessException(401, "请先登录");
    }
}
