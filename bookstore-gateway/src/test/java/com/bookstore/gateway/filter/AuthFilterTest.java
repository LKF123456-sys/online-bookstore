package com.bookstore.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    private AuthFilter authFilter;
    private static final String SECRET = "BookVerseSecretKey2024ForJWTTokenGenerationMustBe256BitsLongEnough";

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // 默认模拟 Redis 黑名单检查返回 false（未加入黑名单）
        org.mockito.Mockito.lenient().when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(reactor.core.publisher.Mono.just(false));

        authFilter = new AuthFilter(redisTemplate);
        // 通过反射设置 jwtSecret 字段
        Field secretField = AuthFilter.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(authFilter, SECRET);
    }

    /**
     * 生成测试用 JWT Token
     */
    private String generateTestToken(String userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    /**
     * 生成过期的 JWT Token
     */
    private String generateExpiredToken(String userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();
    }

    /**
     * 模拟 GatewayFilterChain，记录是否被调用
     */
    private GatewayFilterChain createRecordingChain(AtomicBoolean called) {
        return exchange -> {
            called.set(true);
            return Mono.empty();
        };
    }

    /**
     * 简单的 AtomicBoolean 用于记录 filter chain 是否被调用
     */
    private static class AtomicBoolean {
        volatile boolean value = false;
        void set(boolean v) { value = v; }
        boolean get() { return value; }
    }

    // ==================== 过滤器顺序测试 ====================

    @Test
    @DisplayName("过滤器优先级 — 返回 -100")
    void shouldReturnCorrectOrder() {
        assertEquals(-100, authFilter.getOrder());
    }

    // ==================== 白名单路径测试 ====================

    @Nested
    @DisplayName("白名单路径放行")
    class WhiteListTests {

        @Test
        @DisplayName("/api/auth/login — 无需认证直接放行")
        void shouldAllowLoginPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get(), "白名单路径应直接放行");
        }

        @Test
        @DisplayName("/api/auth/register — 无需认证直接放行")
        void shouldAllowRegisterPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/register").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("/api/product/list — 无需认证直接放行")
        void shouldAllowProductListPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/list").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("/api/product/hot — 无需认证直接放行")
        void shouldAllowHotProductsPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/product/hot").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("/api/search — 无需认证直接放行")
        void shouldAllowSearchPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/search").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("/actuator — 无需认证直接放行")
        void shouldAllowActuatorPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/actuator").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }
    }

    // ==================== Token 验证测试 ====================

    @Nested
    @DisplayName("Token 验证")
    class TokenValidationTests {

        @Test
        @DisplayName("有效 Token — 放行并注入用户信息到请求头")
        void shouldPassValidTokenAndInjectHeaders() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateTestToken("user001", "user");

            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // 使用记录型 chain 来捕获修改后的 exchange
            AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
            GatewayFilterChain chain = ex -> {
                capturedExchange.set(ex);
                return Mono.empty();
            };

            authFilter.filter(exchange, chain).block();

            assertNotNull(capturedExchange.get());
            ServerHttpRequest mutatedRequest = capturedExchange.get().getRequest();
            assertEquals("user001", mutatedRequest.getHeaders().getFirst("X-User-Id"));
            assertEquals("user", mutatedRequest.getHeaders().getFirst("X-User-Role"));
        }

        @Test
        @DisplayName("admin Token — 访问 admin 路径放行并注入 admin 角色")
        void shouldPassAdminTokenForAdminPath() {
            String token = generateTestToken("admin001", "admin");

            MockServerHttpRequest request = MockServerHttpRequest.get("/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
            GatewayFilterChain chain = ex -> {
                capturedExchange.set(ex);
                return Mono.empty();
            };

            authFilter.filter(exchange, chain).block();

            assertNotNull(capturedExchange.get());
            assertEquals("admin", capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Role"));
        }

        @Test
        @DisplayName("缺少 Authorization 头 — 返回 401")
        void shouldReturn401WhenNoAuthHeader() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("Authorization 头格式错误（无 Bearer 前缀）— 返回 401")
        void shouldReturn401WhenAuthHeaderMalformed() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("过期 Token — 返回 401")
        void shouldReturn401WhenTokenExpired() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateExpiredToken("user001", "user");

            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("无效签名 Token — 返回 401")
        void shouldReturn401WhenTokenSignatureInvalid() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            // 使用不同的密钥签名
            String wrongSecret = "WrongSecretKeyThatIsLongEnoughForHS256AlgorithmMinimum256Bits!!";
            SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .claims(Map.of("userId", "user001", "role", "user"))
                    .subject("user001")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(wrongKey)
                    .compact();

            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("完全无效的 Token 字符串 — 返回 401")
        void shouldReturn401WhenTokenIsGarbage() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.jwt.token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("已登出的 Token（在黑名单中）— 返回 401")
        void shouldReturn401WhenTokenIsBlacklisted() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateTestToken("user001", "user");

            // 模拟 Redis 中存在该 token 的黑名单记录
            org.mockito.Mockito.when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.contains(token)))
                    .thenReturn(reactor.core.publisher.Mono.just(true));

            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }
    }

    // ==================== RBAC 测试 ====================

    @Nested
    @DisplayName("RBAC 角色鉴权")
    class RBACTests {

        @Test
        @DisplayName("普通用户访问 /admin/ 路径 — 返回 403")
        void shouldReturn403WhenUserAccessAdminPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateTestToken("user001", "user");

            MockServerHttpRequest request = MockServerHttpRequest.get("/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("管理员访问 /admin/ 路径 — 正常放行")
        void shouldAllowAdminAccessAdminPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateTestToken("admin001", "admin");

            MockServerHttpRequest request = MockServerHttpRequest.get("/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("普通用户访问普通 API — 正常放行")
        void shouldAllowUserAccessNormalPath() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            String token = generateTestToken("user001", "user");

            MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertTrue(chainCalled.get());
        }

        @Test
        @DisplayName("admin Token 中 role 为空时 — 视为非 admin")
        void shouldRejectWhenRoleMissing() {
            AtomicBoolean chainCalled = new AtomicBoolean();
            // 生成不含 role 的 token
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .claims(Map.of("userId", "user001"))
                    .subject("user001")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(key)
                    .compact();

            MockServerHttpRequest request = MockServerHttpRequest.get("/admin/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            authFilter.filter(exchange, createRecordingChain(chainCalled)).block();

            assertFalse(chainCalled.get());
            assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        }
    }
}
