package com.bookstore.admin.feign; // 声明当前接口所属的包路径，遵循Spring Boot项目标准分包结构

// 导入公共模块的统一响应封装类Result，用于包装所有接口返回结果
import com.bookstore.common.api.Result;
// 导入Spring Cloud OpenFeign的@FeignClient注解，用于声明式HTTP客户端
import org.springframework.cloud.openfeign.FeignClient;
// 导入Spring MVC的请求映射注解，用于描述HTTP请求的URL、方法与参数绑定方式
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 订单服务 Feign 客户端 — 声明式调用 bookstore-order 微服务
 *
 * 该接口通过 Spring Cloud OpenFeign 以声明方式定义对订单微服务的 HTTP 调用。
 * Feign 框架会在运行时动态生成代理实现类，自动完成服务发现、负载均衡与 HTTP 请求发送。
 * 涵盖订单管理（创建、查询、支付、取消、确认收货）和购物车管理（增删改查）两大模块。
 *
 * @FeignClient 注解说明：
 *   - name = "bookstore-order"：指定要调用的微服务名称，对应Nacos注册中心中的服务名
 *   - path = "/api"：指定所有方法URL的统一路径前缀
 */
@FeignClient(name = "bookstore-order", path = "/api") // 声明Feign客户端，目标服务为bookstore-order，统一路径前缀为/api
public interface OrderFeignClient {

    // ======================== 订单管理接口 ========================

    /**
     * 创建订单
     * 将购物车中的商品生成正式订单
     *
     * @param userId    当前登录用户的ID，通过请求头X-User-Id传递
     * @param orderData 订单数据Map，包含收货地址、商品列表、优惠券等信息
     * @return Result 包装的订单信息Map，包含orderid（订单ID）等
     */
    // @PostMapping：将HTTP POST请求映射到/order路径，POST语义表示创建新资源
    @PostMapping("/order")
    Result<Map<String, Object>> createOrder(
            // @RequestHeader：将HTTP请求头X-User-Id的值绑定到方法参数userId
            @RequestHeader("X-User-Id") String userId,
            // @RequestBody：订单数据通过请求体以JSON格式传入
            @RequestBody Map<String, Object> orderData);

    /**
     * 获取用户的订单列表（分页）
     * 返回当前登录用户的所有历史订单
     *
     * @param userId   当前登录用户的ID，通过请求头X-User-Id传递
     * @param pageNum  页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param status   订单状态筛选（可选），如"待支付"、"已发货"等
     * @return Result 包装的分页订单数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/order/list路径
    @GetMapping("/order/list")
    Result<Map<String, Object>> userOrderList(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // @RequestParam(required = false)：订单状态为可选筛选条件
            @RequestParam(required = false) String status);

    /**
     * 获取订单详情
     * 返回指定订单的完整信息，包含商品明细
     *
     * @param userId  当前登录用户的ID，通过请求头传递（可选，管理后台可能不需要）
     * @param orderId 订单ID，作为URL路径变量
     * @return Result 包装的订单详情Map
     */
    // @GetMapping：将HTTP GET请求映射到/order/{orderId}路径
    @GetMapping("/order/{orderId}")
    Result<Map<String, Object>> orderDetail(
            // @RequestHeader：用户ID为可选请求头，管理后台查询时可能不传
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            // @PathVariable：将URL路径中的{orderId}占位符的值绑定到方法参数orderId
            @PathVariable String orderId);

    /**
     * 支付订单
     * 将订单状态从"待支付"更新为"已支付"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/order/{orderId}/pay路径
    @PostMapping("/order/{orderId}/pay")
    Result<Void> payOrder(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId);

    /**
     * 取消订单
     * 将待支付订单状态设置为"已取消"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/order/{orderId}/cancel路径
    @PostMapping("/order/{orderId}/cancel")
    Result<Void> cancelOrder(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId);

    /**
     * 确认收货
     * 用户收到商品后确认收货，将订单状态从"已发货"更新为"已完成"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/order/{orderId}/confirm路径
    @PostMapping("/order/{orderId}/confirm")
    Result<Void> confirmReceive(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId);

    // ======================== 购物车管理接口 ========================

    /**
     * 获取当前用户的购物车内容
     *
     * @param userId 当前登录用户的ID
     * @return Result 包装的购物车数据Map，包含items（商品列表）、totalPrice（总价）等
     */
    // @GetMapping：将HTTP GET请求映射到/cart路径
    @GetMapping("/cart")
    Result<Map<String, Object>> getCart(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId);

    /**
     * 添加商品到购物车
     *
     * @param userId   当前登录用户的ID
     * @param cartItem 购物车项数据Map，包含productId（商品ID）和quantity（数量）
     * @return Result 包装的空返回体
     */
    // @PostMapping：将HTTP POST请求映射到/cart路径
    @PostMapping("/cart")
    Result<Void> addToCart(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @RequestBody：购物车项数据通过请求体传入
            @RequestBody Map<String, Object> cartItem);

