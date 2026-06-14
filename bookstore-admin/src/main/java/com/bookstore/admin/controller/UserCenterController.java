package com.bookstore.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bookstore.admin.service.AdminLogService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户中心控制器 - 处理用户个人中心及相关功能页面
 *
 * 职责范围：
 * 1. 用户个人资料页面（GET /user/profile）
 * 2. 用户优惠券列表页面（GET /user/coupon）
 * 3. 用户消息列表页面（GET /user/message）
 * 4. 商品评价功能：查看评价页面、提交评价、管理评价、删除评价
 * 5. 商品评价查看：展示某个商品的所有评价（GET /product/review）
 * 6. 取消订单（POST /order/cancel）
 * 7. 静态信息页面：配送说明、退换货政策、帮助中心、关于我们、招聘、合作伙伴、投诉建议、浏览历史
 *
 * 工作原理：
 * - 大部分功能需要用户登录，通过Session中的user属性判断
 * - 通过RestTemplate调用订单服务（bookstore-order）和营销服务（bookstore-promotion）获取数据
 * - 评价提交支持单商品评价和整个订单批量评价
 */
@Slf4j
@Controller
public class UserCenterController extends BaseController {

    public UserCenterController(RestTemplate restTemplate, AdminLogService adminLogService) {
        super(restTemplate, adminLogService);
    }

    // ======================== 用户中心页面 ========================

    /** 用户个人资料页面 */
    @GetMapping("/user/profile")
    public String userProfile() { return "user/profile"; }

    /** 用户优惠券列表页面 */
    @GetMapping("/user/coupon")
    public String userCoupon() { return "user/coupon/list"; }

    /** 用户消息列表页面 */
    @GetMapping("/user/message")
    public String userMessage() { return "user/message/list"; }

    // ======================== 评价相关 ========================

