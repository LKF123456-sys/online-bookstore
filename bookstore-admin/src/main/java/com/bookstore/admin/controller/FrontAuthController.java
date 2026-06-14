package com.bookstore.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bookstore.admin.service.AdminLogService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台认证控制器 - 处理用户登录、注册和登出
 *
 * 职责范围：
 * 1. 前台用户登录（POST /login）
 * 2. 前台用户注册（POST /register）
 * 3. 前台用户登出（POST /logout、GET /logout）
 * 4. 登录/注册页面展示（GET /login、GET /register）
 *
 * 工作原理：
 * - 登录和注册请求通过RestTemplate转发到用户微服务（bookstore-user）进行认证
 * - 登录成功后将token和用户信息存入HttpSession
 * - 登出时销毁Session清除所有登录状态
 */
@Slf4j
@Controller
public class FrontAuthController extends BaseController {

    public FrontAuthController(RestTemplate restTemplate, AdminLogService adminLogService) {
        super(restTemplate, adminLogService);
    }

    /**
     * 处理前台用户登录请求
     * 将用户名和密码发送到用户服务进行验证，验证成功后将token和用户信息存入Session
     *
     * @param userid 用户输入的用户名/账号
     * @param password 用户输入的密码
     * @param session 当前HTTP会话，用于存储登录状态
     * @param redirectAttributes 重定向属性，用于在重定向时传递错误消息
     * @return 登录成功跳转首页，失败跳转登录页
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam String userid, @RequestParam String password,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("username", userid);
            body.put("password", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/login", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = parseJson(response.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                session.setAttribute("token", data.get("token"));
                session.setAttribute("user", data.get("user"));
                return "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
            return "redirect:/login";
        }
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        return "redirect:/login";
    }

    /**
     * 处理前台用户注册请求，将注册信息发送到用户服务创建新账号
     *
     * @param userid 用户名/账号
     * @param password 密码
     * @param email 邮箱地址
     * @param phone 手机号码（可选）
     * @param redirectAttributes 重定向属性，用于传递成功或失败消息
     * @return 注册成功跳转登录页，失败跳转注册页
     */
    @PostMapping("/register")
    public String doRegister(@RequestParam String userid, @RequestParam String password,
                             @RequestParam String email, @RequestParam(required = false) String phone,
                             RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("username", userid);
            body.put("password", password);
            body.put("email", email);
            body.put("phone", phone != null ? phone : "");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/register", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("msg", "注册成功，请登录");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注册失败: " + e.getMessage());
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("error", "注册失败");
        return "redirect:/register";
    }

    /** 登录页面 */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /** 注册页面 */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 前台用户登出（POST），销毁Session清除所有登录状态
     *
     * @param session HTTP会话
     * @return 重定向到登录页
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /** 前台用户登出（GET），兼容直接链接访问 */
    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
