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
 * 评价 REST API — 为 Vue 前端提供评价相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final PromotionService promotionService;

    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user != null) return String.valueOf(user.get("userid"));
        throw new BusinessException(401, "请先登录");
    }

    /** 提交评价 */
    @PostMapping
    public Result<Void> submit(@RequestBody Map<String, Object> reviewData,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return promotionService.submitReview(getUserId(session, userId), reviewData);
    }

    /** 用户评价列表 */
    @GetMapping
    public Result<Map<String, Object>> myReviews(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  HttpSession session,
                                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return promotionService.userReviews(getUserId(session, userId), pageNum, pageSize);
    }

    /** 商品评价列表 */
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> productReviews(@PathVariable String productId,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize) {
        return promotionService.productReviews(productId, pageNum, pageSize);
    }

    /** 删除评价 */
    @DeleteMapping("/{reviewId}")
    public Result<Void> delete(@PathVariable Long reviewId,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return promotionService.deleteReview(getUserId(session, userId), reviewId);
    }
}
