package com.wissenup.shared.security;

/**
 * JWT claims record containing authenticated user information.
 * Immutable data carrier for JWT claim values.
 */
public record JwtClaims(
    Long userId,
    Long organizationId,
    Long roleId,
    String email
) {
    public JwtClaims {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId cannot be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("roleId cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
    }
}
