package com.wy.review.utils;

import com.wy.review.common.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与解析 token
 * token payload 携带 userId / username / role，
 * 服务端不存会话（无状态），签名校验即身份校验
 */
@Component
public class JwtUtil {

    @Value("${review.jwt.secret}")
    private String secret;

    @Value("${review.jwt.expire}")
    private Long expire;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 token */
    public String createToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 token，签名错误或过期时抛异常（由拦截器转为 401） */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return new LoginUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("role", String.class)
        );
    }
}
