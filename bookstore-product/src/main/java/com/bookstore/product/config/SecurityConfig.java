package com.bookstore.product.config;  // 声明当前类所在的包路径，属于商品服务的配置层

import org.springframework.context.annotation.Bean;  // 导入Spring的@Bean注解，用于将方法返回值注册为Spring容器中的Bean
import org.springframework.context.annotation.Configuration;  // 导入Spring的@Configuration注解，标记当前类为配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // 导入Spring Security的HTTP安全配置构建器
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 导入Spring Security的启用安全注解
import org.springframework.security.web.SecurityFilterChain;  // 导入Spring Security的过滤器链接口，定义安全过滤规则

/**
 * Spring Security安全配置类
 * 商品服务的安全配置，由于商品服务主要提供查询接口，
 * 因此这里将所有请求都设为允许访问（permitAll），
 * 实际的权限控制由网关层统一处理
 */
@Configuration  // 标记为配置类，Spring启动时会自动加载该配置
@EnableWebSecurity  // 启用Spring Security的安全功能
public class SecurityConfig {  // 安全配置类

    /**
     * 配置安全过滤器链
     * 定义了HTTP请求的安全规则，包括CSRF、授权、登录方式等
     * 此处禁用了所有安全限制，因为安全性由API网关统一管控
     *
     * @param http Spring Security的HTTP安全构建器，用于链式配置安全规则
     * @return 配置好的安全过滤器链对象
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean  // 将方法返回值注册为Spring容器中的Bean，Spring Security会自动使用该过滤器链
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // 定义安全过滤器链的配置方法
        http  // 开始链式配置HTTP安全规则
            .csrf(csrf -> csrf.disable())  // 禁用CSRF（跨站请求伪造）防护，因为是REST API服务，使用Token认证而非Cookie
            .authorizeHttpRequests(auth -> auth  // 配置HTTP请求的授权规则
                .anyRequest().permitAll()  // 允许所有请求访问，不做权限校验（权限控制由网关层处理）
            )
            .formLogin(form -> form.disable())  // 禁用表单登录功能（前后端分离项目不使用表单登录）
            .httpBasic(basic -> basic.disable());  // 禁用HTTP Basic认证（使用JWT Token认证方式）

        return http.build();  // 构建并返回安全过滤器链对象
    }
}
