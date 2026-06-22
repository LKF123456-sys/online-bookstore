 package com.bookstore.admin.controller;

 import java.util.HashMap;
 import java.util.Map;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.client.RestTemplate;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;

 import com.bookstore.admin.service.AdminLogService;

 import jakarta.servlet.http.HttpSession;
 import lombok.extern.slf4j.Slf4j;

 /**
  * 路径兼容重定向控制器 - 处理所有路径别名和兼容性重定向
  *
  * 所有重定向目标已更新为指向 Vue 前端，替代旧的 JSP 路径。
  * 前端地址由 bookstore.admin-frontend.url 和 bookstore.user-frontend.url 配置。
  */
 @Slf4j
 @Controller
 public class RedirectController extends BaseController {

     @Value("${bookstore.admin-frontend.url}")
     private String adminFrontendUrl;

     @Value("${bookstore.user-frontend.url}")
     private String userFrontendUrl;

     public RedirectController(RestTemplate restTemplate, AdminLogService adminLogService) {
         super(restTemplate, adminLogService);
     }

     // ======================== 前台路径兼容重定向 ========================

     @GetMapping("/coupon/my")
     public String couponMy() { return "redirect:" + userFrontendUrl + "/user/coupon"; }

     @GetMapping("/message")
     public String message() { return "redirect:" + userFrontendUrl + "/user/message"; }

     @GetMapping("/orders")
     public String orders() { return "redirect:" + userFrontendUrl + "/orders"; }

     @GetMapping("/products/affordable")
     public String affordableProducts() { return "redirect:" + userFrontendUrl + "/products"; }

     // ======================== 支付回调处理 ========================

     /**
      * 支付回调处理 - 第三方支付平台完成后回调此接口
      * 处理完成后重定向到 Vue 用户前端的订单详情页
      */
     @SuppressWarnings("unchecked")
     @GetMapping("/paymentCallback")
     public String paymentCallback(@RequestParam String orderId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String paymentMethod,
                                    @RequestParam(required = false) String userId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
         if ("success".equals(status)) {
             if (userId == null || userId.isEmpty()) {
                 try {
                     Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
                     if (user != null) {
                         userId = String.valueOf(user.get("userid"));
                     }
                 } catch (Exception e) { log.warn("从Session获取用户ID失败: {}", e.getMessage()); }
             }
             if (userId == null || userId.isEmpty()) {
                 try {
                     ResponseEntity<String> orderResp = restTemplate.exchange(
                         "http://bookstore-order/api/order/" + orderId,
                         HttpMethod.GET, null, String.class);
                     Map<String, Object> orderResult = parseJson(orderResp.getBody());
                     Map<String, Object> orderData = (Map<String, Object>) orderResult.get("data");
                     if (orderData != null) {
                         userId = String.valueOf(orderData.get("userid"));
                     }
                 } catch (Exception e) { log.warn("从订单服务查询用户ID失败, orderId={}: {}", orderId, e.getMessage()); }
             }
             if (userId != null && !userId.isEmpty()) {
                 try {
                     HttpHeaders headers = new HttpHeaders();
                     headers.set("X-User-Id", userId);
                     restTemplate.exchange(
                         "http://bookstore-order/api/order/" + orderId + "/pay",
                         HttpMethod.POST, new HttpEntity<>(headers), String.class);
                     return "redirect:" + userFrontendUrl + "/order/" + orderId;
                 } catch (Exception e) {
                     redirectAttributes.addFlashAttribute("error", "订单状态更新失败，请稍后重试");
                     return "redirect:" + userFrontendUrl + "/checkout?orderId=" + orderId;
                 }
             } else {
                 redirectAttributes.addFlashAttribute("error", "无法获取用户身份，支付失败");
                 return "redirect:" + userFrontendUrl + "/checkout?orderId=" + orderId;
             }
         }
         return "redirect:" + userFrontendUrl + "/checkout?orderId=" + orderId;
     }

     // ======================== 管理后台路径兼容重定向 ========================

     @GetMapping({"/admin", "/admin/"})
     public String adminRoot() { return "redirect:" + adminFrontendUrl + "/admin/dashboard"; }

     @GetMapping("/admin/product/list")
     public String adminProductListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/products"); }

     @GetMapping("/admin/order/list")
     public String adminOrderListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/orders"); }

     @GetMapping("/admin/user/list")
     public String adminUserListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/users"); }

     @GetMapping("/admin/coupon/list")
     public String adminCouponListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/coupons"); }

     @GetMapping("/admin/announcement/list")
     public String adminAnnouncementListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/announcements"); }

     @GetMapping("/admin/review/list")
     public String adminReviewListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/reviews"); }

     @GetMapping("/admin/message/list")
     public String adminMessageListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/messages"); }

     @GetMapping("/admin/log/list")
     public String adminLogListAlias(HttpSession session) {
         return checkAdminOrRedirect(session, "redirect:" + adminFrontendUrl + "/admin/logs");
     }
 }
