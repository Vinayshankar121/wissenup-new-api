package com.wissenup.domain.identity.controller;

import com.wissenup.domain.identity.dto.CreateUserRoleRequest;
import com.wissenup.domain.identity.dto.UserRoleDto;
import com.wissenup.domain.identity.service.UserRoleService;
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
@RequestMapping("/api/v1/user-roles")
@Tag(name = "User Roles", description = "User role assignment endpoints (RBAC)")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Assign a role to a user.
     * POST /api/v1/user-roles
     */
    @PostMapping
    @Operation(summary = "Assign role to user", description = "Assigns a role to a user in the organization")
    public ResponseEntity<ApiResponse<UserRoleDto>> assignRoleToUser(@Valid @RequestBody CreateUserRoleRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        UserRoleDto userRole = userRoleService.assignRoleToUser(request, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Role assigned successfully", userRole));
    }

    /**
     * Get all roles for a user.
     * GET /api/v1/user-roles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user roles", description = "Retrieves all roles for a specific user")
    public ResponseEntity<ApiResponse<List<UserRoleDto>>> getUserRoles(@PathVariable Long userId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        List<UserRoleDto> roles = userRoleService.getUserRoles(userId, organizationId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Remove a role from a user.
     * DELETE /api/v1/user-roles/{userRoleId}
     */
    @DeleteMapping("/{userRoleId}")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(@PathVariable Long userRoleId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        userRoleService.removeRoleFromUser(userRoleId, organizationId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
    }
}
