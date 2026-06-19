package com.bookstore.admin.controller;

import java.util.HashMap;
import java.util.Map;

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
 * 职责范围：
 * 1. 前台路径兼容重定向：
 *    - /coupon/my -> /user/coupon（我的优惠券）
 *    - /message -> /user/message（消息中心）
 *    - /orders -> /order/history（订单列表）
 *    - /products/affordable -> /（特价商品，重定向到首页）
 *
 * 2. 支付回调处理：
 *    - /paymentCallback（第三方支付平台完成支付后回调此接口）
 *
 * 3. 管理后台路径兼容重定向：
 *    - /admin, /admin/ -> /admin/index
 *    - /admin/product/list -> /admin/product
 *    - /admin/order/list -> /admin/order
 *    - /admin/user/list -> /admin/user
 *    - /admin/coupon/list -> /admin/coupon
 *    - /admin/announcement/list -> /admin/announcement
 *    - /admin/review/list -> /admin/review
 *    - /admin/message/list -> /admin/message
 *    - /admin/log/list -> /admin/log
 *
 * 设计说明：
 * - 这些重定向主要用于兼容旧版URL和简化路径访问
 * - 管理后台重定向会检查管理员登录状态，未登录则跳转到管理员登录页
 * - 支付回调接口处理第三方支付平台的异步通知，更新订单支付状态
 */
@Slf4j
@Controller
public class RedirectController extends BaseController {

    public RedirectController(RestTemplate restTemplate, AdminLogService adminLogService) {
        super(restTemplate, adminLogService);
    }

    // ======================== 前台路径兼容重定向 ========================

    /** 我的优惠券路径兼容 -> /user/coupon */
    @GetMapping("/coupon/my")
    public String couponMy() { return "redirect:/user/coupon"; }

    /** 消息中心路径兼容 -> /user/message */
    @GetMapping("/message")
    public String message() { return "redirect:/user/message"; }

    /** 订单列表路径兼容 -> /order/history */
    @GetMapping("/orders")
    public String orders() { return "redirect:/order/history"; }

    /** 特价商品路径兼容 -> 首页 */
    @GetMapping("/products/affordable")
    public String affordableProducts() { return "redirect:/"; }

    // ======================== 支付回调处理 ========================

    /**
     * 支付回调处理 - 第三方支付平台完成支付后回调此接口
     * 支付成功后，调用订单服务更新订单状态为"已支付"
     *
     * 用户身份获取策略（按优先级）：
     * 1. URL参数中的userId（支付页面JSP传递）
     * 2. Session中的用户信息（登录时的用户信息）
     * 3. 从订单服务查询（回退方案，无认证时从订单提取userId）
     *
     * @param orderId 订单ID
     * @param status 支付状态（如"success"）
     * @param paymentMethod 支付方式（可选）
     * @param userId 用户ID（可选，优先从URL获取）
     * @param session HTTP会话，用于获取当前登录用户ID
     * @return 根据支付状态跳转到成功或失败页面
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
            // 方式1：从URL参数获取userId
            // 方式2：从Session获取
            if (userId == null || userId.isEmpty()) {
                try {
                    Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
                    if (user != null) {
                        userId = String.valueOf(user.get("userid"));
                    }
                } catch (Exception e) { log.warn("从Session获取用户ID失败: {}", e.getMessage()); }
            }
            // 方式3：回退方案 - 从订单服务直接查询订单获取userId
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
            // 执行支付状态更新
            if (userId != null && !userId.isEmpty()) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-User-Id", userId);
                    restTemplate.exchange(
                        "http://bookstore-order/api/order/" + orderId + "/pay",
                        HttpMethod.POST, new HttpEntity<>(headers), String.class);
                    return "redirect:/payment/success?orderId=" + orderId;
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "订单状态更新失败，请稍后重试");
                    return "redirect:/payment/fail?orderId=" + orderId;
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "无法获取用户身份，支付失败");
                return "redirect:/payment/fail?orderId=" + orderId;
            }
        }
        return "redirect:/payment/fail?orderId=" + orderId;
    }

    // ======================== 管理后台路径兼容重定向 ========================

    /** 管理后台根路径 -> /admin/index */
    @GetMapping({"/admin", "/admin/"})
    public String adminRoot() { return "redirect:/admin/index"; }

    /** 管理后台商品列表路径兼容 -> /admin/product */
    @GetMapping("/admin/product/list")
    public String adminProductListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/product"); }

    /** 管理后台订单列表路径兼容 -> /admin/order */
    @GetMapping("/admin/order/list")
    public String adminOrderListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/order"); }

    /** 管理后台用户列表路径兼容 -> /admin/user */
    @GetMapping("/admin/user/list")
    public String adminUserListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/user"); }

    /** 管理后台优惠券列表路径兼容 -> /admin/coupon */
    @GetMapping("/admin/coupon/list")
    public String adminCouponListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/coupon"); }

    /** 管理后台公告列表路径兼容 -> /admin/announcement */
    @GetMapping("/admin/announcement/list")
    public String adminAnnouncementListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/announcement"); }

    /** 管理后台评价列表路径兼容 -> /admin/review */
    @GetMapping("/admin/review/list")
    public String adminReviewListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/review"); }

    /** 管理后台消息列表路径兼容 -> /admin/message */
    @GetMapping("/admin/message/list")
    public String adminMessageListAlias(HttpSession session) { return checkAdminOrRedirect(session, "redirect:/admin/message"); }

    /** 管理后台日志列表路径兼容 -> /admin/log */
    @GetMapping("/admin/log/list")
    public String adminLogListAlias(HttpSession session) {
        return checkAdminOrRedirect(session, "redirect:/admin/log");
    }
}
