package com.bookstore.order.config;  // 声明当前类所在的包路径：配置类

// 导入Spring的Bean注解
import org.springframework.context.annotation.Bean;
// 导入Spring的Configuration注解，标记为配置类
import org.springframework.context.annotation.Configuration;
// 导入Spring Security的HTTP安全配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// 导入Spring Security的Web安全启用注解
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// 导入Spring Security的过滤器链类（Spring Security 6.x的新API）
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security安全配置类
 * 配置该订单服务的安全策略。
 *
 * 本配置的策略是：关闭所有安全限制（全部放行）。
 * 这是因为在微服务架构中，安全认证通常由网关（Gateway）统一处理，
 * 各个微服务内部不再重复做认证校验，而是信任网关传递过来的用户信息。
 */
@Configuration  // 标记为配置类，Spring启动时会加载该类中的配置
@EnableWebSecurity  // 开启Spring Security的Web安全功能
public class SecurityConfig {  // 安全配置类

    /**
     * 配置安全过滤器链
     * 定义了HTTP请求的安全规则：禁用CSRF、放行所有请求、禁用表单登录和HTTP基本认证
     * @param http HttpSecurity对象，用于配置安全规则
     * @return 构建好的安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean  // 将方法返回值注册为Spring Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // 配置安全过滤器链的方法
        http  // 开始链式配置
            .csrf(csrf -> csrf.disable())  // 禁用CSRF（跨站请求伪造）防护，因为是REST API服务，不需要CSRF保护
            .authorizeHttpRequests(auth -> auth  // 配置请求授权规则
                .anyRequest().permitAll()  // 放行所有请求，不做任何权限校验
            )
            .formLogin(form -> form.disable())  // 禁用表单登录页面（不弹出Spring Security默认的登录页）
            .httpBasic(basic -> basic.disable());  // 禁用HTTP基本认证（不弹出浏览器的用户名密码弹窗）

        return http.build();  // 构建并返回安全过滤器链对象
    }
}
