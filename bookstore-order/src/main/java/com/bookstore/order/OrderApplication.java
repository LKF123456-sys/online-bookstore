package com.bookstore.order;  // 声明当前类所在的包路径：订单服务的根包

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling  // 启用定时任务支持（CompensationRecoveryTask 补偿恢复定时任务）
@MapperScan("com.bookstore.order.mapper")
@ComponentScan(basePackages = {"com.bookstore.order", "com.bookstore.common"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
