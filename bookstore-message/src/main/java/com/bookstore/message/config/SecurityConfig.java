package com.bookstore.message.config;  // 声明当前类所属的包路径，属于消息服务的配置层

import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解，用于向容器注册一个Bean实例
import org.springframework.context.annotation.Configuration;  // 导入Spring的配置类注解，标记这是一个配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // 导入Spring Security的HTTP安全构建器，用于配置安全规则
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 导入启用Web安全的注解
import org.springframework.security.web.SecurityFilterChain;  // 导入安全过滤器链，Spring Security的核心配置接口

/**
 * Spring Security安全配置类
 * 配置消息服务的安全策略
 *
 * 由于消息服务的认证和鉴权由网关统一处理，
 * 本服务内部不做额外的安全校验，所有请求直接放行
 * 同时禁用了CSRF、表单登录和HTTP基本认证
 */
@Configuration  // 标记为Spring配置类，Spring启动时会自动加载此配置
@EnableWebSecurity  // 启用Spring Security的Web安全功能
public class SecurityConfig {  // 安全配置类

    /**
     * 配置安全过滤器链
     * 定义HTTP请求的安全策略，包括哪些请求需要认证、哪些可以直接访问
     * 此处配置为所有请求都放行，因为认证由网关统一处理
     *
     * @param http HTTP安全构建器，用于配置安全规则
     * @return 配置好的安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean  // 将方法返回值注册为Spring容器中的一个Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // 定义安全过滤器链的方法
        http  // 使用链式调用配置HTTP安全策略
            .csrf(csrf -> csrf.disable())  // 禁用CSRF（跨站请求伪造）保护，因为是微服务API，不需要CSRF防护
            .authorizeHttpRequests(auth -> auth  // 配置请求授权规则
                .anyRequest().permitAll()  // 允许所有请求通过，不做权限校验（认证由网关统一处理）
            )
            .formLogin(form -> form.disable())  // 禁用表单登录功能，本服务使用JWT令牌认证，不需要表单登录页
            .httpBasic(basic -> basic.disable());  // 禁用HTTP Basic认证，本服务不使用用户名/密码的Basic认证方式

        return http.build();  // 构建并返回安全过滤器链对象
    }
}
