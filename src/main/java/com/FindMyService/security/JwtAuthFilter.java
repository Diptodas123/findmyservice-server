package com.FindMyService.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            email = jwtUtil.extractEmail(jwt).orElse(null);
            log.debug("Authorization header present, extracted email: {}", email);
        } else{
            log.debug("No Authorization header or header does not start with 'Bearer '");
        }

        if (jwt != null && email == null) {
            String masked = jwt.length() > 8 ? jwt.substring(0, 4) + "..." + jwt.substring(jwt.length() - 4) : "****";
            log.warn("Bearer token present but email extraction failed. token(needle)={} ", masked);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(email);
            } catch (Exception ex) {
                log.debug("UserDetailsService failed to load user for {}: {}", email, ex.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            boolean valid = jwtUtil.validateToken(jwt, userDetails.getUsername());
            log.debug("Token validation for user '{}' returned: {}", userDetails.getUsername(), valid);

            if (valid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                log.debug("Authentication set for user: {} with authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authentication set for user: {}", userDetails.getUsername());
            }
        }

        filterChain.doFilter(request, response);
    }
}