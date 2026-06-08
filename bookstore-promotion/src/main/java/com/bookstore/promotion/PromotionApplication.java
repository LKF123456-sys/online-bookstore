package com.bookstore.promotion;  // 声明当前类所在的包路径，属于营销服务模块

import org.mybatis.spring.annotation.MapperScan;  // 导入MyBatis的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.springframework.boot.SpringApplication;  // 导入Spring Boot应用启动类，负责启动整个Spring应用
import org.springframework.boot.autoconfigure.SpringBootApplication;  // 导入Spring Boot自动配置注解，开启自动配置功能
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;  // 导入服务发现客户端注解，让本服务注册到Nacos等注册中心
import org.springframework.cloud.openfeign.EnableFeignClients;  // 导入Feign客户端注解，允许通过声明式接口调用其他微服务
import org.springframework.context.annotation.ComponentScan;  // 导入组件扫描注解，指定Spring扫描哪些包来发现Bean

/**
 * 营销服务启动类
 * 这是营销微服务的入口，负责启动整个营销服务模块
 * 营销服务提供优惠券管理、用户评价管理、公告管理等功能
 *
 * @SpringBootApplication 是一个组合注解，包含：
 *   - @Configuration：标记为配置类
 *   - @EnableAutoConfiguration：开启Spring Boot自动配置
 *   - @ComponentScan：自动扫描当前包及子包下的组件
 */
@SpringBootApplication  // Spring Boot核心注解，启动自动配置和组件扫描
@EnableDiscoveryClient  // 开启服务发现功能，使本服务可以注册到Nacos注册中心，也能发现其他服务
@EnableFeignClients  // 开启Feign声明式HTTP客户端，允许通过接口方式调用其他微服务（如调用用户服务、商品服务等）
@MapperScan("com.bookstore.promotion.mapper")  // 自动扫描指定包下的MyBatis Mapper接口，将它们注册为Spring Bean
@ComponentScan(basePackages = {"com.bookstore.promotion", "com.bookstore.common"})  // 指定Spring扫描的包路径，包括营销模块和公共模块
public class PromotionApplication {  // 营销服务的主类，程序从这里开始运行

    /**
     * 主方法 - 程序的入口点
     * Java程序启动时会首先执行这个方法
     * SpringApplication.run() 会启动整个Spring Boot应用，包括：
     *   1. 创建Spring应用上下文（IoC容器）
     *   2. 自动配置各种组件
     *   3. 启动内嵌的Web服务器（如Tomcat）
     *   4. 注册到Nacos注册中心
     *
     * @param args 命令行参数，可以在启动时通过命令行传入配置覆盖项
     */
    public static void main(String[] args) {  // Java程序入口方法，static表示无需创建实例即可调用
        SpringApplication.run(PromotionApplication.class, args);  // 启动Spring Boot应用，传入主类的Class对象和命令行参数
    }
}
