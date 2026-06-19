package com.bookstore.order.mq;  // 声明当前类所在的包路径：订单服务消息队列层

import com.rabbitmq.client.Channel;  // 导入RabbitMQ的Channel接口，用于手动确认消息
import lombok.RequiredArgsConstructor;  // Lombok注解，自动生成final字段的构造函数
import lombok.extern.slf4j.Slf4j;  // Lombok注解，自动生成log日志对象
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.springframework.amqp.rabbit.annotation.RabbitListener;  // 导入RabbitListener注解，用于监听RabbitMQ队列
import org.springframework.amqp.support.AmqpHeaders;  // 导入AMQP头信息常量，用于从消息头中获取Channel和deliveryTag
import org.springframework.data.redis.core.RedisTemplate;  // 导入RedisTemplate，用于幂等性校验
import org.springframework.messaging.handler.annotation.Header;  // 导入Header注解，用于获取消息头信息
import org.springframework.stereotype.Component;  // 导入Component注解，注册为Spring组件

import com.bookstore.order.feign.ProductFeignClient;  // 导入商品Feign客户端，用于恢复库存
import com.bookstore.order.mapper.OrderItemMapper;  // 导入订单项Mapper
import com.bookstore.order.mapper.OrdersMapper;  // 导入订单Mapper
import com.bookstore.common.entity.OrderItem;  // 导入订单项实体
import com.bookstore.common.entity.Orders;  // 导入订单实体
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus查询构造器

import java.util.List;  // 导入List集合
import java.util.concurrent.TimeUnit;  // 导入时间单位，用于设置Redis过期时间

/**
 * 订单消息消费者
 * 负责监听RabbitMQ中的订单相关队列，处理异步消息
 *
 * 消费特性：
 *   1. 手动ACK确认：确保消息被成功处理后才从队列中移除
 *   2. 幂等性消费：使用Redis Set记录已处理的消息ID，防止消息重复消费
 *   3. 异常处理：消费失败时不丢弃消息，而是拒绝并让RabbitMQ重新投递
 *
 * 队列说明：
 *   - order.pay.queue：处理订单支付成功后的下游操作（通知、积分等）
 *   - order.cancel.queue：处理订单取消后的库存恢复等操作
 */
@Slf4j  // 自动生成log日志对象
@Component  // 注册为Spring组件
@RequiredArgsConstructor  // 自动生成包含所有final字段的构造函数
public class OrderMessageConsumer {

    private final RedisTemplate<String, Object> redisTemplate;  // Redis操作模板，用于幂等性校验
    private final OrdersMapper ordersMapper;  // 订单Mapper，用于查询订单信息
    private final OrderItemMapper orderItemMapper;  // 订单项Mapper，用于查询订单项
    private final ProductFeignClient productFeignClient;  // 商品Feign客户端，用于远程调用商品服务

    /** 幂等性集合的Redis Key前缀，已处理的消息ID会被存入此Set */
    private static final String IDEMPOTENT_KEY_PREFIX = "mq:processed:";

    /** 幂等性记录的过期时间（24小时），超时后自动清理，防止Redis数据无限增长 */
    private static final long IDEMPOTENT_EXPIRE_HOURS = 24;

    /** 消息投递计数 Key 前缀（用于 DLQ 路由判断） */
    private static final String RETRY_COUNT_KEY_PREFIX = "mq:retry:";

    /** 最大投递次数（超过后路由到 DLQ，防止毒消息无限循环） */
    private static final int MAX_DELIVERY_COUNT = 3;

    // ==================== 支付消息消费 ====================

