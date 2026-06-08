package com.bookstore.admin.controller.api;  // 声明包路径：属于 bookstore-admin 模块的 Controller API 层测试

import com.bookstore.admin.service.UserService;  // 导入 UserService 服务类，测试中通过 Mock 模拟其行为
import com.bookstore.common.api.Result;  // 导入统一返回结果类，用于构建和验证 API 响应
import com.bookstore.common.exception.BusinessException;  // 导入业务异常类，测试异常场景
import com.bookstore.common.config.GlobalExceptionHandler;  // 导入全局异常处理器，用于 standaloneSetup 中注册
import com.fasterxml.jackson.databind.ObjectMapper;  // 导入 Jackson 的 ObjectMapper，用于 Java 对象与 JSON 字符串互转
import org.junit.jupiter.api.BeforeEach;  // 导入 JUnit 5 的 @BeforeEach 注解，在每个测试方法执行前运行初始化
import org.junit.jupiter.api.DisplayName;  // 导入 JUnit 5 的 @DisplayName 注解，为测试方法提供可读的中文描述
import org.junit.jupiter.api.Test;  // 导入 JUnit 5 的 @Test 注解，标识方法为测试用例
import org.junit.jupiter.api.extension.ExtendWith;  // 导入 JUnit 5 的 @ExtendWith 注解，用于注册测试扩展
import org.mockito.InjectMocks;  // 导入 Mockito 的 @InjectMocks 注解，自动将 @Mock 对象注入到被测试对象
import org.mockito.Mock;  // 导入 Mockito 的 @Mock 注解，为字段创建 Mock 代理对象
import org.mockito.junit.jupiter.MockitoExtension;  // 导入 Mockito 的 JUnit 5 扩展，初始化 @Mock 和 @InjectMocks
import org.springframework.http.MediaType;  // 导入 Spring 的 MediaType 常量，设置请求/响应的 Content-Type
import org.springframework.mock.web.MockHttpSession;  // 导入 Spring 的 MockHttpSession，用于模拟 HTTP Session
import org.springframework.test.web.servlet.MockMvc;  // 导入 Spring 的 MockMvc，用于模拟发送 HTTP 请求并验证响应
import org.springframework.test.web.servlet.setup.MockMvcBuilders;  // 导入 MockMvcBuilders，用于构建 MockMvc 实例

import java.util.Map;  // 导入 Java 标准库的 Map 接口

import static org.mockito.ArgumentMatchers.*;  // 静态导入 Mockito 所有参数匹配器（any()、anyMap()、eq() 等）
import static org.mockito.Mockito.*;  // 静态导入 Mockito 所有方法（when()、verify()、times() 等）
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // 静态导入 MockMvc 请求构建方法（get()、post()、put() 等）
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // 静态导入 MockMvc 结果匹配器（status()、jsonPath() 等）

/**
 * AuthRestController 控制器的单元测试
 * 
 * @ExtendWith(MockitoExtension.class) 说明：
 *   该注解注册 MockitoExtension 扩展，由 Mockito 为 JUnit 5 提供支持。
 *   作用：
 *   1. 在测试方法执行前，自动为 @Mock 注解的字段创建 Mock 代理对象
 *   2. 自动将 @Mock 对象注入到 @InjectMocks 标注的对象中（通过构造器注入）
 *   3. 在测试方法执行后，自动验证 Mock 对象的使用是否符合预期
 *   无需手动调用 MockitoAnnotations.openMocks(this)
 * 
 * MockMvc 说明：
 *   Spring MVC 测试框架的核心类，无需启动 Servlet 容器即可模拟 HTTP 请求，
 *   通过 perform() 发送请求，链式调用 andExpect() 验证响应状态码、响应头、响应体等内容。
 *   本测试使用 standaloneSetup()（独立配置模式），只加载指定的 Controller 和异常处理器，
 *   不加载完整的 Spring 应用上下文，执行速度更快。
 */
@ExtendWith(MockitoExtension.class)  // 注册 Mockito 为 JUnit 5 扩展，自动初始化 @Mock 和 @InjectMocks
class AuthRestControllerTest {  // AuthRestController 的单元测试类

