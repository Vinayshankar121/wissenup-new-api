package com.wissenup.domain.identity.service;

import com.wissenup.domain.identity.dto.CreateUserRoleRequest;
import com.wissenup.domain.identity.dto.UserRoleDto;

import java.util.List;

/**
 * Service for user-role assignment operations.
 * Handles RBAC: assigning roles to users within an organization.
 */
public interface UserRoleService {

    /**
     * Assign a role to a user.
     * A user can have multiple roles.
     *
     * @param request role assignment request with userId and roleId
     * @param organizationId organization context (from JWT)
     * @return created user-role DTO
     * @throws com.wissenup.shared.exception.ConflictException if user already has this role
     * @throws com.wissenup.shared.exception.ResourceNotFoundException if user or role not found
     */
    UserRoleDto assignRoleToUser(CreateUserRoleRequest request, Long organizationId);

    /**
     * Get roles for a user.
     *
     * @param userId user ID
     * @param organizationId organization context (from JWT)
     * @return list of role DTOs for the user
     */
    List<UserRoleDto> getUserRoles(Long userId, Long organizationId);

    /**
     * Remove a role from a user.
     *
     * @param userRoleId user-role ID
     * @param organizationId organization context (from JWT)
     * @throws com.wissenup.shared.exception.ResourceNotFoundException if assignment not found
     */
    void removeRoleFromUser(Long userRoleId, Long organizationId);

    /**
     * Check if a user has a specific role.
     *
     * @param userId user ID
     * @param roleId role ID
     * @param organizationId organization context (from JWT)
     * @return true if user has the role, false otherwise
     */
    boolean userHasRole(Long userId, Long roleId, Long organizationId);
}
