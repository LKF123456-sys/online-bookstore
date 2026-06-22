 package com.bookstore.admin.controller;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.GetMapping;

 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;

 /**
  * 用户端页面重定向控制器
  *
  * 替代旧的 JSP 视图控制器（FrontAuthController、FrontPageController、UserCenterController），
  * 将所有用户端页面请求重定向到 Vue 前端（bookstore-frontend）。
  *
  * Vue 用户前端使用 Vue 3 + Naive UI 提供了完整的用户界面：
  * - 首页、商品浏览、搜索
  * - 购物车、订单、支付
  * - 用户中心、优惠券、消息
  * - 登录、注册
  *
  * 通过 catch-all 模式捕获所有非 API 的 GET 请求，重定向到 user-frontend URL。
  */
 @Deprecated
// @Controller — 已禁用，改由 SpaRedirectFilter 处理 SPA 重定向
public class UserPageRedirectController {

     @Value("${bookstore.user-frontend.url}")
     private String userFrontendUrl;

     /**
      * 捕获所有非 API 路径的 GET 请求，重定向到 Vue 用户前端
      */
     @GetMapping("/**")
     public void redirectToVueUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
         String path = request.getRequestURI();
         // 排除 API 路径，避免干扰 REST 接口
         if (path.startsWith("/api/") || path.startsWith("/admin/")) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
         // 排除静态资源路径，这些文件由 admin 服务自己提供（图片、CSS、JS 等）
         if (path.startsWith("/img/") || path.startsWith("/css/") || path.startsWith("/js/") ||
             path.startsWith("/static/") || path.equals("/favicon.svg") || path.equals("/favicon.ico") ||
             path.startsWith("/webjars/") || path.startsWith("/v3/api-docs") || path.equals("/swagger-ui.html")) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
         response.sendRedirect(userFrontendUrl + path);
     }
 }
