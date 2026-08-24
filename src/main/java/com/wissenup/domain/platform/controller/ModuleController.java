package com.wissenup.domain.platform.controller;

import com.wissenup.domain.platform.dto.ModuleDto;
import com.wissenup.domain.platform.service.ModuleService;
import com.wissenup.shared.dto.ApiResponse;
import com.wissenup.shared.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleDto>>> listModules() {
        SecurityContextUtil.requireSuperAdmin();
        return ResponseEntity.ok(ApiResponse.success(moduleService.listAllModules()));
    }

    @GetMapping("/organization/{organizationId}/enabled")
    public ResponseEntity<ApiResponse<List<ModuleDto>>> listEnabledModules(
            @PathVariable Long organizationId) {
        SecurityContextUtil.requireOrganizationAccess(organizationId);
        return ResponseEntity.ok(ApiResponse.success(
            moduleService.listEnabledModulesForSchool(organizationId)));
    }
}