    /**
     * 商品评价页面
     * 加载订单的商品列表，供用户选择评价对象并提交评价
     *
     * @param orderId 订单ID（query参数）
     * @param productId 商品ID（可选，如果指定则评特定商品）
     * @param model Model对象
     * @param session HTTP会话
     * @return 评价页面视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/review")
    public String review(@RequestParam(required = false) String orderId,
                         @RequestParam(required = false) String productId,
                         Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = (String) user.get("userid");
        if (orderId != null && !orderId.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/" + orderId,
                    HttpMethod.GET, entity, String.class);
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    Object itemsObj = data.get("items");
                    if (itemsObj instanceof List) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
                        model.addAttribute("items", items);
                        model.addAttribute("orderId", orderId);
                        if (productId != null && !productId.isEmpty()) {
                            model.addAttribute("productId", productId);
                        }
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "加载订单信息失败");
            }
        }
        model.addAttribute("cartSize", 0);
        return "review";
    }

    /** 用户评价管理页面 - 查看自己发表的所有评价 */
    @SuppressWarnings("unchecked")
    @GetMapping("/review/manage")
    public String reviewManage(Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        String userId = (String) user.get("userid");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                "http://bookstore-promotion/api/review/my?pageNum=1&pageSize=50",
                HttpMethod.GET, entity, String.class);
            Map<String, Object> result = parseJson(resp.getBody());
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null && data.get("records") != null) {
                java.util.List<?> records = (java.util.List<?>) data.get("records");
                for (Object r : records) deepConvertDates(r);
                model.addAttribute("reviews", records);
            } else {
                model.addAttribute("reviews", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("reviews", new ArrayList<>());
        }
        return "review_manage";
    }

    /**
     * 删除评价
     *
     * @param reviewId 评价ID
     * @param session HTTP会话
     * @param redirectAttributes 重定向属性
     * @return 重定向回评价管理页
     */
    @PostMapping("/review/delete")
    public String deleteReview(@RequestParam String reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        String userId = (String) user.get("userid");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                "http://bookstore-promotion/admin/review/" + reviewId,
                HttpMethod.DELETE, entity, String.class);
            redirectAttributes.addFlashAttribute("success", "评价已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除评价失败");
        }
        return "redirect:/review/manage";
    }

    /**
     * 提交商品评价（表单提交接口）
     * 用户对已购买的商品进行评价，评价会存储到营销服务中
     * 如果productId为空，则对订单中所有未评价商品逐一评价
     *
     * @param orderId 订单ID（关联评价的订单）
     * @param productId 商品ID（被评价的商品，空字符串表示评价整个订单）
     * @param rating 评分（1-5星）
     * @param content 评价内容
     * @param imageFile 晒图文件名（可选，前端表单中的file字段名）
     * @param session HTTP会话
     * @return 提交成功重定向到对应订单详情页
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/review/submit")
    public String submitReview(@RequestParam String orderId,
                               @RequestParam(required = false, defaultValue = "") String productId,
                               @RequestParam int rating,
                               @RequestParam String content,
                               @RequestParam(required = false) MultipartFile imageFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = (String) user.get("userid");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "用户信息异常");
            return "redirect:/review?orderId=" + orderId;
        }
        try {
            java.util.List<String> productIdsToReview = new java.util.ArrayList<>();
            if (productId == null || productId.isEmpty()) {
                // 未指定具体商品 -> 评价整个订单
                HttpHeaders oh = new HttpHeaders(); oh.set("X-User-Id", userId);
                HttpEntity<Void> oe = new HttpEntity<>(oh);
                ResponseEntity<String> orderResp = restTemplate.exchange(
                    "http://bookstore-order/api/order/" + orderId,
                    HttpMethod.GET, oe, String.class);
                Map<String, Object> orderResult = parseJson(orderResp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) orderResult.get("data");
                if (orderData != null) {
                    Object itemsObj = orderData.get("items");
                    if (itemsObj instanceof List) {
                        for (Object it : (List<?>) itemsObj) {
                            if (it instanceof Map) {
                                Object pidObj = ((Map<?, ?>) it).get("productId");
                                String pid = pidObj != null ? String.valueOf(pidObj) : null;
                                if (pid != null) productIdsToReview.add(pid);
                            }
                        }
                    }
                }
            } else {
                productIdsToReview.add(productId);
            }

            if (productIdsToReview.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "未找到需要评价的商品");
                return "redirect:/order/detail?orderId=" + orderId;
            }

            int successCount = 0;
            for (String pid : productIdsToReview) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("productId", pid);
                reviewData.put("rating", rating);
                reviewData.put("content", content);
                reviewData.put("image", (imageFile != null && !imageFile.isEmpty()) ? imageFile.getOriginalFilename() : "");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-User-Id", userId);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reviewData, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://bookstore-promotion/api/review", entity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount++;
                }
            }
            if (successCount > 0) {
                if (productIdsToReview.size() > 1) {
                    redirectAttributes.addFlashAttribute("success",
                        "已成功评价 " + successCount + "/" + productIdsToReview.size() + " 件商品");
                } else {
                    redirectAttributes.addFlashAttribute("success", "评价提交成功");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "评价提交失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "评价提交失败：" + e.getMessage());
        }
        return "redirect:/order/detail?orderId=" + orderId;
    }

    // ======================== 取消订单 ========================

    /**
     * 取消订单
     * 将待支付订单状态设置为"已取消"
     *
     * @param orderId 订单ID
     * @param session HTTP会话
     * @param redirectAttributes 重定向属性（用于传递提示消息）
     * @return 重定向到订单历史页
     */
    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam String orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = (String) user.get("userid");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                "http://bookstore-order/api/order/" + orderId + "/cancel",
                HttpMethod.POST, entity, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("success", "订单已取消");
            } else {
                redirectAttributes.addFlashAttribute("error", "取消订单失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "取消订单失败：" + e.getMessage());
        }
        return "redirect:/order/history";
    }

    // ======================== 商品评价查看 ========================

    /**
     * 商品评价查看页面 - 展示某个商品的所有评价
     *
     * @param productId 商品ID
     * @param model Model对象
     * @return 商品评价页面视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/product/review")
    public String productReview(@RequestParam String productId, Model model) {
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-promotion/api/review/product/" + productId + "?pageNum=1&pageSize=50", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null && data.get("records") != null) {
                java.util.List<?> records = (java.util.List<?>) data.get("records");
                for (Object r : records) deepConvertDates(r);
                model.addAttribute("reviews", records);
                // 计算平均评分
                double avgRating = 0;
                int reviewCount = records.size();
                if (reviewCount > 0) {
                    double sum = 0;
                    for (Object r : records) {
                        if (r instanceof Map) {
                            Object rating = ((Map<?, ?>) r).get("rating");
                            if (rating != null) sum += ((Number) rating).doubleValue();
                        }
                    }
                    avgRating = sum / reviewCount;
                }
                model.addAttribute("avgRating", avgRating);
                model.addAttribute("reviewCount", reviewCount);
            } else {
                model.addAttribute("reviews", new ArrayList<>());
                model.addAttribute("avgRating", 0.0);
                model.addAttribute("reviewCount", 0);
            }
        } catch (Exception e) {
            model.addAttribute("reviews", new ArrayList<>());
            model.addAttribute("avgRating", 0.0);
            model.addAttribute("reviewCount", 0);
        }
        return "product_review";
    }

    // ======================== 静态信息页面 ========================

    /** 配送说明页面 */
    @GetMapping("/shipping")
    public String shipping() { return "shipping"; }

    /** 退换货政策页面 */
    @GetMapping("/return-policy")
    public String returnPolicy() { return "return_policy"; }

    /** 帮助中心页面 */
    @GetMapping("/help")
    public String help() { return "help"; }

    /** 关于我们页面 */
    @GetMapping("/about")
    public String about() { return "about"; }

    /** 招聘信息页面 */
    @GetMapping("/careers")
    public String careers() { return "careers"; }

    /** 合作伙伴页面 */
    @GetMapping("/partners")
    public String partners() { return "partners"; }

    /** 投诉建议页面 */
    @GetMapping("/complaint")
    public String complaint() { return "complaint"; }

    /** 浏览历史页面 */
    @GetMapping("/history")
    public String history() { return "history"; }
}