    private MockMvc mockMvc;  // MockMvc 实例，用于模拟 HTTP 请求和验证响应，在 @BeforeEach 中初始化
    private final ObjectMapper objectMapper = new ObjectMapper();  // Jackson 的 JSON 工具，用于将 Java 对象序列化为 JSON 字符串

    @Mock  // Mockito 注解：为 UserService 创建 Mock 代理对象，所有方法默认返回 null/空集合
    private UserService userService;  // 被 Mock 的 UserService，在测试中通过 when() 定义其行为

    @InjectMocks  // Mockito 注解：将 @Mock 的 userService 自动注入到 authRestController 中
    private AuthRestController authRestController;  // 被测试的 Controller 对象，由 Mockito 自动创建并注入依赖

    /**
     * @BeforeEach 初始化方法 — 每个 @Test 方法执行之前都会先调用此方法
     * 
     * MockMvcBuilders.standaloneSetup() 说明：
     *   独立配置模式：仅加载被测试的 Controller，不加载 Spring 完整上下文。
     *   优点：启动极快，适合单元测试。缺点：不加载拦截器、过滤器等 Web 基础设施。
     * 
     * .setControllerAdvice(new GlobalExceptionHandler()) 说明：
     *   手动注册全局异常处理器到 MockMvc 中，使测试能验证异常处理逻辑
     *   （如 Controller 抛出 BusinessException 时 GlobalExceptionHandler 是否能正确拦截并返回错误响应）
     * 
     * .build() 说明：
     *   构建 MockMvc 实例，返回配置好的对象供测试方法使用
     */
    @BeforeEach  // JUnit 5 注解：标识该方法在每个测试方法执行前运行
    void setUp() {  // 测试初始化方法
        mockMvc = MockMvcBuilders  // 使用 MockMvc 构建器开始配置
                .standaloneSetup(authRestController)  // 独立配置模式：只注册被测试的 AuthRestController
                .setControllerAdvice(new GlobalExceptionHandler())  // 手动注册全局异常处理器，使异常处理逻辑生效
                .build();  // 完成构建，返回配置好的 MockMvc 实例
    }