    /**
     * 根据购物车项ID更新商品数量
     *
     * @param userId 当前登录用户的ID
     * @param itemId 购物车项ID，作为URL路径变量
     * @param update 更新数据Map，包含quantity（新数量）
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/cart/{itemId}路径，PUT语义表示幂等更新
    @PutMapping("/cart/{itemId}")
    Result<Void> updateCartItem(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：购物车项ID路径变量
            @PathVariable Long itemId,
            // @RequestBody：更新数据通过请求体传入
            @RequestBody Map<String, Object> update);

    /**
     * 根据商品ID更新购物车中的商品数量
     * 与updateCartItem不同，此接口通过商品ID而非购物车项ID定位
     *
     * @param userId 当前登录用户的ID
     * @param update 更新数据Map，包含productId（商品ID）和quantity（新数量）
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/cart路径
    @PutMapping("/cart")
    Result<Void> updateCartItemByProductId(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @RequestBody：更新数据通过请求体传入
            @RequestBody Map<String, Object> update);

    /**
     * 根据购物车项ID删除购物车中的商品
     *
     * @param userId 当前登录用户的ID
     * @param itemId 购物车项ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/cart/{itemId}路径，DELETE语义表示删除资源
    @DeleteMapping("/cart/{itemId}")
    Result<Void> deleteCartItem(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：购物车项ID路径变量
            @PathVariable Long itemId);

    /**
     * 根据商品ID删除购物车中的商品
     * 与deleteCartItem不同，此接口通过商品ID定位要删除的购物车项
     *
     * @param userId    当前登录用户的ID
     * @param productId 商品ID，作为URL路径变量
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/cart/item/{productId}路径
    @DeleteMapping("/cart/item/{productId}")
    Result<Void> deleteCartItemByProductId(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId,
            // @PathVariable：商品ID路径变量
            @PathVariable String productId);

    /**
     * 清空购物车
     * 删除当前用户购物车中的所有商品
     *
     * @param userId 当前登录用户的ID
     * @return Result 包装的空返回体
     */
    // @DeleteMapping：将HTTP DELETE请求映射到/cart/clear路径
    @DeleteMapping("/cart/clear")
    Result<Void> clearCart(
            // @RequestHeader：通过请求头传递用户ID
            @RequestHeader("X-User-Id") String userId);

    // ===== 以下为管理后台专用接口 =====

    /**
     * 管理后台 — 获取订单分页列表
     * 支持按状态筛选和关键词搜索
     *
     * @param pageNum  页码（从1开始），默认值为1
     * @param pageSize 每页条数，默认值为10
     * @param status   订单状态筛选（可选），如"待支付"、"已发货"等
     * @param keyword  搜索关键词（可选），用于按订单号或用户名模糊匹配
     * @return Result 包装的分页订单数据Map
     */
    // @GetMapping：将HTTP GET请求映射到/admin/order/list路径
    @GetMapping("/admin/order/list")
    Result<Map<String, Object>> adminOrderList(
            // @RequestParam：页码，默认第1页
            @RequestParam(defaultValue = "1") int pageNum,
            // @RequestParam：每页条数，默认10条
            @RequestParam(defaultValue = "10") int pageSize,
            // @RequestParam(required = false)：订单状态为可选筛选条件
            @RequestParam(required = false) String status,
            // @RequestParam(required = false)：搜索关键词为可选参数
            @RequestParam(required = false) String keyword);

    /**
     * 管理后台 — 更新订单状态
     * 管理员可以手动修改订单状态（如发货、完成等）
     *
     * @param orderId      订单ID，作为URL路径变量
     * @param statusUpdate 状态更新数据Map，包含newStatus（新状态）等字段
     * @return Result 包装的空返回体
     */
    // @PutMapping：将HTTP PUT请求映射到/admin/order/{orderId}/status路径
    @PutMapping("/admin/order/{orderId}/status")
    Result<Void> updateOrderStatus(
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId,
            // @RequestBody：状态更新数据通过请求体传入
            @RequestBody Map<String, Object> statusUpdate);

    /**
     * 管理后台 — 获取订单详情
     * 管理员查看指定订单的完整信息
     *
     * @param orderId 订单ID，作为URL路径变量
     * @return Result 包装的订单详情Map
     */
    // @GetMapping：将HTTP GET请求映射到/admin/order/{orderId}路径
    @GetMapping("/admin/order/{orderId}")
    Result<Map<String, Object>> adminOrderDetail(
            // @PathVariable：订单ID路径变量
            @PathVariable String orderId);
}
