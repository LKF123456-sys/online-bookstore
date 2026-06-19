package com.bookstore.order.config;

import com.bookstore.common.api.Result;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 订单服务专用异常处理器 — 处理微服务调用相关的异常
 *
 * 设计说明：
 *   GlobalExceptionHandler（在 common 模块中）处理通用异常（校验、业务、兜底），
 *   本类处理订单服务特有的微服务调用异常（熔断、限流、Feign 调用失败），
 *   通过 @Order(0) 确保本处理器优先于全局处理器匹配。
 *
 * 面试亮点：
 *   1. 熔断器异常 → 503（服务暂不可用），前端可展示"稍后重试"
 *   2. 限流器异常 → 429（请求过多），前端可展示"请求太频繁"
 *   3. Feign 调用异常 → 502（网关错误），携带下游服务状态码
 *   4. 分层异常处理：common 处理通用异常，order 处理微服务特有异常
 */
@Slf4j
@RestControllerAdvice
@Order(0)  // 优先于 GlobalExceptionHandler 匹配
public class FeignExceptionHandler {

    /**
     * 处理熔断器拒绝异常 — 当下游服务（如商品服务）熔断器打开时触发
     * Resilience4j 检测到失败率超过阈值后打开熔断器，后续请求直接快速失败
     *
     * HTTP 503 SERVICE_UNAVAILABLE — 前端展示"服务暂时繁忙，请稍后重试"
     */
    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleCircuitBreakerOpen(CallNotPermittedException e) {
        log.warn("【熔断器开启】下游服务暂不可用: circuitBreaker={}, message={}",
                e.getCircuitBreakerName(), e.getMessage());
        return Result.error(503, "服务暂时繁忙，请稍后重试");
    }

    /**
     * 处理限流器拒绝异常 — 当下游服务请求频率超过限制时触发
     * Resilience4j RateLimiter 控制对下游服务的调用速率，防止压垮下游
     *
     * HTTP 429 TOO_MANY_REQUESTS — 前端展示"请求太频繁，请稍后再试"
     */
    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitExceeded(RequestNotPermitted e) {
        log.warn("【限流器触发】对下游服务请求过于频繁: {}", e.getMessage());
        return Result.error(429, "请求太频繁，请稍后再试");
    }

    /**
     * 处理 Feign 远程调用异常 — 下游微服务调用失败时触发
     * 包括：连接超时、读取超时、下游返回 HTTP 4xx/5xx 等
     *
     * HTTP 502 BAD_GATEWAY — 表示作为微服务网关/代理，无法从上游获取有效响应
     */
    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<Void> handleFeignException(FeignException e) {
        log.error("远程服务调用失败: status={}, message={}", e.status(), e.getMessage());
        // 从 Feign 异常中提取下游的 HTTP 状态码，附加到日志中便于排查
        if (e.status() >= 500) {
            log.error("下游服务内部错误(5xx)，需关注服务健康状态");
        }
        return Result.error(502, "远程服务调用失败，请稍后重试");
    }
}
