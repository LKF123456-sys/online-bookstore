package com.bookstore.message;  // 声明当前类所属的包路径，属于消息服务的根包

import org.mybatis.spring.annotation.MapperScan;  // 导入MyBatis的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.springframework.boot.SpringApplication;  // 导入Spring Boot应用启动类，提供run方法启动应用
import org.springframework.boot.autoconfigure.SpringBootApplication;  // 导入Spring Boot应用启动注解，包含自动配置等功能
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;  // 导入RabbitMQ自动配置类，用于排除其自动装配
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;  // 导入服务发现客户端注解，用于注册到Nacos/Eureka等注册中心
import org.springframework.cloud.openfeign.EnableFeignClients;  // 导入Feign客户端注解，用于启用声明式HTTP调用
import org.springframework.context.annotation.ComponentScan;  // 导入组件扫描注解，用于指定Spring扫描的包路径

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})  // Spring Boot启动注解，排除RabbitMQ自动配置（本服务暂不使用RabbitMQ）
@EnableDiscoveryClient  // 启用服务发现客户端，使本服务能注册到Nacos等注册中心，并发现其他服务
@EnableFeignClients  // 启用Feign声明式HTTP客户端，允许通过接口定义调用其他微服务
@MapperScan("com.bookstore.message.mapper")  // 自动扫描指定包下的Mapper接口，为其生成代理实现类并注册为Spring Bean
@ComponentScan(basePackages = {"com.bookstore.message", "com.bookstore.common"})  // 指定Spring组件扫描的基础包，同时扫描消息服务和公共模块
public class MessageApplication {  // 消息服务的主启动类

    /**
     * 应用程序入口方法
     * 启动Spring Boot应用，初始化所有组件并启动内嵌的Web服务器
     *
     * @param args 命令行参数，可通过 --key=value 形式传递配置
     */
    public static void main(String[] args) {  // 主方法，程序的入口点
        SpringApplication.run(MessageApplication.class, args);  // 启动Spring Boot应用，传入当前类和命令行参数
    }
}
