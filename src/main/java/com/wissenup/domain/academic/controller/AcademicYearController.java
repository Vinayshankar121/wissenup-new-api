package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import com.wissenup.domain.academic.service.AcademicYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.data.domain.PageRequest;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.data.domain.Pageable;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.data.domain.Sort;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.HttpStatus;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.ResponseEntity;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.web.bind.annotation.*;
import com.wissenup.shared.security.SecurityContextUtil;

@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
@Tag(name = "Academic Years", description = "Academic year management")
public class AcademicYearController {

    private final AcademicYearService service;

    @PostMapping
    @Operation(summary = "Create academic year")
    public ResponseEntity<ApiResponse<AcademicYearDto>> create(@Valid @RequestBody CreateAcademicYearRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        AcademicYearDto result = service.createAcademicYear(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Academic year created", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academic year by ID")
    public ResponseEntity<ApiResponse<AcademicYearDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        AcademicYearDto result = service.getAcademicYear(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List academic years with pagination")
    public ResponseEntity<ApiResponse<Page<AcademicYearDto>>> list(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "25") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AcademicYearDto> result = service.listAcademicYears(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active academic year")
    public ResponseEntity<ApiResponse<AcademicYearDto>> getActive() {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        AcademicYearDto result = service.getActiveAcademicYear(organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update academic year")
    public ResponseEntity<ApiResponse<AcademicYearDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAcademicYearRequest request) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        AcademicYearDto result = service.updateAcademicYear(id, request, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Academic year updated", result));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate academic year")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.activateAcademicYear(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Academic year activated"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate academic year")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deactivateAcademicYear(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Academic year deactivated"));
    }

    @PostMapping("/{id}/copy-structure")
    @Operation(summary = "Copy classes, sections, and subject mappings from another academic year")
    public ResponseEntity<ApiResponse<Void>> copyStructure(
            @PathVariable Long id,
            @RequestParam Long sourceAcademicYearId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.copyStructure(id, sourceAcademicYearId, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Academic structure copied"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete academic year")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteAcademicYear(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Academic year deleted"));
    }
}


