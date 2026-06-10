package com.bookstore.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 提供 JWT 令牌的生成、解析和验证功能。
 * 密钥和过期时间通过配置文件注入，不硬编码。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:BookVerseSecretKey2024ForJWTTokenGenerationMustBe256BitsLongEnough}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 令牌
     * @param userId   用户ID（String 类型，与 Account.userid 一致）
     * @param username 用户名
     * @param role     用户角色（user / admin）
     * @return JWT 令牌字符串
     */
    public String generateToken(String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析并验证 JWT 令牌
     * @param token JWT 令牌字符串
     * @return Claims 对象
     * @throws JwtException 令牌无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中提取用户ID
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", String.class);
    }

    /**
     * 从令牌中提取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中提取角色
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证令牌是否有效（签名正确且未过期）
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前配置的过期时间（毫秒）
     */
    public Long getExpiration() {
        return expiration;
    }
}
