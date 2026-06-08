// 声明当前类所属的包路径，遵循 Spring Boot 项目标准分包结构
package com.bookstore.admin.service;

// 导入订单服务的 Feign 客户端接口，用于声明式调用 bookstore-order 微服务
import com.bookstore.admin.feign.OrderFeignClient;
// 导入公共模块的统一响应封装类 Result，所有 Feign 调用返回值都通过此类包装
import com.bookstore.common.api.Result;
// Lombok 注解 @RequiredArgsConstructor：为所有 final 字段生成构造函数，Spring 会自动注入依赖
import lombok.RequiredArgsConstructor;
// Lombok 注解 @Slf4j：自动生成 log 静态字段（使用 SLF4J 日志门面），无需手动声明 Logger
import lombok.extern.slf4j.Slf4j;
// Spring 的 @Service 注解：将该类标记为 Spring 容器管理的业务逻辑层 Bean
import org.springframework.stereotype.Service;

// 导入 Java 集合框架的 Map 接口，用于灵活传递键值对数据
import java.util.Map;

/**
 * 订单服务 — 封装 OrderFeignClient，添加统一错误处理和日志
 * <p>
 * 该类是 Admin 端的订单业务逻辑层，负责：
 * <ul>
 *   <li>封装对 bookstore-order 微服务的远程调用（通过 OrderFeignClient）</li>
 *   <li>关键操作（创建、支付、取消、确认收货）使用 INFO 级别日志，便于运维审计</li>
 *   <li>查询操作（列表、详情、购物车）使用 DEBUG 级别日志，减少生产日志量</li>
 *   <li>作为中间层，未来可在此添加状态机校验、库存检查、消息通知等增强逻辑</li>
 * </ul>
 * 涵盖订单生命周期管理（创建→支付→发货→确认收货）和购物车管理。
 */
// @Slf4j：Lombok 会在编译时生成日志对象 log，可使用 log.info()、log.debug() 等方法记录日志
@Slf4j
// @Service：标识这是一个 Service 层组件，Spring 会扫描并创建单例 Bean 管理其生命周期
@Service
// @RequiredArgsConstructor：Lombok 自动生成包含所有 final 字段的构造函数，实现构造函数注入
@RequiredArgsConstructor
public class OrderService {

    // 订单服务 Feign 客户端，通过构造函数注入
    // 声明为 final 确保一旦注入后不可变，配合 @RequiredArgsConstructor 自动生成构造函数
    private final OrderFeignClient orderFeignClient;

    /**
     * 创建新订单
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order 接口（POST 方法）。
     * 用户身份通过请求头 X-User-Id 传递。日志记录操作人和操作类型。
     *
     * @param userId    当前登录用户的 ID
     * @param orderData 订单数据 Map（包含收货地址、商品列表、优惠券等）
     * @return Result 包装的订单信息 Map
     */
    public Result<Map<String, Object>> createOrder(String userId, Map<String, Object> orderData) {
        // 记录 INFO 级别日志：创建订单属于关键操作，便于运维审计和异常排查
        log.info("OrderService.createOrder: userId={}", userId);
        // 委托 Feign 客户端向 bookstore-order 微服务发送 POST 请求创建订单
        return orderFeignClient.createOrder(userId, orderData);
    }

    /**
     * 获取用户订单列表（前台/用户端）
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order/list 接口。
     * 使用 DEBUG 级别日志，减少生产环境的日志输出量。
     *
     * @param userId   当前登录用户的 ID
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param status   订单状态筛选（可选）
     * @return Result 包装的分页订单数据 Map
     */
    public Result<Map<String, Object>> userOrderList(String userId, int pageNum, int pageSize, String status) {
        // 记录 DEBUG 级别日志：查询类操作使用 DEBUG 级别，生产环境默认不输出
        log.debug("OrderService.userOrderList: userId={}, page={}, status={}", userId, pageNum, status);
        return orderFeignClient.userOrderList(userId, pageNum, pageSize, status);
    }

    /**
     * 获取订单详情
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order/{orderId} 接口。
     *
     * @param userId  当前登录用户的 ID
     * @param orderId 订单号
     * @return Result 包装的订单详情 Map
     */
    public Result<Map<String, Object>> orderDetail(String userId, String orderId) {
        // 记录 DEBUG 日志：输出订单号，便于定位特定订单的问题
        log.debug("OrderService.orderDetail: orderId={}", orderId);
        return orderFeignClient.orderDetail(userId, orderId);
    }

