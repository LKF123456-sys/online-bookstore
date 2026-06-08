package com.bookstore.user;  // 声明当前类所在的包路径，这里是用户服务的根包

import org.mybatis.spring.annotation.MapperScan;  // 导入MyBatis的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.springframework.boot.SpringApplication;  // 导入Spring Boot的应用启动类，用于启动Spring应用
import org.springframework.boot.autoconfigure.SpringBootApplication;  // 导入Spring Boot自动配置注解，开启自动配置功能
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;  // 导入服务发现注解，用于注册到Nacos等注册中心
import org.springframework.cloud.openfeign.EnableFeignClients;  // 导入Feign客户端注解，用于声明式的服务间调用
import org.springframework.context.annotation.ComponentScan;  // 导入组件扫描注解，用于指定Spring扫描哪些包下的组件

/**
 * 用户服务启动类
 * 这是整个用户服务（bookstore-user）的入口，负责启动Spring Boot应用并完成各项初始化配置。
 * <p>
 * 各注解说明：
 * - @SpringBootApplication：组合注解，包含自动配置、组件扫描等功能，是Spring Boot应用的核心注解
 * - @EnableDiscoveryClient：启用服务发现客户端，使本服务能注册到Nacos注册中心，实现微服务的互相调用
 * - @EnableFeignClients：启用Feign声明式HTTP客户端，使本服务可以通过接口方式调用其他微服务
 * - @MapperScan：自动扫描指定包下的MyBatis Mapper接口并注册为Spring Bean，省去每个Mapper都要加@Mapper注解
 * - @ComponentScan：自定义组件扫描路径，除了扫描当前服务的包，还扫描公共模块(com.bookstore.common)下的组件
 */
@SpringBootApplication  // Spring Boot核心注解，开启自动配置、组件扫描等功能
@EnableDiscoveryClient  // 启用服务发现客户端，将本服务注册到Nacos等注册中心
@EnableFeignClients  // 启用Feign客户端，支持通过声明式接口调用其他微服务
@MapperScan("com.bookstore.user.mapper")  // 自动扫描并注册 com.bookstore.user.mapper 包下的所有MyBatis Mapper接口
@ComponentScan(basePackages = {"com.bookstore.user", "com.bookstore.common"})  // 扫描用户服务和公共模块下的所有Spring组件
public class UserApplication {

    /**
     * 主方法 —— 程序的入口点
     * 当Java虚拟机(JVM)运行此类时，会从这个main方法开始执行。
     * SpringApplication.run() 方法会启动Spring Boot的自动配置、内嵌Web服务器等，让整个应用跑起来。
     *
     * @param args 命令行参数，可以通过启动时传入来覆盖默认配置（如 --server.port=8082）
     */
    public static void main(String[] args) {  // Java程序的入口方法，JVM从这里开始执行
        SpringApplication.run(UserApplication.class, args);  // 启动Spring Boot应用，传入主配置类和命令行参数
    }
}
