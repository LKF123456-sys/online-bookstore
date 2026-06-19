package com.bookstore.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 订单服务动态配置
 * 通过 @RefreshScope + Nacos 配置中心实现运行时动态调整，无需重启服务
 *
 * 面试亮点：
 *   1. @RefreshScope：当 Nacos 配置变更时，Spring Cloud 会自动刷新此 Bean
 *   2. @ConfigurationProperties：类型安全的配置绑定（比 @Value 更适合复杂配置）
 *   3. 灰度发布友好：修改 Nacos 配置即可调整行为，配合灰度网关实现无损发布
 *
 * 使用场景：
 *   - 动态调整订单超时时间
 *   - 动态调整库存补偿重试策略
 *   - 运行时开关（如临时关闭某些功能）
 *
 * Nacos 配置示例（bookstore-order.yml）：
 *   order:
 *     config:
 *       payment-timeout-minutes: 30
 *       max-order-items: 50
 *       stock-compensation-max-retries: 5
 *       idempotency-ttl-seconds: 600
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "order.config")
public class OrderConfig {

    /**
     * 订单支付超时时间（分钟）
     * 超时未支付的订单可被自动取消
     * 默认 30 分钟
     */
    private int paymentTimeoutMinutes = 30;

    /**
     * 单个订单最大商品数量
     * 防止恶意超大订单
     * 默认 50
     */
    private int maxOrderItems = 50;

    /**
     * 库存补偿最大重试次数
     * 由 CompensationRecoveryTask 定时任务读取
     * 默认 5 次
     */
    private int stockCompensationMaxRetries = 5;

    /**
     * 幂等性 Key 的 TTL（秒）
     * 防重复提交锁的过期时间
     * 默认 600 秒（10 分钟）
     */
    private int idempotencyTtlSeconds = 600;

    /**
     * 是否启用订单创建功能
     * 紧急情况下可临时关闭（如系统维护、大促前的冻结期）
     */
    private boolean orderCreationEnabled = true;

    /**
     * 库存扣减模式
     * - "pre_deduct": 下单时预扣库存（当前默认）
     * - "pay_deduct": 支付时扣库存（高并发场景更安全）
     */
    private String stockDeductMode = "pre_deduct";
}
