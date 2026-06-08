package com.bookstore.gateway; // 声明当前类所在的包路径，gateway包专门存放网关相关代码

import org.springframework.boot.SpringApplication; // 导入Spring Boot的启动工具类，用于启动应用
import org.springframework.boot.autoconfigure.SpringBootApplication; // 导入Spring Boot自动配置注解
import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // 导入服务发现客户端注解，用于注册到Nacos等注册中心

/**
 * API网关启动类
 * 网关是微服务架构的统一入口，所有外部请求都先经过网关
 * 网关负责路由转发、认证鉴权、限流熔断等功能
 *
 * 工作原理：
 * 1. 客户端发送请求到网关（端口8080）
 * 2. 网关根据URL路径判断该请求应该转发到哪个微服务
 * 3. 网关将请求转发到对应的微服务实例
 * 4. 微服务处理完成后，响应结果原路返回给客户端
 */
@SpringBootApplication  // Spring Boot启动注解，标记这是一个Spring Boot应用，开启自动配置和组件扫描
@EnableDiscoveryClient   // 开启服务发现客户端功能，使网关能够从Nacos注册中心发现其他微服务
public class GatewayApplication {

    /**
     * 程序入口方法（主方法）
     * 所有Java程序都从main方法开始执行
     * @param args 命令行参数，可以在启动时传入，如 --server.port=9090
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args); // 启动Spring Boot应用，参数是主配置类的class对象和命令行参数
    }
}
