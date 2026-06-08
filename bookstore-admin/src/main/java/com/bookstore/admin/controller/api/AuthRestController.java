package com.bookstore.admin.controller.api;

import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.admin.service.UserService;
import com.bookstore.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证 REST API — 为 Vue 前端提供认证接口
 * 支持 JWT Token + Session 双认证模式，逐步迁移到纯 JWT 无状态认证
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;

    /** 用户登录 — 返回 JWT Token + 用户信息 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpSession session) {
        log.info("REST API 登录请求: username={}", dto.getUsername());
        Result<Map<String, Object>> result = userService.login(dto);
        if (result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(result.getMessage());
        }
        Map<String, Object> data = result.getData();
        // 双写 Session（兼容旧 JSP）+ 返回 JWT（Vue 使用）
        session.setAttribute("user", data.get("user"));
        session.setAttribute("token", data.get("token"));
        return result;
    }

    /** 用户注册 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        log.info("REST API 注册请求: username={}", dto.getUsername());
        return userService.register(dto);
    }

    /** 获取当前用户信息（从 Session 或 JWT） */
    @GetMapping("/profile")
    public Result<?> getProfile(HttpSession session,
                                 @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 优先从 Session 获取（兼容模式）
        Object user = session.getAttribute("user");
        if (user != null) {
            return Result.success(user);
        }
        // 从 Header 获取
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            return userService.getProfile(userIdHeader);
        }
        throw new BusinessException(401, "未登录");
    }

    /** 更新用户资料 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> updates,
                                       HttpSession session,
                                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        String userId = userIdHeader;
        if (userId == null || userId.isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            if (user != null) userId = String.valueOf(user.get("userid"));
        }
        if (userId == null || userId.isEmpty()) throw new BusinessException(401, "未登录");
        return userService.updateProfile(userId, updates);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> passwords,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        String userId = userIdHeader;
        if (userId == null || userId.isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            if (user != null) userId = String.valueOf(user.get("userid"));
        }
        if (userId == null || userId.isEmpty()) throw new BusinessException(401, "未登录");
        return userService.updatePassword(userId, passwords);
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }
}
