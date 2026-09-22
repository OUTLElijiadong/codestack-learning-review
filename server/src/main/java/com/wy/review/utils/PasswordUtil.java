package com.wy.review.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具：全局共享一个 BCryptPasswordEncoder
 * BCrypt 自带随机盐，同一明文每次加密密文不同，用 matches 比对；
 * 数据库只存密文，永不存明文（密码与密保答案同样处理）
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** 明文加密为 BCrypt 密文 */
    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    /** 明文与密文比对 */
    public static boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null) {
            return false;
        }
        return ENCODER.matches(raw, encoded);
    }
}
