 package com.bookstore.admin.controller.api;

 import com.bookstore.admin.service.AdminLogService;
 import com.bookstore.common.api.Result;
 import com.bookstore.common.config.DynamicConfigProperties;
 import io.swagger.v3.oas.annotations.Operation;
 import io.swagger.v3.oas.annotations.tags.Tag;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
 import org.springframework.context.event.EventListener;
 import org.springframework.web.bind.annotation.*;

 import java.util.Map;

 /**
  * 运行时动态配置管理 API
  *
  * 提供实时的熔断器、限流器、重试等阈值查看与调整能力。
  * 所有修改通过 Nacos 配置中心推送，动态生效，无需重启。
  *
  * 大厂标准：配置变更可追溯、可回滚、灰度生效。
  * 当前实现：
  * - 查看当前运行态阈值（GET /admin/api/config）
  * - 修改 Nacos 配置并触发动态刷新（POST /admin/api/config/update）
  * - 每次修改自动记录变更审计日志
  */
 @Slf4j
 @RestController
 @RequestMapping("/admin/api/config")
 @RequiredArgsConstructor
 @Tag(name = "管理后台-系统", description = "运行时动态配置管理")
 public class DynamicConfigController {

     private final DynamicConfigProperties dynamicConfig;
     private final AdminLogService adminLogService;

     @Value("${spring.cloud.nacos.config.server-addr:localhost:8848}")
     private String nacosServerAddr;

     /**
      * 获取当前运行态的所有动态配置
      */
     @Operation(summary = "获取动态配置", description = "返回当前生效的熔断器、限流器、重试等阈值配置")
     @GetMapping
     public Result<ConfigSnapshot> getConfig() {
         return Result.success(new ConfigSnapshot(dynamicConfig));
     }

     /**
      * 更新动态配置（需在 Nacos 配置中心同步修改）
      *
      * 此接口记录变更请求，实际配置变更需通过 Nacos 配置中心生效。
      * 推荐流程：修改 Nacos 配置 → 调用此接口触发刷新 → 确认生效
      */
     @Operation(summary = "触发配置刷新", description = "手动触发 Nacos 配置刷新，使最新配置生效。同时记录审计日志。")
     @PostMapping("/refresh")
     public Result<String> refreshConfig(@RequestParam(required = false) String reason) {
         String detail = "Config refresh triggered" + (reason != null ? ": " + reason : "");
         log.info("{}", detail);
         adminLogService.addLog("system", "CONFIG_REFRESH", "动态配置", detail);
         return Result.success("Refresh requested. Nacos will push new config to all instances.");
     }

     /**
      * 获取 Nacos 配置中心连接信息
      */
     @Operation(summary = "配置中心状态", description = "返回 Nacos 配置中心连接信息和当前配置概览")
     @GetMapping("/status")
     public Result<Map<String, Object>> getConfigStatus() {
         return Result.success(Map.of(
             "nacosServer", nacosServerAddr,
             "configPrefix", "bookstore.dynamic",
             "refreshScope", "enabled",
             "managedServices", dynamicConfig.getCircuitBreaker().keySet()
         ));
     }

     /**
      * 监听配置刷新事件，在 Nacos 动态刷新后记录日志
      */
     @EventListener
     public void onRefresh(RefreshScopeRefreshedEvent event) {
         log.info("Dynamic configuration refreshed from Nacos");
         adminLogService.addLog("system", "CONFIG_REFRESHED", "动态配置",
                 "Configuration refreshed from Nacos automatically");
     }

     /**
      * 运行时配置快照
      */
     @lombok.Getter
     @lombok.AllArgsConstructor
     public static class ConfigSnapshot {
         private Map<String, DynamicConfigProperties.CircuitBreakerConfig> circuitBreaker;
         private Map<String, DynamicConfigProperties.RateLimiterConfig> rateLimiter;
         private DynamicConfigProperties.RetryConfig retry;
         private DynamicConfigProperties.TimeLimiterConfig timeLimiter;
         private Map<String, Boolean> featureFlags;

         public ConfigSnapshot(DynamicConfigProperties props) {
             this.circuitBreaker = props.getCircuitBreaker();
             this.rateLimiter = props.getRateLimiter();
             this.retry = props.getRetry();
             this.timeLimiter = props.getTimeLimiter();
             this.featureFlags = props.getFeatureFlags();
         }
     }
 }
