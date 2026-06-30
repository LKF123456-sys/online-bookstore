package com.bookstore.agent.feign; // feign 包：存放 Spring Cloud OpenFeign 声明式 HTTP 客户端接口

// 导入统一响应 Result<T>
import com.bookstore.common.api.Result;
// 导入订单 VO
import com.bookstore.common.api.vo.OrderVO;
// 导入分页结果
import com.bookstore.common.api.vo.PageResult;
// Spring Cloud OpenFeign 的 @FeignClient 注解 — 声明一个远程 HTTP 服务客户端
import org.springframework.cloud.openfeign.FeignClient;
// Spring MVC 注解：@GetMapping、@PostMapping、@PathVariable、@RequestParam、@RequestHeader
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务 Feign 客户端 — AI Agent 调用订单微服务（bookstore-order）的工具接口
 *
 * Feign 工作原理：
 *   1. @FeignClient 声明目标服务名（bookstore-order）
 *   2. Spring Cloud 通过 Nacos 服务发现获取目标服务的实际 IP:Port
 *   3. 运行时通过 JDK 动态代理生成接口实现类
 *   4. 方法调用时自动构建 HTTP 请求（GET/POST/路径/参数/请求头）
 *
 * 安全设计：
 *   所有方法通过 @RequestHeader("X-User-Id") 传递用户 ID，
 *   确保下游服务能验证操作权限（用户只能操作自己的订单）。
 *   userId 由 Agent 层从请求上下文提取后传入，不可由客户端伪造。
 *
 * 容错设计：
 *   fallbackFactory = OrderFeignFallbackFactory.class
 *   当下游服务不可用或超时时，FallbackFactory 创建降级实现返回默认值，
 *   避免级联故障（雪崩效应）。
 */
@FeignClient(name = "bookstore-order", fallbackFactory = OrderFeignFallbackFactory.class) // Feign 客户端注解：服务名 + 降级工厂
public interface OrderFeignClient { // 订单服务 Feign 客户端接口

    /**
     * 查询订单详情
     * 调用 bookstore-order 服务的 GET /api/order/{id} 端点
     *
     * @param userId 用户 ID（请求头 X-User-Id）
     * @param orderId 订单 ID（路径参数）
     * @return Result<OrderVO> 包含订单完整信息
     */
    @GetMapping("/api/order/{id}") // GET 映射：{id} 为路径变量
    Result<OrderVO> getOrderById( // 查询订单详情方法
            @RequestHeader("X-User-Id") String userId, // 请求头：用于权限验证
            @PathVariable("id") String orderId); // 路径参数：订单 ID

    /**
     * 查询用户订单列表（分页）
     * 调用 bookstore-order 服务的 GET /api/order/list 端点
     *
     * @param userId 用户 ID（请求头）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页条数，默认 5
     * @param status 状态筛选（可选）：PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED
     * @return Result<PageResult<OrderVO>> 分页订单列表
     */
    @GetMapping("/api/order/list") // GET 映射
    Result<PageResult<OrderVO>> listOrders( // 查询订单列表方法
            @RequestHeader("X-User-Id") String userId, // 用户身份
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, // 页码，默认1
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize, // 每页条数，默认5
            @RequestParam(value = "status", required = false) String status); // 状态筛选，可选

    /**
     * 取消订单
     * 调用 bookstore-order 服务的 POST /api/order/{id}/cancel 端点
     * 仅 PENDING_PAYMENT（待支付）状态的订单可取消，取消后自动恢复库存
     *
     * @param userId 用户 ID（请求头）
     * @param orderId 订单 ID（路径参数）
     * @return Result<Void> 取消结果
     */
    @PostMapping("/api/order/{id}/cancel") // POST 映射
    Result<Void> cancelOrder( // 取消订单方法
            @RequestHeader("X-User-Id") String userId, // 用户身份
            @PathVariable("id") String orderId); // 订单 ID

    /**
     * 支付订单
     * 调用 bookstore-order 服务的 POST /api/order/{id}/pay 端点
     *
     * @param userId 用户 ID（请求头）
     * @param orderId 订单 ID（路径参数）
     * @return Result<Void> 支付结果
     */
    @PostMapping("/api/order/{id}/pay") // POST 映射
    Result<Void> payOrder( // 支付订单方法
            @RequestHeader("X-User-Id") String userId, // 用户身份
            @PathVariable("id") String orderId); // 订单 ID
}
