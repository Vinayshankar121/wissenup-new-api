package com.wissenup.domain.staff.controller;

import com.wissenup.domain.staff.dto.StaffRequest;
import com.wissenup.domain.staff.entity.Department;
import com.wissenup.domain.staff.entity.Designation;
import com.wissenup.domain.staff.entity.Staff;
import com.wissenup.domain.staff.entity.StaffClassAssignment;
import com.wissenup.domain.staff.entity.StaffSubjectAssignment;
import com.wissenup.domain.staff.service.StaffManagementService;
import com.wissenup.shared.dto.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StaffManagementController {
    private final StaffManagementService service;

    private Long organizationId() { return SecurityContextUtil.getCurrentOrganizationId(); }
    private Long currentUserId() { return SecurityContextUtil.getCurrentUserId(); }

    @GetMapping("/departments/organization/{ignoredOrganizationId}")
    public ApiResponse<List<Department>> departments(@PathVariable Long ignoredOrganizationId) {
        return ApiResponse.success(service.departments(organizationId()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> department(@RequestBody Department department) {
        Department saved = service.saveDepartment(department, organizationId(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    @PutMapping("/departments/{id}")
    public ApiResponse<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return ApiResponse.success("Department updated", service.updateDepartment(id, department, organizationId()));
    }

    @DeleteMapping("/departments/{id}")
    public ApiResponse<Department> archiveDepartment(@PathVariable Long id) {
        return ApiResponse.success("Department archived", service.departmentStatus(id, organizationId(), "INACTIVE"));
    }

    @PatchMapping("/departments/{id}/status")
    public ApiResponse<Department> departmentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success("Department status updated", service.departmentStatus(id, organizationId(), body.get("status")));
    }

    @GetMapping("/designations/organization/{ignoredOrganizationId}")
    public ApiResponse<List<Designation>> designations(@PathVariable Long ignoredOrganizationId) {
        return ApiResponse.success(service.designations(organizationId()));
    }

    @PostMapping("/designations")
    public ResponseEntity<ApiResponse<Designation>> designation(@RequestBody Designation designation) {
        Designation saved = service.saveDesignation(designation, organizationId(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    @PutMapping("/designations/{id}")
    public ApiResponse<Designation> updateDesignation(@PathVariable Long id, @RequestBody Designation designation) {
        return ApiResponse.success("Designation updated", service.updateDesignation(id, designation, organizationId()));
    }

    @DeleteMapping("/designations/{id}")
    public ApiResponse<Designation> archiveDesignation(@PathVariable Long id) {
        return ApiResponse.success("Designation archived", service.designationStatus(id, organizationId(), "INACTIVE"));
    }

    @PatchMapping("/designations/{id}/status")
    public ApiResponse<Designation> designationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success("Designation status updated", service.designationStatus(id, organizationId(), body.get("status")));
    }

    @GetMapping("/staff/organization/{ignoredOrganizationId}")
    public ApiResponse<List<Staff>> listStaff(@PathVariable Long ignoredOrganizationId) {
        return ApiResponse.success(service.staff(organizationId()));
    }

    @GetMapping("/staff/{id}")
    public ApiResponse<Staff> getStaff(@PathVariable Long id) {
        return ApiResponse.success(service.get(id, organizationId()));
    }

    @PostMapping("/staff/create")
    public ResponseEntity<ApiResponse<Staff>> create(@Valid @RequestBody StaffRequest request) {
        Staff saved = service.create(request, organizationId(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Staff created", saved));
    }

    @PutMapping("/staff/{id}")
    public ApiResponse<Staff> update(@PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return ApiResponse.success("Staff updated", service.update(id, request, organizationId(), currentUserId()));
    }

    @DeleteMapping("/staff/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id, organizationId());
        return ApiResponse.success("Staff deleted");
    }

    @PatchMapping("/staff/{id}/status")
    public ApiResponse<Staff> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(service.status(id, organizationId(), body.getOrDefault("status", "ACTIVE")));
    }

    @GetMapping("/staff-class-assignments/staff/{id}")
    public ApiResponse<List<StaffClassAssignment>> classes(@PathVariable Long id) { return ApiResponse.success(service.classAssignments(id, organizationId())); }

    @GetMapping("/staff-subject-assignments/staff/{id}")
    public ApiResponse<List<StaffSubjectAssignment>> subjects(@PathVariable Long id) { return ApiResponse.success(service.subjectAssignments(id, organizationId())); }
}
