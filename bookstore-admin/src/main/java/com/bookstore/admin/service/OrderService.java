package com.bookstore.admin.service; // 声明当前类所属的包路径，遵循Spring Boot项目标准分包结构

// 导入订单服务的Feign客户端接口，用于声明式调用bookstore-order微服务
import com.bookstore.admin.feign.OrderFeignClient;
// 导入公共模块的统一响应封装类Result，所有Feign调用返回值都通过此类包装
import com.bookstore.common.api.Result;
// Lombok注解@RequiredArgsConstructor：为所有final字段生成构造函数，Spring会自动注入依赖
import lombok.RequiredArgsConstructor;
// Lombok注解@Slf4j：自动生成log静态字段（使用SLF4J日志门面），无需手动声明Logger
import lombok.extern.slf4j.Slf4j;
// Spring的@Service注解：将该类标记为Spring容器管理的业务逻辑层Bean
import org.springframework.stereotype.Service;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 订单服务 — 封装 OrderFeignClient，添加日志和数据转换逻辑
 *
 * 该类是Admin端的订单业务逻辑层，负责：
 * - 封装对bookstore-order微服务的远程调用（通过OrderFeignClient）
 * - 在调用前后添加日志记录，便于问题排查
 * - 数据格式转换：将前端提交的订单数据转换为后端DTO兼容格式
 * - 作为中间层，未来可在此添加缓存、权限校验、数据转换等增强逻辑
 */
@Slf4j // Lombok注解，自动生成private static final Logger log字段
@Service // 标记为Spring Service组件，Spring会扫描并创建单例Bean
@RequiredArgsConstructor // Lombok自动生成包含所有final字段的构造函数，实现构造函数注入
public class OrderService {

    // 订单服务Feign客户端，通过构造函数注入
    // 声明为final确保一旦注入后不可变，配合@RequiredArgsConstructor自动生成构造函数
    private final OrderFeignClient orderFeignClient;

    /**
     * 创建订单
     * 将前端提交的订单数据进行格式转换后，调用订单服务创建订单
     *
     * @param userId    当前登录用户的ID
     * @param orderData 订单数据Map，包含收货地址、商品列表、优惠券等
     * @return Result 包装的订单信息Map，包含orderid（订单ID）
     */
    public Result<Map<String, Object>> createOrder(String userId, Map<String, Object> orderData) {
        // 记录INFO级别日志：标记方法名和用户ID，用于审计和问题排查
        log.info("OrderService.createOrder: userId={}", userId);
        // 将前端格式的订单数据转换为OrderCreateDTO兼容格式（拆分地址、转换优惠券字段等）
        normalizeOrderData(orderData);
        // 委托Feign客户端发起HTTP POST请求到bookstore-order微服务创建订单
        return orderFeignClient.createOrder(userId, orderData);
    }

