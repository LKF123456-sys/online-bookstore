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
 * 用户认证 REST API 控制器
 * <p>
 * 职责：为 Vue 前端提供用户认证相关的 RESTful 接口，包括登录、注册、
 *       个人信息查询与修改、密码修改、登出等功能。
 * <p>
 * 所属模块：bookstore-admin · controller · api
 * <p>
 * 认证模式：支持 JWT Token + Session 双认证模式，逐步迁移到纯 JWT 无状态认证。
 *           接口同时接受 Session（兼容旧 JSP 页面）和请求头 X-User-Id（JWT 解析后传入）。
 * <p>
 * 包含接口：
 * <ul>
 *   <li>POST /api/auth/login      — 用户登录</li>
 *   <li>POST /api/auth/register   — 用户注册</li>
 *   <li>GET  /api/auth/profile    — 获取当前用户信息</li>
 *   <li>PUT  /api/auth/profile    — 更新用户资料</li>
 *   <li>PUT  /api/auth/password   — 修改密码</li>
 *   <li>POST /api/auth/logout     — 登出</li>
 * </ul>
 *
 * @author bookstore
 */
// @Slf4j：Lombok 注解，自动生成 log 日志对象，用于记录运行时日志
@Slf4j
// @RestController：Spring MVC 注解，标识该类为 REST 控制器，
// 所有方法返回值自动序列化为 JSON 响应体
@RestController
// @RequestMapping：将控制器映射到 /api/auth 路径下，所有接口 URL 以此为前缀
@RequestMapping("/api/auth")
// @RequiredArgsConstructor：Lombok 注解，为所有 final 字段生成构造方法，
// Spring 自动注入对应的 Bean
@RequiredArgsConstructor
public class AuthRestController {

    // 用户服务层依赖，处理用户登录、注册、信息查询等核心业务逻辑
    private final UserService userService;

    // ========================================================================
    // 用户登录接口
    // ========================================================================

