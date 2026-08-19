package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.CreateTeacherSubjectAssignmentRequest;
import com.wissenup.domain.academic.dto.TeacherSubjectAssignmentDto;
import com.wissenup.domain.academic.service.TeacherSubjectAssignmentService;
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
@RequestMapping("/api/v1/teacher-subject-assignments")
@RequiredArgsConstructor
@Tag(name = "Teacher Subject Assignments", description = "Teacher subject assignment")
public class TeacherSubjectAssignmentController {

    private final TeacherSubjectAssignmentService service;

    @PostMapping
    @Operation(summary = "Assign subject to teacher")
    public ResponseEntity<ApiResponse<TeacherSubjectAssignmentDto>> create(
            @Valid @RequestBody CreateTeacherSubjectAssignmentRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        TeacherSubjectAssignmentDto result = service.createAssignment(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject assigned to teacher", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher subject assignment by ID")
    public ResponseEntity<ApiResponse<TeacherSubjectAssignmentDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        TeacherSubjectAssignmentDto result = service.getAssignment(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List subjects for a teacher")
    public ResponseEntity<ApiResponse<Page<TeacherSubjectAssignmentDto>>> list(
            @RequestParam Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size);
        Page<TeacherSubjectAssignmentDto> result = service.listByStaff(organizationId, staffId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove subject assignment from teacher")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteAssignment(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject assignment removed"));
    }
}


