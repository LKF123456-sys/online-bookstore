package com.bookstore.user;

import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.user.mapper.AccountMapper;
import com.bookstore.user.service.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 用户服务集成测试
 * 使用 Testcontainers 启动真实 MySQL 容器，验证端到端用户注册/登录流程
 *
 * 面试点：
 *   1. Testcontainers：启动真实 Docker 容器而非 Mock/H2，覆盖真实 SQL 兼容性
 *   2. @DynamicPropertySource：动态注入容器连接信息
 *   3. 密码加密验证：BCrypt 加密后写入数据库，登录时验证
 *   4. JWT Token 生成：登录成功后验证 Token 包含正确的用户信息
 *
 * 运行前提：本地需要安装 Docker 环境
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class UserServiceIntegrationTest {

    // ==================== Testcontainers 容器定义 ====================

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bookstore_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-schema.sql");

    // ==================== 动态属性注入 ====================

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // 禁用 Redis（用户服务有 Redis 依赖但集成测试中不需要）
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "16379");
    }

    // ==================== 注入被测对象 ====================

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    // ==================== 测试数据 ====================

    private static final String TEST_USER = "testuser_integ";
    private static final String TEST_PASSWORD = "TestPass123!";
    private static final String TEST_EMAIL = "test@bookstore.com";

    @BeforeEach
    void setUp() {
        // 每个测试前清理测试用户
        accountMapper.deleteById(TEST_USER);
    }

    // ==================== 测试用例 ====================

    @Test
    @DisplayName("用户注册 - 成功创建新用户")
    void testRegisterUser_Success() {
        // 准备注册数据
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USER);
        dto.setPassword(TEST_PASSWORD);
        dto.setEmail(TEST_EMAIL);
        dto.setPhone("13800138000");

        // 执行注册
        assertDoesNotThrow(() -> accountService.register(dto));

        // 验证用户已写入数据库
        var saved = accountMapper.selectById(TEST_USER);
        assertNotNull(saved, "用户应存在于数据库");
        assertEquals(TEST_EMAIL, saved.getEmail(), "邮箱应匹配");
        assertEquals("user", saved.getRole(), "角色应为普通用户");
        assertEquals(1, saved.getStatus(), "状态应启用");
        // 验证密码已加密（而非明文存储）
        assertNotEquals(TEST_PASSWORD, saved.getPassword(), "密码应加密存储");
    }

    @Test
    @DisplayName("用户登录 - 用户名密码正确时返回 Token")
    void testLogin_Success() {
        // 先注册用户
        RegisterDTO regDto = new RegisterDTO();
        regDto.setUsername(TEST_USER);
        regDto.setPassword(TEST_PASSWORD);
        regDto.setEmail(TEST_EMAIL);
        accountService.register(regDto);

        // 登录
        LoginDTO loginDto = new LoginDTO();
        loginDto.setUsername(TEST_USER);
        loginDto.setPassword(TEST_PASSWORD);

        // 执行登录并断言返回结果包含 Token
        var result = assertDoesNotThrow(() -> accountService.login(loginDto));
        assertNotNull(result, "登录返回结果不应为空");
        assertTrue(result.containsKey("token"), "登录成功应包含 token");
        assertTrue(result.containsKey("userInfo"), "登录成功应包含用户信息");
    }

    @Test
    @DisplayName("用户登录 - 密码错误时抛出异常")
    void testLogin_WrongPassword() {
        // 先注册用户
        RegisterDTO regDto = new RegisterDTO();
        regDto.setUsername(TEST_USER);
        regDto.setPassword(TEST_PASSWORD);
        regDto.setEmail(TEST_EMAIL);
        accountService.register(regDto);

        // 用错误密码登录
        LoginDTO loginDto = new LoginDTO();
        loginDto.setUsername(TEST_USER);
        loginDto.setPassword("wrong_password_123");

        var exception = assertThrows(Exception.class, () -> accountService.login(loginDto));
        assertTrue(exception.getMessage().contains("用户名或密码错误"),
                "应抛出用户名或密码错误的异常");
    }

    @Test
    @DisplayName("用户注册 - 重复用户名抛出异常")
    void testRegister_DuplicateUser() {
        // 先注册用户
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USER);
        dto.setPassword(TEST_PASSWORD);
        dto.setEmail(TEST_EMAIL);
        accountService.register(dto);

        // 再次注册同一用户名
        RegisterDTO duplicate = new RegisterDTO();
        duplicate.setUsername(TEST_USER);
        duplicate.setPassword("AnotherPass1!");
        duplicate.setEmail("other@bookstore.com");

        assertThrows(Exception.class, () -> accountService.register(duplicate),
                "重复用户名应抛出异常");
    }
}
