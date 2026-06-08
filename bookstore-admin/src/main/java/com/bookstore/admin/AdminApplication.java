package com.bookstore.admin; // 声明当前类所在的包路径

// 导入MyBatis的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.mybatis.spring.annotation.MapperScan;
// 导入Spring Boot应用启动类，用于启动整个Spring Boot应用
import org.springframework.boot.SpringApplication;
// 导入Spring Boot自动配置注解，包含@ComponentScan、@EnableAutoConfiguration等功能
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 导入服务发现客户端注解，启用向Nacos注册中心注册服务的能力
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// 导入Feign客户端注解，启用声明式HTTP客户端调用能力
import org.springframework.cloud.openfeign.EnableFeignClients;
// 导入组件扫描注解，用于指定Spring扫描哪些包下的组件
import org.springframework.context.annotation.ComponentScan;

/**
 * 管理后台启动类
 * 这是bookstore-admin微服务的入口点，负责启动整个管理后台应用。
 *
 * 主要注解说明：
 * - @SpringBootApplication：Spring Boot核心注解，开启自动配置和组件扫描
 * - @EnableDiscoveryClient：启用服务发现，将本服务注册到Nacos注册中心
 * - @EnableFeignClients：启用Feign声明式HTTP客户端，方便调用其他微服务
 * - @MapperScan：自动扫描并注册MyBatis的Mapper接口
 * - @ComponentScan：指定需要扫描的包路径，除了本模块还扫描公共模块
 */
@SpringBootApplication // 标记这是一个Spring Boot应用，开启自动配置
@EnableDiscoveryClient // 启用服务发现客户端，向Nacos注册本服务
@EnableFeignClients // 启用Feign客户端，支持声明式微服务调用
@MapperScan("com.bookstore.admin.mapper") // 自动扫描并注册mapper包下所有的MyBatis Mapper接口
@ComponentScan(basePackages = {"com.bookstore.admin", "com.bookstore.common"}) // 扫描admin模块和common公共模块的组件
public class AdminApplication { // 管理后台应用的主类，包含启动方法

    /**
     * 应用程序主入口方法
     * 通过SpringApplication.run()启动整个Spring Boot应用
     *
     * @param args 命令行参数，可以通过--key=value的形式传入配置覆盖
     */
    public static void main(String[] args) { // 主方法，程序的入口点
        // 调用Spring Boot的启动方法，启动整个应用并加载所有自动配置
        SpringApplication.run(AdminApplication.class, args);
    } // 主方法结束
}
