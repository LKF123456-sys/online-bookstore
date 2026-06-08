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
 * 订单 REST API 控制器
 * <p>
 * 职责：为 Vue 前端提供订单及购物车相关的 RESTful 接口，包括创建订单、
 *       订单列表查询、订单详情、支付、取消、确认收货，以及购物车的增删改查。
 * <p>
 * 所属模块：bookstore-admin · controller · api
 * <p>
 * 用户身份获取：通过私有方法 getUserId() 统一处理，
 *       优先从请求头 X-User-Id（JWT 解析结果）获取，其次从 Session 获取。
 * <p>
 * 包含接口：
 * <ul>
 *   <li>订单：
 *     <ul>
 *       <li>POST   /api/orders                    — 创建订单</li>
 *       <li>GET    /api/orders                    — 用户订单列表</li>
 *       <li>GET    /api/orders/{orderId}          — 订单详情</li>
 *       <li>POST   /api/orders/{orderId}/pay      — 支付订单</li>
 *       <li>POST   /api/orders/{orderId}/cancel   — 取消订单</li>
 *       <li>POST   /api/orders/{orderId}/confirm  — 确认收货</li>
 *     </ul>
 *   </li>
 *   <li>购物车：
 *     <ul>
 *       <li>GET    /api/orders/cart              — 获取购物车</li>
 *       <li>POST   /api/orders/cart              — 添加到购物车</li>
 *       <li>PUT    /api/orders/cart/{itemId}     — 更新购物车项</li>
 *       <li>DELETE /api/orders/cart/{itemId}     — 删除购物车项</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @author bookstore
 */
// @Slf4j：Lombok 注解，自动生成 log 日志对象，用于记录运行时日志
@Slf4j
// @RestController：Spring MVC 注解，标识该类为 REST 控制器，
// 所有方法返回值自动序列化为 JSON 响应体
@RestController
// @RequestMapping：将控制器映射到 /api/orders 路径下，所有接口 URL 以此为前缀
@RequestMapping("/api/orders")
// @RequiredArgsConstructor：Lombok 注解，为所有 final 字段生成构造方法，
// Spring 自动注入对应的 Bean
@RequiredArgsConstructor
public class OrderRestController {

    // 订单服务层依赖，处理订单创建、支付、取消及购物车操作等核心业务逻辑
    private final OrderService orderService;

    // ========================================================================
    // 用户身份辅助方法
    // ========================================================================

