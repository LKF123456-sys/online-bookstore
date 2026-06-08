package com.bookstore.admin.config; // 声明当前类所在的包路径：配置层

// 导入日志拦截器，用于记录管理后台操作日志
import com.bookstore.admin.interceptor.LogInterceptor;
// 导入Lombok的@RequiredArgsConstructor注解
import lombok.RequiredArgsConstructor;
// 导入Spring的@Bean注解，用于注册Spring Bean
import org.springframework.context.annotation.Bean;
// 导入Spring的@Configuration注解，标记这是一个配置类
import org.springframework.context.annotation.Configuration;
// 导入Spring Security的HTTP安全配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// 导入Spring Security的启用Web安全注解
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// 导入Spring Security的过滤器链类
import org.springframework.security.web.SecurityFilterChain;
// 导入Spring MVC的拦截器注册类
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// 导入Spring MVC的WebMvc配置接口
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全配置类 - 同时配置Spring Security和Spring MVC拦截器
 *
 * 这个配置类做了两件事：
 * 1. 配置Spring Security：禁用CSRF防护（因为是管理后台，使用Session认证）、
 *    允许所有请求通过（不使用Spring Security的认证机制，而是在Controller层自行处理）
 * 2. 注册日志拦截器：拦截管理后台的所有操作请求，自动记录操作日志
 */
@Configuration // 标记这是一个Spring配置类
@EnableWebSecurity // 启用Spring Security的Web安全功能
@RequiredArgsConstructor // 使用Lombok自动生成构造方法（注入LogInterceptor）
public class SecurityConfig implements WebMvcConfigurer { // 实现WebMvcConfigurer接口以自定义MVC配置

    // 注入日志拦截器
    private final LogInterceptor logInterceptor;

    /**
     * 配置Spring Security的过滤器链
     * 本项目采用Session认证方式（而非Spring Security内置认证），
     * 所以这里基本禁用了Spring Security的所有安全措施，让它"放行"所有请求。
     * 真正的权限控制在Controller层通过Session判断实现。
     *
     * @param http HttpSecurity配置对象
     * @return 配置好的SecurityFilterChain
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean // 注册为Spring Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用CSRF防护（管理后台使用Session认证，不需要CSRF令牌）
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // 允许同源的iframe嵌入（管理后台可能使用iframe）
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 允许所有请求通过，不做Spring Security层面的权限校验
            )
            .formLogin(form -> form.disable()) // 禁用Spring Security的默认登录表单（使用自定义登录页面）
            .httpBasic(basic -> basic.disable()); // 禁用HTTP Basic认证

        return http.build(); // 构建并返回过滤器链
    }

    /**
     * 配置Spring MVC拦截器
     * 将日志拦截器注册到Spring MVC的拦截器链中，
     * 对管理后台的所有请求（/admin/**）进行拦截记录日志，
     * 但排除登录页面（/admin/login）和API接口（/admin/api/**）
     *
     * @param registry 拦截器注册对象
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor) // 注册日志拦截器
                .addPathPatterns("/admin/**") // 拦截所有/admin/开头的请求
                .excludePathPatterns("/admin/login", "/admin/api/**"); // 排除登录页面和API接口
    }
}
