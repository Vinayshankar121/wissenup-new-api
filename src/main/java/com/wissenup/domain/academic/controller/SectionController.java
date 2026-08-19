package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.CreateSectionRequest;
import com.wissenup.domain.academic.dto.SectionDto;
import com.wissenup.domain.academic.dto.UpdateSectionRequest;
import com.wissenup.domain.academic.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.data.domain.PageRequest;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.data.domain.Pageable;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.HttpStatus;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.ResponseEntity;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.web.bind.annotation.*;
import com.wissenup.shared.security.SecurityContextUtil;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Section management")
public class SectionController {

    private final SectionService service;

    @PostMapping
    @Operation(summary = "Create section")
    public ResponseEntity<ApiResponse<SectionDto>> create(@Valid @RequestBody CreateSectionRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        SectionDto result = service.createSection(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Section created", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get section by ID")
    public ResponseEntity<ApiResponse<SectionDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        SectionDto result = service.getSection(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List sections by class")
    public ResponseEntity<ApiResponse<Page<SectionDto>>> list(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size);
        Page<SectionDto> result = service.listSectionsByClass(organizationId, classId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update section")
    public ResponseEntity<ApiResponse<SectionDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSectionRequest request) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        SectionDto result = service.updateSection(id, request, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Section updated", result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete section")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteSection(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted"));
    }
}


