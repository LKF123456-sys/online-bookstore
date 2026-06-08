package com.bookstore.user.config;  // 声明当前类所在的包路径，这里是配置层

import org.springframework.context.annotation.Bean;  // 导入@Bean注解，用于将方法的返回值注册为Spring容器中的一个Bean
import org.springframework.context.annotation.Configuration;  // 导入@Configuration注解，标记该类为Spring的配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // 导入HttpSecurity构建器，用于配置HTTP安全策略
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 导入@EnableWebSecurity注解，启用Spring Security的Web安全功能
import org.springframework.security.web.SecurityFilterChain;  // 导入安全过滤器链接口，定义了请求经过的安全过滤规则

/**
 * Spring Security 安全配置类
 * 用于配置HTTP请求的安全策略，包括哪些URL需要认证、哪些可以公开访问等。
 * <p>
 * 当前配置将所有请求都设为允许访问（permitAll），即不进行安全拦截。
 * 这是因为该微服务项目通过网关（Gateway）统一进行JWT Token认证和权限校验，
 * 用户服务本身不再重复进行安全拦截，避免双重校验。
 * <p>
 * 注解说明：
 * - @Configuration：标记为Spring的配置类，等同于一个Spring的XML配置文件
 * - @EnableWebSecurity：启用Spring Security的Web安全功能
 * - @Bean：将方法返回的SecurityFilterChain对象注册到Spring容器中
 */
@Configuration  // 配置类注解，告诉Spring这是一个配置类，其中可以定义@Bean方法来注册组件
@EnableWebSecurity  // 启用Spring Security的Web安全功能
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 定义了HTTP请求的安全规则：哪些URL需要认证、哪些可以公开访问等。
     * 当前配置为全部放行（permitAll），因为实际的认证由API网关统一处理。
     *
     * @param http HttpSecurity对象，由Spring Security自动注入，用于构建安全配置
     * @return 配置好的SecurityFilterChain对象
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean  // 将方法返回值注册为Spring容器中的Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // 定义安全过滤器链的方法
        http
            .csrf(csrf -> csrf.disable())  // 禁用CSRF（跨站请求伪造）保护，因为REST API通常使用Token认证而非Cookie，不需要CSRF保护
            .authorizeHttpRequests(auth -> auth  // 配置HTTP请求的授权规则
                .requestMatchers(  // 指定需要配置访问规则的URL路径
                    "/api/auth/**",  // 所有认证相关接口（登录、注册等）放行
                    "/actuator/**"  // 所有Actuator监控端点放行（健康检查、指标等）
                ).permitAll()  // 上述路径允许所有人访问，无需认证
                .anyRequest().permitAll()  // 所有其他请求也放行（全局放行，认证交给网关处理）
            )
            .formLogin(form -> form.disable())  // 禁用Spring Security自带的表单登录页面（因为使用前后端分离的Token认证方式）
            .httpBasic(basic -> basic.disable());  // 禁用HTTP Basic认证（因为使用JWT Token认证方式）

        return http.build();  // 构建并返回配置好的安全过滤器链对象
    }
}
