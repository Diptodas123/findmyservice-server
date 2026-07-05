package com.FindMyService.security;

import com.FindMyService.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    // 64-char secret satisfies HS256 minimum key length
    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm!!";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil(SECRET, 3600000L);
    }

    @Test
    void generateAndExtractEmail() {
        String token = jwtTokenUtil.generateToken("1", "user@test.com", Role.USER);
        assertThat(jwtTokenUtil.extractEmail(token)).contains("user@test.com");
    }

    @Test
    void generateAndExtractUserId() {
        String token = jwtTokenUtil.generateToken("42", "user@test.com", Role.USER);
        assertThat(jwtTokenUtil.extractUserId(token)).contains("42");
    }

    @Test
    void generateAndExtractRole() {
        String token = jwtTokenUtil.generateToken("1", "user@test.com", Role.PROVIDER);
        assertThat(jwtTokenUtil.extractRole(token)).contains("PROVIDER");
    }

    @Test
    void validateTokenValidReturnsTrue() {
        String token = jwtTokenUtil.generateToken("1", "user@test.com", Role.USER);
        assertThat(jwtTokenUtil.validateToken(token, "user@test.com")).isTrue();
    }

    @Test
    void validateTokenWrongEmailReturnsFalse() {
        String token = jwtTokenUtil.generateToken("1", "user@test.com", Role.USER);
        assertThat(jwtTokenUtil.validateToken(token, "other@test.com")).isFalse();
    }

    @Test
    void validateTokenExpiredReturnsFalse() {
        JwtTokenUtil expiredUtil = new JwtTokenUtil(SECRET, -1000L); // already expired
        String token = expiredUtil.generateToken("1", "user@test.com", Role.USER);
        assertThat(expiredUtil.validateToken(token, "user@test.com")).isFalse();
    }

    @Test
    void extractEmailFromInvalidTokenReturnsEmpty() {
        assertThat(jwtTokenUtil.extractEmail("not.a.token")).isEmpty();
    }

    @Test
    void extractUserIdFromInvalidTokenReturnsEmpty() {
        assertThat(jwtTokenUtil.extractUserId("not.a.token")).isEmpty();
    }
}
