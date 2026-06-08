package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.OrderService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单 REST API — 为 Vue 前端提供订单相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    /** 获取当前用户 ID（Session 或 Header） */
    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user != null) return String.valueOf(user.get("userid"));
        throw new BusinessException(401, "请先登录");
    }

    /** 创建订单 */
    @PostMapping
    public Result<?> createOrder(@RequestBody Map<String, Object> orderData,
                                  HttpSession session,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.createOrder(getUserId(session, userId), orderData);
    }

    /** 用户订单列表 */
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String status,
                          HttpSession session,
                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.userOrderList(getUserId(session, userId), pageNum, pageSize, status);
    }

    /** 订单详情 */
    @GetMapping("/{orderId}")
    public Result<?> detail(@PathVariable String orderId,
                             HttpSession session,
                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.orderDetail(getUserId(session, userId), orderId);
    }

    /** 支付订单 */
    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable String orderId,
                             HttpSession session,
                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Result<Void> result = orderService.payOrder(getUserId(session, userId), orderId);
        if (result.getCode() != 200) throw new BusinessException(result.getMessage());
        return result;
    }

    /** 取消订单 */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.cancelOrder(getUserId(session, userId), orderId);
    }

    /** 确认收货 */
    @PostMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable String orderId,
                                 HttpSession session,
                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.confirmReceive(getUserId(session, userId), orderId);
    }

    /** 获取购物车 */
    @GetMapping("/cart")
    public Result<?> cart(HttpSession session,
                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.getCart(getUserId(session, userId));
    }

    /** 添加到购物车 */
    @PostMapping("/cart")
    public Result<Void> addToCart(@RequestBody Map<String, Object> cartItem,
                                   HttpSession session,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.addToCart(getUserId(session, userId), cartItem);
    }

    /** 更新购物车项 */
    @PutMapping("/cart/{itemId}")
    public Result<Void> updateCartItem(@PathVariable Long itemId,
                                        @RequestBody Map<String, Object> update,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.updateCartItem(getUserId(session, userId), itemId, update);
    }

    /** 删除购物车项 */
    @DeleteMapping("/cart/{itemId}")
    public Result<Void> deleteCartItem(@PathVariable Long itemId,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return orderService.deleteCartItem(getUserId(session, userId), itemId);
    }
}
