package com.FindMyService.utils;

import com.FindMyService.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OwnerCheck {

    private final JwtTokenUtil jwtTokenUtil;

    public OwnerCheck(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void verifyOwner(Long resourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()));
        if (isAdmin) {
            return;
        }

        String token = extractTokenFromRequest();
        Long tokenId = Long.valueOf(jwtTokenUtil.extractUserId(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token")));

        if (!tokenId.equals(resourceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: You can only access your own resources");
        }
    }

    private String extractTokenFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No token found");
    }
}
