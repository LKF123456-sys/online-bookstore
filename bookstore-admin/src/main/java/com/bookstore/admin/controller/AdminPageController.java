 package com.bookstore.admin.controller;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestMapping;

 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpSession;

 /**
  * 管理后台页面重定向控制器
  *
  * 此控制器替代了旧的 JSP 视图渲染方式，将所有管理后台页面请求重定向到 Vue 前端。
  * Vue 管理前端（bookstore-admin-frontend）使用 Naive UI 提供了更现代化的后台管理界面。
  *
  * 旧的 JSP 视图和依赖（tomcat-embed-jasper、JSTL）已移除：
  * - 所有 GET 请求的页面路径重定向到 Vue 前端（地址由 ADMIN_FRONTEND_URL 配置）
  * - POST 登录/登出请求向后兼容处理，完成后仍重定向到 Vue 前端
  *
  * 配置方式（application.yml）：
  *   bookstore:
  *     admin-frontend:
  *       url: ${ADMIN_FRONTEND_URL:http://localhost:5174}
  */
 @Controller
 @RequestMapping("/admin")
 public class AdminPageController {
 
     @Value("${bookstore.admin-frontend.url}")
     private String adminFrontendUrl;
 
     /**
      * 捕获所有 /admin/** 的 GET 页面请求，重定向到 Vue 前端
      */
     @GetMapping("/**")
     public String redirectToVueAdmin(HttpServletRequest request) {
         String requestPath = request.getRequestURI();
         // 排除 API 路径（/admin/api/**）避免干扰 REST 接口
         if (requestPath.startsWith("/admin/api/")) {
             return null; // 让 Spring 继续查找其他处理器
         }
         return "redirect:" + adminFrontendUrl + requestPath;
     }
 
     /**
      * 处理 /admin/login 的 GET 请求，重定向到 Vue 前端的登录页
      */
     @GetMapping("/login")
     public String adminLoginPage() {
         return "redirect:" + adminFrontendUrl + "/admin/login";
     }
 
     /**
      * 处理 /admin/logout 的 GET 请求，清除会话后重定向到 Vue 前端
      */
     @GetMapping("/logout")
     public String adminLogoutGet(HttpSession session) {
         session.invalidate();
         return "redirect:" + adminFrontendUrl + "/admin/login";
     }
 }
