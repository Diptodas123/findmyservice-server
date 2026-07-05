package com.FindMyService.utils;

import com.FindMyService.security.JwtTokenUtil;
import com.FindMyService.model.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerCheckTest {

    @Mock private JwtTokenUtil jwtTokenUtil;

    private OwnerCheck ownerCheck;

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm!!";

    @BeforeEach
    void setUp() {
        ownerCheck = new OwnerCheck(jwtTokenUtil);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void setAuth(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user@test.com", null, role));
    }

    private void setRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void adminBypassesOwnerCheck() {
        setAuth("ADMIN");
        // should not throw — no token extraction needed
        ownerCheck.verifyOwner(99L);
    }

    @Test
    void ownerWithMatchingIdPasses() {
        setAuth("USER");
        setRequest("valid-token");
        when(jwtTokenUtil.extractUserId("valid-token")).thenReturn(Optional.of("1"));

        ownerCheck.verifyOwner(1L); // no exception
    }

    @Test
    void ownerWithDifferentIdThrowsForbidden() {
        setAuth("USER");
        setRequest("valid-token");
        when(jwtTokenUtil.extractUserId("valid-token")).thenReturn(Optional.of("1"));

        assertThatThrownBy(() -> ownerCheck.verifyOwner(2L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void noAuthenticationThrowsUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> ownerCheck.verifyOwner(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void missingBearerTokenThrowsForbidden() {
        setAuth("USER");
        setRequest(null); // no Authorization header

        assertThatThrownBy(() -> ownerCheck.verifyOwner(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void invalidTokenUserIdThrowsForbidden() {
        setAuth("USER");
        setRequest("bad-token");
        when(jwtTokenUtil.extractUserId("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerCheck.verifyOwner(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}