package com.wissenup.shared.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for accessing authentication context and validating tenant access.
 * Provides convenient methods for services and repositories to:
 * - Get current user ID
 * - Get current organization ID
 * - Get current role ID
 * - Get current email
 * - Validate tenant access
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
        // Utility class
    }

    /**
     * Get JWT claims from security context.
     *
     * @return JWT claims of authenticated user
     * @throws IllegalStateException if no authenticated user in context
     */
    public static JwtClaims getClaims() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null ?
            null :
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof JwtClaims claims)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return claims;
    }

    /**
     * Get current authenticated user ID.
     */
    public static Long getCurrentUserId() {
        return getClaims().userId();
    }

    /**
     * Get current tenant (organization) ID.
     */
    public static Long getCurrentOrganizationId() {
        return getClaims().organizationId();
    }

    /**
     * Get current user's primary role ID.
     */
    public static Long getCurrentRoleId() {
        return getClaims().roleId();
    }

    /**
     * Get current authenticated user's email.
     */
    public static String getCurrentUserEmail() {
        return getClaims().email();
    }

    /**
     * Validate that the requested organization ID matches the authenticated user's organization.
     * SUPER_ADMIN (organization 0) can access all organizations.
     *
     * @param requestedOrganizationId organization ID from request
     * @throws AccessDeniedException if organization mismatch
     */
    public static void requireOrganizationAccess(Long requestedOrganizationId) {
        Long authenticatedOrgId = getCurrentOrganizationId();

        // SUPER_ADMIN check: organization_id = 0
        if (authenticatedOrgId != null && authenticatedOrgId == 0L) {
            return; // SUPER_ADMIN can access any organization
        }

        // Cross-tenant access check
        if (requestedOrganizationId != null && authenticatedOrgId != null &&
            !authenticatedOrgId.equals(requestedOrganizationId)) {
            throw new AccessDeniedException(
                "Organization access denied: user belongs to organization " +
                authenticatedOrgId + ", requested " + requestedOrganizationId
            );
        }
    }

    /**
     * Check if a specific organization ID matches authenticated user's tenant.
     *
     * @param organizationId organization ID to check
     * @return true if user has access to this organization
     */
    public static boolean hasOrganizationAccess(Long organizationId) {
        try {
            requireOrganizationAccess(organizationId);
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

    /**
     * Check if current user is SUPER_ADMIN.
     */
    public static boolean isSuperAdmin() {
        return Long.valueOf(0L).equals(getCurrentOrganizationId());
    }

    public static void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new AccessDeniedException("Only a platform administrator can perform this operation");
        }
    }

    /**
     * Check if user is authenticated.
     */
    public static boolean isAuthenticated() {
        try {
            getClaims();
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }
}
