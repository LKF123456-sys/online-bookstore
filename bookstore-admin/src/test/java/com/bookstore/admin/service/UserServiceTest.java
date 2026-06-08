package com.bookstore.admin.service;  // 声明包路径：属于 bookstore-admin 模块的 Service 层测试

import com.bookstore.admin.feign.UserFeignClient;  // 导入 UserFeignClient 接口，UserService 通过它远程调用 user-service 微服务
import com.bookstore.common.api.Result;  // 导入统一返回结果类
import com.bookstore.common.api.dto.LoginDTO;  // 导入登录请求 DTO
import com.bookstore.common.api.dto.RegisterDTO;  // 导入注册请求 DTO
import org.junit.jupiter.api.BeforeEach;  // 导入 JUnit 5 的 @BeforeEach 注解，在每个测试方法前运行初始化
import org.junit.jupiter.api.DisplayName;  // 导入 JUnit 5 的 @DisplayName 注解，提供测试方法的中文描述
import org.junit.jupiter.api.Test;  // 导入 JUnit 5 的 @Test 注解，标识测试方法
import org.junit.jupiter.api.extension.ExtendWith;  // 导入 JUnit 5 的 @ExtendWith 注解，注册测试扩展
import org.mockito.InjectMocks;  // 导入 Mockito 的 @InjectMocks 注解，自动注入 @Mock 依赖
import org.mockito.Mock;  // 导入 Mockito 的 @Mock 注解，创建 Mock 代理对象
import org.mockito.junit.jupiter.MockitoExtension;  // 导入 Mockito 的 JUnit 5 扩展

import java.util.HashMap;  // 导入 HashMap，用于构造测试数据
import java.util.Map;  // 导入 Map 接口

import static org.junit.jupiter.api.Assertions.*;  // 静态导入 JUnit 5 所有断言方法（assertNotNull、assertEquals 等）
import static org.mockito.ArgumentMatchers.*;  // 静态导入 Mockito 参数匹配器（any()、anyString()、anyMap()、eq() 等）
import static org.mockito.Mockito.*;  // 静态导入 Mockito 核心方法（when()、verify()、times() 等）

/**
 * UserService 的单元测试
 * 
 * @ExtendWith(MockitoExtension.class) 说明：
 *   注册 Mockito 扩展，自动完成以下工作：
 *   1. 初始化 @Mock 注解的字段 — 为 UserFeignClient 创建 Mock 代理
 *   2. 初始化 @InjectMocks 注解的字段 — 将 Mock 的 UserFeignClient 通过构造器注入到 UserService
 *   3. 每个测试方法独立，互不影响
 * 
 * 测试策略：
 *   UserService 本质上是 UserFeignClient 的代理层，其方法直接委托给 Feign 客户端调用远程微服务。
 *   因此单元测试的核心是验证：
 *   1. UserService 正确地调用了 UserFeignClient 的对应方法
 *   2. UserService 正确地返回了 Feign 客户端的调用结果
 *   3. 调用参数正确传递
 * 
 * @Mock 注解：标注 UserFeignClient，Mockito 会创建其代理对象，所有方法默认返回 null/空
 * @InjectMocks 注解：标注 UserService，Mockito 将 @Mock 的对象注入其中
 */
@ExtendWith(MockitoExtension.class)  // 注册 Mockito JUnit 5 扩展，自动初始化 Mock 和注入
class UserServiceTest {  // UserService 的单元测试类

    @Mock  // Mockito 注解：为 UserFeignClient 创建 Mock 代理对象，模拟远程 Feign 调用
    private UserFeignClient userFeignClient;  // 被 Mock 的 Feign 客户端，测试中所有 Feign 调用不会真正发出 HTTP 请求

    @InjectMocks  // Mockito 注解：将 @Mock 的 userFeignClient 注入到 userService 中
    private UserService userService;  // 被测试的 Service 对象，由 Mockito 自动创建并注入依赖

    /**
     * @BeforeEach 初始化方法 — 每个 @Test 执行前运行
     * 当前为空实现，因为 Mock 的初始化由 MockitoExtension 自动完成。
     * 保留此方法为后续可能的通用初始化逻辑（如共享测试数据）预留扩展点。
     */
    @BeforeEach  // JUnit 5 注解：每个测试方法执行前都会调用此方法
    void setUp() {  // 初始化方法，当前为空，Mock 初始化由 MockitoExtension 完成
    }

