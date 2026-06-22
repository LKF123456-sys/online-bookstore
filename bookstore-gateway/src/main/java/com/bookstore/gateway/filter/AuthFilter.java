package com.bookstore.gateway.filter; // 声明当前类所在的包路径，filter包专门存放网关过滤器相关代码

import io.jsonwebtoken.Claims; // 导入JJWT库的Claims类，用于表示JWT令牌中包含的声明（载荷数据）
import io.jsonwebtoken.Jwts; // 导入JJWT库的核心工具类，用于解析和验证JWT令牌
import io.jsonwebtoken.security.Keys; // 导入JJWT的密钥工具类，用于根据字节数组生成HMAC签名密钥
import lombok.extern.slf4j.Slf4j; // 导入Lombok的日志注解
import org.springframework.beans.factory.annotation.Value; // 导入Spring的@Value注解，用于从配置文件中注入属性值
import org.springframework.cloud.gateway.filter.GatewayFilterChain; // 导入网关过滤器链接口，用于将请求传递给下一个过滤器
import org.springframework.cloud.gateway.filter.GlobalFilter; // 导入全局过滤器接口，实现此接口的过滤器会对所有路由生效
import org.springframework.core.Ordered; // 导入Ordered接口，用于控制过滤器的执行顺序（数值越小优先级越高）
import org.springframework.data.redis.core.ReactiveStringRedisTemplate; // 导入响应式Redis模板，用于检查token黑名单
import org.springframework.http.HttpHeaders; // 导入HTTP请求头常量类，提供标准请求头名称常量如AUTHORIZATION
import org.springframework.http.HttpStatus; // 导入HTTP状态码枚举类，如UNAUTHORIZED(401)、FORBIDDEN(403)
import org.springframework.http.server.reactive.ServerHttpRequest; // 导入响应式HTTP请求对象，用于读取请求信息（路径、头信息等）
import org.springframework.http.server.reactive.ServerHttpResponse; // 导入响应式HTTP响应对象，用于构建和发送响应
import org.springframework.stereotype.Component; // 导入Spring组件注解，标记该类为Spring管理的Bean，自动注册到容器中
import org.springframework.web.server.ServerWebExchange; // 导入ServerWebExchange对象，封装了请求和响应的上下文信息
import reactor.core.publisher.Mono; // 导入Project Reactor的Mono类型，表示一个异步的0或1个元素的响应式流

import javax.crypto.SecretKey; // 导入Java加密API的SecretKey接口，表示对称加密的密钥
import java.nio.charset.StandardCharsets; // 导入标准字符集类，提供UTF_8等编码常量，避免编码字符串的歧义
import java.util.List; // 导入Java集合框架的List接口，用于存储白名单路径和管理员路径列表

/**
 * 网关认证过滤器
 * 职责：
 * 1. 白名单路径直接放行（登录、注册、公开查询接口）
 * 2. 使用 JJWT 对 JWT Token 进行签名验证和信息提取
 * 3. 将 userId、role 注入请求头传递给下游微服务
 * 4. 对 admin 接口进行角色校验（RBAC）
 */
@Slf4j // 启用Lombok日志，用于记录降级和异常情况
@Component // Spring组件注解，将该过滤器自动注册为Spring容器中的Bean，使其对所有网关路由生效
public class AuthFilter implements GlobalFilter, Ordered { // 实现GlobalFilter接口（全局过滤器）和Ordered接口（控制执行顺序）

    /**
     * 无需认证的白名单路径
     * 这些路径允许未登录用户直接访问，包括登录、注册、商品浏览等公开接口
     */
    private static final List<String> WHITE_LIST = List.of( // 定义不可变的白名单路径列表，static final确保全局唯一且不可修改
            "/api/auth/login", "/api/auth/register", // 登录接口和注册接口，未登录用户必须可以访问
            "/swagger-ui.html", "/swagger-ui/", "/v3/api-docs", "/webjars/", // Swagger UI 文档接口
            "/api/product/list", "/api/product/recommend", "/api/product/hot", // 商品列表、推荐商品、热门商品等公开浏览接口
            "/api/product/detail", "/api/category/list", "/api/announcement/active", // 商品详情、分类列表、活动公告等公开查询接口
            "/api/products", "/api/products/search", "/api/products/categories", "/api/products/recommend", "/api/products/hot", // 复数路径公开商品浏览接口
            "/api/search", "/api/coupon/list", "/api/coupons", // 搜索接口和优惠券列表接口，允许匿名访问
            "/api/review/product/", "/api/reviews/product/", // 商品评论查询接口，任何人可以查看评论
            "/actuator" // Spring Boot健康检查端点，用于运维监控
    );

