package com.bookstore.admin.service;

import com.bookstore.admin.feign.UserFeignClient;
import com.bookstore.common.api.Result;
import com.bookstore.common.api.dto.LoginDTO;
import com.bookstore.common.api.dto.RegisterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("用户登录 — 调用 Feign 并返回结果")
    void shouldLoginViaFeign() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", "jwt-token-xxx");
        loginData.put("user", Map.of("userid", "testuser"));
        Result<Map<String, Object>> expected = Result.success(loginData);

        when(userFeignClient.login(any(LoginDTO.class))).thenReturn(expected);

        Result<Map<String, Object>> result = userService.login(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(userFeignClient, times(1)).login(dto);
    }

    @Test
    @DisplayName("用户注册 — 调用 Feign 并返回结果")
    void shouldRegisterViaFeign() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@test.com");
        dto.setPassword("password");

        Result<Map<String, Object>> expected = Result.success(Map.of("userid", "newuser"));
        when(userFeignClient.register(any(RegisterDTO.class))).thenReturn(expected);

        Result<Map<String, Object>> result = userService.register(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(userFeignClient, times(1)).register(dto);
    }

    @Test
    @DisplayName("获取用户信息 — 通过 Feign 调用")
    void shouldGetProfileViaFeign() {
        Result<?> expected = Result.success(Map.of("userid", "testuser", "email", "a@b.com"));
        when(userFeignClient.getProfile(anyString())).thenReturn((Result) expected);

        Result<?> result = userService.getProfile("testuser");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(userFeignClient, times(1)).getProfile("testuser");
    }

    @Test
    @DisplayName("更新用户资料 — 通过 Feign 调用")
    void shouldUpdateProfileViaFeign() {
        Result<Void> expected = Result.success();
        when(userFeignClient.updateProfile(anyString(), anyMap())).thenReturn(expected);

        Map<String, Object> updates = Map.of("email", "new@test.com");
        Result<Void> result = userService.updateProfile("testuser", updates);

        assertEquals(200, result.getCode());
        verify(userFeignClient, times(1)).updateProfile(eq("testuser"), eq(updates));
    }

    @Test
    @DisplayName("修改密码 — 通过 Feign 调用")
    void shouldUpdatePasswordViaFeign() {
        Result<Void> expected = Result.success();
        when(userFeignClient.updatePassword(anyString(), anyMap())).thenReturn(expected);

        Map<String, String> passwords = Map.of("oldPassword", "old", "newPassword", "new");
        Result<Void> result = userService.updatePassword("testuser", passwords);

        assertEquals(200, result.getCode());
        verify(userFeignClient, times(1)).updatePassword(eq("testuser"), eq(passwords));
    }
}
