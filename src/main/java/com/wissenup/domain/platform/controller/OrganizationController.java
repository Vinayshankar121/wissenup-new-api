package com.wissenup.domain.platform.controller;

import com.wissenup.domain.platform.dto.SchoolDto;
import com.wissenup.domain.platform.dto.OrganizationUpdateRequest;
import com.wissenup.domain.platform.service.SchoolService;
import com.wissenup.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import com.wissenup.shared.security.SecurityContextUtil;

import java.util.List;
import java.util.Map;

/**
 * Organization-facing aliases for platform school management.
 * A school is the platform record associated with a tenant organization.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDto>>> listOrganizations() {
        SecurityContextUtil.requireSuperAdmin();
        List<SchoolDto> organizations = schoolService.listSchools(Pageable.unpaged()).getContent();
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<SchoolDto>> getOrganization(
            @PathVariable Long organizationId) {
        SecurityContextUtil.requireOrganizationAccess(organizationId);
        return ResponseEntity.ok(ApiResponse.success(
            schoolService.getSchoolByOrganizationId(organizationId)));
    }

    @GetMapping("/context")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContext(
            @RequestParam Long organizationId) {
        SecurityContextUtil.requireOrganizationAccess(organizationId);
        return ResponseEntity.ok(ApiResponse.success(schoolService.getOrganizationContext(organizationId)));
    }

    @PutMapping("/update/{organizationId}")
    public ResponseEntity<ApiResponse<SchoolDto>> updateOrganization(
            @PathVariable Long organizationId, @RequestBody OrganizationUpdateRequest request) {
        SecurityContextUtil.requireOrganizationAccess(organizationId);
        return ResponseEntity.ok(ApiResponse.success("Organization updated",
            schoolService.updateOrganization(organizationId, request, SecurityContextUtil.getCurrentUserId())));
    }

    @DeleteMapping("/delete/{organizationId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable Long organizationId) {
        SecurityContextUtil.requireSuperAdmin();
        schoolService.deleteOrganization(organizationId, SecurityContextUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Organization deleted"));
    }

}
