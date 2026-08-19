package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.ClassTeacherAssignmentDto;
import com.wissenup.domain.academic.dto.CreateClassTeacherAssignmentRequest;
import com.wissenup.domain.academic.service.ClassTeacherAssignmentService;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/class-teacher-assignments")
@RequiredArgsConstructor
@Tag(name = "Class Teacher Assignments", description = "Class teacher assignment")
public class ClassTeacherAssignmentController {

    private final ClassTeacherAssignmentService service;

    @PostMapping
    @Operation(summary = "Assign teacher to class")
    public ResponseEntity<ApiResponse<ClassTeacherAssignmentDto>> create(
            @Valid @RequestBody CreateClassTeacherAssignmentRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        ClassTeacherAssignmentDto result = service.createAssignment(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teacher assigned to class", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class teacher assignment by ID")
    public ResponseEntity<ApiResponse<ClassTeacherAssignmentDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        ClassTeacherAssignmentDto result = service.getAssignment(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List teachers for a class")
    public ResponseEntity<ApiResponse<Page<ClassTeacherAssignmentDto>>> list(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ClassTeacherAssignmentDto> result = service.listByClass(organizationId, classId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{classId}/class-teacher")
    @Operation(summary = "Get class teacher (advisor)")
    public ResponseEntity<ApiResponse<ClassTeacherAssignmentDto>> getClassTeacher(@PathVariable Long classId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Optional<ClassTeacherAssignmentDto> result = service.getClassTeacher(organizationId, classId);
        return ResponseEntity.ok(ApiResponse.success(result.orElse(null)));
    }

    @PatchMapping("/{classId}/assign-class-teacher")
    @Operation(summary = "Assign class teacher (advisor)")
    public ResponseEntity<ApiResponse<Void>> assignClassTeacher(
            @PathVariable Long classId,
            @RequestParam Long staffId) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.assignClassTeacher(organizationId, classId, staffId, userId);
        return ResponseEntity.ok(ApiResponse.success("Class teacher assigned"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove teacher from class")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteAssignment(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Teacher assignment removed"));
    }
}


