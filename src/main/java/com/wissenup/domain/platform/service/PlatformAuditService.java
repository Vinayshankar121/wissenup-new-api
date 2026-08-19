package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.PlatformAuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Platform audit service for logging all super admin actions.
 * Every action on platform endpoints is recorded with WHO, WHAT, WHEN, WHERE, WHY.
 */
public interface PlatformAuditService {

    /**
     * Log a platform action.
     *
     * @param action - Action name (e.g., SCHOOL_ONBOARDED, SCHOOL_ACTIVATED)
     * @param entityType - Entity type (e.g., SCHOOL, USER, PLAN)
     * @param entityId - Entity ID
     * @param organizationId - Organization ID
     * @param details - JSON details map
     * @param superAdminId - Super admin performing action
     */
    void log(String action, String entityType, Long entityId, Long organizationId,
             Map<String, Object> details, Long superAdminId);

    /**
     * List all audit logs with pagination.
     *
     * @param pageable - Pagination info
     * @return - Page of audit logs
     */
    Page<PlatformAuditLogDto> listLogs(Pageable pageable);

    /**
     * List audit logs by action.
     *
     * @param action - Action name
     * @param pageable - Pagination info
     * @return - Page of audit logs
     */
    Page<PlatformAuditLogDto> listByAction(String action, Pageable pageable);

    /**
     * List audit logs by super admin.
     *
     * @param superAdminId - Super admin ID
     * @param pageable - Pagination info
     * @return - Page of audit logs
     */
    Page<PlatformAuditLogDto> listByAdmin(Long superAdminId, Pageable pageable);

    /**
     * List audit logs by organization.
     *
     * @param organizationId - Organization ID
     * @param pageable - Pagination info
     * @return - Page of audit logs
     */
    Page<PlatformAuditLogDto> listByOrganization(Long organizationId, Pageable pageable);
}
