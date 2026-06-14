package com.bookstore.gateway.config;  // 声明当前类所在的包路径，属于网关配置层

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;  // 导入KeyResolver接口，用于定义限流的key提取策略
import org.springframework.context.annotation.Bean;  // 导入@Bean注解，用于注册Spring Bean
import org.springframework.context.annotation.Configuration;  // 导入@Configuration注解，标记该类为配置类
import reactor.core.publisher.Mono;  // 导入Project Reactor的Mono类型

/**
 * 网关限流配置
 * 配置基于客户端 IP 的限流策略（Redis + 令牌桶算法）
 *
 * 限流粒度：每个客户端 IP 独立计算请求次数
 * 限流位置：Spring Cloud Gateway 的 RequestRateLimiter 过滤器
 * 存储后端：Redis（令牌桶 Lua 脚本原子执行）
 */
@Configuration  // 配置类注解，Spring 会扫描并处理其中的 @Bean 方法
public class RateLimiterConfig {

    /**
     * 基于客户端 IP 的限流 Key 解析器
     * 从请求中提取客户端真实 IP 地址作为限流维度
     * 优先读取 X-Forwarded-For 头（经过代理时），否则使用 RemoteAddr
     *
     * @return KeyResolver 实例，解析出客户端 IP 字符串
     */
    @Bean  // 注册为 Spring Bean，Spring Cloud Gateway 会自动识别并使用
    public KeyResolver ipKeyResolver() {  // 定义基于 IP 的限流 Key 解析器
        return exchange -> {  // Lambda 表达式，接收 ServerWebExchange 对象
            // 优先从 X-Forwarded-For 头获取真实客户端 IP（经过 Nginx/负载均衡器时会携带）
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");  // 获取代理头
            if (xff != null && !xff.isEmpty()) {  // 如果代理头存在
                return Mono.just(xff.split(",")[0].trim());  // 取第一个 IP（即原始客户端 IP）
            }
            // 如果没有代理头，使用直接连接的远程地址
            return Mono.just(  // 返回 IP 地址
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()  // 获取远程 IP
            );
        };
    }
}
