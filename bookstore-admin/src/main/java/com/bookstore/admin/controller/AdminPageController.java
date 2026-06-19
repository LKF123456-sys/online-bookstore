package com.bookstore.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bookstore.admin.service.AdminLogService;
import com.bookstore.common.api.vo.PageResult;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理后台页面控制器 - 处理所有管理后台的视图请求
 *
 * 职责范围：
 * 1. 管理员认证：登录（POST /admin/login）、登出（POST/GET /admin/logout）
 * 2. 数据大屏：展示统计数据和可视化图表（GET /admin/dashboard）
 * 3. 商品管理：列表、添加、编辑、库存、热销排行（GET /admin/product*）
 * 4. 用户管理：列表、添加、编辑（GET /admin/user*）
 * 5. 订单管理：列表、详情（GET /admin/order*）
 * 6. 优惠券管理：列表（GET /admin/coupon）
 * 7. 评价管理：列表（GET /admin/review）
 * 8. 消息管理：列表（GET /admin/message）
 * 9. 公告管理：列表（GET /admin/announcement）
 * 10. 操作日志：列表（GET /admin/log）
 * 11. 分类管理：列表（GET /admin/categories）
 *
 * 工作原理：
 * - 所有管理后台页面均需管理员登录（Session中需有"admin"属性）
 * - 通过RestTemplate调用各微服务的admin接口获取管理数据
 * - 仪表盘数据由多个微服务聚合而成
 * - 操作日志通过本地AdminLogService获取
 */
@Slf4j
@Controller
public class AdminPageController extends BaseController {

    public AdminPageController(RestTemplate restTemplate, AdminLogService adminLogService) {
        super(restTemplate, adminLogService);
    }

    // ======================== 管理后台认证 ========================

