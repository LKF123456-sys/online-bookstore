 package com.bookstore.admin.controller.api;

 import com.bookstore.admin.service.AdminLogService;
 import com.bookstore.common.api.Result;
 import com.bookstore.common.config.GrayReleaseConfig;
 import io.swagger.v3.oas.annotations.Operation;
 import io.swagger.v3.oas.annotations.tags.Tag;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.web.bind.annotation.*;

 import java.util.List;
 import java.util.Map;
 import java.util.stream.Collectors;

 /**
  * 灰度发布管理 API
  *
  * 提供灰度规则的查看、刷新和状态监控。
  * 灰度配置通过 Nacos 配置中心管理（bookstore.gray-release），动态生效。
  *
  * 灰度策略：
  * 1. 白名单用户：指定 userId 列表
  * 2. 按比例：userId.hashCode() % 100 < percentage
  * 3. 标签匹配：X-Gray-Tag 请求头匹配
  */
 @Slf4j
 @RestController
 @RequestMapping("/admin/api/gray-release")
 @RequiredArgsConstructor
 @Tag(name = "管理后台-系统", description = "灰度发布管理")
 public class GrayReleaseController {

     private final GrayReleaseConfig grayReleaseConfig;
     private final AdminLogService adminLogService;

     @Operation(summary = "获取灰度规则", description = "返回当前生效的灰度发布规则列表")
     @GetMapping
     public Result<List<GrayRuleVO>> getGrayRules() {
         List<GrayRuleVO> rules = grayReleaseConfig.getRules().stream()
                 .map(r -> new GrayRuleVO(
                         r.getName(), r.getServiceName(), r.getVersion(),
                         r.getPercentage(), r.getWhitelistUserIds(), r.getGrayTags()))
                 .collect(Collectors.toList());
         return Result.success(rules);
     }

     @Operation(summary = "触发灰度配置刷新", description = "手动刷新 Nacos 灰度配置")
     @PostMapping("/refresh")
     public Result<String> refreshGrayConfig() {
         adminLogService.addLog("system", "GRAY_REFRESH", "灰度发布", "手动触发灰度配置刷新");
         return Result.success("Gray config refresh triggered. Nacos will push new rules to all instances.");
     }

     @Operation(summary = "灰度发布状态", description = "查看灰度发布功能的运行状态")
     @GetMapping("/status")
     public Result<Map<String, Object>> getGrayStatus() {
         return Result.success(Map.of(
                 "enabled", grayReleaseConfig.isEnabled(),
                 "ruleCount", grayReleaseConfig.getRules().size(),
                 "rules", grayReleaseConfig.getRules().stream()
                         .map(r -> Map.of(
                                 "name", r.getName(),
                                 "serviceName", r.getServiceName(),
                                 "version", r.getVersion(),
                                 "percentage", r.getPercentage(),
                                 "whitelistSize", r.getWhitelistUserIds().size(),
                                 "grayTags", r.getGrayTags()
                         )).collect(Collectors.toList())
         ));
     }

     @lombok.Getter @lombok.AllArgsConstructor
     public static class GrayRuleVO {
         private String name;
         private String serviceName;
         private String version;
         private int percentage;
         private List<String> whitelistUserIds;
         private List<String> grayTags;
     }
 }
