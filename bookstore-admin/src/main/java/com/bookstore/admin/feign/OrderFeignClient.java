package com.bookstore.admin.feign;

import com.bookstore.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单服务 Feign 客户端 — 声明式调用 bookstore-order 微服务
 */
@FeignClient(name = "bookstore-order", path = "/api")
public interface OrderFeignClient {

    /** 创建订单 */
    @PostMapping("/order")
    Result<Map<String, Object>> createOrder(@RequestHeader("X-User-Id") String userId,
                                             @RequestBody Map<String, Object> orderData);

    /** 订单列表（用户） */
    @GetMapping("/order/list")
    Result<Map<String, Object>> userOrderList(@RequestHeader("X-User-Id") String userId,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String status);

    /** 订单详情 */
    @GetMapping("/order/{orderId}")
    Result<Map<String, Object>> orderDetail(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                             @PathVariable String orderId);

    /** 支付订单 */
    @PostMapping("/order/{orderId}/pay")
    Result<Void> payOrder(@RequestHeader("X-User-Id") String userId,
                           @PathVariable String orderId);

    /** 取消订单 */
    @PostMapping("/order/{orderId}/cancel")
    Result<Void> cancelOrder(@RequestHeader("X-User-Id") String userId,
                              @PathVariable String orderId);

    /** 确认收货 */
    @PostMapping("/order/{orderId}/confirm")
    Result<Void> confirmReceive(@RequestHeader("X-User-Id") String userId,
                                 @PathVariable String orderId);

    // ===== 购物车 =====

    /** 获取购物车 */
    @GetMapping("/cart")
    Result<Map<String, Object>> getCart(@RequestHeader("X-User-Id") String userId);

    /** 添加到购物车 */
    @PostMapping("/cart")
    Result<Void> addToCart(@RequestHeader("X-User-Id") String userId,
                            @RequestBody Map<String, Object> cartItem);

    /** 更新购物车项 */
    @PutMapping("/cart/{itemId}")
    Result<Void> updateCartItem(@RequestHeader("X-User-Id") String userId,
                                 @PathVariable Long itemId,
                                 @RequestBody Map<String, Object> update);

    /** 删除购物车项 */
    @DeleteMapping("/cart/{itemId}")
    Result<Void> deleteCartItem(@RequestHeader("X-User-Id") String userId,
                                 @PathVariable Long itemId);

    /** 清空购物车 */
    @DeleteMapping("/cart")
    Result<Void> clearCart(@RequestHeader("X-User-Id") String userId);

    // ===== 管理后台接口 =====

    /** 管理后台订单列表 */
    @GetMapping("/admin/order/list")
    Result<Map<String, Object>> adminOrderList(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String keyword);

    /** 管理后台更新订单状态 */
    @PutMapping("/admin/order/{orderId}/status")
    Result<Void> updateOrderStatus(@PathVariable String orderId,
                                    @RequestBody Map<String, Object> statusUpdate);

    /** 管理后台订单详情 */
    @GetMapping("/admin/order/{orderId}")
    Result<Map<String, Object>> adminOrderDetail(@PathVariable String orderId);
}
