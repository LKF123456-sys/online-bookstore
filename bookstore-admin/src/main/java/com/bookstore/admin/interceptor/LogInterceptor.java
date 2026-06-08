package com.bookstore.admin.interceptor; // 声明当前类所在的包路径：拦截器层

// 导入操作日志服务类，用于保存日志记录
import com.bookstore.admin.service.AdminLogService;
// 导入Jakarta Servlet的HTTP请求/响应接口
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// 导入HTTP会话接口
import jakarta.servlet.http.HttpSession;
// 导入Spring的@Component注解，标记这是一个Spring组件
import org.springframework.stereotype.Component;
// 导入Spring MVC的HandlerInterceptor接口，用于实现请求拦截
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map; // 导入Map接口

/**
 * 日志拦截器 - 自动记录管理后台的操作日志
 *
 * 工作原理：
 * 这是一个Spring MVC的拦截器（Interceptor），在每个HTTP请求到达Controller之前或之后执行。
 * 本拦截器在postHandle阶段（Controller处理完毕、视图渲染之前）执行，
 * 自动记录管理员在管理后台的每一次操作（如查看商品列表、编辑用户、删除订单等）。
 *
 * 过滤规则：
 * - 只记录/admin/开头的路径（只关注管理后台操作）
 * - 排除登录相关的路径（login、Login）
 * - 排除API路径（api，API请求由其他机制记录）
 * - 排除GET /admin/index（首页访问太频繁，不记录）
 */
@Component // 标记这是一个Spring组件，会被自动扫描和注册
public class LogInterceptor implements HandlerInterceptor {

    // 注入日志服务，用于保存日志记录
    private final AdminLogService adminLogService;

    /**
     * 构造方法，通过Spring自动注入AdminLogService
     *
     * @param adminLogService 操作日志服务实例
     */
    public LogInterceptor(AdminLogService adminLogService) {
        this.adminLogService = adminLogService; // 注入日志服务
    }

    /**
     * 请求后置处理方法 - 在Controller方法执行完毕后、视图渲染之前执行
     * 用于自动记录管理员的操作日志
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象（通常是Controller中的方法）
     * @param modelAndView 视图模型对象（可以为null）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, org.springframework.web.servlet.ModelAndView modelAndView) {
        String path = request.getRequestURI(); // 获取请求路径
        // 只处理管理后台的路径
        if (!path.startsWith("/admin/")) {
            return; // 非管理后台路径，直接跳过
        }
        // 排除登录相关路径和API路径（这些不需要记录日志）
        if (path.contains("login") || path.contains("Login") || path.contains("api")) {
            return; // 登录和API路径跳过
        }
        String method = request.getMethod(); // 获取HTTP请求方法（GET/POST等）
        // 排除GET /admin/index（首页访问频繁，不记录）
        if ("GET".equalsIgnoreCase(method) && path.contains("/index")) {
            return; // 首页GET请求跳过
        }

        // 获取当前Session（false表示不创建新的Session）
        HttpSession session = request.getSession(false);
        if (session == null) {
            return; // 没有Session，说明未登录，跳过
        }
        @SuppressWarnings("unchecked") // 抑制泛型转换警告
        // 从Session中获取用户信息
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return; // Session中没有用户信息，说明未登录，跳过
        }
        // 获取管理员名称（优先取userid，其次取username，都没有则用"unknown"）
        String adminName = (String) user.getOrDefault("userid", user.getOrDefault("username", "unknown").toString());

        // 根据请求路径和方法解析操作类型描述
        String operation = parseOperation(path, method);
        // 构建操作目标（路径 + 查询参数）
        String targetPath = path + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        // 获取操作者的IP地址
        String ip = request.getRemoteAddr();

        // 保存操作日志到数据库
        adminLogService.saveLog(adminName, operation, targetPath, "成功", ip);
    }

    /**
     * 根据请求路径和HTTP方法解析出人类可读的操作类型描述
     * 例如：/admin/product/add -> "新增图书"，/admin/user/delete -> "删除用户"
     *
     * @param path 请求路径
     * @param method HTTP方法（GET/POST/PUT/DELETE）
     * @return 操作类型的中文描述
     */
    private String parseOperation(String path, String method) {
        String lowerPath = path.toLowerCase(); // 转为小写方便匹配

        // --- 商品管理相关操作 ---
        if (lowerPath.contains("/product/bestseller")) return "热销排行"; // 热销排行查看
        if (lowerPath.contains("/product/stock")) return "库存管理"; // 库存管理
        if (lowerPath.contains("/product/add")) return "新增图书"; // 新增图书
        if (lowerPath.contains("/product/edit")) return "编辑图书"; // 编辑图书
        if (lowerPath.contains("/product/delete")) return "删除图书"; // 删除图书
        if (lowerPath.contains("/product")) return "图书管理"; // 其他图书操作

        // --- 用户管理相关操作 ---
        if (lowerPath.contains("/user/enable")) return "启用用户"; // 启用用户
        if (lowerPath.contains("/user/disable")) return "禁用用户"; // 禁用用户
        if (lowerPath.contains("/user/edit")) return "编辑用户"; // 编辑用户
        if (lowerPath.contains("/user/add")) return "新增用户"; // 新增用户
        if (lowerPath.contains("/user/delete")) return "删除用户"; // 删除用户
        if (lowerPath.contains("/user")) return "用户管理"; // 其他用户操作

        // --- 公告管理相关操作 ---
        if (lowerPath.contains("/announcement/delete")) return "删除公告"; // 删除公告
        if (lowerPath.contains("/announcement")) return "公告管理"; // 其他公告操作

        // --- 评价管理相关操作 ---
        if (lowerPath.contains("/review/reply")) return "回复评价"; // 回复评价
        if (lowerPath.contains("/review/delete")) return "删除评价"; // 删除评价
        if (lowerPath.contains("/review")) return "评价管理"; // 其他评价操作

        // --- 优惠券管理相关操作 ---
        if (lowerPath.contains("/coupon/delete")) return "删除优惠券"; // 删除优惠券
        if (lowerPath.contains("/coupon/add")) return "新增优惠券"; // 新增优惠券
        if (lowerPath.contains("/coupon/edit")) return "编辑优惠券"; // 编辑优惠券
        if (lowerPath.contains("/coupon")) return "优惠券管理"; // 其他优惠券操作

        // --- 其他管理操作 ---
        if (lowerPath.contains("/order")) return "订单管理"; // 订单管理
        if (lowerPath.contains("/dashboard")) return "数据大屏"; // 数据大屏
        if (lowerPath.contains("/log")) return "操作日志"; // 操作日志查看
        if (lowerPath.contains("/message")) return "消息管理"; // 消息管理

        return "后台操作"; // 默认操作描述（未匹配到特定路径时使用）
    }
}
