package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.ClassDto;
import com.wissenup.domain.academic.dto.CreateClassRequest;
import com.wissenup.domain.academic.dto.UpdateClassRequest;
import com.wissenup.domain.academic.service.ClassService;
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
import org.springframework.data.domain.Sort;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.HttpStatus;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.http.ResponseEntity;
import com.wissenup.shared.security.SecurityContextUtil;
import org.springframework.web.bind.annotation.*;
import com.wissenup.shared.security.SecurityContextUtil;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Class management")
public class ClassController {

    private final ClassService service;

    @PostMapping
    @Operation(summary = "Create class")
    public ResponseEntity<ApiResponse<ClassDto>> create(@Valid @RequestBody CreateClassRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        ClassDto result = service.createClass(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Class created", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<ApiResponse<ClassDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        ClassDto result = service.getClass(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List classes by academic year")
    public ResponseEntity<ApiResponse<Page<ClassDto>>> list(
            @RequestParam Long academicYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "level") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ClassDto> result = service.listClasses(organizationId, academicYearId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update class")
    public ResponseEntity<ApiResponse<ClassDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassRequest request) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        ClassDto result = service.updateClass(id, request, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Class updated", result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete class")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteClass(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Class deleted"));
    }
}