    /**
     * 需要 admin 角色的路径前缀
     * 以这些前缀开头的路径要求用户必须具有管理员角色
     */
    private static final List<String> ADMIN_PATHS = List.of( // 定义需要管理员权限的路径前缀列表
            "/admin/" // 管理后台的所有接口前缀，需要admin角色才能访问
    );

    /** Redis 黑名单 key 前缀 */
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Value("${jwt.secret}") // 从配置文件注入JWT签名密钥，必须配置，无默认值
    private String jwtSecret; // JWT签名密钥字符串，用于验证Token的完整性

    private final ReactiveStringRedisTemplate redisTemplate; // 响应式Redis模板，用于检查token黑名单

    /**
     * 构造函数注入 Redis 模板
     * @param redisTemplate 响应式字符串Redis模板
     */
    public AuthFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据密钥字符串生成HMAC签名密钥对象
     * @return SecretKey HMAC-SHA签名密钥，用于JWT令牌的签名验证
     */
    /**
     * 提取 JWT Token（优先从 Cookie 读取，兼容 Authorization header）
     * @param request 请求对象
     * @return JWT Token，如果不存在则返回 null
     */
    private String extractToken(ServerHttpRequest request) {
        // 优先从 Cookie 读取
        String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieHeader != null) {
            for (String ck : cookieHeader.split(";")) {
                String[] parts = ck.trim().split("=", 2);
                if (parts.length == 2 && "BOOKSTORE_TOKEN".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        // 兼容 Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 根据密钥字符串生成HMAC签名密钥对象
     * @return SecretKey HMAC-SHA签名密钥，用于JWT令牌的签名验证
     */
    private SecretKey getSigningKey() { // 私有方法，将字符串密钥转换为JJWT所需的SecretKey对象
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)); // 将密钥字符串转为UTF-8字节数组，生成HMAC-SHA密钥
    }

    /**
     * 网关全局过滤器的核心方法，所有请求都会经过此方法处理
     * @param exchange 请求和响应的上下文对象，包含完整的HTTP请求/响应信息
     * @param chain 过滤器链，调用chain.filter()将请求传递给下一个过滤器或目标服务
     * @return Mono<Void> 响应式的空返回值，表示异步处理完成
     */
    @Override // 重写GlobalFilter接口的filter方法
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) { // 全局过滤器的主逻辑方法
        ServerHttpRequest request = exchange.getRequest(); // 从上下文中获取当前HTTP请求对象
        String path = request.getURI().getPath(); // 提取请求的URI路径部分，如 /api/product/list

        // 1. 白名单路径直接放行
        if (isWhiteListed(path)) { // 判断当前请求路径是否在白名单中
            return chain.filter(exchange); // 白名单路径无需认证，直接传递给下一个过滤器
        }

        // 2. 提取 JWT Token（优先从 Cookie 读取，兼容 Authorization header）
        final String token = extractToken(request);
        if (token == null) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        Claims claims; // 声明JWT声明（载荷）变量，用于存储解析后的Token数据
        try {
            claims = Jwts.parser() // 创建JWT解析器构建器
                    .verifyWith(getSigningKey()) // 设置用于验证签名的HMAC密钥
                    .build() // 构建JWT解析器实例
                    .parseSignedClaims(token) // 解析并验证JWT令牌的签名和有效性
                    .getPayload(); // 提取JWT的载荷部分（包含用户信息等声明）
        } catch (Exception e) { // 捕获所有解析异常，包括令牌过期、签名无效、格式错误等
            return unauthorized(exchange, "Invalid or expired token"); // Token验证失败，返回401未授权响应
        }

        // 2.5 检查 Token 是否在黑名单中（已登出的 token）— 完全响应式实现
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token)
                .defaultIfEmpty(false)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, "Token has been revoked");
                    }
                    // 3. 提取用户信息
                    return processClaims(exchange, chain, claims, token);
                })
                .onErrorResume(e -> {
                    // Redis 不可用时降级放行，但记录告警日志
                    log.warn("Redis 黑名单检查失败，降级为放行模式: {}", e.getMessage());
                    return processClaims(exchange, chain, claims, token);
                });
    }

    /**
     * 处理 JWT Claims：提取用户信息、RBAC 校验、注入请求头
     */
    private Mono<Void> processClaims(ServerWebExchange exchange, GatewayFilterChain chain, Claims claims, String token) {
        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);

        if (userId == null || userId.isEmpty()) {
            return unauthorized(exchange, "Token missing userId claim");
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isAdminPath(path) && !"admin".equals(role)) {
            return forbidden(exchange, "Admin access required");
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role != null ? role : "user")
                .header("X-Auth-Token", token)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 判断请求路径是否在白名单中
     * @param path 请求的URI路径
     * @return true表示路径在白名单中（无需认证），false表示需要认证
     */
    private boolean isWhiteListed(String path) { // 私有方法，检查路径是否匹配白名单
        for (String whitePath : WHITE_LIST) { // 遍历白名单中的每一个路径
            if (path.startsWith(whitePath) || path.equals(whitePath)) { // 判断请求路径是否以白名单路径开头或完全相等
                return true; // 匹配成功，该路径无需认证
            }
        }
        return false; // 遍历完所有白名单路径都未匹配，该路径需要认证
    }

    /**
     * 判断请求路径是否为管理员接口路径
     * @param path 请求的URI路径
     * @return true表示是管理员路径（需要admin角色），false表示不是
     */
    private boolean isAdminPath(String path) { // 私有方法，检查路径是否属于管理后台
        for (String adminPath : ADMIN_PATHS) { // 遍历管理员路径前缀列表
            if (path.startsWith(adminPath)) { // 判断请求路径是否以管理员路径前缀开头
                return true; // 匹配成功，该路径需要admin角色
            }
        }
        return false; // 遍历完所有管理员路径前缀都未匹配，该路径不需要特殊角色
    }

    /**
     * 返回401未授权的JSON响应
     * @param exchange 请求上下文对象
     * @param message 错误描述信息，将包含在响应体中
     * @return Mono<Void> 响应式空返回值，表示响应已写入完成
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) { // 私有方法，构建401未授权响应
        ServerHttpResponse response = exchange.getResponse(); // 从上下文中获取HTTP响应对象
        response.setStatusCode(HttpStatus.UNAUTHORIZED); // 设置HTTP状态码为401（未授权）
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8"); // 设置响应内容类型为JSON，字符编码为UTF-8
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}"; // 拼接JSON格式的响应体，包含状态码、错误消息和空数据
        return response.writeWith( // 将响应体写入HTTP响应流
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))) // 将JSON字符串转为UTF-8字节数组，再包装为数据缓冲区，通过Mono发出
        );
    }

    /**
     * 返回403禁止访问的JSON响应
     * @param exchange 请求上下文对象
     * @param message 错误描述信息，将包含在响应体中
     * @return Mono<Void> 响应式空返回值，表示响应已写入完成
     */
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) { // 私有方法，构建403禁止访问响应
        ServerHttpResponse response = exchange.getResponse(); // 从上下文中获取HTTP响应对象
        response.setStatusCode(HttpStatus.FORBIDDEN); // 设置HTTP状态码为403（禁止访问）
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8"); // 设置响应内容类型为JSON，字符编码为UTF-8
        String body = "{\"code\":403,\"message\":\"" + message + "\",\"data\":null}"; // 拼接JSON格式的响应体，包含状态码、错误消息和空数据
        return response.writeWith( // 将响应体写入HTTP响应流
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))) // 将JSON字符串转为UTF-8字节数组，再包装为数据缓冲区，通过Mono发出
        );
    }

    /**
     * 获取过滤器的执行顺序
     * 数值越小优先级越高，-100确保认证过滤器在大多数其他过滤器之前执行
     * @return 过滤器排序值，-100表示高优先级
     */
    @Override // 重写Ordered接口的getOrder方法
    public int getOrder() { // 返回过滤器的执行优先级顺序
        return -100; // 返回-100，确保认证过滤器优先执行，在路由转发之前完成身份验证
    }
}
