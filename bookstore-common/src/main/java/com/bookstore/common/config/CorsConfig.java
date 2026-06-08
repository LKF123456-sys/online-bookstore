package com.bookstore.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 跨域配置
 * 允许 Swagger UI 页面（localhost:8086）跨域加载各微服务的 OpenAPI 规范（localhost:8081-8087）
 * 也允许前端开发时跨域访问后端 API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径的跨域访问
                .allowedOriginPatterns("*")  // 允许所有来源域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(false)  // 不发送 Cookie（跨域时使用 * 来源不能同时启用 credentials）
                .maxAge(3600);  // 预检请求缓存时间（秒），减少 OPTIONS 请求次数
    }
}