    /**
     * 监听订单支付队列
     * 收到消息后执行以下操作：
     *   1. 幂等性校验（检查消息是否已处理）
     *   2. 解析消息内容（orderId、userId）
     *   3. 执行支付后的业务处理（日志记录、通知等）
     *   4. 手动ACK确认消息
     *
     * @param message   消息内容，格式为 "orderId|userId"
     * @param messageId 消息唯一标识，由生产者在发送时设置
     * @param channel   RabbitMQ通道，用于手动ACK
     * @param deliveryTag 消息投递标签，ACK时需要此标识
     */
    @RabbitListener(queues = "order.pay.queue")  // 监听支付队列
    @Tag(key = "mq.orderPaid", value = "arg[0]")
    public void handleOrderPaid(String message,
                                @Header(name = "id", required = false) String messageId,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 幂等性校验：检查消息是否已被处理过
            if (messageId != null && isMessageProcessed(messageId)) {  // 如果消息ID存在且已处理
                log.warn("支付消息已处理过，跳过重复消费: messageId={}", messageId);  // 记录警告日志
                channel.basicAck(deliveryTag, false);  // 手动ACK确认，从队列中移除消息
                return;  // 直接返回，不重复处理
            }

            // 解析消息内容
            String[] parts = message.split("\\|");  // 按 "|" 分隔消息内容
            String orderId = parts[0];  // 第一段为订单ID
            String userId = parts.length > 1 ? parts[1] : "unknown";  // 第二段为用户ID

            // 处理支付成功后的业务逻辑
            log.info("处理订单支付事件: orderId={}, userId={}, messageId={}", orderId, userId, messageId);  // 记录处理日志
            // 在实际项目中，这里可以触发：
            //   - 发送支付成功通知（邮件/短信/站内信）
            //   - 增加用户积分
            //   - 更新销售统计数据
            //   - 通知仓库备货

            // 标记消息已处理（幂等性记录）
            markMessageProcessed(messageId);  // 将消息ID存入Redis

            // 手动ACK确认消息已被成功消费
            channel.basicAck(deliveryTag, false);  // false表示只确认当前这一条消息
            log.info("订单支付消息处理完成: orderId={}", orderId);

        } catch (Exception e) {
            log.error("处理订单支付消息异常: message={}", message, e);  // 记录异常日志
            try {
                // 检查重试次数：如果已经重试超过 3 次，发送到死信队列（DLQ）而不是无限重入队
                if (isRetryExhausted(messageId)) {
                    log.error("支付消息重试次数耗尽，路由到死信队列: messageId={}", messageId);
                    channel.basicNack(deliveryTag, false, false);  // requeue=false → 路由到 DLQ
                } else {
                    channel.basicNack(deliveryTag, false, true);  // requeue=true → 重新入队重试
                }
            } catch (Exception ackError) {
                log.error("消息拒绝(ACK)操作失败", ackError);  // ACK本身也失败时记录日志
            }
        }
    }

    // ==================== 取消消息消费 ====================

    /**
     * 监听订单取消队列
     * 收到消息后尝试恢复商品库存：
     *   1. 幂等性校验
     *   2. 解析消息内容
     *   3. 查询订单项并逐个恢复库存（带重试机制）
     *   4. 手动ACK确认
     *
     * @param message   消息内容，格式为 "orderId|userId"
     * @param messageId 消息唯一标识
     * @param channel   RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = "order.cancel.queue")  // 监听取消队列
    @Tag(key = "mq.orderCancelled", value = "arg[0]")
    public void handleOrderCancelled(String message,
                                     @Header(name = "id", required = false) String messageId,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 幂等性校验
            if (messageId != null && isMessageProcessed(messageId)) {  // 如果消息已处理
                log.warn("取消消息已处理过，跳过重复消费: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);  // 直接ACK
                return;
            }

            // 解析消息内容
            String[] parts = message.split("\\|");  // 分隔消息
            String orderId = parts[0];  // 订单ID
            String userId = parts.length > 1 ? parts[1] : "unknown";  // 用户ID

            log.info("处理订单取消事件: orderId={}, userId={}, messageId={}", orderId, userId, messageId);

            // 查询该订单的所有订单项，用于恢复库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

            // 逐个恢复商品库存（带重试机制）
            for (OrderItem item : items) {  // 遍历每个订单项
                restoreStockWithRetry(item.getProductId(), item.getQuantity(), 3);  // 最多重试3次
            }

            // 标记消息已处理
            markMessageProcessed(messageId);

            // 手动ACK确认
            channel.basicAck(deliveryTag, false);
            log.info("订单取消消息处理完成，库存已恢复: orderId={}", orderId);

        } catch (Exception e) {
            log.error("处理订单取消消息异常: message={}", message, e);
            try {
                if (isRetryExhausted(messageId)) {
                    log.error("取消消息重试次数耗尽，路由到死信队列: messageId={}", messageId);
                    channel.basicNack(deliveryTag, false, false);  // requeue=false → 路由到 DLQ
                } else {
                    channel.basicNack(deliveryTag, false, true);  // requeue=true → 重新入队重试
                }
            } catch (Exception ackError) {
                log.error("消息拒绝(ACK)操作失败", ackError);
            }
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查消息是否已被处理（幂等性校验）
     * 通过Redis Set判断消息ID是否已存在
     *
     * @param messageId 消息唯一标识
     * @return true=已处理，false=未处理
     */
    private boolean isMessageProcessed(String messageId) {  // 幂等性检查
        String key = IDEMPOTENT_KEY_PREFIX + messageId;  // 拼接Redis Key
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));  // 检查Key是否存在
        } catch (Exception e) {
            log.warn("幂等性校验Redis查询失败，允许重复处理: messageId={}", messageId, e);
            return false;  // Redis异常时允许处理，宁可重复处理也不能丢失消息
        }
    }

    /**
     * 标记消息为已处理
     * 将消息ID存入Redis，设置24小时过期
     *
     * @param messageId 消息唯一标识
     */
    private void markMessageProcessed(String messageId) {  // 标记消息已处理
        if (messageId == null) return;  // 如果消息ID为空则跳过
        String key = IDEMPOTENT_KEY_PREFIX + messageId;  // 拼接Redis Key
        try {
            redisTemplate.opsForValue().set(key, "1", IDEMPOTENT_EXPIRE_HOURS, TimeUnit.HOURS);  // 存入Redis，24小时后自动过期
        } catch (Exception e) {
            log.warn("幂等性标记Redis写入失败: messageId={}", messageId, e);  // 记录警告日志
        }
    }

    /**
     * 带重试机制的库存恢复
     * 当Feign调用商品服务失败时，会按指定间隔重试
     *
     * @param productId  商品ID
     * @param quantity   要恢复的数量（正数）
     * @param maxRetries 最大重试次数
     */
    private void restoreStockWithRetry(String productId, Integer quantity, int maxRetries) {  // 带重试的库存恢复
        for (int attempt = 1; attempt <= maxRetries; attempt++) {  // 循环重试
            try {
                // 调用商品服务恢复库存（传入负数表示增加库存）
                productFeignClient.updateStock(productId, -quantity);
                log.info("库存恢复成功: productId={}, quantity={}", productId, quantity);
                return;  // 成功后直接返回
            } catch (Exception e) {
                log.warn("库存恢复失败 (第{}/{}次): productId={}, quantity={}",
                        attempt, maxRetries, productId, quantity, e);  // 记录重试日志
                if (attempt < maxRetries) {  // 如果还有重试机会
                    try {
                        Thread.sleep(1000L * attempt);  // 线性退避等待：第1次等1秒，第2次等2秒，第3次等3秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();  // 恢复中断标志
                        log.error("库存恢复重试等待被中断: productId={}", productId);
                        return;  // 中断时退出重试
                    }
                } else {
                    // 所有重试都失败了，记录错误日志，需要人工介入
                    log.error("库存恢复最终失败，需要人工介入: productId={}, quantity={}", productId, quantity);
                }
            }
        }
    }

    // ==================== DLQ 路由辅助方法 ====================

    /**
     * 判断消息是否已超过最大重试次数
     * 使用 Redis 计数器追踪每条消息的投递次数
     * 超过 MAX_DELIVERY_COUNT 次后返回 true，消息将被路由到 DLQ
     *
     * @param messageId 消息唯一标识
     * @return true=已超过最大重试次数，应路由到 DLQ
     */
    private boolean isRetryExhausted(String messageId) {
        if (messageId == null) return false;
        String key = RETRY_COUNT_KEY_PREFIX + messageId;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            // 首次访问时设置 1 小时过期
            if (count != null && count == 1L) {
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }
            return count != null && count > MAX_DELIVERY_COUNT;
        } catch (Exception e) {
            log.warn("DLQ 重试计数查询失败，默认允许重入队: messageId={}", messageId, e);
            return false;  // Redis 不可用时默认允许重试
        }
    }
}
