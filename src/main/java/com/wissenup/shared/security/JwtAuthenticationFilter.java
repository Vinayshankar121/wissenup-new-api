package com.wissenup.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter.
 * Extracts JWT from Authorization header, validates it, and sets authentication context.
 * Also sets TenantContext for use by service layer.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            // No authorization header
            if (header == null || !header.startsWith(BEARER_PREFIX)) {
                chain.doFilter(request, response);
                return;
            }

            // Extract and validate token
            String token = header.substring(BEARER_PREFIX.length());
            JwtClaims claims;

            try {
                claims = jwtService.parseAndValidate(token);
            } catch (IllegalArgumentException ex) {
                log.warn("JWT validation failed for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
                TenantContext.clear();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid or expired authentication token\"}"
                );
                return;
            }

            // Set tenant context
            TenantContext.setTenantId(claims.organizationId());

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    claims,
                    null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"))
                );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } finally {
            // Clear thread-local on request completion
            TenantContext.clear();
        }
    }
}