    /**
     * 获取当前请求用户的 ID
     * <p>
     * 优先从请求头 X-User-Id 获取（由 JWT 过滤器 / 网关解析后注入），
     * 其次从 HTTP Session 的用户对象中提取 userid 字段。
     * 两种方式均失败则抛出未登录异常。
     *
     * @param session      HTTP 会话对象，用于兼容模式下读取 user 属性
     * @param headerUserId 请求头 X-User-Id 的值，可为 null
     * @return 当前登录用户的 ID 字符串
     * @throws BusinessException(401) 当用户未登录或无法识别身份时
     */
    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        // 优先使用请求头中的用户 ID（JWT 无状态认证模式）
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        // 回退到 Session 模式：从会话中获取登录时存入的 user 对象
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        // 若 Session 中有 user 对象，提取其中的 userid 字段
        if (user != null) return String.valueOf(user.get("userid"));
        // 无法识别用户身份，抛出 401 异常，需前端跳转登录页
        throw new BusinessException(401, "请先登录");
    }

    // ========================================================================
    // 订单相关接口
    // ========================================================================

    /**
     * 创建订单
     * <p>
     * 接收前端传来的订单数据（商品列表、收货地址、优惠券等），
     * 在服务端生成订单记录并返回订单信息。
     *
     * @param orderData 请求体，包含订单所需的完整数据 Map
     * @param session   HTTP 会话对象，用于获取当前用户 ID
     * @param userId    请求头 X-User-Id（可选）
     * @return Result 包含新创建订单信息的成功响应
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/orders
    @PostMapping
    // @RequestBody：将 HTTP 请求体 JSON 反序列化为 Map 对象
    public Result<?> createOrder(@RequestBody Map<String, Object> orderData,
                                  HttpSession session,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 先解析当前用户 ID，再委托 OrderService 创建订单
        return orderService.createOrder(getUserId(session, userId), orderData);
    }

    /**
     * 获取当前用户的订单列表
     * <p>
     * 支持按订单状态筛选（如待支付、已发货等）。
     *
     * @param pageNum  页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @param status   订单状态筛选（可选），如 "pending" / "shipped" / "completed"
     * @param session  HTTP 会话对象
     * @param userId   请求头 X-User-Id（可选）
     * @return Result 包含分页订单列表的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/orders
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String status,
                          HttpSession session,
                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后查询该用户的订单分页列表，可按状态筛选
        return orderService.userOrderList(getUserId(session, userId), pageNum, pageSize, status);
    }

    /**
     * 获取指定订单的详细信息
     *
     * @param orderId 订单号（路径变量），唯一标识一个订单
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 包含订单详情（订单基本信息 + 商品明细 + 物流信息）的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/orders/{orderId}
    @GetMapping("/{orderId}")
    // @PathVariable：将 URL 路径中的 {orderId} 占位符绑定到方法参数
    public Result<?> detail(@PathVariable String orderId,
                             HttpSession session,
                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后查询指定订单的完整详情
        return orderService.orderDetail(getUserId(session, userId), orderId);
    }

    /**
     * 支付订单
     * <p>
     * 对指定订单执行支付操作（假设为模拟支付或调用第三方支付）。
     * 支付成功后检查返回码，非 200 则抛出业务异常。
     *
     * @param orderId 订单号（路径变量）
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 空成功响应
     * @throws BusinessException 当支付业务返回失败时
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/orders/{orderId}/pay
    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable String orderId,
                             HttpSession session,
                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用 OrderService 执行支付逻辑
        Result<Void> result = orderService.payOrder(getUserId(session, userId), orderId);
        // 检查支付结果的业务状态码，非 200 表示支付失败（如余额不足、订单状态不允许等）
        if (result.getCode() != 200) throw new BusinessException(result.getMessage());
        // 支付成功，返回结果
        return result;
    }

    /**
     * 取消订单
     * <p>
     * 将指定订单状态置为"已取消"，仅允许在特定状态下取消（如待支付）。
     *
     * @param orderId 订单号（路径变量）
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/orders/{orderId}/cancel
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId,
                                HttpSession session,
                                @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后取消指定订单
        return orderService.cancelOrder(getUserId(session, userId), orderId);
    }

    /**
     * 确认收货
     * <p>
     * 买家收到商品后确认收货，将订单状态置为"已完成"。
     *
     * @param orderId 订单号（路径变量）
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/orders/{orderId}/confirm
    @PostMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable String orderId,
                                 HttpSession session,
                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后确认收货
        return orderService.confirmReceive(getUserId(session, userId), orderId);
    }

    // ========================================================================
    // 购物车相关接口
    // ========================================================================

    /**
     * 获取当前用户的购物车内容
     *
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 包含购物车列表（商品项 + 数量 + 小计）的成功响应
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/orders/cart
    @GetMapping("/cart")
    public Result<?> cart(HttpSession session,
                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后获取其购物车数据
        return orderService.getCart(getUserId(session, userId));
    }

    /**
     * 将商品添加到购物车
     *
     * @param cartItem 请求体，包含商品 ID (productId) 和数量 (quantity) 等信息
     * @param session  HTTP 会话对象
     * @param userId   请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/orders/cart
    @PostMapping("/cart")
    public Result<Void> addToCart(@RequestBody Map<String, Object> cartItem,
                                   HttpSession session,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后将商品项加入购物车
        return orderService.addToCart(getUserId(session, userId), cartItem);
    }

    /**
     * 更新购物车中某个商品项的数量或其他属性
     *
     * @param itemId  购物车项的主键 ID（路径变量）
     * @param update  请求体，包含要更新的字段（如 quantity 新数量）
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /api/orders/cart/{itemId}
    @PutMapping("/cart/{itemId}")
    public Result<Void> updateCartItem(@PathVariable Long itemId,
                                        @RequestBody Map<String, Object> update,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后更新指定购物车项
        return orderService.updateCartItem(getUserId(session, userId), itemId, update);
    }

    /**
     * 从购物车中删除指定商品项
     *
     * @param itemId  购物车项的主键 ID（路径变量）
     * @param session HTTP 会话对象
     * @param userId  请求头 X-User-Id（可选）
     * @return Result 空成功响应
     */
    // @DeleteMapping：将 HTTP DELETE 请求映射到 /api/orders/cart/{itemId}
    @DeleteMapping("/cart/{itemId}")
    public Result<Void> deleteCartItem(@PathVariable Long itemId,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 解析用户 ID 后删除指定购物车项
        return orderService.deleteCartItem(getUserId(session, userId), itemId);
    }
}
