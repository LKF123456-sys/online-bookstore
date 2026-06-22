package com.bookstore.agent.feign;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.vo.OrderVO;
import com.bookstore.common.api.vo.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务 Feign 客户端 — AI Agent 调用订单微服务的工具接口
 *
 * 设计说明：
 *   Agent 通过此 Feign 客户端查询/操作用户订单，
 *   所有方法都传递 X-User-Id 以确保操作权限隔离。
 */
@FeignClient(name = "bookstore-order", fallbackFactory = OrderFeignFallbackFactory.class)
public interface OrderFeignClient {

    /**
     * 查询订单详情
     */
    @GetMapping("/api/order/{id}")
    Result<OrderVO> getOrderById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") String orderId);

    /**
     * 查询用户订单列表（分页）
     */
    @GetMapping("/api/order/list")
    Result<PageResult<OrderVO>> listOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(value = "status", required = false) String status);

    /**
     * 取消订单
     */
    @PostMapping("/api/order/{id}/cancel")
    Result<Void> cancelOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") String orderId);

    /**
     * 支付订单
     */
    @PostMapping("/api/order/{id}/pay")
    Result<Void> payOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") String orderId);
}
