package com.FindMyService.utils;

import com.FindMyService.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class OwnerCheck {

    private final JwtTokenUtil jwtTokenUtil;

    public OwnerCheck(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void verifyOwner(Long resourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
        if (isAdmin) {
            return;
        }

        String token = extractTokenFromRequest();
        String tokenId = jwtTokenUtil.extractUserId(token)
                .orElseThrow(() -> new AccessDeniedException("Invalid token"));

        if (!tokenId.equals(resourceId)) {
            throw new AccessDeniedException("Forbidden: You can only access your own resources");
        }
    }

    private String extractTokenFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new AccessDeniedException("No token found");
    }
}
