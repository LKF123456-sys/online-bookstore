package com.bookstore.common.security;

import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-algorithm!");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test void generateToken_ContainsCorrectClaims() {
        String token = jwtUtil.generateToken("u001", "alice", "user");
        assertNotNull(token);
        assertEquals("u001", jwtUtil.getUserId(token));
        assertEquals("alice", jwtUtil.getUsername(token));
        assertEquals("user", jwtUtil.getRole(token));
    }

    @Test void isTokenValid_ValidToken_ReturnsTrue() {
        assertTrue(jwtUtil.isTokenValid(jwtUtil.generateToken("u001", "alice", "user")));
    }

    @Test void isTokenValid_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        assertFalse(jwtUtil.isTokenValid(jwtUtil.generateToken("u001", "alice", "user")));
    }

    @Test void isTokenValid_InvalidToken_ReturnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid-token"));
        assertFalse(jwtUtil.isTokenValid(""));
    }

    @Test void generateToken_AdminRole() {
        String token = jwtUtil.generateToken("admin001", "admin", "admin");
        assertEquals("admin", jwtUtil.getRole(token));
    }
}