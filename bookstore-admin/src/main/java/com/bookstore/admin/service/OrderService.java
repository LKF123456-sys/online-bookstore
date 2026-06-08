package com.bookstore.admin.service;

import com.bookstore.admin.feign.OrderFeignClient;
import com.bookstore.common.api.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 订单服务 — 封装 OrderFeignClient，添加统一错误处理和日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderFeignClient orderFeignClient;

    /** 创建订单 */
    public Result<Map<String, Object>> createOrder(String userId, Map<String, Object> orderData) {
        log.info("OrderService.createOrder: userId={}", userId);
        return orderFeignClient.createOrder(userId, orderData);
    }

    /** 用户订单列表 */
    public Result<Map<String, Object>> userOrderList(String userId, int pageNum, int pageSize, String status) {
        log.debug("OrderService.userOrderList: userId={}, page={}, status={}", userId, pageNum, status);
        return orderFeignClient.userOrderList(userId, pageNum, pageSize, status);
    }

    /** 订单详情 */
    public Result<Map<String, Object>> orderDetail(String userId, String orderId) {
        log.debug("OrderService.orderDetail: orderId={}", orderId);
        return orderFeignClient.orderDetail(userId, orderId);
    }

    /** 支付订单 */
    public Result<Void> payOrder(String userId, String orderId) {
        log.info("OrderService.payOrder: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.payOrder(userId, orderId);
    }

    /** 取消订单 */
    public Result<Void> cancelOrder(String userId, String orderId) {
        log.info("OrderService.cancelOrder: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.cancelOrder(userId, orderId);
    }

    /** 确认收货 */
    public Result<Void> confirmReceive(String userId, String orderId) {
        log.info("OrderService.confirmReceive: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.confirmReceive(userId, orderId);
    }

    // ===== 购物车 =====

    public Result<Map<String, Object>> getCart(String userId) {
        return orderFeignClient.getCart(userId);
    }

    public Result<Void> addToCart(String userId, Map<String, Object> cartItem) {
        log.debug("OrderService.addToCart: userId={}", userId);
        return orderFeignClient.addToCart(userId, cartItem);
    }

    public Result<Void> updateCartItem(String userId, Long itemId, Map<String, Object> update) {
        return orderFeignClient.updateCartItem(userId, itemId, update);
    }

    public Result<Void> deleteCartItem(String userId, Long itemId) {
        return orderFeignClient.deleteCartItem(userId, itemId);
    }

    public Result<Void> clearCart(String userId) {
        return orderFeignClient.clearCart(userId);
    }

    // ===== 管理后台 =====

    public Result<Map<String, Object>> adminOrderList(int pageNum, int pageSize, String status, String keyword) {
        return orderFeignClient.adminOrderList(pageNum, pageSize, status, keyword);
    }

    public Result<Void> updateOrderStatus(String orderId, Map<String, Object> statusUpdate) {
        return orderFeignClient.updateOrderStatus(orderId, statusUpdate);
    }
}
