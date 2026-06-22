 package com.bookstore.common.config;

 import lombok.Getter;
 import lombok.Setter;
 import org.springframework.boot.context.properties.ConfigurationProperties;
 import org.springframework.cloud.context.config.annotation.RefreshScope;
 import org.springframework.stereotype.Component;

 import java.util.HashMap;
 import java.util.Map;

 /**
  * 动态配置属性 — 支持 Nacos 运行态刷新
  *
  * 所有阈值参数可通过 Nacos 配置中心动态调整，无需重启服务。
  * 配置前缀：bookstore.dynamic
  *
  * Nacos 配置示例（bookstore-admin.yml）：
  *   bookstore:
  *     dynamic:
  *       circuit-breaker:
  *         product-service:
  *           sliding-window-size: 10
  *           failure-rate-threshold: 50
  *           wait-duration-in-open-state: 10
  *         order-service:
  *           sliding-window-size: 20
  *           failure-rate-threshold: 30
  */
 @Getter
 @Setter
 @Component
 @RefreshScope
 @ConfigurationProperties(prefix = "bookstore.dynamic")
 public class DynamicConfigProperties {

     /** 熔断器配置（按服务名） */
     private Map<String, CircuitBreakerConfig> circuitBreaker = new HashMap<>();

     /** 限流器配置（按服务名） */
     private Map<String, RateLimiterConfig> rateLimiter = new HashMap<>();

     /** 重试配置 */
     private RetryConfig retry = new RetryConfig();

     /** 超时限制配置 */
     private TimeLimiterConfig timeLimiter = new TimeLimiterConfig();

     /** 功能开关 */
     private Map<String, Boolean> featureFlags = new HashMap<>();

     @Getter @Setter
     public static class CircuitBreakerConfig {
         private int slidingWindowSize = 10;
         private int minimumNumberOfCalls = 5;
         private float failureRateThreshold = 50;
         private int waitDurationInOpenState = 10; // seconds
         private int permittedNumberOfCallsInHalfOpenState = 3;
         private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
         private int slowCallDurationThreshold = 8; // seconds
         private float slowCallRateThreshold = 80;

         public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig toResilience4jConfig() {
             return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                     .slidingWindowSize(slidingWindowSize)
                     .minimumNumberOfCalls(minimumNumberOfCalls)
                     .failureRateThreshold(failureRateThreshold)
                     .waitDurationInOpenState(java.time.Duration.ofSeconds(waitDurationInOpenState))
                     .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                     .automaticTransitionFromOpenToHalfOpenEnabled(automaticTransitionFromOpenToHalfOpenEnabled)
                     .slowCallDurationThreshold(java.time.Duration.ofSeconds(slowCallDurationThreshold))
                     .slowCallRateThreshold(slowCallRateThreshold)
                     .build();
         }
     }

     @Getter @Setter
     public static class RateLimiterConfig {
         private int limitForPeriod = 100;
         private int limitRefreshPeriod = 1; // seconds
         private int timeoutDuration = 0; // seconds

         public io.github.resilience4j.ratelimiter.RateLimiterConfig toResilience4jConfig() {
             return io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                     .limitForPeriod(limitForPeriod)
                     .limitRefreshPeriod(java.time.Duration.ofSeconds(limitRefreshPeriod))
                     .timeoutDuration(java.time.Duration.ofSeconds(timeoutDuration))
                     .build();
         }
     }

     @Getter @Setter
     public static class RetryConfig {
         private int maxAttempts = 3;
         private int waitDuration = 500; // milliseconds

         public io.github.resilience4j.retry.RetryConfig toResilience4jConfig() {
             return io.github.resilience4j.retry.RetryConfig.custom()
                     .maxAttempts(maxAttempts)
                     .waitDuration(java.time.Duration.ofMillis(waitDuration))
                     .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                     .build();
         }
     }

     @Getter @Setter
     public static class TimeLimiterConfig {
         private int timeoutDuration = 10; // seconds

         public io.github.resilience4j.timelimiter.TimeLimiterConfig toResilience4jConfig() {
             return io.github.resilience4j.timelimiter.TimeLimiterConfig.custom()
                     .timeoutDuration(java.time.Duration.ofSeconds(timeoutDuration))
                     .build();
         }
     }

     /**
      * 获取指定服务的熔断器配置，不存在时返回默认值
      */
     public CircuitBreakerConfig getCircuitBreakerConfig(String serviceName) {
         return circuitBreaker.getOrDefault(serviceName, new CircuitBreakerConfig());
     }

     /**
      * 获取指定服务的限流器配置，不存在时返回默认值
      */
     public RateLimiterConfig getRateLimiterConfig(String serviceName) {
         return rateLimiter.getOrDefault(serviceName, new RateLimiterConfig());
     }

     /**
      * 检查功能开关是否启用
      */
     public boolean isFeatureEnabled(String feature) {
         return featureFlags.getOrDefault(feature, true);
     }
 }
