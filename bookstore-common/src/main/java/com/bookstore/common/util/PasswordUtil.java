package com.bookstore.common.util;  // 声明当前类所属的包路径

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // 导入BCrypt密码编码器

/**
 * 密码加密工具类
 * 提供密码的加密和验证功能
 * 使用BCrypt算法进行密码加密，安全性高
 */
public class PasswordUtil {  // 密码工具类

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();  // 创建BCrypt编码器实例，static确保全局唯一

    /**
     * 对明文密码进行加密
     * @param rawPassword 明文密码
     * @return 加密后的密码字符串
     */
    public static String encode(String rawPassword) {  // 加密方法
        return encoder.encode(rawPassword);  // 使用BCrypt算法加密密码并返回
    }

    /**
     * 验证明文密码是否与加密后的密码匹配
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 匹配返回true，否则返回false
     */
    public static boolean matches(String rawPassword, String encodedPassword) {  // 验证方法
        return encoder.matches(rawPassword, encodedPassword);  // 比较明文密码和加密密码是否匹配
    }
}
