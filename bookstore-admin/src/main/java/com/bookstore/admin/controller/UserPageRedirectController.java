 package com.bookstore.admin.controller;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.GetMapping;

 import jakarta.servlet.http.HttpServletRequest;

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
 @Controller
 public class UserPageRedirectController {

     @Value("${bookstore.user-frontend.url}")
     private String userFrontendUrl;

     /**
      * 捕获所有非 API 路径的 GET 请求，重定向到 Vue 用户前端
      */
     @GetMapping("/**")
     public String redirectToVueUser(HttpServletRequest request) {
         String path = request.getRequestURI();
         // 排除 API 路径，避免干扰 REST 接口
         if (path.startsWith("/api/") || path.startsWith("/admin/")) {
             return null;
         }
         return "redirect:" + userFrontendUrl + path;
     }
 }