    /**
     * 将前端提交的订单数据转换为 OrderCreateDTO 兼容格式
     * 前端发送 billingAddress/shippingAddress（单行字符串，格式为"name|phone|addr"），
     * 后端 OrderCreateDTO 需要 billToFirstName/billAddr1 等拆分字段
     *
     * @param orderData 订单数据Map，方法内会直接修改此Map
     */
    @SuppressWarnings("unchecked") // 抑制泛型转换的编译警告
    private void normalizeOrderData(Map<String, Object> orderData) {
        // --- 拆分 billingAddress: "name|phone|addr" → billToFirstName, billAddr1, billAddr2 ---
        Object billingAddr = orderData.remove("billingAddress"); // 从Map中取出并移除billingAddress字段
        if (billingAddr instanceof String addr && !addr.isBlank()) { // 判断是否为非空字符串
            String[] parts = addr.split("\\|", 3); // 以"|"为分隔符，最多拆分为3段
            if (parts.length >= 1) orderData.put("billToFirstName", parts[0].trim()); // 第1段：账单联系人姓名
            if (parts.length >= 2) orderData.put("billAddr1", parts[1].trim()); // 第2段：账单地址
            if (parts.length >= 3) orderData.put("billAddr2", parts[2].trim()); // 第3段：账单补充地址
        }
        // --- 拆分 shippingAddress: "name|phone|addr" → shipToFirstName, shipAddr1, shipAddr2 ---
        Object shippingAddr = orderData.remove("shippingAddress"); // 从Map中取出并移除shippingAddress字段
        if (shippingAddr instanceof String addr && !addr.isBlank()) { // 判断是否为非空字符串
            String[] parts = addr.split("\\|", 3); // 以"|"为分隔符，最多拆分为3段
            if (parts.length >= 1) orderData.put("shipToFirstName", parts[0].trim()); // 第1段：收货联系人姓名
            if (parts.length >= 2) orderData.put("shipAddr1", parts[1].trim()); // 第2段：收货地址
            if (parts.length >= 3) orderData.put("shipAddr2", parts[2].trim()); // 第3段：收货补充地址
        }
        // --- couponId → couponName（后端DTO使用couponName字段名） ---
        Object couponId = orderData.remove("couponId"); // 从Map中取出并移除couponId字段
        if (couponId != null) { // 如果存在优惠券ID
            orderData.put("couponName", String.valueOf(couponId)); // 转换为字符串后以couponName键存入
        }
    }

    /**
     * 获取用户的订单列表（分页）
     *
     * @param userId   当前登录用户的ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @param status   订单状态筛选（可选）
     * @return Result 包装的分页订单数据Map
     */
    public Result<Map<String, Object>> userOrderList(String userId, int pageNum, int pageSize, String status) {
        // 记录DEBUG级别日志：输出查询参数，便于开发调试
        log.debug("OrderService.userOrderList: userId={}, page={}, status={}", userId, pageNum, status);
        // 委托Feign客户端发起HTTP GET请求获取订单列表
        return orderFeignClient.userOrderList(userId, pageNum, pageSize, status);
    }

    /**
     * 获取订单详情
     *
     * @param userId  当前登录用户的ID（可选）
     * @param orderId 订单ID
     * @return Result 包装的订单详情Map
     */
    public Result<Map<String, Object>> orderDetail(String userId, String orderId) {
        // 记录DEBUG级别日志
        log.debug("OrderService.orderDetail: orderId={}", orderId);
        // 委托Feign客户端获取订单详情
        return orderFeignClient.orderDetail(userId, orderId);
    }

    /**
     * 支付订单
     * 将订单状态从"待支付"更新为"已支付"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID
     * @return Result 包装的空返回体
     */
    public Result<Void> payOrder(String userId, String orderId) {
        // 记录INFO级别日志：支付是关键操作，需要记录审计
        log.info("OrderService.payOrder: orderId={}, userId={}", orderId, userId);
        // 委托Feign客户端发起支付请求
        return orderFeignClient.payOrder(userId, orderId);
    }

    /**
     * 取消订单
     * 将待支付订单状态设置为"已取消"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID
     * @return Result 包装的空返回体
     */
    public Result<Void> cancelOrder(String userId, String orderId) {
        // 记录INFO级别日志：取消订单是关键操作
        log.info("OrderService.cancelOrder: orderId={}, userId={}", orderId, userId);
        // 委托Feign客户端发起取消请求
        return orderFeignClient.cancelOrder(userId, orderId);
    }

    /**
     * 确认收货
     * 用户收到商品后确认收货，将订单状态从"已发货"更新为"已完成"
     *
     * @param userId  当前登录用户的ID
     * @param orderId 订单ID
     * @return Result 包装的空返回体
     */
    public Result<Void> confirmReceive(String userId, String orderId) {
        // 记录INFO级别日志：确认收货是关键操作
        log.info("OrderService.confirmReceive: orderId={}, userId={}", orderId, userId);
        // 委托Feign客户端发起确认收货请求
        return orderFeignClient.confirmReceive(userId, orderId);
    }

