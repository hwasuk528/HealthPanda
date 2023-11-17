package com.ssafy.ssafit.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
    private static final String SALT = generateRandomSalt();

    public String createToken(Long value) {
        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1시간 후 만료
                .claim("id", value)
                .signWith(SignatureAlgorithm.HS256, SALT.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public boolean isValid(String token) {
        token = replaceToken(token);

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(SALT.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token);

            if (claimsJws.getBody().getExpiration().before(new Date())) {
                System.out.println("토큰이 만료되었습니다.");
                return false;
            }

            return true;

        } catch (JwtException e) {
            System.out.println("토큰 검증 에러: " + e.getMessage());
            return false;
        }
    }

    private String replaceToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }

        return token;
    }

    public Long extractUserIdFromToken(String token) {
        token = replaceToken(token);

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SALT.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("id", Long.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String generateRandomSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
