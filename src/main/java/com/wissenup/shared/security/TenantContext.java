package com.wissenup.shared.security;

/**
 * Thread-local tenant context holder.
 * Provides centralized access to the current request's tenant (organization) information.
 *
 * Extracted from JWT claims during authentication and made available
 * to service layer, repository, and interceptor layers.
 */
public class TenantContext {
    private static final ThreadLocal<Long> tenantId = new ThreadLocal<>();

    public static void setTenantId(Long organizationId) {
        if (organizationId != null) {
            tenantId.set(organizationId);
        }
    }

    public static Long getTenantId() {
        return tenantId.get();
    }

    public static void clear() {
        tenantId.remove();
    }

    public static boolean isSet() {
        return tenantId.get() != null;
    }
}
