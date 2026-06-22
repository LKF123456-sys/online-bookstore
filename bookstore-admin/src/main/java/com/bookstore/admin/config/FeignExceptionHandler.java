package com.bookstore.admin.config; // 声明当前类所在的包路径：配置层

// 导入公共模块的统一响应封装类Result
import com.bookstore.common.api.Result;
// 导入Feign异常基类，涵盖所有Feign远程调用异常
import feign.FeignException;
// 导入Resilience4j熔断器开启异常，当熔断器处于打开状态时抛出
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
// 导入Resilience4j限流器异常，当请求超过限流阈值时抛出
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring的Order注解，用于指定异常处理器的优先级
import org.springframework.core.annotation.Order;
// 导入HTTP状态码枚举
import org.springframework.http.HttpStatus;
// 导入Spring MVC的异常处理注解
import org.springframework.web.bind.annotation.ExceptionHandler;
// 导入Spring MVC的响应状态注解
import org.springframework.web.bind.annotation.ResponseStatus;
// 导入Spring MVC的全局异常处理注解
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Feign远程调用异常处理器
 * 统一捕获并处理Feign调用过程中产生的各类异常，返回友好的错误响应
 *
 * 处理三类异常：
 * 1. CallNotPermittedException — 熔断器开启，下游服务暂不可用（503）
 * 2. RequestNotPermitted — 限流器触发，请求过于频繁（429）
 * 3. FeignException — Feign调用失败，如下游服务异常、超时等（502）
 *
 * @Order(0) 表示此异常处理器优先级最高（数值越小优先级越高）
 */
@Slf4j
@RestControllerAdvice // 全局异常处理注解，拦截所有Controller层抛出的异常
@Order(0) // 优先级最高，确保Feign异常优先被此处理器捕获
public class FeignExceptionHandler {

    /**
     * 处理熔断器开启异常
     * 当Resilience4j熔断器处于打开状态时，所有对该下游服务的调用都会被拒绝，抛出此异常
     *
     * @param e 熔断器拒绝调用异常
     * @return 503状态码 + 友好提示信息
     */
    @ExceptionHandler(CallNotPermittedException.class) // 捕获熔断器开启异常
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // 设置HTTP响应状态码为503（服务不可用）
    public Result<Void> handleCircuitBreakerOpen(CallNotPermittedException e) {
        log.warn("【熔断器开启】下游服务暂不可用: message={}", e.getMessage()); // 记录警告日志
        return Result.error(503, "服务暂时繁忙，请稍后重试"); // 返回友好的错误提示
    }

    /**
     * 处理限流器触发异常
     * 当对下游服务的请求频率超过限流器配置的阈值时，抛出此异常
     *
     * @param e 限流器拒绝请求异常
     * @return 429状态码 + 友好提示信息
     */
    @ExceptionHandler(RequestNotPermitted.class) // 捕获限流器触发异常
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) // 设置HTTP响应状态码为429（请求过多）
    public Result<Void> handleRateLimitExceeded(RequestNotPermitted e) {
        log.warn("【限流器触发】对下游服务请求过于频繁: {}", e.getMessage()); // 记录警告日志
        return Result.error(429, "请求太频繁，请稍后再试"); // 返回友好的错误提示
    }

    /**
     * 处理Feign通用调用异常
     * 涵盖所有Feign远程调用失败的场景，如连接超时、下游服务返回4xx/5xx错误等
     *
     * @param e Feign异常基类，包含HTTP状态码和错误信息
     * @return 502状态码 + 友好提示信息
     */
    @ExceptionHandler(FeignException.class) // 捕获所有Feign调用异常
    @ResponseStatus(HttpStatus.BAD_GATEWAY) // 设置HTTP响应状态码为502（错误网关）
    public Result<Void> handleFeignException(FeignException e) {
        log.error("远程服务调用失败: status={}, message={}", e.status(), e.getMessage()); // 记录错误日志，包含HTTP状态码和异常信息
        if (e.status() >= 500) { // 如果下游服务返回5xx错误
            log.error("下游服务内部错误(5xx)，需关注服务健康状态"); // 额外记录严重错误日志，提示运维关注
        }
        return Result.error(502, "远程服务调用失败，请稍后重试"); // 返回友好的错误提示
    }
}
