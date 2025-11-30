package com.FindMyService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtUtil;
    private final SecretKey secretKey;

    public JwtAuthFilter(JwtTokenUtil jwtUtil,
                        @Value("${jwt.secret}") String secret) {
        this.jwtUtil = jwtUtil;
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                // Try to extract email - this will fail if token is expired
                email = jwtUtil.extractEmail(jwt).orElse(null);
                log.debug("Authorization header present, extracted email: {}", email);
            } catch (Exception e) {
                String masked = jwt.length() > 8 ? jwt.substring(0, 4) + "..." + jwt.substring(jwt.length() - 4) : "****";
                log.warn("Failed to extract email from token: {} - {}", masked, e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"JWT token is expired or invalid. Please login again.\"}");
                return;
            }

            if (email == null) {
                String masked = jwt.length() > 8 ? jwt.substring(0, 4) + "..." + jwt.substring(jwt.length() - 4) : "****";
                log.warn("Bearer token present but email extraction failed. token(needle)={} ", masked);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"JWT token is expired or invalid. Please login again.\"}");
                return;
            }
        } else{
            log.debug("No Authorization header or header does not start with 'Bearer '");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String role = claims.get("role", String.class);

                if (role == null) {
                    log.warn("JWT token for {} does not contain role claim", email);
                    filterChain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails = User.builder()
                        .username(email)
                        .password("") // Not needed for JWT auth
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                        .build();

                boolean valid = jwtUtil.validateToken(jwt, email);
                log.debug("Token validation for user '{}' returned: {}", email, valid);

                if (valid) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    log.debug("Authentication set for user: {} with authorities: {}", email, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set for user: {}", email);
                }
            } catch (Exception ex) {
                log.warn("Failed to parse JWT for {}: {}", email, ex.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}