    /**
     * 测试场景：用户通过正确的用户名和密码登录
     * 验证点：
     *   - HTTP 状态码 = 200（OK）
     *   - 响应 JSON 中 code = 200（业务成功）
     *   - 响应 JSON 中 data.token = "jwt-fake-token"（登录成功返回 token）
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("POST /api/auth/login — 登录成功返回200")  // 测试场景的中文描述
    void shouldLoginSuccessfully() throws Exception {  // 测试方法：验证登录成功场景，throws Exception 因为 MockMvc 操作可能抛异常
        Map<String, Object> data = Map.of("token", "jwt-fake-token", "user", Map.of("userid", "admin"));  // 构造模拟的登录成功返回数据，包含 token 和用户信息
        when(userService.login(any())).thenReturn(Result.success(data));  // 定义 Mock 行为：无论传入什么参数，login() 都返回成功结果

        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "123456"));  // 将登录请求参数序列化为 JSON 字符串

        mockMvc.perform(post("/api/auth/login")  // perform：模拟发送 POST 请求到 /api/auth/login
                        .contentType(MediaType.APPLICATION_JSON)  // 设置请求 Content-Type 为 application/json
                        .content(body))  // 设置请求体为序列化后的 JSON 字符串
                .andExpect(status().isOk())  // 验证 HTTP 响应状态码为 200（OK）
                .andExpect(jsonPath("$.code").value(200))  // 验证响应 JSON 中 code 字段的值为 200（业务成功）
                .andExpect(jsonPath("$.data.token").value("jwt-fake-token"));  // 验证响应 JSON 中 data.token 字段为预期的 token 值
    }

    /**
     * 测试场景：登录时 UserService 返回的 Result 中 data 为 null
     * 验证点：
     *   - Controller 检测到 data 为 null 时抛 BusinessException
     *   - GlobalExceptionHandler 捕获异常并返回 JSON 错误响应
     *   - 响应中 code = 500，message = "登录失败"
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("POST /api/auth/login — Result 返回 null data 时抛 BusinessException")  // 测试场景的中文描述
    void shouldThrowWhenLoginReturnsNullData() throws Exception {  // 测试方法：验证登录返回空数据时的异常处理
        Result<Map<String, Object>> badResult = new Result<>();  // 手动创建 Result 对象（不使用静态工厂方法，模拟异常返回）
        badResult.setCode(500);  // 手动设置错误码为 500
        badResult.setMessage("登录失败");  // 手动设置错误消息
        badResult.setData(null);  // 设置 data 为 null，触发 Controller 中的空数据检查逻辑
        when(userService.login(any())).thenReturn(badResult);  // Mock login() 返回这个包含空 data 的 Result

        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "123456"));  // 构造请求 JSON

        mockMvc.perform(post("/api/auth/login")  // 模拟 POST 请求到 /api/auth/login
                        .contentType(MediaType.APPLICATION_JSON)  // 设置 Content-Type
                        .content(body))  // 设置请求体
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200（BusinessException 无 @ResponseStatus，默认 200）
                .andExpect(jsonPath("$.code").value(500))  // 验证响应 JSON 中 code = 500（来自 BusinessException 的错误码）
                .andExpect(jsonPath("$.message").value("登录失败"));  // 验证响应 JSON 中 message = "登录失败"
    }

    /**
     * 测试场景：用户通过完整信息注册成功
     * 验证点：HTTP 200，code = 200
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("POST /api/auth/register — 注册成功返回200")  // 测试场景的中文描述
    void shouldRegisterSuccessfully() throws Exception {  // 测试方法：验证注册成功场景
        Map<String, Object> data = Map.of("userid", "newuser");  // 构造注册成功返回数据，包含新用户 ID
        when(userService.register(any())).thenReturn(Result.success(data));  // Mock register() 返回成功结果

        String body = objectMapper.writeValueAsString(Map.of(  // 构造注册请求参数并序列化为 JSON
                "username", "newuser",  // 用户名
                "email", "a@b.com",  // 邮箱
                "password", "123456"));  // 密码

        mockMvc.perform(post("/api/auth/register")  // 模拟 POST 请求到 /api/auth/register
                        .contentType(MediaType.APPLICATION_JSON)  // 设置 Content-Type
                        .content(body))  // 设置请求体
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200));  // 验证业务状态码为 200（成功）
    }

    /**
     * 测试场景：用户登出
     * 验证点：HTTP 200，code = 200
     * 注意：登出无需请求体，只需 POST 请求
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("POST /api/auth/logout — 登出成功")  // 测试场景的中文描述
    void shouldLogoutSuccessfully() throws Exception {  // 测试方法：验证登出成功场景
        mockMvc.perform(post("/api/auth/logout"))  // 模拟 POST 请求到 /api/auth/logout（无需请求体）
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200));  // 验证业务状态码为 200（成功）
    }

    /**
     * 测试场景：通过 Session 中存储的用户信息获取用户资料
     * 验证点：
     *   - 携带包含用户信息的 MockHttpSession 发送 GET 请求
     *   - 响应中 code = 200，data.userid = "testuser"
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("GET /api/auth/profile — Session 中有用户时返回用户信息")  // 测试场景的中文描述
    void shouldGetProfileFromSession() throws Exception {  // 测试方法：验证 Session 方式获取用户信息
        MockHttpSession session = new MockHttpSession();  // 创建模拟的 HTTP Session 对象
        session.setAttribute("user", Map.of("userid", "testuser", "email", "a@b.com"));  // 在 Session 中设置 "user" 属性，模拟已登录状态

        mockMvc.perform(get("/api/auth/profile").session(session))  // 模拟 GET 请求到 /api/auth/profile，并附带 Session
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200))  // 验证业务状态码为 200
                .andExpect(jsonPath("$.data.userid").value("testuser"));  // 验证返回的用户 ID 为 "testuser"
    }

    /**
     * 测试场景：通过 HTTP Header X-User-Id 传递用户 ID 获取用户资料（无 Session 降级方案）
     * 验证点：
     *   - 请求 Header 中携带 X-User-Id = "testuser"
     *   - UserService.getProfile() 被调用并返回用户信息
     *   - 响应中 code = 200
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("GET /api/auth/profile — 无 Session 时使用 Header X-User-Id")  // 测试场景的中文描述
    void shouldGetProfileFromHeader() throws Exception {  // 测试方法：验证 Header 方式获取用户信息
        when(userService.getProfile("testuser")).thenReturn((Result) Result.success(Map.of("userid", "testuser")));  // Mock getProfile() 返回成功结果

        mockMvc.perform(get("/api/auth/profile")  // 模拟 GET 请求到 /api/auth/profile
                        .header("X-User-Id", "testuser"))  // 在请求 Header 中设置 X-User-Id，传递用户身份
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200));  // 验证业务状态码为 200
    }

    /**
     * 测试场景：既无 Session 也无 X-User-Id Header 时请求获取用户资料
     * 验证点：
     *   - 响应 code = 401（未登录/未授权）
     *   - 响应 message = "未登录"
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("GET /api/auth/profile — 无 Session 无 Header 返回401")  // 测试场景的中文描述
    void shouldReturn401WhenNotLoggedIn() throws Exception {  // 测试方法：验证未登录时的 401 响应
        mockMvc.perform(get("/api/auth/profile"))  // 模拟 GET 请求到 /api/auth/profile（无 Session，无 Header）
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200（BusinessException 使用默认 200）
                .andExpect(jsonPath("$.code").value(401))  // 验证业务错误码为 401（Unauthorized — 未登录）
                .andExpect(jsonPath("$.message").value("未登录"));  // 验证错误消息为 "未登录"
    }

    /**
     * 测试场景：已登录用户通过 X-User-Id Header 更新个人资料
     * 验证点：
     *   - HTTP 200，code = 200
     *   - verify() 确认 UserService.updateProfile() 被正确调用了一次，且参数匹配
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("PUT /api/auth/profile — 更新用户资料成功")  // 测试场景的中文描述
    void shouldUpdateProfile() throws Exception {  // 测试方法：验证更新用户资料场景
        when(userService.updateProfile(eq("testuser"), anyMap())).thenReturn(Result.success());  // Mock updateProfile()：指定 userId 为 "testuser"，任意 Map 参数，返回成功

        Map<String, Object> updates = Map.of("email", "new@test.com", "phone", "13800138000");  // 构造要更新的字段数据
        String body = objectMapper.writeValueAsString(updates);  // 序列化为 JSON

        mockMvc.perform(put("/api/auth/profile")  // 模拟 PUT 请求到 /api/auth/profile
                        .contentType(MediaType.APPLICATION_JSON)  // 设置 Content-Type
                        .content(body)  // 设置请求体
                        .header("X-User-Id", "testuser"))  // 设置 X-User-Id Header 标识用户身份
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200));  // 验证业务状态码为 200

        verify(userService).updateProfile(eq("testuser"), eq(updates));  // 验证 userService.updateProfile() 被调用，且参数匹配 testuser 和 updates
    }

    /**
     * 测试场景：已登录用户修改密码
     * 验证点：
     *   - HTTP 200，code = 200
     *   - verify() 确认 UserService.updatePassword() 被正确调用，传入旧密码和新密码
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("PUT /api/auth/password — 修改密码成功")  // 测试场景的中文描述
    void shouldUpdatePassword() throws Exception {  // 测试方法：验证修改密码场景
        when(userService.updatePassword(eq("testuser"), anyMap())).thenReturn(Result.success());  // Mock updatePassword() 返回成功

        Map<String, String> passwords = Map.of("oldPassword", "old", "newPassword", "new");  // 构造密码修改数据：旧密码和新密码
        String body = objectMapper.writeValueAsString(passwords);  // 序列化为 JSON

        mockMvc.perform(put("/api/auth/password")  // 模拟 PUT 请求到 /api/auth/password
                        .contentType(MediaType.APPLICATION_JSON)  // 设置 Content-Type
                        .content(body)  // 设置请求体
                        .header("X-User-Id", "testuser"))  // 设置 X-User-Id Header 标识用户身份
                .andExpect(status().isOk())  // 验证 HTTP 状态码为 200
                .andExpect(jsonPath("$.code").value(200));  // 验证业务状态码为 200

        verify(userService).updatePassword(eq("testuser"), eq(passwords));  // 验证 updatePassword 被调用且参数正确
    }
}
