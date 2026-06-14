package com.bookstore.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.PasswordUpdateDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.UserVO;
import com.bookstore.common.entity.Account;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.security.JwtUtil;
import com.bookstore.common.util.PasswordUtil;
import com.bookstore.user.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private AccountService accountService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new Account();
        sampleAccount.setUserid("testuser");
        sampleAccount.setEmail("test@example.com");
        sampleAccount.setPassword(PasswordUtil.encode("123456"));
        sampleAccount.setRole("user");
        sampleAccount.setStatus(1);
        sampleAccount.setPhone("13800138000");
    }

    // ==================== 登录测试 ====================

    @Nested
    @DisplayName("用户登录")
    class LoginTests {

        @Test
        @DisplayName("登录成功 — 返回 token 和用户信息")
        void shouldLoginSuccessfully() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("testuser");
            dto.setPassword("123456");

            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAccount);
            when(jwtUtil.generateToken("testuser", "testuser", "user")).thenReturn("fake-jwt-token");

            Map<String, Object> result = accountService.login(dto);

            assertNotNull(result);
            assertEquals("fake-jwt-token", result.get("token"));
            assertNotNull(result.get("user"));
            verify(jwtUtil).generateToken("testuser", "testuser", "user");
        }

        @Test
        @DisplayName("登录失败 — 用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("nonexistent");
            dto.setPassword("123456");

            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.login(dto));
            assertEquals("用户名或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("登录失败 — 密码错误时抛出异常")
        void shouldThrowWhenPasswordWrong() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("testuser");
            dto.setPassword("wrongpassword");

            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAccount);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.login(dto));
            assertEquals("用户名或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("登录失败 — 账号被禁用时抛出异常")
        void shouldThrowWhenAccountDisabled() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("testuser");
            dto.setPassword("123456");

            sampleAccount.setStatus(0);
            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAccount);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.login(dto));
            assertEquals("账号已被禁用", ex.getMessage());
        }
    }

    // ==================== 登出测试 ====================

    @Nested
    @DisplayName("用户登出（Token 黑名单）")
    class LogoutTests {

        @Test
        @DisplayName("登出成功 — 有效 token 加入 Redis 黑名单")
        void shouldBlacklistValidToken() {
            String token = "valid-jwt-token";
            // 模拟 JwtUtil 解析 token 返回 claims（含过期时间）
            io.jsonwebtoken.Claims mockClaims = mock(io.jsonwebtoken.Claims.class);
            when(mockClaims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
            when(jwtUtil.parseToken(token)).thenReturn(mockClaims);

            when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

            assertDoesNotThrow(() -> accountService.logout(token));

            verify(jwtUtil).parseToken(token);
            verify(redisTemplate.opsForValue()).set(
                    eq("jwt:blacklist:" + token),
                    eq("revoked"),
                    anyLong(),
                    eq(java.util.concurrent.TimeUnit.MILLISECONDS)
            );
        }

        @Test
        @DisplayName("登出 — token 为空时不报错")
        void shouldHandleNullTokenGracefully() {
            assertDoesNotThrow(() -> accountService.logout(null));
            verify(jwtUtil, never()).parseToken(anyString());
        }

        @Test
        @DisplayName("登出 — token 已过期时跳过黑名单处理")
        void shouldSkipBlacklistForExpiredToken() {
            String token = "expired-token";
            io.jsonwebtoken.Claims mockClaims = mock(io.jsonwebtoken.Claims.class);
            when(mockClaims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() - 1000));
            when(jwtUtil.parseToken(token)).thenReturn(mockClaims);

            assertDoesNotThrow(() -> accountService.logout(token));

            verify(redisTemplate, never()).opsForValue();
        }
    }

    // ==================== 注册测试 ====================

    @Nested
    @DisplayName("用户注册")
    class RegisterTests {

        @Test
        @DisplayName("注册成功 — 新用户插入数据库")
        void shouldRegisterSuccessfully() {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("newuser");
            dto.setPassword("password123");
            dto.setEmail("new@example.com");
            dto.setPhone("13900139000");

            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(accountMapper.insert(any(Account.class))).thenReturn(1);

            assertDoesNotThrow(() -> accountService.register(dto));

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountMapper).insert(captor.capture());
            Account saved = captor.getValue();
            assertEquals("newuser", saved.getUserid());
            assertEquals("new@example.com", saved.getEmail());
            assertEquals("user", saved.getRole());
            assertEquals(1, saved.getStatus());
            assertTrue(PasswordUtil.matches("password123", saved.getPassword()));
        }

        @Test
        @DisplayName("注册失败 — 用户名已存在时抛出异常")
        void shouldThrowWhenUsernameExists() {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("existinguser");
            dto.setPassword("password123");
            dto.setEmail("new@example.com");

            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAccount);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.register(dto));
            assertEquals("用户名已存在", ex.getMessage());
            verify(accountMapper, never()).insert(any());
        }

        @Test
        @DisplayName("注册失败 — 邮箱已被注册时抛出异常")
        void shouldThrowWhenEmailExists() {
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername("newuser");
            dto.setPassword("password123");
            dto.setEmail("existing@example.com");

            // 第一次查询（用户名检查）返回 null，第二次查询（邮箱检查）返回已有账号
            when(accountMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null)
                    .thenReturn(sampleAccount);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.register(dto));
            assertEquals("邮箱已被注册", ex.getMessage());
            verify(accountMapper, never()).insert(any());
        }
    }

    // ==================== 查询用户信息测试 ====================

    @Nested
    @DisplayName("查询用户信息")
    class GetUserByIdTests {

        @Test
        @DisplayName("查询成功 — 返回用户视图对象")
        void shouldReturnUserVO() {
            when(accountMapper.selectById("testuser")).thenReturn(sampleAccount);

            UserVO vo = accountService.getUserById("testuser");

            assertNotNull(vo);
            assertEquals("testuser", vo.getUserid());
            assertEquals("test@example.com", vo.getEmail());
            assertEquals("user", vo.getRole());
            // 确认不包含密码（UserVO 没有 password 字段）
        }

        @Test
        @DisplayName("查询失败 — 用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            when(accountMapper.selectById("nonexistent")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.getUserById("nonexistent"));
            assertEquals("用户不存在", ex.getMessage());
        }
    }

    // ==================== 修改密码测试 ====================

    @Nested
    @DisplayName("修改密码")
    class UpdatePasswordTests {

        @Test
        @DisplayName("修改成功 — 旧密码正确时更新为新密码")
        void shouldUpdatePasswordSuccessfully() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("123456");
            dto.setNewPassword("newpass123");

            when(accountMapper.selectById("testuser")).thenReturn(sampleAccount);
            when(accountMapper.updateById(any(Account.class))).thenReturn(1);

            assertDoesNotThrow(() -> accountService.updatePassword("testuser", dto));

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountMapper).updateById(captor.capture());
            assertTrue(PasswordUtil.matches("newpass123", captor.getValue().getPassword()));
        }

        @Test
        @DisplayName("修改失败 — 用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("123456");
            dto.setNewPassword("newpass123");

            when(accountMapper.selectById("nonexistent")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.updatePassword("nonexistent", dto));
            assertEquals("用户不存在", ex.getMessage());
        }

        @Test
        @DisplayName("修改失败 — 旧密码错误时抛出异常")
        void shouldThrowWhenOldPasswordWrong() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("wrongpassword");
            dto.setNewPassword("newpass123");

            when(accountMapper.selectById("testuser")).thenReturn(sampleAccount);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.updatePassword("testuser", dto));
            assertEquals("原密码错误", ex.getMessage());
            verify(accountMapper, never()).updateById(any());
        }
    }

    // ==================== 修改个人资料测试 ====================

    @Nested
    @DisplayName("修改个人资料")
    class UpdateProfileTests {

        @Test
        @DisplayName("修改成功 — 更新邮箱、手机号、头像")
        void shouldUpdateProfileSuccessfully() {
            UserVO vo = new UserVO();
            vo.setEmail("new@example.com");
            vo.setPhone("13900139000");
            vo.setAvatar("http://img.example.com/avatar.jpg");

            when(accountMapper.selectById("testuser")).thenReturn(sampleAccount);
            when(accountMapper.updateById(any(Account.class))).thenReturn(1);

            assertDoesNotThrow(() -> accountService.updateProfile("testuser", vo));

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountMapper).updateById(captor.capture());
            Account updated = captor.getValue();
            assertEquals("new@example.com", updated.getEmail());
            assertEquals("13900139000", updated.getPhone());
            assertEquals("http://img.example.com/avatar.jpg", updated.getAvatar());
        }

        @Test
        @DisplayName("修改失败 — 用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            UserVO vo = new UserVO();
            vo.setEmail("new@example.com");

            when(accountMapper.selectById("nonexistent")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.updateProfile("nonexistent", vo));
            assertEquals("用户不存在", ex.getMessage());
        }
    }

    // ==================== 管理员功能测试 ====================

    @Nested
    @DisplayName("管理员功能")
    class AdminTests {

        @Test
        @DisplayName("修改用户状态 — 禁用用户")
        void shouldDisableUser() {
            when(accountMapper.selectById("testuser")).thenReturn(sampleAccount);
            when(accountMapper.updateById(any(Account.class))).thenReturn(1);

            assertDoesNotThrow(() -> accountService.updateUserStatus("testuser", 0));

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountMapper).updateById(captor.capture());
            assertEquals(0, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("修改用户状态 — 用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            when(accountMapper.selectById("nonexistent")).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> accountService.updateUserStatus("nonexistent", 0));
            assertEquals("用户不存在", ex.getMessage());
        }

        @Test
        @DisplayName("删除用户")
        void shouldDeleteUser() {
            doNothing().when(accountMapper).deleteById("testuser");

            assertDoesNotThrow(() -> accountService.deleteUser("testuser"));

            verify(accountMapper).deleteById("testuser");
        }

        @Test
        @DisplayName("分页查询用户列表")
        void shouldGetUserList() {
            Page<Account> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleAccount));
            page.setTotal(1);

            when(accountMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<UserVO> result = accountService.getUserList(1, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
            assertEquals("testuser", result.getList().get(0).getUserid());
        }

        @Test
        @DisplayName("分页查询用户列表 — 带关键词搜索")
        void shouldGetUserListWithKeyword() {
            Page<Account> page = new Page<>(1, 10);
            page.setRecords(List.of(sampleAccount));
            page.setTotal(1);

            when(accountMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<UserVO> result = accountService.getUserList(1, 10, "test");

            assertNotNull(result);
            verify(accountMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }
}
