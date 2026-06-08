package com.bookstore.common.security;  // 声明当前类所属的包路径

import jakarta.servlet.*;  // 导入Servlet核心接口
import jakarta.servlet.http.HttpServletRequest;  // 导入HTTP请求对象
import org.springframework.core.annotation.Order;  // 导入Spring的@Order注解，用于指定过滤器执行顺序
import org.springframework.stereotype.Component;  // 导入Spring的@Component注解

import java.io.IOException;  // 导入IO异常类

/**
 * 用户ID过滤器
 * 从请求头中提取用户ID，并设置到请求属性中
 * 供Controller通过@RequestAttribute注解获取当前用户ID
 */
@Component  // Spring注解，将该类注册为Spring Bean
@Order(1)  // 指定过滤器执行顺序为1（最先执行）
public class UserIdFilter implements Filter {  // 实现Servlet Filter接口

    /**
     * 过滤器核心方法
     * @param request Servlet请求对象
     * @param response Servlet响应对象
     * @param chain 过滤器链，调用doFilter继续执行后续过滤器
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override  // 重写Filter接口的doFilter方法
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  // 过滤器方法
            throws IOException, ServletException {  // 声明可能抛出的异常
        HttpServletRequest httpRequest = (HttpServletRequest) request;  // 将ServletRequest转换为HttpServletRequest
        String userId = httpRequest.getHeader("X-User-Id");  // 从请求头中获取"X-User-Id"
        if (userId != null && !userId.isEmpty()) {  // 判断用户ID是否为空
            // 将userId设置为request attribute，供@Controller使用@RequestAttribute获取
            httpRequest.setAttribute("userId", userId);  // 将用户ID设置到请求属性中
        }
        chain.doFilter(request, response);  // 继续执行后续的过滤器或目标资源
    }
}
