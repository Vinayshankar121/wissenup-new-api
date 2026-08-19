package com.wissenup.shared.security;

/**
 * Service for JWT token generation and validation.
 * Handles symmetric signing and validation using HMAC-SHA256.
 */
public interface JwtService {

    /**
     * Generate a signed JWT token.
     *
     * @param userId        authenticated user ID
     * @param organizationId tenant organization ID
     * @param roleId        primary role ID
     * @param email         user email address
     * @return signed JWT token string
     */
    String generateToken(Long userId, Long organizationId, Long roleId, String email);

    /**
     * Parse and validate a JWT token.
     * Verifies signature and required claims.
     *
     * @param token JWT token string
     * @return validated JWT claims
     * @throws IllegalArgumentException if token is invalid or missing required claims
     */
    JwtClaims parseAndValidate(String token);
}