    /**
     * 管理员登录处理，验证用户名密码并检查管理员权限
     *
     * @param username 管理员用户名
     * @param password 管理员密码
     * @param session HTTP会话
     * @param redirectAttributes 重定向属性
     * @return 成功跳转管理后台首页，失败跳转管理员登录页
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/admin/login")
    public String doAdminLogin(@RequestParam String username, @RequestParam String password,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("username", username);
            body.put("password", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/login", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = parseJson(response.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data == null) {
                    redirectAttributes.addFlashAttribute("error", "登录服务返回异常，请稍后重试");
                    return "redirect:/admin/login";
                }
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                if (user == null) {
                    redirectAttributes.addFlashAttribute("error", "登录服务返回异常，请稍后重试");
                    return "redirect:/admin/login";
                }
                Object role = user.get("role");
                if ("admin".equals(role)) {
                    session.setAttribute("token", data.get("token"));
                    session.setAttribute("admin", user);
                    return "redirect:/admin/index";
                }
                redirectAttributes.addFlashAttribute("error", "该账号角色为 [" + role + "]，无管理员权限");
                return "redirect:/admin/login";
            }
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Connection refused")) {
                redirectAttributes.addFlashAttribute("error", "用户服务未启动，请联系管理员");
            } else if (msg != null && (msg.contains("账号已被禁用") || msg.contains("禁用"))) {
                redirectAttributes.addFlashAttribute("error", "该管理员账号已被禁用");
            } else {
                redirectAttributes.addFlashAttribute("error", "登录失败: " + (msg != null ? msg : "未知错误"));
            }
        }
        return "redirect:/admin/login";
    }

    /** 管理员登出（POST），销毁Session */
    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /** 管理员登出（GET），兼容链接访问 */
    @GetMapping("/admin/logout")
    public String adminLogoutGet(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ======================== 管理后台页面 ========================

    /** 管理后台登录页面 */
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    /**
     * 管理后台首页/仪表盘，展示统计数据和最近公告
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 管理后台首页视图
     */
    @GetMapping("/admin/index")
    public String adminIndex(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        return "redirect:/admin/dashboard";
    }

    /**
     * 管理后台数据大屏页面，展示详细的数据统计和可视化图表
     *
     * @param model Model对象
     * @return 数据大屏视图
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        loadDashboardData(model);
        loadDashboardExtras(model);
        model.addAttribute("pageTitle", "数据大屏");
        return "admin/dashboard";
    }

    /**
     * 加载仪表盘辅助数据：低库存商品、最近操作日志、未读消息数
     *
     * @param model Model对象
     */
    @SuppressWarnings("unchecked")
    private void loadDashboardExtras(Model model) {
        // 低库存商品（按库存升序排列，取前5个）
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/list?pageNum=1&pageSize=5&sortBy=stock&sortOrder=asc", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) model.addAttribute("lowStockItems", data.getOrDefault("records", new ArrayList<>()));
            else model.addAttribute("lowStockItems", new ArrayList<>());
        } catch (Exception e) { log.warn("加载低库存商品列表失败: {}", e.getMessage()); model.addAttribute("lowStockItems", new ArrayList<>()); }
        // 最近操作日志（前5条）
        try {
            PageResult<Map<String, Object>> logResult = adminLogService.getLogList(1, 5, null);
            model.addAttribute("recentLogs", logResult.getRecords());
        } catch (Exception e) { log.warn("加载最近操作日志失败: {}", e.getMessage()); model.addAttribute("recentLogs", new ArrayList<>()); }
        // 未读消息数
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-message/api/message/unread-count", String.class);
            Map<String, Object> result = parseJson(resp);
            Object count = result.get("data");
            long unreadCount = count != null ? ((Number) count).longValue() : 0;
            model.addAttribute("adminUnreadMsg", unreadCount);
            model.addAttribute("adminUnreadMsgCount", unreadCount);
        } catch (Exception e) {
            log.warn("加载未读消息数失败: {}", e.getMessage());
            model.addAttribute("adminUnreadMsg", 0);
            model.addAttribute("adminUnreadMsgCount", 0);
        }
    }

    /**
     * 加载仪表盘核心统计数据
     * 包括：商品总数、分类数、用户总数、订单总数、总销售额、待处理订单数、各状态订单数、热销商品、优惠券总数
     *
     * @param model Model对象
     */
    @SuppressWarnings("unchecked")
    private void loadDashboardData(Model model) {
        long totalProducts = 0, totalOrders = 0, totalUsers = 0, totalCoupons = 0;
        long pendingOrders = 0;
        double totalRevenue = 0;
        java.util.List<?> bestsellers = new ArrayList<>();

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalProducts = ((Number) data.get("total")).longValue();
        } catch (Exception e) { log.warn("获取商品总数失败: {}", e.getMessage()); }

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/category/list", String.class);
            Map<String, Object> result = parseJson(resp);
            java.util.List<?> catList = (java.util.List<?>) result.get("data");
            model.addAttribute("activeProducts", catList != null ? catList.size() : 0);
        } catch (Exception e) { log.warn("获取分类列表失败: {}", e.getMessage()); model.addAttribute("activeProducts", 0); }

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-user/admin/user/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalUsers = ((Number) data.get("total")).longValue();
        } catch (Exception e) { log.warn("获取用户总数失败: {}", e.getMessage()); }

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=10000", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                totalOrders = ((Number) data.get("total")).longValue();
                java.util.List<?> orderList = (java.util.List<?>) data.get("records");
                if (orderList != null) {
                    for (Object obj : orderList) {
                        Map<String, Object> order = (Map<String, Object>) obj;
                        totalRevenue += ((Number) order.getOrDefault("totalprice", 0)).doubleValue();
                    }
                }
            }
        } catch (Exception e) { log.warn("获取订单列表及总销售额失败: {}", e.getMessage()); }

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=待支付", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) pendingOrders = ((Number) data.get("total")).longValue();
        } catch (Exception e) { log.warn("获取待处理订单数失败: {}", e.getMessage()); }

        String[] statuses = {"待支付", "待发货", "已发货", "已完成", "已取消"};
        String[] countKeys = {"pendingCount", "paidCount", "shippingCount", "completedCount", "cancelledCount"};
        for (int i = 0; i < statuses.length; i++) {
            try {
                String resp = restTemplate.getForObject(
                    "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=" + statuses[i], String.class);
                Map<String, Object> result = parseJson(resp);
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                long count = data != null ? ((Number) data.get("total")).longValue() : 0;
                model.addAttribute(countKeys[i], count);
            } catch (Exception e) { log.warn("获取订单状态[{}]数量失败: {}", statuses[i], e.getMessage()); model.addAttribute(countKeys[i], 0); }
        }
        model.addAttribute("pendingPay", model.getAttribute("pendingCount"));
        model.addAttribute("pendingShip", model.getAttribute("paidCount"));

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/hot?limit=5", String.class);
            Map<String, Object> result = parseJson(resp);
            bestsellers = (java.util.List<?>) result.get("data");
            if (bestsellers == null) bestsellers = new ArrayList<>();
        } catch (Exception e) { log.warn("获取热销商品失败: {}", e.getMessage()); }

        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-promotion/admin/coupon/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalCoupons = ((Number) data.get("total")).longValue();
        } catch (Exception e) { log.warn("获取优惠券总数失败: {}", e.getMessage()); }

        model.addAttribute("productCount", totalProducts);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCoupons", totalCoupons);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("hotProducts", bestsellers);
        model.addAttribute("bestsellers", bestsellers);
    }

    // ======================== 商品管理 ========================

    /**
     * 管理后台商品列表页面，支持搜索和分页
     *
     * @param model Model对象
     * @param keyword 搜索关键词（可选）
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 商品列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/product")
    public String adminProductList(Model model,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("productList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
                model.addAttribute("pages", data.get("totalPages"));
            }
        } catch (Exception e) {
            log.warn("加载商品列表失败: {}", e.getMessage());
            model.addAttribute("productList", new ArrayList<>());
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> catResult = parseJson(catResp);
            model.addAttribute("categories", catResult.get("data"));
        } catch (Exception e) {
            log.warn("加载分类列表失败: {}", e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword);
        return "admin/product/list";
    }

    /**
     * 管理后台添加商品页面，加载分类列表供选择
     *
     * @param model Model对象
     * @return 添加商品页视图
     */
    @GetMapping("/admin/product/add")
    public String adminProductAdd(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categories", result.get("data"));
        } catch (Exception e) {
            log.warn("加载分类列表失败: {}", e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
        }
        return "admin/product/add";
    }

    /**
     * 管理后台编辑商品页面，加载商品详情和分类列表
     *
     * @param id 商品ID（可选）
     * @param model Model对象
     * @return 编辑商品页视图
     */
    @GetMapping("/admin/product/edit")
    public String adminProductEdit(@RequestParam(required = false) String id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-product/api/product/" + id, String.class);
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("product", result.get("data"));
            } catch (Exception e) {
                log.warn("加载商品详情失败, id={}: {}", id, e.getMessage());
                model.addAttribute("product", new HashMap<>());
            }
        }
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categories", result.get("data"));
        } catch (Exception e) {
            log.warn("加载分类列表失败: {}", e.getMessage());
            model.addAttribute("categories", new ArrayList<>());
        }
        return "admin/product/edit";
    }

    /**
     * 管理后台库存管理页面，展示所有商品的库存信息
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 库存管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/product/stock")
    public String adminProductStock(Model model,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("productList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
            }
        } catch (Exception e) {
            log.warn("加载库存列表失败: {}", e.getMessage());
            model.addAttribute("productList", new ArrayList<>());
        }
        return "admin/product/stock";
    }

    /**
     * 管理后台热销排行页面，展示热销商品
     *
     * @param model Model对象
     * @return 热销排行页视图
     */
    @GetMapping("/admin/product/bestseller")
    public String adminProductBestseller(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/product/hot?limit=20", String.class);
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("productList", result.get("data"));
        } catch (Exception e) {
            log.warn("加载热销商品列表失败: {}", e.getMessage());
            model.addAttribute("productList", new ArrayList<>());
        }
        return "admin/product/bestseller";
    }

    // ======================== 用户管理 ========================

    /**
     * 管理后台用户列表页面，支持搜索和分页
     *
     * @param model Model对象
     * @param keyword 搜索关键词（可选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/user")
    public String adminUserList(Model model,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-user/admin/user/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("userList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
                model.addAttribute("pages", data.get("totalPages"));
            }
        } catch (Exception e) {
            log.warn("加载用户列表失败: {}", e.getMessage());
            model.addAttribute("userList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword);
        return "admin/user/list";
    }

    /** 管理后台添加用户页面 */
    @GetMapping("/admin/user/add")
    public String adminUserAdd(HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        return "admin/user/add";
    }

    /**
     * 管理后台编辑用户页面，加载用户详情
     *
     * @param id 用户ID（可选）
     * @param model Model对象
     * @return 编辑用户页视图
     */
    @GetMapping("/admin/user/edit")
    public String adminUserEdit(@RequestParam(required = false) String id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-user/api/user/" + id, String.class);
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("user", result.get("data"));
            } catch (Exception e) {
                log.warn("加载用户详情失败, id={}: {}", id, e.getMessage());
                model.addAttribute("user", new HashMap<>());
            }
        }
        return "admin/user/edit";
    }

    // ======================== 订单管理 ========================

    /**
     * 管理后台订单列表页面，支持按状态筛选和分页，展示各状态订单统计
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param status 订单状态筛选（可选）
     * @return 订单列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/order")
    public String adminOrderList(Model model,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String status,
                                 HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        long totalOrders = 0, pendingCount = 0, paidCount = 0, shippingCount = 0, completedCount = 0, cancelledCount = 0;
        double totalRevenue = 0;
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalOrders = ((Number) data.get("total")).longValue();
        } catch (Exception e) { log.warn("获取订单总数失败: {}", e.getMessage()); }
        String[] statuses = {"待支付", "已支付", "已发货", "已完成", "已取消"};
        long[] counts = new long[5];
        for (int i = 0; i < statuses.length; i++) {
            try {
                String resp = restTemplate.getForObject(
                    "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=" + statuses[i], String.class);
                Map<String, Object> result = parseJson(resp);
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                counts[i] = data != null ? ((Number) data.get("total")).longValue() : 0;
            } catch (Exception e) { log.warn("获取订单状态[{}]数量失败: {}", statuses[i], e.getMessage()); }
        }
        pendingCount = counts[0]; paidCount = counts[1]; shippingCount = counts[2];
        completedCount = counts[3]; cancelledCount = counts[4];

        try {
            String url = "http://bookstore-order/admin/order/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            if (status != null && !status.isEmpty()) url += "&status=" + status;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("orderList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
                model.addAttribute("pages", data.get("totalPages"));
                Map<String, Object> pageInfo = new HashMap<>();
                pageInfo.put("total", data.get("total"));
                pageInfo.put("pageNum", data.get("pageNum"));
                pageInfo.put("pageSize", data.get("pageSize"));
                pageInfo.put("pages", data.get("totalPages"));
                pageInfo.put("hasPreviousPage", ((Number) data.get("pageNum")).intValue() > 1);
                pageInfo.put("hasNextPage", data.get("totalPages") != null && ((Number) data.get("pageNum")).intValue() < ((Number) data.get("totalPages")).intValue());
                model.addAttribute("pageInfo", pageInfo);
            }
        } catch (Exception e) {
            log.warn("加载订单列表失败: {}", e.getMessage());
            model.addAttribute("orderList", new ArrayList<>());
        }
        model.addAttribute("selectedStatus", status);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("totalRevenue", totalRevenue);
        return "admin/order/list";
    }

    /**
     * 管理后台订单详情页面
     *
     * @param id 订单ID（可选）
     * @param model Model对象
     * @return 订单详情页视图
     */
    @GetMapping("/admin/order/detail")
    public String adminOrderDetail(@RequestParam(required = false) String id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-order/admin/order/" + id, String.class);
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("order", result.get("data"));
            } catch (Exception e) {
                log.warn("加载订单详情失败, id={}: {}", id, e.getMessage());
                model.addAttribute("order", new HashMap<>());
            }
        }
        return "admin/order/detail";
    }

    // ======================== 优惠券管理 ========================

    /**
     * 管理后台优惠券列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 优惠券列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/coupon")
    public String adminCouponList(Model model,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        long totalCoupons = 0;
        try {
            String url = "http://bookstore-promotion/admin/coupon/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("couponList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
                if (data.get("total") != null) totalCoupons = ((Number) data.get("total")).longValue();
            }
        } catch (Exception e) {
            log.warn("加载优惠券列表失败: {}", e.getMessage());
            model.addAttribute("couponList", new ArrayList<>());
        }
        model.addAttribute("totalCoupons", totalCoupons);
        model.addAttribute("activeCoupons", 0);
        model.addAttribute("totalIssued", 0);
        return "admin/coupon/list";
    }

    // ======================== 评价管理 ========================

    /**
     * 管理后台评价列表页面，支持分页和搜索
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（可选）
     * @param status 状态筛选（可选）
     * @return 评价列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/review")
    public String adminReviewList(Model model,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status,
                                  HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-promotion/admin/review/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("reviewList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
            }
        } catch (Exception e) {
            log.warn("加载评价列表失败: {}", e.getMessage());
            model.addAttribute("reviewList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("statusFilter", status != null ? status : "");
        return "admin/review/list";
    }

    // ======================== 消息管理 ========================

    /**
     * 管理后台消息列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 消息列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/message")
    public String adminMessageList(Model model,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-message/admin/message/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("messageList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
            }
        } catch (Exception e) {
            log.warn("加载消息列表失败: {}", e.getMessage());
            model.addAttribute("messageList", new ArrayList<>());
        }
        model.addAttribute("unreadCount", 0);
        model.addAttribute("sentMessages", new ArrayList<>());
        return "admin/message/list";
    }

    // ======================== 公告管理 ========================

    /**
     * 管理后台公告列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 公告列表管理页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/announcement")
    public String adminAnnouncementList(Model model,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String url = "http://bookstore-promotion/admin/announcement/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("announcementList", data.get("records"));
                model.addAttribute("total", data.get("total"));
                model.addAttribute("pageNum", data.get("pageNum"));
                model.addAttribute("pageSize", data.get("pageSize"));
            }
        } catch (Exception e) {
            log.warn("加载公告列表失败: {}", e.getMessage());
            model.addAttribute("announcementList", new ArrayList<>());
        }
        return "admin/announcement/list";
    }

    // ======================== 操作日志 ========================

    /**
     * 管理后台操作日志列表页面，支持分页和关键词搜索
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（可选）
     * @return 操作日志列表页视图
     */
    @GetMapping("/admin/log")
    public String adminLogList(Model model,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            PageResult<Map<String, Object>> logResult = adminLogService.getLogList(pageNum, pageSize, keyword);
            model.addAttribute("logList", logResult.getRecords());
            model.addAttribute("total", logResult.getTotal());
            model.addAttribute("pageNum", logResult.getPageNum());
            model.addAttribute("pageSize", logResult.getPageSize());
        } catch (Exception e) {
            log.warn("加载操作日志列表失败: {}", e.getMessage());
            model.addAttribute("logList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword);
        return "admin/log/list";
    }

    // ======================== 分类管理 ========================

    /**
     * 管理后台分类列表页面，从商品服务获取分类数据
     *
     * @param model Model对象
     * @return 分类列表管理页视图
     */
    @GetMapping("/admin/categories")
    public String adminCategoryList(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categoryList", result.get("data"));
        } catch (Exception e) {
            log.warn("加载分类列表失败: {}", e.getMessage());
            model.addAttribute("categoryList", new ArrayList<>());
        }
        return "admin/category/list";
    }
}
