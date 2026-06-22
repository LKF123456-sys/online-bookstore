 package com.bookstore.gateway.filter;

 import com.bookstore.common.config.GrayReleaseConfig;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.cloud.gateway.filter.GatewayFilterChain;
 import org.springframework.cloud.gateway.filter.GlobalFilter;
 import org.springframework.core.Ordered;
 import org.springframework.http.server.reactive.ServerHttpRequest;
 import org.springframework.stereotype.Component;
 import org.springframework.web.server.ServerWebExchange;
 import reactor.core.publisher.Mono;

 /**
  * 灰度发布路由过滤器 — 基于用户身份/标签的灰度流量分发
  *
  * 执行流程（在 AuthFilter 之后）：
  * 1. 从请求中提取 X-User-Id 和 X-Gray-Tag
  * 2. 从 GrayReleaseConfig 匹配灰度规则
  * 3. 若匹配，注入 X-Gray-Version 头，下游服务可据此切换行为
  *
  * 扩展点（在 Gateway LB 层面）：
  * - GrayReleaseMetadataRule：Nacos 元数据匹配，将灰度流量路由到有 gray-version=xxx 标签的实例
  * - 部署时给灰度实例添加 Nacos metadata: gray-version=v2
  *
  * 灰度规则管理：
  * - 通过 Nacos 配置中心动态修改（@RefreshScope）
  * - 通过管理后台 /admin/api/gray-release 查看和触发刷新
  */
 @Slf4j
 @Component
 @RequiredArgsConstructor
 public class GrayReleaseFilter implements GlobalFilter, Ordered {

     private final GrayReleaseConfig grayReleaseConfig;

     private static final String HEADER_USER_ID = "X-User-Id";
     private static final String HEADER_GRAY_TAG = "X-Gray-Tag";
     private static final String HEADER_GRAY_VERSION = "X-Gray-Version";

     @Override
     public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
         if (!grayReleaseConfig.isEnabled()) {
             return chain.filter(exchange);
         }

         ServerHttpRequest request = exchange.getRequest();
         String path = request.getURI().getPath();
         String userId = request.getHeaders().getFirst(HEADER_USER_ID);
         String grayTag = request.getHeaders().getFirst(HEADER_GRAY_TAG);

         // API 路径不参与灰度路由
         if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
             return chain.filter(exchange);
         }

         // 从路径中提取服务名（如 /api/product → bookstore-product）
         String serviceName = resolveServiceName(path);
         if (serviceName == null) {
             return chain.filter(exchange);
         }

         GrayReleaseConfig.GrayRule rule = grayReleaseConfig.matchRule(serviceName, userId, grayTag);
         if (rule != null) {
             log.debug("Gray routing: user={} service={} version={} rule={}", userId, serviceName, rule.getVersion(), rule.getName());
             ServerHttpRequest mutated = request.mutate()
                     .header(HEADER_GRAY_VERSION, rule.getVersion())
                     .build();
             return chain.filter(exchange.mutate().request(mutated).build());
         }

         return chain.filter(exchange);
     }

     /**
      * 根据请求路径解析目标服务名
      */
     private String resolveServiceName(String path) {
         if (path.startsWith("/api/product") || path.startsWith("/admin/product")) return "bookstore-product";
         if (path.startsWith("/api/order") || path.startsWith("/admin/order")) return "bookstore-order";
         if (path.startsWith("/api/auth") || path.startsWith("/api/user") || path.startsWith("/admin/user")) return "bookstore-user";
         if (path.startsWith("/api/coupon") || path.startsWith("/admin/coupon")) return "bookstore-promotion";
         if (path.startsWith("/api/review") || path.startsWith("/admin/review")) return "bookstore-promotion";
         if (path.startsWith("/api/message") || path.startsWith("/admin/message")) return "bookstore-message";
         if (path.startsWith("/api/agent")) return "bookstore-agent";
         return null;
     }

     @Override
     public int getOrder() {
         // 在 AuthFilter(-100) 之后执行
         return -50;
     }
 }
