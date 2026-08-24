package com.wissenup.domain.fee.controller;

import com.wissenup.domain.fee.dto.FeeRequests;
import com.wissenup.domain.fee.dto.FeeViews;
import com.wissenup.domain.fee.entity.FeeType;
import com.wissenup.domain.fee.service.FeeService;
import com.wissenup.shared.exception.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FeeController {
    private final FeeService service;

    private Long organizationId() { return SecurityContextUtil.getCurrentOrganizationId(); }
    private Long userId() { return SecurityContextUtil.getCurrentUserId(); }
    private Long roleId() { return SecurityContextUtil.getCurrentRoleId(); }
    private void requireTenant(Long requested) { SecurityContextUtil.requireOrganizationAccess(requested); }

    @GetMapping("/fee-types")
    public ApiResponse<List<FeeType>> feeTypes(@RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.types(organizationId()));
    }

    @GetMapping("/fee-types/search")
    public ApiResponse<List<FeeType>> searchFeeTypes(@RequestParam(required = false) Long organizationId,
                                                     @RequestParam String name) {
        requireTenant(organizationId);
        return ApiResponse.success(service.searchTypes(organizationId(), name));
    }

    @GetMapping("/fee-types/{id}")
    public ApiResponse<FeeType> feeType(@PathVariable Long id) {
        return ApiResponse.success(service.type(id, organizationId()));
    }

    @PostMapping("/fee-types")
    public ApiResponse<FeeType> createFeeType(@Valid @RequestBody FeeRequests.Type request) {
        return ApiResponse.success("Fee type created", service.saveType(null, organizationId(), userId(), roleId(), request));
    }

    @PutMapping("/fee-types/{id}")
    public ApiResponse<FeeType> updateFeeType(@PathVariable Long id, @Valid @RequestBody FeeRequests.Type request) {
        return ApiResponse.success("Fee type updated", service.saveType(id, organizationId(), userId(), roleId(), request));
    }

    @DeleteMapping("/fee-types/{id}")
    public ApiResponse<Void> archiveFeeType(@PathVariable Long id, @RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        service.archiveType(id, organizationId(), userId(), roleId());
        return ApiResponse.success("Fee type archived");
    }

    @GetMapping("/fee-structures")
    public ApiResponse<List<FeeViews.Structure>> feeStructures(@RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.structures(organizationId(), null, null));
    }

    @GetMapping("/fee-structures/by-academic-year")
    public ApiResponse<List<FeeViews.Structure>> structuresByYear(@RequestParam(required = false) Long organizationId,
                                                                  @RequestParam Long academicYearId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.structures(organizationId(), academicYearId, null));
    }

    @GetMapping("/fee-structures/by-class-and-academic-year")
    public ApiResponse<List<FeeViews.Structure>> structuresByClass(@RequestParam(required = false) Long organizationId,
                                                                   @RequestParam Long academicYearId,
                                                                   @RequestParam Long classId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.structures(organizationId(), academicYearId, classId));
    }

    @GetMapping("/fee-structures/{id}")
    public ApiResponse<FeeViews.Structure> feeStructure(@PathVariable Long id) {
        return ApiResponse.success(service.structure(id, organizationId()));
    }

    @PostMapping("/fee-structures")
    public ApiResponse<FeeViews.Structure> createStructure(@Valid @RequestBody FeeRequests.Structure request) {
        return ApiResponse.success("Fee structure created", service.saveStructure(null, organizationId(), userId(), roleId(), request));
    }

    @PutMapping("/fee-structures/{id}")
    public ApiResponse<FeeViews.Structure> updateStructure(@PathVariable Long id, @Valid @RequestBody FeeRequests.Structure request) {
        return ApiResponse.success("Fee structure updated", service.saveStructure(id, organizationId(), userId(), roleId(), request));
    }

    @DeleteMapping("/fee-structures/{id}")
    public ApiResponse<Void> archiveStructure(@PathVariable Long id, @RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        service.archiveStructure(id, organizationId(), userId(), roleId());
        return ApiResponse.success("Fee structure archived");
    }

    @PostMapping("/student-fees/assign")
    public ApiResponse<List<FeeViews.StudentFee>> assignStudent(@Valid @RequestBody FeeRequests.Assign request) {
        return ApiResponse.success("Fees assigned", service.assignStudent(organizationId(), userId(), roleId(), request));
    }

    @PostMapping("/student-fees/assign-class")
    public ApiResponse<FeeViews.AssignResult> assignClass(@Valid @RequestBody FeeRequests.AssignClass request) {
        return ApiResponse.success("Class fees assigned", service.assignClass(organizationId(), userId(), roleId(), request));
    }

    @PostMapping("/student-fees/additional-fee")
    public ApiResponse<FeeViews.StudentFee> additionalFee(@Valid @RequestBody FeeRequests.Additional request) {
        return ApiResponse.success("Additional fee added", service.additional(organizationId(), userId(), roleId(), request));
    }

    @PutMapping("/student-fees/{id}/discount")
    public ApiResponse<FeeViews.StudentFee> discount(@PathVariable Long id, @Valid @RequestBody FeeRequests.Discount request) {
        return ApiResponse.success("Discount applied", service.discount(id, organizationId(), userId(), roleId(), request));
    }

    @GetMapping("/student-fees/student/{studentId}")
    public ApiResponse<List<FeeViews.StudentFee>> studentFees(@PathVariable Long studentId) {
        return ApiResponse.success(service.studentFees(studentId, organizationId()));
    }

    @GetMapping("/student-fees/pending")
    public ApiResponse<List<FeeViews.StudentFee>> allStudentFees(@RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.allStudentFees(organizationId()));
    }

    @PostMapping("/fee-payments")
    public ApiResponse<FeeViews.Payment> collect(@Valid @RequestBody FeeRequests.Payment request) {
        return ApiResponse.success("Payment collected", service.collect(organizationId(), userId(), roleId(), request));
    }

    @GetMapping("/fee-payments")
    public ApiResponse<List<FeeViews.Payment>> payments(@RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.payments(organizationId(), null));
    }

    @GetMapping("/fee-payments/student/{studentId}")
    public ApiResponse<List<FeeViews.Payment>> paymentsByStudent(@PathVariable Long studentId) {
        return ApiResponse.success(service.payments(organizationId(), studentId));
    }

    @GetMapping("/fee-payments/receipt/{receiptNumber}")
    public ApiResponse<FeeViews.Payment> receipt(@PathVariable String receiptNumber,
                                                 @RequestParam(required = false) Long organizationId) {
        requireTenant(organizationId);
        return ApiResponse.success(service.receipt(receiptNumber, organizationId()));
    }

    @GetMapping("/fee-payments/{id}")
    public ApiResponse<FeeViews.Payment> payment(@PathVariable Long id) {
        return ApiResponse.success(service.payment(id, organizationId()));
    }

    @PostMapping("/fee-payments/refund")
    public ApiResponse<FeeViews.Payment> refund(@Valid @RequestBody FeeRequests.Refund request) {
        return ApiResponse.success("Payment refunded", service.refund(organizationId(), userId(), roleId(), request));
    }
}
