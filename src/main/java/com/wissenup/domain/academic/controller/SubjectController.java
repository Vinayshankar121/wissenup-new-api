package com.wissenup.domain.academic.controller;


import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.domain.academic.dto.CreateSubjectRequest;
import com.wissenup.domain.academic.dto.SubjectDto;
import com.wissenup.domain.academic.dto.UpdateSubjectRequest;
import com.wissenup.domain.academic.service.SubjectService;
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
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject management")
public class SubjectController {

    private final SubjectService service;

    @PostMapping
    @Operation(summary = "Create subject")
    public ResponseEntity<ApiResponse<SubjectDto>> create(@Valid @RequestBody CreateSubjectRequest request) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        SubjectDto result = service.createSubject(request, organizationId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID")
    public ResponseEntity<ApiResponse<SubjectDto>> get(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        SubjectDto result = service.getSubject(id, organizationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List subjects")
    public ResponseEntity<ApiResponse<Page<SubjectDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Pageable pageable = PageRequest.of(page, size);
        Page<SubjectDto> result = service.listSubjects(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subject")
    public ResponseEntity<ApiResponse<SubjectDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        SubjectDto result = service.updateSubject(id, request, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject updated", result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subject")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long organizationId = SecurityContextUtil.getCurrentOrganizationId();
        Long userId = SecurityContextUtil.getCurrentUserId();
        service.deleteSubject(id, organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted"));
    }
}


