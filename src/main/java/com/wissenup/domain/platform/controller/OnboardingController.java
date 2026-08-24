package com.wissenup.domain.platform.controller;

import com.wissenup.domain.platform.dto.OnboardingRequest;
import com.wissenup.domain.platform.dto.OnboardingResponse;
import com.wissenup.domain.platform.service.OnboardingService;
import com.wissenup.shared.dto.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/schools")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<OnboardingResponse>> onboard(
            @Valid @RequestBody OnboardingRequest request) {
        SecurityContextUtil.requireSuperAdmin();
        OnboardingResponse response = onboardingService.onboardSchool(
            request, SecurityContextUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("School onboarded successfully", response));
    }
}