    /**
     * 测试场景：用户登录 — 验证 UserService.login() 正确委托给 UserFeignClient.login()
     * 
     * Mock 行为：
     *   when(userFeignClient.login(any(LoginDTO.class))) — 无论传入什么 LoginDTO，都返回预设的成功结果
     * 
     * 验证点：
     *   - assertNotNull(result)：返回结果不为 null
     *   - assertEquals(200, result.getCode())：业务状态码为 200
     *   - verify(userFeignClient, times(1)).login(dto)：确认 Feign 客户端的 login() 被精确调用了一次
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("用户登录 — 调用 Feign 并返回结果")  // 测试场景的中文描述
    void shouldLoginViaFeign() {  // 测试方法：验证登录委托调用
        LoginDTO dto = new LoginDTO();  // 创建登录请求 DTO 对象
        dto.setUsername("testuser");  // 设置测试用户名
        dto.setPassword("123456");  // 设置测试密码

        Map<String, Object> loginData = new HashMap<>();  // 创建 HashMap 模拟登录成功的返回数据
        loginData.put("token", "jwt-token-xxx");  // 设置 token 字段
        loginData.put("user", Map.of("userid", "testuser"));  // 设置 user 信息（嵌套 Map）
        Result<Map<String, Object>> expected = Result.success(loginData);  // 构建预期的成功结果作为 Mock 返回值

        when(userFeignClient.login(any(LoginDTO.class))).thenReturn(expected);  // 定义 Mock 行为：Feign 客户端 login() 返回预设的成功结果

        Result<Map<String, Object>> result = userService.login(dto);  // 执行被测试方法：调用 UserService.login()

        assertNotNull(result);  // 断言：返回的 Result 对象不为 null
        assertEquals(200, result.getCode());  // 断言：业务状态码应为 200（成功）
        verify(userFeignClient, times(1)).login(dto);  // 验证：Feign 客户端的 login() 方法被调用了恰好 1 次，且参数为传入的 dto
    }

    /**
     * 测试场景：用户注册 — 验证 UserService.register() 正确委托给 UserFeignClient.register()
     * 
     * 验证点：
     *   - assertNotNull(result)：返回结果不为 null
     *   - assertEquals(200, result.getCode())：业务状态码为 200
     *   - verify(userFeignClient, times(1)).register(dto)：确认 Feign 调用了 register()
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("用户注册 — 调用 Feign 并返回结果")  // 测试场景的中文描述
    void shouldRegisterViaFeign() {  // 测试方法：验证注册委托调用
        RegisterDTO dto = new RegisterDTO();  // 创建注册请求 DTO 对象
        dto.setUsername("newuser");  // 设置注册用户名
        dto.setEmail("new@test.com");  // 设置注册邮箱
        dto.setPassword("password");  // 设置注册密码

        Result<Map<String, Object>> expected = Result.success(Map.of("userid", "newuser"));  // 构建预期的成功结果，包含新用户 ID
        when(userFeignClient.register(any(RegisterDTO.class))).thenReturn(expected);  // Mock Feign 客户端 register() 返回预设结果

        Result<Map<String, Object>> result = userService.register(dto);  // 执行被测试方法

        assertNotNull(result);  // 断言：返回结果不为 null
        assertEquals(200, result.getCode());  // 断言：业务状态码为 200
        verify(userFeignClient, times(1)).register(dto);  // 验证：register() 被精确调用 1 次，参数匹配
    }

    /**
     * 测试场景：获取用户信息 — 验证 UserService.getProfile() 正确委托给 UserFeignClient.getProfile()
     * 验证点：
     *   - 返回结果不为 null
     *   - 业务状态码为 200
     *   - Feign 的 getProfile("testuser") 被调用 1 次
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("获取用户信息 — 通过 Feign 调用")  // 测试场景的中文描述
    void shouldGetProfileViaFeign() {  // 测试方法：验证获取用户信息委托调用
        Result<?> expected = Result.success(Map.of("userid", "testuser", "email", "a@b.com"));  // 构建预期的用户信息结果
        when(userFeignClient.getProfile(anyString())).thenReturn((Result) expected);  // Mock Feign 客户端 getProfile() 返回预设结果（需要强制类型转换）

        Result<?> result = userService.getProfile("testuser");  // 执行被测试方法，传入用户 ID

        assertNotNull(result);  // 断言：返回结果不为 null
        assertEquals(200, result.getCode());  // 断言：业务状态码为 200
        verify(userFeignClient, times(1)).getProfile("testuser");  // 验证：getProfile("testuser") 被调用 1 次
    }

    /**
     * 测试场景：更新用户资料 — 验证 UserService.updateProfile() 正确委托给 UserFeignClient.updateProfile()
     * 验证点：
     *   - 业务状态码为 200
     *   - verify() 确认 Feign 客户端的 updateProfile() 被调用 1 次，参数匹配（eq 精确匹配）
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("更新用户资料 — 通过 Feign 调用")  // 测试场景的中文描述
    void shouldUpdateProfileViaFeign() {  // 测试方法：验证更新资料委托调用
        Result<Void> expected = Result.success();  // 构建预期的无数据成功结果
        when(userFeignClient.updateProfile(anyString(), anyMap())).thenReturn(expected);  // Mock Feign 客户端 updateProfile() 返回成功

        Map<String, Object> updates = Map.of("email", "new@test.com");  // 构造更新数据：修改邮箱
        Result<Void> result = userService.updateProfile("testuser", updates);  // 执行被测试方法

        assertEquals(200, result.getCode());  // 断言：业务状态码为 200
        verify(userFeignClient, times(1)).updateProfile(eq("testuser"), eq(updates));  // 验证：updateProfile 被调用 1 次，userId 和 updates 参数精确匹配
    }

    /**
     * 测试场景：修改密码 — 验证 UserService.updatePassword() 正确委托给 UserFeignClient.updatePassword()
     * 验证点：
     *   - 业务状态码为 200
     *   - verify() 确认 Feign 客户端的 updatePassword() 被调用 1 次，且参数（userId、旧密码、新密码）精确匹配
     */
    @Test  // JUnit 5 注解：标识该方法为测试用例
    @DisplayName("修改密码 — 通过 Feign 调用")  // 测试场景的中文描述
    void shouldUpdatePasswordViaFeign() {  // 测试方法：验证修改密码委托调用
        Result<Void> expected = Result.success();  // 构建预期的无数据成功结果
        when(userFeignClient.updatePassword(anyString(), anyMap())).thenReturn(expected);  // Mock Feign 客户端 updatePassword() 返回成功

        Map<String, String> passwords = Map.of("oldPassword", "old", "newPassword", "new");  // 构造密码数据：旧密码和新密码
        Result<Void> result = userService.updatePassword("testuser", passwords);  // 执行被测试方法

        assertEquals(200, result.getCode());  // 断言：业务状态码为 200
        verify(userFeignClient, times(1)).updatePassword(eq("testuser"), eq(passwords));  // 验证：updatePassword 被调用 1 次，参数精确匹配
    }
}
