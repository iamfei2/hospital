package com.hospit.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    
    // 密钥（至少256位）
    private static final String SECRET = "HospitalSecretKeyForJWT2024VeryLongSecretKey123456789";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    // Token过期时间（2小时）
    private static final long EXPIRATION = 2 * 60 * 60 * 1000;
    
    // 生成JWT Token
    public static String generateToken(Integer userId, String loginAccount) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("loginAccount", loginAccount);
        
        return Jwts.builder()
                .claims(claims)
                .subject(loginAccount)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }
    
    // 解析JWT Token
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
    
    // 验证JWT Token有效性
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }
    
    // 判断Token是否过期
    private static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
    
    // 从Token中获取用户ID
    public static Integer getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Integer.class);
        }
        return null;
    }
    
    // 从Token中获取登录账号
    public static String getLoginAccount(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("loginAccount", String.class);
        }
        return null;
    }
}
