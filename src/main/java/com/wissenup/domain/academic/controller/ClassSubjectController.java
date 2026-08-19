package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.ClassSubjectDto;
import com.wissenup.domain.academic.dto.CreateClassSubjectRequest;
import com.wissenup.domain.academic.service.ClassSubjectService;
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
@RequestMapping("/api/v1/class-subjects")
@RequiredArgsConstructor
@Tag(name = "Class Subjects", description = "Class subject assignment")
public class ClassSubjectController {

    private final ClassSubjectService service;

    @PostMapping
    @Operation(summary = "Assign subject to class")
    public ResponseEntity<ApiResponse<ClassSubjectDto>> create(@Valid @RequestBody CreateClassSubjectRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        ClassSubjectDto result = service.createClassSubject(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject assigned to class", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class subject by ID")
    public ResponseEntity<ApiResponse<ClassSubjectDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        ClassSubjectDto result = service.getClassSubject(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List subjects for a class")
    public ResponseEntity<ApiResponse<Page<ClassSubjectDto>>> list(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ClassSubjectDto> result = service.listByClass(organizationId, classId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove subject from class")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteClassSubject(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject removed from class"));
    }
}


