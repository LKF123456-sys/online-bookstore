package com.bookstore.admin.controller.api; // 声明当前接口所属的包路径：REST API控制器层

// 导入订单服务类，处理订单创建、查询、支付、取消等业务逻辑
import com.bookstore.admin.service.OrderService;
// 导入公共模块的统一响应封装类Result，所有接口返回值都通过此类包装
import com.bookstore.common.api.Result;
// 导入业务异常类，用于抛出业务逻辑相关的异常（如未登录、支付失败等）
import com.bookstore.common.exception.BusinessException;
// 导入Jakarta Servlet的HTTP会话接口，用于获取当前登录用户信息
import jakarta.servlet.http.HttpSession;
// 导入Lombok的@RequiredArgsConstructor注解，为所有final字段生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Lombok的@Slf4j注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 订单 REST API 控制器
 * 为Vue前端提供订单和购物车相关的RESTful接口，
 * 包括订单的创建、查询、支付、取消、确认收货，以及购物车的增删改查等操作。
 * 用户身份优先从请求头X-User-Id获取（JWT认证模式），其次从Session获取（兼容模式）。
 */
@Slf4j // Lombok注解，自动生成log日志对象
@RestController // 标记为REST控制器，返回值自动序列化为JSON响应体
@RequestMapping("/api/orders") // 设置该控制器所有接口的URL前缀为/api/orders
@RequiredArgsConstructor // Lombok自动生成包含final字段的构造函数，实现构造函数注入
public class OrderRestController {

    // 注入订单服务，处理订单和购物车相关的业务逻辑
    private final OrderService orderService;

    /**
     * 获取当前登录用户的ID（私有辅助方法）
     * 优先从请求头X-User-Id获取（由JWT过滤器/网关解析后注入），
     * 其次从HTTP Session的user对象中提取userid字段。
     * 两种方式均失败则抛出未登录异常。
     *
     * @param session      HTTP会话对象，用于兼容模式下读取user属性
     * @param headerUserId 请求头X-User-Id的值，可为null
     * @return 当前登录用户的ID字符串
     * @throws BusinessException(401) 当用户未登录或无法识别身份时
     */
    private String getUserId(HttpSession session, @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        // 优先使用请求头中的用户ID（JWT无状态认证模式）
        if (headerUserId != null && !headerUserId.isEmpty()) return headerUserId;
        // 回退到Session模式：从会话中获取登录时存入的user对象
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        // 若Session中有user对象，提取其中的userid字段作为用户标识
        if (user != null) return String.valueOf(user.get("userid"));
        // 无法识别用户身份，抛出401未登录异常
        throw new BusinessException(401, "请先登录");
    }

    // ======================== 订单管理接口 ========================

    /**
     * 创建订单
     * 将购物车中的商品生成正式订单
     *
     * @param orderData 订单数据Map，包含收货地址、商品列表、优惠券等信息
     * @param session   HTTP会话对象
     * @param userId    请求头X-User-Id（可选）
     * @return Result 包装的订单信息Map，包含orderid（订单ID）
     */
    // @PostMapping：将HTTP POST请求映射到/api/orders路径，POST语义表示创建新资源
    @PostMapping
    public Result<?> createOrder(
            // @RequestBody：订单数据通过请求体以JSON格式传入
            @RequestBody Map<String, Object> orderData,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务创建订单，getUserId解析用户身份
        return orderService.createOrder(getUserId(session, userId), orderData);
    }

    /**
     * 获取用户的订单列表（分页）
     * 返回当前登录用户的所有历史订单
     *
     * @param pageNum  页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param status   订单状态筛选（可选），如"待支付"、"已发货"等
     * @param session  HTTP会话对象
     * @param userId   请求头X-User-Id（可选）
     * @return Result 包装的分页订单数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/api/orders路径
    @GetMapping
    public Result<?> list(
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // @RequestParam(required = false)：订单状态为可选筛选条件
            @RequestParam(required = false) String status,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务查询用户订单列表
        return orderService.userOrderList(getUserId(session, userId), pageNum, pageSize, status);
    }

    /**
     * 获取订单详情
     * 返回指定订单的完整信息，包含商品明细
     *
     * @param orderId 订单ID，作为URL路径变量
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的订单详情Map
     */
    // @GetMapping：将HTTP GET请求映射到/api/orders/{orderId}路径
    @GetMapping("/{orderId}")
    public Result<?> detail(
            // @PathVariable：将URL路径中的{orderId}占位符的值绑定到方法参数
            @PathVariable String orderId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务获取订单详情
        return orderService.orderDetail(getUserId(session, userId), orderId);
    }

