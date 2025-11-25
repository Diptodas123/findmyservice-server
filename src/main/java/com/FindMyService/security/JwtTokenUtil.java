package com.FindMyService.security;

import com.FindMyService.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:36000000}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String email, Role role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractEmail(String token) {
        return parseClaims(token)
                .map(Claims::getSubject); // Subject == email
    }

    public boolean validateToken(String token, String expectedEmail) {
        return parseClaims(token)
                .map(claims -> {
                    String email = claims.getSubject();
                    if (email == null) return false;

                    Date exp = claims.getExpiration();
                    return exp.after(new Date()) && email.equals(expectedEmail);
                })
                .orElse(false);
    }

    private Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Optional.of(claims);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return Optional.empty();

        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> extractUserId(String token) {
        return parseClaims(token)
                .map(claims -> claims.get("userId", String.class));
    }

    public Optional<String> extractRole(String token) {
        return parseClaims(token)
                .map(claims -> claims.get("role", String.class));
    }

}
