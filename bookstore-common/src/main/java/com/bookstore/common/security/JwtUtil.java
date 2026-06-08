package com.bookstore.common.security;  // 声明当前类所属的包路径

import io.jsonwebtoken.*;  // 导入JJWT库的核心类
import io.jsonwebtoken.security.Keys;  // 导入密钥生成工具
import org.springframework.beans.factory.annotation.Value;  // 导入Spring的@Value注解，用于读取配置文件
import org.springframework.stereotype.Component;  // 导入Spring的@Component注解，标记为Spring组件

import javax.crypto.SecretKey;  // 导入密钥接口
import java.nio.charset.StandardCharsets;  // 导入字符编码类
import java.util.Date;  // 导入日期类
import java.util.HashMap;  // 导入HashMap集合
import java.util.Map;  // 导入Map接口

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析和验证功能
 * JWT（JSON Web Token）是一种用于身份认证的令牌格式
 */
@Component  // Spring注解，将该类注册为Spring Bean
public class JwtUtil {  // JWT工具类

    @Value("${jwt.secret:BookVerseSecretKey2024ForJWTTokenGenerationMustBe256BitsLongEnough}")  // 从配置文件读取JWT密钥，有默认值
    private String secret;  // JWT签名密钥

    @Value("${jwt.expiration:86400000}")  // 从配置文件读取JWT过期时间（毫秒），默认24小时
    private Long expiration;  // JWT过期时间，单位毫秒

    /**
     * 获取JWT签名密钥
     * @return SecretKey密钥对象
     */
    private SecretKey getSigningKey() {  // 获取签名密钥方法
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));  // 将密钥字符串转换为HMAC-SHA密钥
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, String username, String role) {  // 生成JWT令牌方法
        Map<String, Object> claims = new HashMap<>();  // 创建claims（声明）集合
        claims.put("userId", userId);  // 将用户ID添加到claims中
        claims.put("username", username);  // 将用户名添加到claims中
        claims.put("role", role);  // 将用户角色添加到claims中
        return Jwts.builder()  // 创建JWT构建器
                .claims(claims)  // 设置claims
                .subject(username)  // 设置主题（通常是用户名）
                .issuedAt(new Date())  // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 设置过期时间
                .signWith(getSigningKey())  // 使用密钥进行签名
                .compact();  // 生成JWT令牌字符串
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌字符串
     * @return Claims对象，包含令牌中的所有声明信息
     */
    public Claims parseToken(String token) {  // 解析JWT令牌方法
        return Jwts.parser()  // 创建JWT解析器
                .verifyWith(getSigningKey())  // 设置验证密钥
                .build()  // 构建解析器
                .parseSignedClaims(token)  // 解析并验证签名
                .getPayload();  // 获取claims（声明）内容
    }

    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {  // 获取用户ID方法
        Claims claims = parseToken(token);  // 解析令牌获取claims
        return claims.get("userId", Long.class);  // 从claims中提取用户ID
    }

    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsername(String token) {  // 获取用户名方法
        Claims claims = parseToken(token);  // 解析令牌获取claims
        return claims.getSubject();  // 从claims中获取主题（用户名）
    }

    /**
     * 从JWT令牌中获取用户角色
     * @param token JWT令牌字符串
     * @return 用户角色
     */
    public String getRole(String token) {  // 获取用户角色方法
        Claims claims = parseToken(token);  // 解析令牌获取claims
        return claims.get("role", String.class);  // 从claims中提取用户角色
    }

    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌字符串
     * @return 有效返回true，无效返回false
     */
    public boolean isTokenValid(String token) {  // 验证令牌有效性方法
        try {
            Claims claims = parseToken(token);  // 尝试解析令牌
            return !claims.getExpiration().before(new Date());  // 检查令牌是否已过期，未过期返回true
        } catch (Exception e) {  // 捕获解析异常
            return false;  // 解析失败说明令牌无效
        }
    }
}
