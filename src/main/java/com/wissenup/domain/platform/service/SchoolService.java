package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.SchoolDto;
import com.wissenup.domain.platform.dto.OrganizationUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

/**
 * School management service for super admin operations.
 * Handles CRUD operations and status management for schools.
 */
public interface SchoolService {

    /**
     * List all schools with pagination.
     *
     * @param pageable - Pagination info
     * @return - Page of school DTOs
     */
    Page<SchoolDto> listSchools(Pageable pageable);

    /**
     * Get school by ID.
     *
     * @param schoolId - School ID
     * @return - School DTO
     */
    SchoolDto getSchool(Long schoolId);

    /**
     * Get school by its tenant organization ID.
     *
     * @param organizationId - Tenant organization ID
     * @return - School DTO
     */
    SchoolDto getSchoolByOrganizationId(Long organizationId);

    /**
     * Get school by email.
     *
     * @param email - School email
     * @return - School DTO
     */
    SchoolDto getSchoolByEmail(String email);

    /**
     * Activate a school.
     *
     * @param schoolId - School ID
     * @param superAdminId - Super admin ID
     */
    void activateSchool(Long schoolId, Long superAdminId);

    /**
     * Deactivate a school.
     *
     * @param schoolId - School ID
     * @param superAdminId - Super admin ID
     */
    void deactivateSchool(Long schoolId, Long superAdminId);

    /**
     * Update school status.
     *
     * @param schoolId - School ID
     * @param status - New status
     * @param superAdminId - Super admin ID
     */
    void updateStatus(Long schoolId, String status, Long superAdminId);

    SchoolDto updateOrganization(Long organizationId, OrganizationUpdateRequest request, Long userId);

    void deleteOrganization(Long organizationId, Long userId);

    Map<String, Object> getOrganizationContext(Long organizationId);

}
