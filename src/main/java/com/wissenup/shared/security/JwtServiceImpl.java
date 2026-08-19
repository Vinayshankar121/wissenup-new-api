package com.wissenup.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT service implementation using JJWT library.
 * Generates and validates tokens with HMAC-SHA256 signature.
 */
@Slf4j
public class JwtServiceImpl implements JwtService {

    private static final String USER_ID = "uid";
    private static final String ORGANIZATION_ID = "org";
    private static final String ROLE_ID = "roleId";
    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtServiceImpl(String secret, long expiryMinutes) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                "JWT secret must be at least " + MIN_SECRET_LENGTH + " characters. " +
                "Generate with: openssl rand -base64 32"
            );
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    @Override
    public String generateToken(Long userId, Long organizationId, Long roleId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiryMinutes * 60);

        return Jwts.builder()
            .subject(email)
            .claim(USER_ID, userId)
            .claim(ORGANIZATION_ID, organizationId)
            .claim(ROLE_ID, roleId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    }

    @Override
    public JwtClaims parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            Long userId = claims.get(USER_ID, Long.class);
            Long organizationId = claims.get(ORGANIZATION_ID, Long.class);
            Long roleId = claims.get(ROLE_ID, Long.class);
            String email = claims.getSubject();

            if (userId == null || organizationId == null || roleId == null ||
                email == null || email.isBlank()) {
                throw new IllegalArgumentException("JWT is missing required claims");
            }

            return new JwtClaims(userId, organizationId, roleId, email);

        } catch (Exception ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            throw new IllegalArgumentException("Invalid or expired JWT token", ex);
        }
    }
}
