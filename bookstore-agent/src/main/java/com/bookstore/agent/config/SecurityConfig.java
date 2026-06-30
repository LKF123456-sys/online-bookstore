package com.bookstore.agent.config; // config 包：存放 Spring Boot 配置类

// Spring 的 @Bean 注解 — 标记方法返回值为 Spring 管理的 Bean
import org.springframework.context.annotation.Bean;
// Spring 的 @Configuration 注解 — 标记此类为配置类
import org.springframework.context.annotation.Configuration;
// Spring Security 的 HttpSecurity — 安全配置的流式 API 构建器
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Spring Security 的 @EnableWebSecurity — 启用 Web 安全配置
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Spring Security 的 SecurityFilterChain — 过滤器链 Bean，定义安全规则
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 *
 * 安全策略：完全信任网关层，内网服务间免认证
 *
 * 为什么禁用 CSRF 和认证？
 *   这是一个微服务架构中的内部服务，所有外部请求先经过 bookstore-gateway 网关。
 *   网关层的 AuthFilter 已完成 JWT Token 验证和用户身份解析，
 *   通过 X-User-Id、X-User-Role 等请求头将用户信息透传给下游服务。
 *   内网服务之间的通信通过 Docker 内部网络或 K8s Service，无需重复认证。
 *
 * 安全层级分工：
 *   ┌─────────────┐     ┌────────────────┐     ┌─────────────────┐
 *   │   浏览器/App  │ ──▶ │  Gateway 网关   │ ──▶ │  bookstore-agent │
 *   │  (JWT Token) │     │  认证 + 解析    │     │  信任请求头      │
 *   └─────────────┘     └────────────────┘     └─────────────────┘
 *
 * 安全边界说明：
 *   1. CSRF 禁用：RESTful API 使用 JWT 认证，不依赖 Cookie-Session 机制
 *   2. 所有请求放行：内网服务间调用无需额外认证
 *   3. Form Login 禁用：不使用传统表单登录，前端 SPA 通过 JWT 管理会话
 *   4. HTTP Basic 禁用：不使用基本认证
 *
 * 注意：此配置仅适用于内网部署场景。如果 Agent 服务需要直接暴露到公网，
 *   必须重新启用认证机制（JWT Filter 或 OAuth2 Resource Server）。
 */
@Configuration // Spring：标记为配置类
@EnableWebSecurity // Spring Security：启用 Web 安全，激活 HttpSecurity 配置
public class SecurityConfig { // 安全配置类

    /**
     * SecurityFilterChain Bean — 定义安全过滤规则
     *
     * @param http Spring Security 注入的 HttpSecurity 构建器，用于配置安全规则
     * @return SecurityFilterChain 过滤器链
     * @throws Exception 配置异常时抛出
     */
    @Bean // 注册 SecurityFilterChain Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { // 安全过滤器链工厂方法
        http // 开始配置 HttpSecurity
            .csrf(csrf -> csrf.disable()) // 禁用 CSRF 保护：RESTful API 不基于 Cookie，无需 CSRF 防御
            .authorizeHttpRequests(auth -> auth // 配置请求授权规则
                .anyRequest().permitAll() // 所有请求无条件放行：信任网关已做认证
            )
            .formLogin(form -> form.disable()) // 禁用表单登录：不使用 Spring Security 的默认登录页面
            .httpBasic(basic -> basic.disable()); // 禁用 HTTP Basic 认证：不使用用户名/密码的认证方式

        return http.build(); // 构建并返回 SecurityFilterChain
    }
}
