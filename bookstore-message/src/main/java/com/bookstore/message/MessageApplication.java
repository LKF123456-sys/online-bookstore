package com.bookstore.message;  // 声明当前类所属的包路径，属于消息服务模块

import org.mybatis.spring.annotation.MapperScan;  // 导入MyBatis的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.springframework.boot.SpringApplication;  // 导入Spring Boot应用启动类，用于启动Spring应用
import org.springframework.boot.autoconfigure.SpringBootApplication;  // 导入Spring Boot自动配置注解，标记这是一个Spring Boot应用
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;  // 导入服务发现客户端注解，使本服务能被注册中心（Nacos）发现
import org.springframework.cloud.openfeign.EnableFeignClients;  // 导入Feign客户端注解，启用声明式HTTP客户端，用于调用其他微服务
import org.springframework.context.annotation.ComponentScan;  // 导入组件扫描注解，指定Spring扫描哪些包下的组件

/**
 * 消息服务启动类
 * 这是整个消息微服务的入口程序，负责启动Spring Boot应用
 *
 * 主要功能：
 *   - 消息的发送和接收
 *   - 消息已读/未读状态管理
 *   - 系统公告和广播消息
 *
 * 通过Nacos注册中心实现服务发现，支持Feign远程调用
 */
@SpringBootApplication  // Spring Boot核心注解，包含自动配置、组件扫描等功能
@EnableDiscoveryClient  // 启用服务发现客户端，将本服务注册到Nacos注册中心，让其他服务可以发现并调用它
@EnableFeignClients  // 启用Feign客户端，允许本服务通过声明式接口调用其他微服务的API
@MapperScan("com.bookstore.message.mapper")  // 自动扫描指定包下的Mapper接口，将其注册为Spring Bean，无需手动添加@Repository注解
@ComponentScan(basePackages = {"com.bookstore.message", "com.bookstore.common"})  // 指定Spring扫描的包路径，除了本模块还要扫描common公共模块中的组件
public class MessageApplication {  // 消息服务启动类

    /**
     * 应用程序主入口方法
     * Java程序的标准入口，Spring Boot通过此方法启动整个应用
     *
     * @param args 命令行参数，可以通过 --key=value 的形式传递配置
     */
    public static void main(String[] args) {  // 主方法，程序启动的入口
        SpringApplication.run(MessageApplication.class, args);  // 启动Spring Boot应用，MessageApplication.class指定主配置类，args传递命令行参数
    }
}