    /**
     * 用户登录
     * <p>
     * 接收登录凭证（用户名 / 密码），调用 UserService 完成身份验证，
     * 成功后返回 JWT Token 和用户基本信息，同时写入 Session 以兼容旧版 JSP 页面。
     *
     * @param dto     登录请求体，包含 username 和 password，由 @Valid 触发 JSR-303 校验
     * @param session HTTP 会话对象，用于双写 Session（兼容模式）
     * @return Result 包含 JWT Token 和用户信息 Map 的成功响应，
     *         业务失败时抛 BusinessException
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/auth/login
    @PostMapping("/login")
    // @Valid：触发 LoginDTO 上的 JSR-303 Bean Validation 校验注解（如 @NotBlank）
    // @RequestBody：将 HTTP 请求体 JSON 反序列化为 LoginDTO 对象
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpSession session) {
        // 记录登录请求日志，仅输出用户名，避免在日志中泄露密码
        log.info("REST API 登录请求: username={}", dto.getUsername());
        // 调用 UserService 执行登录业务逻辑，返回包含 token 和 user 的 Result
        Result<Map<String, Object>> result = userService.login(dto);
        // 如果业务返回码不是 200（成功）或数据为空，说明登录失败
        if (result.getCode() != 200 || result.getData() == null) {
            // 将业务异常消息抛出，由全局异常处理器统一处理
            throw new BusinessException(result.getMessage());
        }
        // 提取返回数据中的 user 和 token 信息
        Map<String, Object> data = result.getData();
        // 双写 Session：将用户信息存入 Session，兼容旧的 JSP 页面通过 Session 读取
        session.setAttribute("user", data.get("user"));
        // 双写 Session：将 JWT Token 也存入 Session，方便 JSP 页面获取
        session.setAttribute("token", data.get("token"));
        // 返回包含 token 和 user 的完整响应给 Vue 前端
        return result;
    }

    // ========================================================================
    // 用户注册接口
    // ========================================================================

    /**
     * 用户注册
     * <p>
     * 接收注册信息（用户名、密码、邮箱等），调用 UserService 创建新账户。
     *
     * @param dto 注册请求体，包含注册所需的用户信息，由 @Valid 触发校验
     * @return Result 包含注册成功后用户信息的 Map，业务失败时 Service 层抛异常
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/auth/register
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        // 记录注册请求日志，仅输出用户名
        log.info("REST API 注册请求: username={}", dto.getUsername());
        // 调用 UserService 执行注册逻辑
        return userService.register(dto);
    }

    // ========================================================================
    // 获取当前用户信息接口
    // ========================================================================

    /**
     * 获取当前登录用户的个人信息
     * <p>
     * 优先从 Session 中读取用户信息（兼容旧模式），
     * 若 Session 中无数据则从请求头 X-User-Id 获取用户 ID 后查询。
     *
     * @param session      HTTP 会话对象，兼容模式下从中读取 user 属性
     * @param userIdHeader 请求头 X-User-Id，由网关 / 拦截器解析 JWT 后传入的用户 ID
     * @return Result 包含用户信息的成功响应；未登录时抛 BusinessException(401)
     */
    // @GetMapping：将 HTTP GET 请求映射到 /api/auth/profile
    @GetMapping("/profile")
    public Result<?> getProfile(HttpSession session,
                                 // @RequestHeader：从 HTTP 请求头中获取指定字段值
                                 // required=false 表示该请求头非必填
                                 @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 优先从 Session 获取用户信息（兼容旧的 JSP 页面模式）
        Object user = session.getAttribute("user");
        if (user != null) {
            // Session 中存在用户信息，直接返回
            return Result.success(user);
        }
        // Session 中无数据，尝试从请求头 X-User-Id 获取用户 ID
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            // 通过用户 ID 查询完整的用户资料
            return userService.getProfile(userIdHeader);
        }
        // 两种方式均无法获取用户身份，抛出 401 未登录异常
        throw new BusinessException(401, "未登录");
    }

    // ========================================================================
    // 更新用户资料接口
    // ========================================================================

    /**
     * 更新当前登录用户的个人信息（昵称、邮箱、手机号等）
     * <p>
     * 同样支持 Session 和 Header 两种方式获取用户身份。
     *
     * @param updates      请求体，包含需要更新的字段键值对（如 nickname、email）
     * @param session      HTTP 会话对象
     * @param userIdHeader 请求头 X-User-Id
     * @return Result 空成功响应，表示更新操作完成
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /api/auth/profile
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> updates,
                                       HttpSession session,
                                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 优先使用请求头中的用户 ID
        String userId = userIdHeader;
        // 请求头中没有用户 ID，尝试从 Session 中提取
        if (userId == null || userId.isEmpty()) {
            // 从 Session 中获取当前登录用户信息
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            // 从用户 Map 中提取 userid 字段
            if (user != null) userId = String.valueOf(user.get("userid"));
        }
        // 两种方式都无法获取用户 ID，说明用户未登录
        if (userId == null || userId.isEmpty()) throw new BusinessException(401, "未登录");
        // 调用 UserService 执行资料更新
        return userService.updateProfile(userId, updates);
    }

    // ========================================================================
    // 修改密码接口
    // ========================================================================

    /**
     * 修改当前登录用户的密码
     * <p>
     * 需要提供原密码和新密码，验证原密码正确后才能修改。
     *
     * @param passwords    请求体，包含旧密码 (oldPassword) 和新密码 (newPassword)
     * @param session      HTTP 会话对象
     * @param userIdHeader 请求头 X-User-Id
     * @return Result 空成功响应
     */
    // @PutMapping：将 HTTP PUT 请求映射到 /api/auth/password
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> passwords,
                                        HttpSession session,
                                        @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 优先使用请求头中的用户 ID
        String userId = userIdHeader;
        // 请求头中没有用户 ID，尝试从 Session 中获取
        if (userId == null || userId.isEmpty()) {
            // 从 Session 中提取当前登录用户信息
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            // 取出用户标识
            if (user != null) userId = String.valueOf(user.get("userid"));
        }
        // 未能确定用户身份，拒绝操作
        if (userId == null || userId.isEmpty()) throw new BusinessException(401, "未登录");
        // 调用 UserService 执行密码修改
        return userService.updatePassword(userId, passwords);
    }

    // ========================================================================
    // 登出接口
    // ========================================================================

    /**
     * 用户登出
     * <p>
     * 使当前 HTTP 会话失效（invalidate），清除服务端 Session 中保存的所有用户信息。
     *
     * @param session HTTP 会话对象，调用 invalidate() 使其失效
     * @return Result 空成功响应
     */
    // @PostMapping：将 HTTP POST 请求映射到 /api/auth/logout
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        // 使 Session 失效，清除所有已存储的会话属性（用户信息、Token 等）
        session.invalidate();
        // 返回成功
        return Result.success();
    }
}
