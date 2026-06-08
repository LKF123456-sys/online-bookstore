package com.bookstore.order;  // 声明当前类所在的包路径：订单服务的根包

// 导入MyBatis-Plus的Mapper扫描注解，用于自动扫描并注册Mapper接口
import org.mybatis.spring.annotation.MapperScan;
// 导入Spring Boot的应用启动类
import org.springframework.boot.SpringApplication;
// 导入Spring Boot的自动配置注解
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 导入Nacos服务发现客户端注解，用于将服务注册到Nacos注册中心
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// 导入OpenFeign客户端注解，用于开启Feign远程调用功能
import org.springframework.cloud.openfeign.EnableFeignClients;
// 导入组件扫描注解，用于指定Spring扫描哪些包下的组件
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单服务启动类
 * 这是整个订单微服务的入口，Spring Boot会从这里启动应用。
 * 通过各种注解开启了服务注册发现、Feign远程调用、MyBatis映射扫描等功能。
 */
@SpringBootApplication  // 标记这是一个Spring Boot应用，自动配置Spring的各种功能
@EnableDiscoveryClient  // 开启服务发现客户端，让本服务能注册到Nacos注册中心，也能从Nacos发现其他服务
@EnableFeignClients  // 开启Feign远程调用功能，允许通过接口方式调用其他微服务
@MapperScan("com.bookstore.order.mapper")  // 自动扫描指定包下的Mapper接口，并注册为Spring Bean，省去每个Mapper加@Mapper注解
@ComponentScan(basePackages = {"com.bookstore.order", "com.bookstore.common"})  // 指定Spring扫描的包路径，包含订单模块和公共模块
public class OrderApplication {  // 定义订单服务的主类

    /**
     * 程序主入口方法
     * 这是Java程序的标准main方法，Spring Boot从这里开始启动
     * @param args 命令行参数，可以用来传递配置信息
     */
    public static void main(String[] args) {  // Java程序的入口方法
        SpringApplication.run(OrderApplication.class, args);  // 启动Spring Boot应用，传入当前类和命令行参数
    }
}
