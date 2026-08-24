package com.wissenup.domain.platform.controller;

import com.wissenup.domain.platform.dto.SubscriptionPlanDto;
import com.wissenup.domain.platform.service.SubscriptionService;
import com.wissenup.shared.dto.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDto>>> list() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.listPlans()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getPlan(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> create(@RequestBody SubscriptionPlanDto request) {
        SecurityContextUtil.requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success("Subscription plan created", subscriptionService.createPlan(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> update(
            @PathVariable Long id, @RequestBody SubscriptionPlanDto request) {
        SecurityContextUtil.requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated", subscriptionService.updatePlan(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        SecurityContextUtil.requireSuperAdmin();
        subscriptionService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deactivated"));
    }
}
