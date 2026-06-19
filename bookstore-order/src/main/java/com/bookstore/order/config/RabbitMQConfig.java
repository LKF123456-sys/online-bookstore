package com.bookstore.order.config;  // 声明当前类所在的包路径：订单服务配置层

import org.springframework.amqp.core.*;  // 导入Spring AMQP核心类（Exchange、Queue、Binding等）
import org.springframework.context.annotation.Bean;  // 导入Bean注解，用于将方法返回值注册为Spring容器中的Bean
import org.springframework.context.annotation.Configuration;  // 导入Configuration注解，标记为配置类

/**
 * RabbitMQ消息队列配置类
 * 定义订单相关的交换机、队列、路由规则以及死信队列（DLQ）
 *
 * 消息流转流程：
 *   生产者 -> Exchange（交换机） -> 根据RoutingKey路由到对应Queue -> 消费者
 *   消费失败 -> 重试3次后 -> 路由到死信队列（DLQ） -> 人工介入
 *
 * 面试亮点：
 *   1. 死信队列（DLQ）：消费失败的消息不会丢失，而是路由到 DLQ 等待人工处理
 *   2. 消息持久化：所有队列和交换机都设置了 durable=true
 *   3. TTL 机制：死信消息 7 天后自动过期清理
 *   4. 消息可靠性保障：生产者确认 + 手动 ACK + 幂等消费 + DLQ 兜底
 */
@Configuration  // 标记为Spring配置类，类中的@Bean方法会被Spring容器管理
public class RabbitMQConfig {

    // ==================== 常量定义 ====================

    /** 死信交换机名称 */
    private static final String DLX_EXCHANGE = "order.dlx.exchange";

    /** 死信路由键前缀 */
    private static final String DLX_PAY_ROUTING_KEY = "dlx.order.pay";
    private static final String DLX_CANCEL_ROUTING_KEY = "dlx.order.cancel";

    /** 消息在死信队列中的存活时间（7天，单位毫秒） */
    private static final int DLQ_TTL_MS = 7 * 24 * 3600 * 1000;

    // ==================== 业务交换机 ====================

    /**
     * 订单交换机（Direct类型）
     * Direct交换机会根据消息的RoutingKey精确匹配到对应的队列
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("order.exchange");
    }

    // ==================== 死信交换机 ====================

    /**
     * 死信交换机（DLX）
     * 当消息消费失败被 nack（requeue=false）或消息过期时，会被路由到此交换机
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 业务队列（带死信路由） ====================

    /**
     * 订单支付队列
     * 配置了死信路由：消费失败的消息会被自动转发到死信队列
     * x-dead-letter-exchange：指定死信交换机
     * x-dead-letter-routing-key：指定死信路由键
     */
    @Bean
    public Queue orderPayQueue() {
        return QueueBuilder.durable("order.pay.queue")
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_PAY_ROUTING_KEY)
                .build();
    }

    /**
     * 订单取消队列
     * 同样配置了死信路由
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable("order.cancel.queue")
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_CANCEL_ROUTING_KEY)
                .build();
    }

    // ==================== 死信队列 ====================

    /**
     * 支付死信队列
     * 存储消费失败的支付消息，等待人工介入处理
     * 设置 TTL = 7天，超期自动清理，避免无限堆积
     */
    @Bean
    public Queue orderPayDlq() {
        return QueueBuilder.durable("order.pay.dlq")
                .ttl(DLQ_TTL_MS)
                .build();
    }

    /**
     * 取消死信队列
     * 存储消费失败的取消消息，等待人工介入处理
     */
    @Bean
    public Queue orderCancelDlq() {
        return QueueBuilder.durable("order.cancel.dlq")
                .ttl(DLQ_TTL_MS)
                .build();
    }

    // ==================== 绑定关系（Binding） ====================

    /** 将支付队列绑定到订单交换机 */
    @Bean
    public Binding orderPayBinding(Queue orderPayQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPayQueue).to(orderExchange).with("order.pay");
    }

    /** 将取消队列绑定到订单交换机 */
    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCancelQueue).to(orderExchange).with("order.cancel");
    }

    /** 将支付死信队列绑定到死信交换机 */
    @Bean
    public Binding orderPayDlqBinding(Queue orderPayDlq, DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderPayDlq).to(orderDlxExchange).with(DLX_PAY_ROUTING_KEY);
    }

    /** 将取消死信队列绑定到死信交换机 */
    @Bean
    public Binding orderCancelDlqBinding(Queue orderCancelDlq, DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderCancelDlq).to(orderDlxExchange).with(DLX_CANCEL_ROUTING_KEY);
    }
}
