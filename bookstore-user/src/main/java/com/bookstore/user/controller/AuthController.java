package com.bookstore.user.controller;  // 声明当前类所在的包路径，这里是控制器层

import com.bookstore.common.api.Result;  // 导入统一结果封装类，用于包装所有接口的返回数据
import com.bookstore.common.api.dto.LoginDTO;  // 导入登录数据传输对象（DTO），包含用户名和密码等登录信息
import com.bookstore.common.api.dto.RegisterDTO;  // 导入注册数据传输对象（DTO），包含用户名、密码、邮箱等注册信息
import com.bookstore.user.service.AccountService;  // 导入用户账户服务类，负责处理用户相关的业务逻辑
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;  // 导入HTTP请求对象，用于获取请求头中的token
import jakarta.validation.Valid;  // 导入参数校验注解，用于触发对DTO对象的字段校验（如非空、格式等）
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造函数，用于依赖注入
import org.springframework.web.bind.annotation.*;  // 导入Spring Web相关的注解（@RestController、@RequestMapping、@PostMapping等）

import java.util.Map;  // 导入Map集合类，用于存放键值对数据

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关请求。
 * <p>
 * 该控制器提供以下接口：
 * - POST /api/auth/login：用户登录
 * - POST /api/auth/register：用户注册
 * <p>
 * 注解说明：
 * - @RestController：标记这是一个REST风格的控制器，所有方法的返回值会自动序列化为JSON作为HTTP响应体
 * - @RequestMapping("/api/auth")：统一设置该控制器下所有接口的URL前缀为 /api/auth
 * - @RequiredArgsConstructor：Lombok注解，自动生成包含所有final字段的构造函数，实现构造函数注入
 */
@RestController  // REST控制器注解，方法返回值直接作为HTTP响应体（JSON格式）
@RequestMapping("/api/auth")  // 请求映射，该控制器下所有接口的URL前缀为 /api/auth
@RequiredArgsConstructor  // Lombok注解，自动生成包含final成员变量的构造函数，用于Spring的构造函数依赖注入
public class AuthController {

    private final AccountService accountService;  // 用户账户服务，处理登录、注册等用户相关的业务逻辑

    /**
     * 用户登录接口
     * 接收用户提交的用户名和密码，验证通过后返回JWT Token和用户信息。
     *
     * @param dto 登录信息对象（DTO），包含用户名（username）和密码（password），由前端JSON自动转换而来
     * @return 统一结果对象，成功时包含token和用户信息（Map），失败时包含错误提示信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        Map<String, Object> result = accountService.login(dto);
        String token = (String) result.get("token");
        ResponseCookie cookie = ResponseCookie.from("BOOKSTORE_TOKEN", token)
                .httpOnly(true).secure(false).path("/").maxAge(86400).sameSite("Lax").build();
        response.addHeader("Set-Cookie", cookie.toString());
        return Result.success(result);
    }

    /**
     * 用户注册接口
     * 接收用户提交的注册信息，创建新用户账户。注册成功返回空数据的成功结果。
     *
     * @param dto 注册信息对象（DTO），包含用户名、密码、邮箱、手机号等，由前端JSON自动转换而来
     * @return 统一结果对象，成功时无额外数据，失败时包含错误提示信息
     */
    @PostMapping("/register")  // POST请求映射，处理 /api/auth/register 路径的注册请求
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {  // @Valid 触发参数校验，@RequestBody 将JSON请求体转换为RegisterDTO对象
        accountService.register(dto);  // 调用服务层的注册方法，创建新用户
        return Result.success();  // 注册成功，返回不带数据的成功结果
    }

    /**
     * 用户登出接口
     * 将当前 JWT Token 加入 Redis 黑名单，使其立即失效。
     * Gateway AuthFilter 会检查黑名单，被撤销的 token 将返回 401。
     *
     * @param request HTTP 请求对象，用于从请求属性中获取 Gateway 注入的原始 token
     * @return 统一结果对象，成功时无额外数据
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = (String) request.getAttribute("authToken");
        accountService.logout(token);
        ResponseCookie cookie = ResponseCookie.from("BOOKSTORE_TOKEN", "")
                .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();
        response.addHeader("Set-Cookie", cookie.toString());
        return Result.success();
    }
}
