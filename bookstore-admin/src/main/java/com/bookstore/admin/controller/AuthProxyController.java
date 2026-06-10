package com.bookstore.admin.controller; // 声明当前类所属的包路径，遵循Spring Boot项目标准分包结构

// 导入Swagger/OpenAPI的@Operation注解，用于描述API接口的功能说明
import io.swagger.v3.oas.annotations.Operation;
// 导入Swagger/OpenAPI的@Tag注解，用于对接口进行分组归类
import io.swagger.v3.oas.annotations.tags.Tag;
// 导入Lombok的@RequiredArgsConstructor注解，为所有final字段生成构造函数
import lombok.RequiredArgsConstructor;
// 导入Spring Cloud的@LoadBalanced注解，启用RestTemplate的服务发现与负载均衡能力
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// 导入Spring的@Bean注解，用于注册Spring Bean
import org.springframework.context.annotation.Bean;
// 导入Spring的@Configuration注解，标记这是一个配置类
import org.springframework.context.annotation.Configuration;
// 导入Spring HTTP相关的类：HttpHeaders（请求头）、HttpMethod（请求方法）、MediaType（媒体类型）、ResponseEntity（响应实体）
import org.springframework.http.*;
// 导入Spring MVC的@RestController和@RequestMapping等注解
import org.springframework.web.bind.annotation.*;
// 导入RestTemplate，用于向其他微服务发送HTTP请求
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest; // 导入Jakarta Servlet的HTTP请求接口

/**
 * 微服务代理控制器
 * 负责将前端请求转发到对应的微服务实例
 *
 * 工作原理：
 * 当前端请求的路径在本服务中没有专属的REST Controller处理时，
 * 由本控制器统一拦截并通过RestTemplate转发到对应的微服务。
 * 使用LoadBalanced RestTemplate，通过Nacos服务名自动解析实际的IP:Port。
 *
 * 注意：仅代理"没有专属REST Controller"的路径，避免与已有Controller产生映射冲突。
 * 异常时返回HTTP 503（服务不可用），而非500，让前端优雅降级。
 */
@RestController // 标记这是一个REST控制器，返回值直接作为HTTP响应体
@RequiredArgsConstructor // 使用Lombok自动生成包含final字段的构造方法（依赖注入）
@Tag(name = "微服务代理", description = "将特定路径请求转发至对应微服务") // Swagger文档分组标签
public class AuthProxyController {

    // 注入RestTemplate实例（已在内部配置类中标记@LoadBalanced，支持服务发现和负载均衡）
    private final RestTemplate restTemplate;

    /**
     * 消息服务代理
     * 将所有/api/message/**请求转发到bookstore-message微服务
     * 如果微服务不可用，返回空数据而非500，让前端优雅降级
     *
     * @param request HTTP请求对象，包含原始请求的所有信息（路径、方法、头信息等）
     * @param body    请求体内容（可选），POST/PUT请求时包含JSON数据
     * @return ResponseEntity 包装的响应字符串，原样转发下游微服务的响应
     */
    // @Operation：Swagger文档描述接口功能
    @Operation(summary = "消息代理", description = "将所有 /api/message/** 请求转发至 bookstore-message 服务")
    // @RequestMapping：将HTTP请求映射到/api/message/**路径，**匹配任意子路径
    @RequestMapping("/api/message/**")
    public ResponseEntity<String> proxyApiMessage(HttpServletRequest request, @RequestBody(required = false) String body) {
        // 调用通用代理方法，将请求转发到bookstore-message微服务
        return proxyToService(request, body, "bookstore-message");
    }

    /**
     * 执行微服务代理转发的核心方法
     * 通过LoadBalanced RestTemplate将请求转发到Nacos注册的微服务实例
     * 异常时返回HTTP 503（服务不可用），不再返回500
     *
     * @param request     原始HTTP请求对象，用于获取请求路径、方法、查询参数等
     * @param body        请求体内容（可选）
     * @param serviceName 目标微服务的服务名（对应Nacos注册中心中的服务名）
     * @return ResponseEntity 包装的响应字符串
     */
    private ResponseEntity<String> proxyToService(HttpServletRequest request, String body, String serviceName) {
        // 获取原始请求的URI路径（如/api/message/list）
        String path = request.getRequestURI();
        // 获取原始请求的查询参数字符串（如pageNum=1&pageSize=10）
        String queryString = request.getQueryString();
        // 拼接目标URL：http://服务名 + 请求路径 + 查询参数
        String targetUrl = "http://" + serviceName + path + (queryString != null ? "?" + queryString : "");

        // 创建HTTP请求头
        HttpHeaders headers = new HttpHeaders();
        // 设置请求体内容类型为JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 封装请求实体（包含请求体和请求头）
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // 将原始请求的HTTP方法字符串（如"GET"、"POST"）转换为HttpMethod枚举
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            // 通过RestTemplate发起HTTP请求到目标微服务，并获取响应
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);
            // 将下游微服务的响应状态码和响应体原样返回给前端
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            // 下游服务不可用时返回503 + 空数据JSON，而非500触发前端"服务器错误"弹窗
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"code\":503,\"message\":\"服务暂不可用\",\"data\":null}");
        }
    }

    /**
     * 内部静态配置类 — 配置支持服务发现的RestTemplate
     * 通过@LoadBalanced注解，RestTemplate在发起HTTP请求时会通过Nacos服务发现
     * 自动将服务名（如bookstore-message）解析为实际的IP:Port地址
     */
    @Configuration // 标记这是一个Spring配置类
    static class RestTemplateConfig {

        /**
         * 创建并注册一个支持负载均衡的RestTemplate Bean
         * @LoadBalanced注解使RestTemplate具备服务发现能力，
         * 能够将"http://服务名/路径"形式的URL自动解析为实际的服务实例地址
         *
         * @return RestTemplate实例
         */
        @Bean // 注册为Spring Bean
        @LoadBalanced // 启用客户端负载均衡，配合Nacos服务发现使用
        public RestTemplate restTemplate() {
            return new RestTemplate(); // 创建RestTemplate实例
        }
    }
}
