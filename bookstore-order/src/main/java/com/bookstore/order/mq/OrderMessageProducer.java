package com.bookstore.order.mq;  // 声明当前类所在的包路径：订单服务消息队列层

import lombok.RequiredArgsConstructor;  // Lombok注解，自动生成final字段的构造函数
import lombok.extern.slf4j.Slf4j;  // Lombok注解，自动生成log日志对象
import org.springframework.amqp.rabbit.core.RabbitTemplate;  // 导入RabbitTemplate，用于发送消息到RabbitMQ
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;  // 导入Component注解，注册为Spring组件

import java.util.UUID;  // 导入UUID类，用于生成全局唯一的消息ID（幂等性保障）

/**
 * 订单消息生产者
 * 负责向RabbitMQ发送订单相关的异步消息
 *
 * 使用场景：
 *   1. 订单支付成功后，发送支付消息到 order.pay 队列
 *   2. 订单取消后，发送取消消息到 order.cancel 队列
 *
 * 消息发送是异步的，不会阻塞主业务流程。
 * 每条消息都带有唯一的 messageId，用于消费端的幂等性校验（防止重复消费）。
 */
@Slf4j  // 自动生成log日志对象
@Component  // 注册为Spring组件，可被其他Bean注入
@ConditionalOnBean(RabbitTemplate.class)
@RequiredArgsConstructor  // 自动生成包含所有final字段的构造函数
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;  // RabbitMQ操作模板，用于发送消息

    // ==================== 常量定义 ====================

    /** 订单交换机名称，必须与 RabbitMQConfig 中定义的一致 */
    private static final String ORDER_EXCHANGE = "order.exchange";

    /** 订单支付路由键，消息会通过此路由键被路由到 order.pay.queue */
    private static final String ORDER_PAY_ROUTING_KEY = "order.pay";

    /** 订单取消路由键，消息会通过此路由键被路由到 order.cancel.queue */
    private static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    // ==================== 消息发送方法 ====================

    /**
     * 发送订单支付成功消息
     * 在订单支付成功后调用，将支付事件异步通知给下游消费者
     * 消费者可以执行：发送支付通知、增加用户积分、更新销售统计等操作
     *
     * @param orderId 订单ID，标识哪个订单完成了支付
     * @param userId  用户ID，标识哪个用户的订单完成了支付
     */
    public void sendOrderPaidMessage(String orderId, String userId) {  // 发送支付消息
        String messageId = UUID.randomUUID().toString();  // 生成全局唯一的消息ID，用于幂等性校验
        String message = orderId + "|" + userId;  // 拼接消息内容：订单ID|用户ID

        try {
            // 使用rabbitTemplate发送消息到指定交换机，通过路由键路由到对应队列
            rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_PAY_ROUTING_KEY, message, msg -> {
                msg.getMessageProperties().setMessageId(messageId);  // 设置消息ID，消费端用于幂等性去重
                return msg;  // 返回处理后的消息对象
            });
            log.info("订单支付消息已发送: orderId={}, userId={}, messageId={}", orderId, userId, messageId);  // 记录发送成功日志
        } catch (Exception e) {
            // 消息发送失败不影响主业务流程，仅记录错误日志
            log.error("订单支付消息发送失败: orderId={}, userId={}", orderId, userId, e);  // 记录发送失败日志
        }
    }

    /**
     * 发送订单取消消息
     * 在订单取消后调用，将取消事件异步通知给下游消费者
     * 消费者可以执行：恢复商品库存、发送取消通知、释放锁定资源等操作
     *
     * @param orderId 订单ID，标识哪个订单被取消
     * @param userId  用户ID，标识哪个用户取消了订单
     */
    public void sendOrderCancelledMessage(String orderId, String userId) {  // 发送取消消息
        String messageId = UUID.randomUUID().toString();  // 生成全局唯一的消息ID
        String message = orderId + "|" + userId;  // 拼接消息内容：订单ID|用户ID

        try {
            // 发送消息到取消队列
            rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_CANCEL_ROUTING_KEY, message, msg -> {
                msg.getMessageProperties().setMessageId(messageId);  // 设置消息ID
                return msg;  // 返回处理后的消息对象
            });
            log.info("订单取消消息已发送: orderId={}, userId={}, messageId={}", orderId, userId, messageId);  // 记录发送成功日志
        } catch (Exception e) {
            // 消息发送失败不影响主业务流程
            log.error("订单取消消息发送失败: orderId={}, userId={}", orderId, userId, e);  // 记录发送失败日志
        }
    }
}
