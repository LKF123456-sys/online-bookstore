package com.bookstore.order.config;  // 声明当前类所在的包路径：订单服务配置层

import org.springframework.amqp.core.*;  // 导入Spring AMQP核心类（Exchange、Queue、Binding等）
import org.springframework.context.annotation.Bean;  // 导入Bean注解，用于将方法返回值注册为Spring容器中的Bean
import org.springframework.context.annotation.Configuration;  // 导入Configuration注解，标记为配置类

/**
 * RabbitMQ消息队列配置类
 * 定义订单相关的交换机、队列和路由规则
 *
 * 消息流转流程：
 *   生产者 -> Exchange（交换机） -> 根据RoutingKey路由到对应Queue -> 消费者
 *
 * 本配置定义了两种消息场景：
 *   1. 订单支付成功（order.pay）：支付成功后发送通知，触发下游处理（积分、通知等）
 *   2. 订单取消（order.cancel）：取消订单后发送通知，触发库存恢复等后续操作
 */
@Configuration  // 标记为Spring配置类，类中的@Bean方法会被Spring容器管理
public class RabbitMQConfig {

    // ==================== 交换机定义 ====================

    /**
     * 订单交换机（Direct类型）
     * Direct交换机会根据消息的RoutingKey精确匹配到对应的队列
     * 例如：routingKey="order.pay" 的消息会被路由到绑定了 "order.pay" 的队列
     */
    @Bean
    public DirectExchange orderExchange() {  // 创建并注册一个Direct类型的交换机
        return new DirectExchange("order.exchange");  // 交换机名称为 "order.exchange"
    }

    // ==================== 队列定义 ====================

    /**
     * 订单支付队列
     * 用于接收订单支付成功的消息
     * 消费者监听此队列后，可执行发送通知、增加用户积分等下游操作
     */
    @Bean
    public Queue orderPayQueue() {  // 创建并注册支付队列
        return QueueBuilder.durable("order.pay.queue").build();  // durable=true 表示队列持久化，RabbitMQ重启后队列仍然存在
    }

    /**
     * 订单取消队列
     * 用于接收订单取消的消息
     * 消费者监听此队列后，可执行库存恢复、发送取消通知等操作
     */
    @Bean
    public Queue orderCancelQueue() {  // 创建并注册取消队列
        return QueueBuilder.durable("order.cancel.queue").build();  // 队列持久化
    }

    // ==================== 绑定关系（Binding）====================

    /**
     * 将支付队列绑定到订单交换机，路由键为 "order.pay"
     * 当生产者向 order.exchange 发送 routingKey="order.pay" 的消息时，消息会被路由到 order.pay.queue
     */
    @Bean
    public Binding orderPayBinding(Queue orderPayQueue, DirectExchange orderExchange) {  // 绑定支付队列到交换机
        return BindingBuilder.bind(orderPayQueue)  // 绑定支付队列
                .to(orderExchange)                  // 到订单交换机
                .with("order.pay");                 // 路由键为 "order.pay"
    }

    /**
     * 将取消队列绑定到订单交换机，路由键为 "order.cancel"
     * 当生产者向 order.exchange 发送 routingKey="order.cancel" 的消息时，消息会被路由到 order.cancel.queue
     */
    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderExchange) {  // 绑定取消队列到交换机
        return BindingBuilder.bind(orderCancelQueue)  // 绑定取消队列
                .to(orderExchange)                     // 到订单交换机
                .with("order.cancel");                 // 路由键为 "order.cancel"
    }
}
