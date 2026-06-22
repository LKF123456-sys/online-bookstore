package com.bookstore.admin.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * SPA 重定向过滤器
 * <p>
 * 将非 API、非静态资源的 GET 请求重定向到 Vue 前端。
 * 使用 Filter 而非 @Controller 的好处是：Filter 在 Servlet 层面工作，
 * 不经过 Spring MVC 的 HandlerMapping，不会拦截静态资源请求。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SpaRedirectFilter implements Filter {

    @Value("${bookstore.user-frontend.url}")
    private String userFrontendUrl;

    private static final Set<String> API_PREFIXES = Set.of("/api/", "/admin/");
    private static final Set<String> STATIC_PREFIXES = Set.of(
            "/img/", "/css/", "/js/", "/static/", "/webjars/", "/v3/api-docs",
            "/swagger-ui", "/favicon.", "/actuator"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();

        for (String prefix : API_PREFIXES) {
            if (path.startsWith(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        for (String prefix : STATIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        resp.sendRedirect(userFrontendUrl + path);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