    /**
     * 支付订单
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order/{orderId}/pay 接口（POST 方法）。
     * 触发支付流程，对接第三方支付网关。
     *
     * @param userId  当前登录用户的 ID
     * @param orderId 订单号
     * @return Result 包装的空返回体
     */
    public Result<Void> payOrder(String userId, String orderId) {
        // 记录 INFO 日志：支付操作属于核心交易，需要完整审计记录
        log.info("OrderService.payOrder: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.payOrder(userId, orderId);
    }

    /**
     * 取消订单
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order/{orderId}/cancel 接口（POST 方法）。
     * 只能取消待支付状态的订单，取消后会释放已锁定的库存。
     *
     * @param userId  当前登录用户的 ID
     * @param orderId 订单号
     * @return Result 包装的空返回体
     */
    public Result<Void> cancelOrder(String userId, String orderId) {
        // 记录 INFO 日志：取消订单是重要操作，记录操作人和订单号
        log.info("OrderService.cancelOrder: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.cancelOrder(userId, orderId);
    }

    /**
     * 确认收货
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/order/{orderId}/confirm 接口（POST 方法）。
     * 用户收到商品后确认，订单状态变更为"已完成"。
     *
     * @param userId  当前登录用户的 ID
     * @param orderId 订单号
     * @return Result 包装的空返回体
     */
    public Result<Void> confirmReceive(String userId, String orderId) {
        // 记录 INFO 日志：确认收货是订单生命周期的关键节点
        log.info("OrderService.confirmReceive: orderId={}, userId={}", orderId, userId);
        return orderFeignClient.confirmReceive(userId, orderId);
    }

    // ===== 以下为购物车相关方法 =====

    /**
     * 获取当前用户的购物车内容
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/cart 接口。
     *
     * @param userId 当前登录用户的 ID
     * @return Result 包装的购物车数据 Map
     */
    public Result<Map<String, Object>> getCart(String userId) {
        // 直接委托 Feign 客户端，购物车查询频繁，不记录日志以提升性能
        return orderFeignClient.getCart(userId);
    }

    /**
     * 添加商品到购物车
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/cart 接口（POST 方法）。
     *
     * @param userId   当前登录用户的 ID
     * @param cartItem 购物车条目数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> addToCart(String userId, Map<String, Object> cartItem) {
        // 记录 DEBUG 日志：添加购物车操作频率较高，使用 DEBUG 级别
        log.debug("OrderService.addToCart: userId={}", userId);
        return orderFeignClient.addToCart(userId, cartItem);
    }

    /**
     * 更新购物车中某一条目的数量
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/cart/{itemId} 接口（PUT 方法）。
     *
     * @param userId 当前登录用户的 ID
     * @param itemId 购物车条目 ID
     * @param update 更新数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updateCartItem(String userId, Long itemId, Map<String, Object> update) {
        return orderFeignClient.updateCartItem(userId, itemId, update);
    }

    /**
     * 从购物车中删除某一项
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/cart/{itemId} 接口（DELETE 方法）。
     *
     * @param userId 当前登录用户的 ID
     * @param itemId 购物车条目 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteCartItem(String userId, Long itemId) {
        return orderFeignClient.deleteCartItem(userId, itemId);
    }

    /**
     * 清空当前用户的购物车
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/cart 接口（DELETE 方法）。
     *
     * @param userId 当前登录用户的 ID
     * @return Result 包装的空返回体
     */
    public Result<Void> clearCart(String userId) {
        return orderFeignClient.clearCart(userId);
    }

    // ===== 以下为管理后台专用方法 =====

    /**
     * 管理后台 — 获取所有订单分页列表
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/admin/order/list 接口。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @param status   订单状态筛选（可选）
     * @param keyword  搜索关键词（可选）
     * @return Result 包装的分页订单数据 Map
     */
    public Result<Map<String, Object>> adminOrderList(int pageNum, int pageSize, String status, String keyword) {
        return orderFeignClient.adminOrderList(pageNum, pageSize, status, keyword);
    }

    /**
     * 管理后台 — 更新订单状态（如标记发货、完成等）
     * <p>
     * 通过 Feign 调用 bookstore-order 微服务的 /api/admin/order/{orderId}/status 接口（PUT 方法）。
     *
     * @param orderId      订单号
     * @param statusUpdate 状态更新数据 Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updateOrderStatus(String orderId, Map<String, Object> statusUpdate) {
        return orderFeignClient.updateOrderStatus(orderId, statusUpdate);
    }
}