    /**
     * 获取当前用户的购物车内容
     *
     * @param userId 当前登录用户的ID
     * @return Result 包装的购物车数据Map
     */
    public Result<Map<String, Object>> getCart(String userId) {
        // 直接委托Feign客户端，无日志记录（此操作频率较高）
        return orderFeignClient.getCart(userId);
    }

    /**
     * 添加商品到购物车
     *
     * @param userId   当前登录用户的ID
     * @param cartItem 购物车项数据Map，包含productId和quantity
     * @return Result 包装的空返回体
     */
    public Result<Void> addToCart(String userId, Map<String, Object> cartItem) {
        // 记录DEBUG级别日志
        log.debug("OrderService.addToCart: userId={}", userId);
        // 委托Feign客户端添加商品到购物车
        return orderFeignClient.addToCart(userId, cartItem);
    }

    /**
     * 根据购物车项ID更新商品数量
     *
     * @param userId 当前登录用户的ID
     * @param itemId 购物车项ID
     * @param update 更新数据Map，包含quantity（新数量）
     * @return Result 包装的空返回体
     */
    public Result<Void> updateCartItem(String userId, Long itemId, Map<String, Object> update) {
        // 从update Map中提取quantity字段，若为null则默认为1
        Integer quantity = update.get("quantity") != null ? ((Number) update.get("quantity")).intValue() : 1;
        // 委托Feign客户端更新购物车项数量
        return orderFeignClient.updateCartItem(userId, itemId, quantity);
    }

    /**
     * 根据商品ID更新购物车中的商品数量
     *
     * @param userId 当前登录用户的ID
     * @param update 更新数据Map，包含productId和quantity
     * @return Result 包装的空返回体
     */
    public Result<Void> updateCartItemByProductId(String userId, Map<String, Object> update) {
        // 直接委托Feign客户端更新
        return orderFeignClient.updateCartItemByProductId(userId, update);
    }

    /**
     * 根据购物车项ID删除购物车中的商品
     *
     * @param userId 当前登录用户的ID
     * @param itemId 购物车项ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteCartItem(String userId, Long itemId) {
        // 直接委托Feign客户端删除购物车项
        return orderFeignClient.deleteCartItem(userId, itemId);
    }

    /**
     * 根据商品ID删除购物车中的商品
     *
     * @param userId    当前登录用户的ID
     * @param productId 商品ID
     * @return Result 包装的空返回体
     */
    public Result<Void> deleteCartItemByProductId(String userId, String productId) {
        // 直接委托Feign客户端删除
        return orderFeignClient.deleteCartItemByProductId(userId, productId);
    }

    /**
     * 清空购物车
     * 删除当前用户购物车中的所有商品
     *
     * @param userId 当前登录用户的ID
     * @return Result 包装的空返回体
     */
    public Result<Void> clearCart(String userId) {
        // 直接委托Feign客户端清空购物车
        return orderFeignClient.clearCart(userId);
    }

    /**
     * 管理后台 — 获取订单分页列表
     * 支持按状态筛选和关键词搜索
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @param status   订单状态筛选（可选）
     * @param keyword  搜索关键词（可选）
     * @return Result 包装的分页订单数据Map
     */
    public Result<Map<String, Object>> adminOrderList(int pageNum, int pageSize, String status, String keyword) {
        // 直接委托Feign客户端查询管理后台订单列表
        return orderFeignClient.adminOrderList(pageNum, pageSize, status, keyword);
    }

    /**
     * 管理后台 — 更新订单状态
     * 管理员手动修改订单状态（如发货、完成等）
     *
     * @param orderId      订单ID
     * @param statusUpdate 状态更新数据Map
     * @return Result 包装的空返回体
     */
    public Result<Void> updateOrderStatus(String orderId, Map<String, Object> statusUpdate) {
        // 直接委托Feign客户端更新订单状态
        return orderFeignClient.updateOrderStatus(orderId, statusUpdate);
    }
}
