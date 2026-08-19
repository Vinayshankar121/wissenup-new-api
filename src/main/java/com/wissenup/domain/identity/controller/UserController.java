package com.wissenup.domain.identity.controller;

import com.wissenup.domain.identity.dto.CreateUserRequest;
import com.wissenup.domain.identity.dto.UpdateUserRequest;
import com.wissenup.domain.identity.dto.UserDto;
import com.wissenup.domain.identity.service.UserService;
import com.wissenup.shared.dto.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user in the organization.
     * POST /api/v1/users
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user in the authenticated organization")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        UserDto createdUser = userService.createUser(request, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User created successfully", createdUser));
    }

    /**
     * Get user by ID.
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID (tenant-scoped)")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long userId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        UserDto user = userService.getUserById(userId, organizationId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Get all users in the organization.
     * GET /api/v1/users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users in the authenticated organization")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        List<UserDto> users = userService.getAllUsers(organizationId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Update user.
     * PUT /api/v1/users/{userId}
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Updates user details (phone, password, status)")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        UserDto updatedUser = userService.updateUser(userId, request, organizationId);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Delete user.
     * DELETE /api/v1/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user from the organization")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        userService.deleteUser(userId, organizationId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
