package com.wissenup.domain.student.controller;

import com.wissenup.domain.student.dto.*;
import com.wissenup.domain.student.entity.*;
import com.wissenup.domain.student.repository.*;
import com.wissenup.domain.student.service.StudentRegistrationService;
import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.shared.security.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
public class StudentController {
    private final StudentRegistrationService service;
    private final StudentParentRepository studentParents;
    private final AddressRepository addresses;

    @PostMapping("/api/v1/student-registration")
    public ResponseEntity<ApiResponse<StudentResponse>> register(@Valid @RequestBody StudentRegistrationRequest request) {
        var result=service.register(request, SecurityContextUtil.getCurrentOrganizationId(), SecurityContextUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Student admitted",result));
    }
    @GetMapping("/api/v1/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> list(){return ResponseEntity.ok(ApiResponse.success(service.list(SecurityContextUtil.getCurrentOrganizationId())));}
    @GetMapping("/api/v1/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> get(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success(service.get(id,SecurityContextUtil.getCurrentOrganizationId())));}
    @GetMapping("/api/v1/student-enrollments/class/{classId}/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> enrolledStudents(@PathVariable Long classId,@PathVariable Long sectionId){
        return ResponseEntity.ok(ApiResponse.success(service.enrolledStudents(SecurityContextUtil.getCurrentOrganizationId(),classId,sectionId)));
    }
    @PutMapping("/api/v1/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> update(@PathVariable Long id, @Valid @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Student updated", service.update(id, request, SecurityContextUtil.getCurrentOrganizationId())));
    }
    @DeleteMapping("/api/v1/students/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){service.delete(id,SecurityContextUtil.getCurrentOrganizationId());return ResponseEntity.ok(ApiResponse.success("Student deleted"));}
    @GetMapping("/api/v1/parents/search")
    public ResponseEntity<ApiResponse<Parent>> parent(@RequestParam String phoneNumber){return ResponseEntity.ok(ApiResponse.success(service.findParent(SecurityContextUtil.getCurrentOrganizationId(),phoneNumber)));}
    @GetMapping("/api/v1/parents/by-user/{userId}")
    public ResponseEntity<ApiResponse<Parent>> parentByUser(@PathVariable Long userId) {
        Parent parent = service.findParentByUser(userId, SecurityContextUtil.getCurrentOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(parent));
    }
    @GetMapping("/api/v1/student-parents/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<StudentParent>>> links(@PathVariable Long parentId){return ResponseEntity.ok(ApiResponse.success(studentParents.findAllByParentId(parentId)));}
    @GetMapping("/api/v1/student-parents/parent/{parentId}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> children(@PathVariable Long parentId) {
        return ResponseEntity.ok(ApiResponse.success(service.findChildren(parentId, SecurityContextUtil.getCurrentOrganizationId())));
    }
    @GetMapping("/api/v1/addresses/{id}")
    public ResponseEntity<ApiResponse<Address>> address(@PathVariable Long id){
        Address a=addresses.findById(id).filter(row -> row.getOrganizationId().equals(SecurityContextUtil.getCurrentOrganizationId()))
            .orElseThrow(() -> ResourceNotFoundException.notFound("Address",id));
        return ResponseEntity.ok(ApiResponse.success(a));
    }
}
