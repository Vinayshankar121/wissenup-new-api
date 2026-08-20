package com.wissenup.domain.platform.controller;

import com.wissenup.domain.platform.dto.SchoolDto;
import com.wissenup.domain.platform.service.SchoolService;
import com.wissenup.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        List<SchoolDto> organizations = schoolService.listSchools(Pageable.unpaged()).getContent();
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<SchoolDto>> getOrganization(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success(
            schoolService.getSchoolByOrganizationId(organizationId)));
    }
}
