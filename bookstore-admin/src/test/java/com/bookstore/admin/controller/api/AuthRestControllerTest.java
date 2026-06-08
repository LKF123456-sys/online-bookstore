package com.bookstore.admin.controller.api;

import com.bookstore.admin.service.UserService;
import com.bookstore.common.api.Result;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthRestController authRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login — 登录成功返回200")
    void shouldLoginSuccessfully() throws Exception {
        Map<String, Object> data = Map.of("token", "jwt-fake-token", "user", Map.of("userid", "admin"));
        when(userService.login(any())).thenReturn(Result.success(data));

        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-fake-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login — Result 返回 null data 时抛 BusinessException")
    void shouldThrowWhenLoginReturnsNullData() throws Exception {
        Result<Map<String, Object>> badResult = new Result<>();
        badResult.setCode(500);
        badResult.setMessage("登录失败");
        badResult.setData(null);
        when(userService.login(any())).thenReturn(badResult);

        String body = objectMapper.writeValueAsString(Map.of("username", "admin", "password", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("登录失败"));
    }

    @Test
    @DisplayName("POST /api/auth/register — 注册成功返回200")
    void shouldRegisterSuccessfully() throws Exception {
        Map<String, Object> data = Map.of("userid", "newuser");
        when(userService.register(any())).thenReturn(Result.success(data));

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "newuser", "email", "a@b.com", "password", "123456"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/auth/logout — 登出成功")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/auth/profile — Session 中有用户时返回用户信息")
    void shouldGetProfileFromSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", Map.of("userid", "testuser", "email", "a@b.com"));

        mockMvc.perform(get("/api/auth/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userid").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/auth/profile — 无 Session 时使用 Header X-User-Id")
    void shouldGetProfileFromHeader() throws Exception {
        when(userService.getProfile("testuser")).thenReturn((Result) Result.success(Map.of("userid", "testuser")));

        mockMvc.perform(get("/api/auth/profile")
                        .header("X-User-Id", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/auth/profile — 无 Session 无 Header 返回401")
    void shouldReturn401WhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    @DisplayName("PUT /api/auth/profile — 更新用户资料成功")
    void shouldUpdateProfile() throws Exception {
        when(userService.updateProfile(eq("testuser"), anyMap())).thenReturn(Result.success());

        Map<String, Object> updates = Map.of("email", "new@test.com", "phone", "13800138000");
        String body = objectMapper.writeValueAsString(updates);

        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateProfile(eq("testuser"), eq(updates));
    }

    @Test
    @DisplayName("PUT /api/auth/password — 修改密码成功")
    void shouldUpdatePassword() throws Exception {
        when(userService.updatePassword(eq("testuser"), anyMap())).thenReturn(Result.success());

        Map<String, String> passwords = Map.of("oldPassword", "old", "newPassword", "new");
        String body = objectMapper.writeValueAsString(passwords);

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updatePassword(eq("testuser"), eq(passwords));
    }
}
