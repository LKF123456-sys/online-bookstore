package com.bookstore.admin.controller;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.bookstore.admin.service.AdminLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

/**
 * 控制器基类 - 提供所有页面控制器共用的工具方法
 *
 * 本类封装了以下通用功能：
 * 1. JSON解析与日期转换（parseJson / deepConvertDates）
 * 2. 管理员会话校验（checkAdminSession / checkAdminOrRedirect）
 *
 * 所有拆分后的控制器均继承此类，以复用核心工具方法，避免代码重复。
 */
public abstract class BaseController {

    /** RestTemplate用于向其他微服务发送HTTP请求（通过服务名进行负载均衡调用） */
    protected final RestTemplate restTemplate;

    /** 操作日志服务，用于记录管理员的操作记录 */
    protected final AdminLogService adminLogService;

    /** Jackson的ObjectMapper实例，用于将JSON字符串转换为Java对象 */
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected BaseController(RestTemplate restTemplate, AdminLogService adminLogService) {
        this.restTemplate = restTemplate;
        this.adminLogService = adminLogService;
    }

    /**
     * 递归将Map中的日期字符串转换为java.util.Date对象
     * 解决JSP中使用JSTL的fmt:formatDate标签时类型不匹配的问题
     *
     * 为什么需要这个方法？
     * 从微服务API获取的数据是JSON格式，日期会被序列化为字符串（如"2024-01-15 10:30:00"）。
     * 但JSP的fmt:formatDate标签需要java.util.Date类型的对象才能正确格式化显示。
     * 所以需要在Controller层把日期字符串预先转换为Date对象。
     *
     * @param data 要处理的数据对象，可以是Map、List或其他类型
     */
    @SuppressWarnings("unchecked")
    protected void deepConvertDates(Object data) {
        if (data == null) return;
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    String str = (String) value;
                    if (str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") ||
                        str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                str.length() == 19 ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm");
                            entry.setValue(sdf.parse(str));
                        } catch (Exception ignored) {}
                    }
                } else if (value instanceof Map) {
                    deepConvertDates(value);
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        deepConvertDates(item);
                    }
                }
            }
        } else if (data instanceof List) {
            for (Object item : (List<?>) data) {
                deepConvertDates(item);
            }
        }
    }

    /**
     * 将JSON字符串解析为Map对象，并自动转换其中的日期字符串
     *
     * @param json JSON格式的字符串
     * @return 解析后的Map对象，日期字段已被转换为java.util.Date类型
     * @throws Exception JSON解析失败时抛出异常
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseJson(String json) throws Exception {
        Map<String, Object> result = objectMapper.readValue(json, Map.class);
        deepConvertDates(result);
        return result;
    }

    /**
     * 检查管理员是否已登录，未登录则返回重定向路径，已登录返回null
     *
     * @param session HTTP会话
     * @return 未登录时返回重定向字符串，已登录时返回null
     */
    protected String checkAdminSession(HttpSession session) {
        return session.getAttribute("admin") == null ? "redirect:/admin/login" : null;
    }

    /**
     * 检查管理员session，未认证重定向到登录页，已认证返回指定的重定向目标
     *
     * @param session HTTP会话
     * @param redirectTarget 已认证时的重定向目标
     * @return 未认证时重定向到管理员登录页，已认证时返回redirectTarget
     */
    protected String checkAdminOrRedirect(HttpSession session, String redirectTarget) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        return redirectTarget;
    }
}
