package com.wissenup.domain.identity.service;

import com.wissenup.domain.identity.dto.CreateUserRequest;
import com.wissenup.domain.identity.dto.UpdateUserRequest;
import com.wissenup.domain.identity.dto.UserDto;

import java.util.List;

/**
 * Service for user management operations.
 * Handles CRUD operations for users within an organization.
 */
public interface UserService {

    /**
     * Create a new user in the organization.
     * Email must be unique globally.
     *
     * @param request user creation request with email, phone, password
     * @param organizationId organization context (from JWT)
     * @return created user DTO
     * @throws com.wissenup.shared.exception.ConflictException if email already exists
     */
    UserDto createUser(CreateUserRequest request, Long organizationId);

    /**
     * Get user by ID within the organization.
     * Tenant isolation: only users in the authenticated organization can be retrieved.
     *
     * @param userId user ID
     * @param organizationId organization context (from JWT)
     * @return user DTO
     * @throws com.wissenup.shared.exception.ResourceNotFoundException if user not found
     * @throws org.springframework.security.access.AccessDeniedException if user in different organization
     */
    UserDto getUserById(Long userId, Long organizationId);

    /**
     * Get all users in the organization.
     *
     * @param organizationId organization context (from JWT)
     * @return list of user DTOs
     */
    List<UserDto> getAllUsers(Long organizationId);

    /**
     * Update user in the organization.
     * Cannot change email or organization.
     *
     * @param userId user ID
     * @param request update request
     * @param organizationId organization context (from JWT)
     * @return updated user DTO
     * @throws com.wissenup.shared.exception.ResourceNotFoundException if user not found
     */
    UserDto updateUser(Long userId, UpdateUserRequest request, Long organizationId);

    /**
     * Delete user from the organization (soft or hard delete per business rules).
     *
     * @param userId user ID
     * @param organizationId organization context (from JWT)
     * @throws com.wissenup.shared.exception.ResourceNotFoundException if user not found
     */
    void deleteUser(Long userId, Long organizationId);
}
