package com.bookstore.promotion.config;  // 声明当前类所在的包路径，属于营销服务的配置层

import org.springframework.context.annotation.Bean;  // 导入Spring的Bean注解，用于将方法返回值注册为Spring Bean
import org.springframework.context.annotation.Configuration;  // 导入Spring的Configuration注解，标记为配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // 导入Spring Security的HTTP安全构建器，用于配置安全规则
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 导入启用Web安全注解，开启Spring Security功能
import org.springframework.security.web.SecurityFilterChain;  // 导入安全过滤器链，定义安全过滤的规则链

/**
 * Spring Security安全配置类
 * 营销服务的安全配置，主要功能：
 *   - 禁用CSRF防护（微服务间调用不需要CSRF）
 *   - 放行所有请求（本服务的鉴权由网关统一处理）
 *   - 禁用表单登录和HTTP Basic认证（使用无状态的Token认证方式）
 *
 * 注意：实际的用户身份认证和鉴权在API网关层完成
 * 本服务只是简单地放行所有请求，信任网关传递过来的用户信息
 */
@Configuration  // 标记为Spring配置类，Spring容器启动时会加载此配置
@EnableWebSecurity  // 开启Spring Security的Web安全功能
public class SecurityConfig {  // 安全配置类

    /**
     * 配置安全过滤器链
     * 定义了本服务的HTTP安全规则：
     *   1. 禁用CSRF：微服务内部调用不需要CSRF保护
     *   2. 放行所有请求：鉴权由网关层统一处理
     *   3. 禁用表单登录：使用Token认证，不需要表单登录页
     *   4. 禁用HTTP Basic认证：使用Token认证，不需要Basic认证
     *
     * @param http Spring Security的HTTP安全构建器
     * @return 配置好的安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean  // 将方法返回值注册为Spring Bean，Spring Security会自动使用此过滤器链
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // 配置安全过滤器链的方法
        http
            .csrf(csrf -> csrf.disable())  // 禁用CSRF（跨站请求伪造）保护，因为微服务间调用使用Token认证，不需要CSRF
            .authorizeHttpRequests(auth -> auth  // 配置HTTP请求的授权规则
                .anyRequest().permitAll()  // 放行所有请求，不做任何权限校验（鉴权交给网关处理）
            )
            .formLogin(form -> form.disable())  // 禁用表单登录功能（不需要登录页面）
            .httpBasic(basic -> basic.disable());  // 禁用HTTP Basic认证（使用Token认证方式）

        return http.build();  // 构建并返回安全过滤器链对象
    }
}