    /**
     * 支付订单
     * 将订单状态从"待支付"更新为"已支付"
     *
     * @param orderId 订单ID，作为URL路径变量
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     * @throws BusinessException 当支付失败时抛出异常
     */
    // @PostMapping：将HTTP POST请求映射到/api/orders/{orderId}/pay路径
    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务执行支付操作
        Result<Void> result = orderService.payOrder(getUserId(session, userId), orderId);
        // 如果支付返回码不是200（成功），抛出业务异常
        if (result.getCode() != 200) throw new BusinessException(result.getMessage());
        // 支付成功，返回结果
        return result;
    }

    /**
     * 取消订单
     * 将待支付订单状态设置为"已取消"
     *
     * @param orderId 订单ID，作为URL路径变量
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/api/orders/{orderId}/cancel路径
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务取消订单
        return orderService.cancelOrder(getUserId(session, userId), orderId);
    }

    /**
     * 确认收货
     * 用户收到商品后确认收货，将订单状态从"已发货"更新为"已完成"
     *
     * @param orderId 订单ID，作为URL路径变量
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/api/orders/{orderId}/confirm路径
    @PostMapping("/{orderId}/confirm")
    public Result<Void> confirm(
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务确认收货
        return orderService.confirmReceive(getUserId(session, userId), orderId);
    }

    // ======================== 购物车管理接口 ========================

    /**
     * 获取当前用户的购物车内容
     *
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的购物车数据Map，包含items（商品列表）、totalPrice（总价）等
     */
    // @GetMapping：将HTTP GET请求映射到/api/orders/cart路径
    @GetMapping("/cart")
    public Result<?> cart(
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务获取购物车内容
        return orderService.getCart(getUserId(session, userId));
    }

    /**
     * 添加商品到购物车
     *
     * @param cartItem 购物车项数据Map，包含productId（商品ID）和quantity（数量）
     * @param session  HTTP会话对象
     * @param userId   请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/api/orders/cart路径
    @PostMapping("/cart")
    public Result<Void> addToCart(
            // @RequestBody：购物车项数据通过请求体以JSON格式传入
            @RequestBody Map<String, Object> cartItem,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务添加商品到购物车
        return orderService.addToCart(getUserId(session, userId), cartItem);
    }

    /**
     * 根据购物车项ID更新商品数量
     *
     * @param itemId  购物车项ID，作为URL路径变量
     * @param update  更新数据Map，包含quantity（新数量）
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/api/orders/cart/{itemId}路径，PUT语义表示幂等更新
    @PutMapping("/cart/{itemId}")
    public Result<Void> updateCartItemByItemId(
            // @PathVariable：购物车项ID路径变量
            @PathVariable Long itemId,
            // @RequestBody：更新数据通过请求体传入
            @RequestBody Map<String, Object> update,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务更新购物车项数量
        return orderService.updateCartItem(getUserId(session, userId), itemId, update);
    }

    /**
     * 根据商品ID更新购物车中的商品数量
     * 与updateCartItemByItemId不同，此接口通过商品ID而非购物车项ID定位
     *
     * @param update  更新数据Map，包含productId（商品ID）和quantity（新数量）
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/api/orders/cart路径
    @PutMapping("/cart")
    public Result<Void> updateCartItemByProductId(
            // @RequestBody：更新数据通过请求体传入
            @RequestBody Map<String, Object> update,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务根据商品ID更新购物车
        return orderService.updateCartItemByProductId(getUserId(session, userId), update);
    }

    /**
     * 删除购物车中的商品
     * 支持两种方式定位：购物车项ID（Long类型）或商品ID（String类型）
     * 如果itemId能解析为Long，则按购物车项ID删除；否则按商品ID删除
     *
     * @param itemId  购物车项ID或商品ID，作为URL路径变量
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/api/orders/cart/{itemId}路径
    @DeleteMapping("/cart/{itemId}")
    public Result<Void> deleteCartItem(
            // @PathVariable：路径变量，可能是购物车项ID或商品ID
            @PathVariable String itemId,
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            // 尝试将itemId解析为Long类型（购物车项ID）
            Long id = Long.parseLong(itemId);
            // 解析成功，按购物车项ID删除
            return orderService.deleteCartItem(getUserId(session, userId), id);
        } catch (NumberFormatException e) {
            // 解析失败，说明itemId是商品ID（非数字），按商品ID删除
            return orderService.deleteCartItemByProductId(getUserId(session, userId), itemId);
        }
    }

    /**
     * 清空购物车
     * 删除当前用户购物车中的所有商品
     *
     * @param session HTTP会话对象
     * @param userId  请求头X-User-Id（可选）
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/api/orders/cart/clear路径
    @DeleteMapping("/cart/clear")
    public Result<Void> clearCart(
            // 注入HTTP会话对象
            HttpSession session,
            // @RequestHeader：从请求头获取用户ID（可选）
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 调用订单服务清空购物车
        return orderService.clearCart(getUserId(session, userId));
    }
}
