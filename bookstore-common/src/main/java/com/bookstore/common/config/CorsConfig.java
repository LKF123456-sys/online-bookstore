package com.bookstore.common.config;  // 声明包路径：属于 bookstore-common 通用模块的配置子包

import org.springframework.context.annotation.Configuration;  // 导入 Spring 的 @Configuration 注解，标识配置类
import org.springframework.web.servlet.config.annotation.CorsRegistry;  // 导入 CORS 注册器，用于注册跨域映射规则
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;  // 导入 WebMvcConfigurer 接口，用于自定义 Spring MVC 配置

/**
 * 全局 CORS 跨域配置类
 * 
 * WebMvcConfigurer 接口说明：
 *   Spring MVC 提供的配置接口，通过实现该接口可以自定义 MVC 的行为（如跨域、拦截器、消息转换器等），
 *   而不需要继承 WebMvcConfigurationSupport（后者会覆盖 Spring Boot 的自动配置）。
 *   推荐使用 implements WebMvcConfigurer 方式来扩展 MVC 配置。
 * 
 * 跨域配置目的：
 *   1. 允许 Swagger UI 页面（如 localhost:8086）跨域加载各微服务的 OpenAPI 规范（localhost:8081-8087）
 *   2. 允许前端开发时从不同域名/端口跨域访问后端 API
 */
@Configuration  // Spring 注解：标识当前类为配置类，Spring 容器启动时会加载并执行其中的配置逻辑
public class CorsConfig implements WebMvcConfigurer {  // 实现 WebMvcConfigurer 接口以自定义 MVC 跨域行为

    /**
     * 重写 addCorsMappings 方法，注册全局 CORS 跨域映射规则
     * 该方法在 Spring 容器初始化时由 Spring MVC 自动调用，应用配置到所有 Controller
     * @param registry CORS 注册器，通过链式调用添加跨域规则
     */
    @Override  // 标识该方法重写了父接口 WebMvcConfigurer 中的 addCorsMappings 方法
    public void addCorsMappings(CorsRegistry registry) {  // 方法签名：接收 CorsRegistry 进行跨域配置
        registry.addMapping("/**")  // 添加路径映射："/**" 表示对所有 URL 路径启用 CORS（包括所有子路径）
                .allowedOriginPatterns("*")  // 允许的来源域名模式："*" 表示允许所有来源（使用 allowedOriginPatterns 而非 allowedOrigins，因为允许携带凭证时不能同时使用 "*" 通配符作为精确来源）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的 HTTP 请求方法列表，包含常见的 RESTful 方法和 OPTIONS 预检请求
                .allowedHeaders("*")  // 允许的请求头："*" 表示允许所有自定义请求头（如 Authorization、Content-Type 等）
                .allowCredentials(false)  // 是否允许发送 Cookie 和认证信息：设为 false，因为使用 "*" 来源模式时不允许同时启用 credentials（浏览器安全限制）
                .maxAge(3600);  // 预检请求（OPTIONS）的缓存时间（秒）：设置为 3600 秒（1小时），在此时间内浏览器不会重复发送 OPTIONS 预检请求
    }
}
