package com.bookstore.admin.feign; // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 订单服务Feign降级工厂
 * 当订单服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：
 * - 查询类方法：返回友好的错误提示Result（503状态码）
 * - 写入/变更类方法：抛出RuntimeException，阻止业务继续执行
 */
@Slf4j // Lombok注解：自动生成名为log的SLF4J日志对象
@Component // 标记为Spring组件，注册到Spring容器中
public class OrderFeignFallbackFactory implements FallbackFactory<OrderFeignClient> { // 实现Feign降级工厂接口，泛型指定要降级的Feign客户端

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（OrderFeignClient的匿名实现）
     */
    @Override
    public OrderFeignClient create(Throwable cause) { // 重写create方法，参数cause是导致降级的原始异常
        log.error("订单服务调用失败", cause); // 记录错误日志，包含异常堆栈信息
        return new OrderFeignClient() { // 返回OrderFeignClient的匿名内部类实现（即降级逻辑）

            // ======================== 查询类方法（返回友好错误提示） ========================

            /**
             * 获取用户的订单列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> userOrderList(String userId, int pageNum, int pageSize, String status) {
                log.warn("获取用户订单列表降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 获取订单详情 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> orderDetail(String userId, String orderId) {
                log.warn("获取订单详情降级处理: orderId={}, 原因: {}", orderId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 获取购物车 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> getCart(String userId) {
                log.warn("获取购物车降级处理: userId={}, 原因: {}", userId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 管理后台 — 获取订单分页列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminOrderList(int pageNum, int pageSize, String status, String keyword) {
                log.warn("管理后台获取订单列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            /**
             * 管理后台 — 获取订单详情 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminOrderDetail(String orderId) {
                log.warn("管理后台获取订单详情降级处理: orderId={}, 原因: {}", orderId, cause.getMessage());
                return Result.error(503, "订单服务暂时不可用");
            }

            // ======================== 写入/变更类方法（抛出异常） ========================

            /**
             * 创建订单 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Map<String, Object>> createOrder(String userId, Map<String, Object> orderData) {
                log.error("创建订单失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 支付订单 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> payOrder(String userId, String orderId) {
                log.error("支付订单失败: orderId={}, 原因: {}", orderId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 取消订单 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> cancelOrder(String userId, String orderId) {
                log.error("取消订单失败: orderId={}, 原因: {}", orderId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 确认收货 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> confirmReceive(String userId, String orderId) {
                log.error("确认收货失败: orderId={}, 原因: {}", orderId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 添加商品到购物车 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> addToCart(String userId, Map<String, Object> cartItem) {
                log.error("添加购物车失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 根据购物车项ID更新商品数量 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateCartItem(String userId, Long itemId, Map<String, Object> update) {
                log.error("更新购物车项失败: itemId={}, 原因: {}", itemId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 根据商品ID更新购物车中的商品数量 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateCartItemByProductId(String userId, Map<String, Object> update) {
                log.error("根据商品ID更新购物车失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 根据购物车项ID删除购物车中的商品 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> deleteCartItem(String userId, Long itemId) {
                log.error("删除购物车项失败: itemId={}, 原因: {}", itemId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 根据商品ID删除购物车中的商品 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> deleteCartItemByProductId(String userId, String productId) {
                log.error("根据商品ID删除购物车失败: productId={}, 原因: {}", productId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 清空购物车 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> clearCart(String userId) {
                log.error("清空购物车失败: userId={}, 原因: {}", userId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 更新订单状态 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateOrderStatus(String orderId, Map<String, Object> statusUpdate) {
                log.error("更新订单状态失败: orderId={}, 原因: {}", orderId, cause.getMessage());
                throw new RuntimeException("订单服务暂时不可用，请稍后重试");
            }
        };
    }
}
