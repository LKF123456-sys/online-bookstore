package com.bookstore.admin.controller; // 声明当前类所在的包路径：控制器层

// 导入Swagger/OpenAPI注解，用于生成API文档
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// 导入Lombok的@RequiredArgsConstructor注解，自动生成构造方法
import lombok.RequiredArgsConstructor;
// 导入Spring Cloud负载均衡注解，使RestTemplate支持服务名调用
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// 导入Spring配置相关注解
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 导入Spring HTTP相关类
import org.springframework.http.*;
// 导入Spring Web注解
import org.springframework.web.bind.annotation.*;
// 导入RestTemplate，用于发送HTTP请求
import org.springframework.web.client.RestTemplate;

// 导入Jakarta Servlet的请求接口
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证代理控制器 - 负责将特定URL前缀的请求转发到对应的微服务
 *
 * 这个控制器充当"反向代理"角色：
 * - /api/auth/** 请求转发到 bookstore-user（用户服务）
 * - /api/coupon/** 请求转发到 bookstore-promotion（促销服务）
 * - /message/** 请求转发到 bookstore-message（消息服务）
 *
 * 为什么需要代理？
 * 管理后台作为BFF（Backend For Frontend）层，前端只需要访问管理后台的地址，
 * 由管理后台负责将不同类型的请求转发到对应的微服务，简化前端的调用逻辑。
 */
@RestController // 标记这是一个REST控制器
@RequiredArgsConstructor // 自动生成构造方法（依赖注入RestTemplate）
@Tag(name = "认证代理", description = "用户认证转发、微服务间调用代理") // Swagger分组
public class AuthProxyController {

    // RestTemplate用于向其他微服务发送HTTP请求（已配置负载均衡）
    private final RestTemplate restTemplate;

    /**
     * 用户认证代理接口
     * 将所有 /api/auth/** 请求转发至 bookstore-user（用户服务）
     * 例如：/api/auth/login -> bookstore-user/api/auth/login
     *
     * @param request 原始HTTP请求对象，用于获取请求路径、方法、参数等信息
     * @param body 请求体内容（可选，GET请求没有请求体）
     * @return 转发后的响应结果
     */
    @Operation(summary = "用户认证代理", description = "将所有 /api/auth/** 请求转发至 bookstore-user 服务")
    @RequestMapping("/api/auth/**") // 匹配所有以/api/auth/开头的请求路径
    public ResponseEntity<String> proxyAuth(HttpServletRequest request, @RequestBody(required = false) String body) {
        // 调用通用代理方法，将请求转发到用户服务
        return proxyToService(request, body, "bookstore-user");
    }

    /**
     * 优惠券代理接口
     * 将所有 /api/coupon/** 请求转发至 bookstore-promotion（促销服务）
     *
     * @param request 原始HTTP请求
     * @param body 请求体（可选）
     * @return 转发后的响应
     */
    @Operation(summary = "优惠券代理", description = "将所有 /api/coupon/** 请求转发至 bookstore-promotion 服务")
    @RequestMapping("/api/coupon/**") // 匹配所有以/api/coupon/开头的请求
    public ResponseEntity<String> proxyCoupon(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyToService(request, body, "bookstore-promotion");
    }

    /**
     * 消息代理接口
     * 将所有 /message/** 请求转发至 bookstore-message（消息服务）
     *
     * @param request 原始HTTP请求
     * @param body 请求体（可选）
     * @return 转发后的响应
     */
    @Operation(summary = "消息代理", description = "将所有 /message/** 请求转发至 bookstore-message 服务")
    @RequestMapping("/message/**") // 匹配所有以/message/开头的请求
    public ResponseEntity<String> proxyMessage(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyToService(request, body, "bookstore-message");
    }

    /**
     * 通用代理转发方法
     * 将原始请求的所有信息（路径、查询参数、方法、请求体）转发到目标微服务
     *
     * @param request 原始HTTP请求
     * @param body 请求体内容
     * @param serviceName 目标微服务的服务名（通过Nacos服务发现解析为实际地址）
     * @return 目标服务的响应结果
     */
    private ResponseEntity<String> proxyToService(HttpServletRequest request, String body, String serviceName) {
        String path = request.getRequestURI(); // 获取请求路径（如/api/auth/login）
        String queryString = request.getQueryString(); // 获取查询参数字符串（如pageNum=1&pageSize=10）
        // 拼接完整的目标URL：http://服务名/路径?查询参数
        String targetUrl = "http://" + serviceName + path + (queryString != null ? "?" + queryString : "");

        // 创建HTTP请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // 设置内容类型为JSON
        // 将请求体和请求头封装为HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // 将原始请求的HTTP方法（GET/POST/PUT/DELETE等）转换为HttpMethod枚举
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            // 使用RestTemplate向目标服务发送请求，并获取响应
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);
            // 将目标服务的响应状态码和响应体返回给调用方
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            // 转发失败时，返回500错误和错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"code\":500,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
        }
    }

    /**
     * RestTemplate配置类（内部静态类）
     * 创建并配置RestTemplate Bean，使其支持通过服务名进行负载均衡调用
     * 例如：http://bookstore-user/api/auth/login 会被自动解析为实际的服务地址
     */
    @Configuration // 标记这是一个Spring配置类
    static class RestTemplateConfig {
        /**
         * 创建RestTemplate Bean
         * @LoadBalanced注解使RestTemplate具备客户端负载均衡能力，
         * 能够通过Nacos注册的服务名自动解析为实际的服务实例地址
         */
        @Bean // 将方法返回值注册为Spring Bean
        @LoadBalanced // 启用客户端负载均衡
        public RestTemplate restTemplate() {
            return new RestTemplate(); // 创建RestTemplate实例
        }
    }
}
