package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.ModuleDto;

import java.util.List;

/**
 * Module management service for enable/disable operations.
 */
public interface ModuleService {

    /**
     * List all available modules.
     *
     * @return - List of module DTOs
     */
    List<ModuleDto> listAllModules();

    /**
     * List enabled modules for a school.
     *
     * @param organizationId - School organization ID
     * @return - List of enabled module DTOs
     */
    List<ModuleDto> listEnabledModulesForSchool(Long organizationId);

    /**
     * Check if a module is enabled for a school.
     *
     * @param organizationId - School organization ID
     * @param moduleCode - Module code
     * @return - true if enabled, false otherwise
     */
    boolean isModuleEnabledForSchool(Long organizationId, String moduleCode);

    /**
     * Enable a module for a school.
     *
     * @param organizationId - School organization ID
     * @param moduleCode - Module code
     * @param superAdminId - Super admin ID
     */
    void enableModuleForSchool(Long organizationId, String moduleCode, Long superAdminId);

    /**
     * Disable a module for a school.
     *
     * @param organizationId - School organization ID
     * @param moduleCode - Module code
     * @param superAdminId - Super admin ID
     */
    void disableModuleForSchool(Long organizationId, String moduleCode, Long superAdminId);
}
