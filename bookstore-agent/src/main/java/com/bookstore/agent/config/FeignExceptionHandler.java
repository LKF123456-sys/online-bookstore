package com.bookstore.agent.config;  // 声明当前类所在的包路径：配置层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入Feign异常基类（包含所有远程调用异常信息）
import feign.FeignException;
// 导入Resilience4j熔断器开启时抛出的异常
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
// 导入Resilience4j限流器触发时抛出的异常
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
// 导入Lombok的Slf4j日志注解
import lombok.extern.slf4j.Slf4j;
// 导入Spring的Order注解，控制多个Advice的执行优先级
import org.springframework.core.annotation.Order;
// 导入HTTP状态码枚举
import org.springframework.http.HttpStatus;
// 导入全局异常处理器注解
import org.springframework.web.bind.annotation.ExceptionHandler;
// 导入响应状态注解
import org.springframework.web.bind.annotation.ResponseStatus;
// 导入RestController通知注解
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Feign远程调用异常处理器
 * 统一处理Feign调用过程中可能出现的各类异常，包括：
 * - 熔断器开启（CallNotPermittedException）
 * - 限流器触发（RequestNotPermitted）
 * - Feign通用异常（FeignException，包含超时、连接失败、5xx等）
 *
 * 使用@Order(0)确保此处理器优先于其他全局异常处理器执行
 */
@Slf4j
@RestControllerAdvice
@Order(0)
public class FeignExceptionHandler {

    /**
     * 处理熔断器开启异常
     * 当下游服务故障率超过阈值，熔断器进入Open状态时抛出此异常
     * 返回503 Service Unavailable，提示用户稍后重试
     */
    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleCircuitBreakerOpen(CallNotPermittedException e) {
        log.warn("【熔断器开启】下游服务暂不可用: message={}", e.getMessage());
        return Result.error(503, "服务暂时繁忙，请稍后重试");
    }

    /**
     * 处理限流器触发异常
     * 当对下游服务的请求频率超过限流器配置的阈值时抛出此异常
     * 返回429 Too Many Requests，提示用户降低请求频率
     */
    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitExceeded(RequestNotPermitted e) {
        log.warn("【限流器触发】对下游服务请求过于频繁: {}", e.getMessage());
        return Result.error(429, "请求太频繁，请稍后再试");
    }

    /**
     * 处理Feign通用异常
     * 包括连接超时、读取超时、HTTP 4xx/5xx响应等
     * 返回502 Bad Gateway，提示远程服务调用失败
     * 对于5xx错误额外记录告警日志，提醒关注下游服务健康状态
     */
    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<Void> handleFeignException(FeignException e) {
        log.error("远程服务调用失败: status={}, message={}", e.status(), e.getMessage());
        if (e.status() >= 500) {
            log.error("下游服务内部错误(5xx)，需关注服务健康状态");
        }
        return Result.error(502, "远程服务调用失败，请稍后重试");
    }
}
