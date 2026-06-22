 package com.bookstore.common.config;

 import lombok.Getter;
 import lombok.Setter;
 import org.springframework.boot.context.properties.ConfigurationProperties;
 import org.springframework.cloud.context.config.annotation.RefreshScope;
 import org.springframework.stereotype.Component;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;

 /**
  * 灰度发布配置 — 支持 Nacos 运行态刷新
  *
  * 灰度策略支持三种模式，按优先级从高到低：
  * 1. 用户白名单：指定 userId 直接走灰度版本
  * 2. 按比例：userId.hashCode() % 100 < percentage 的走灰度
  * 3. 按标签：请求头 X-Gray-Tag 匹配 grayTags 中的标签
  *
  * 配合 Gateway 的 GrayReleaseFilter 实现流量路由，
  * 通过管理后台 /admin/api/gray-release 管理灰度规则。
  *
  * Nacos 配置示例（bookstore-gateway.yml）：
  *   bookstore:
  *     gray-release:
  *       enabled: true
  *       rules:
  *         - name: "product-new-feature"
  *           service-name: "bookstore-product"
  *           version: "v2"
  *           percentage: 10
  *           whitelist-user-ids: ["101", "102"]
  *           gray-tags: ["beta", "canary"]
  */
 @Getter @Setter
 @Component
 @RefreshScope
 @ConfigurationProperties(prefix = "bookstore.gray-release")
 public class GrayReleaseConfig {

     /** 全局灰度开关 */
     private boolean enabled = false;

     /** 灰度规则列表 */
     private List<GrayRule> rules = new ArrayList<>();

     /** 灰度路由缓存：service-name -> GrayRule */
     private final Map<String, GrayRule> ruleCache = new ConcurrentHashMap<>();

     @Getter @Setter
     public static class GrayRule {
         /** 规则名称（用于标识和管理） */
         private String name;
         /** 目标服务名（如 bookstore-product） */
         private String serviceName;
         /** 灰度版本号（作为 Nacos metadata 标签） */
         private String version = "v2";
         /** 灰度比例 0-100 */
         private int percentage = 10;
         /** 白名单用户 ID */
         private List<String> whitelistUserIds = new ArrayList<>();
         /** 灰度标签（匹配 X-Gray-Tag 请求头） */
         private List<String> grayTags = new ArrayList<>();

         public boolean isUserInGray(String userId) {
             if (userId == null || userId.isEmpty()) return false;
             if (whitelistUserIds.contains(userId)) return true;
             return Math.abs(userId.hashCode() % 100) < percentage;
         }

         public boolean matchesTag(String tag) {
             return tag != null && grayTags.contains(tag);
         }
     }

     /**
      * 判断指定服务的指定用户是否应走灰度版本
      */
     public GrayRule matchRule(String serviceName, String userId, String grayTag) {
         for (GrayRule rule : rules) {
             if (!rule.getServiceName().equals(serviceName)) continue;
             if (grayTag != null && !grayTag.isEmpty() && rule.matchesTag(grayTag)) return rule;
             if (userId != null && rule.isUserInGray(userId)) return rule;
         }
         return null;
     }

     public boolean isEnabled() { return enabled; }
 }
