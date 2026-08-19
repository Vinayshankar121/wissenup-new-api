package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.platform.dto.ModuleDto;
import com.wissenup.domain.platform.entity.Module;
import com.wissenup.domain.platform.entity.SchoolModule;
import com.wissenup.domain.platform.repository.ModuleRepository;
import com.wissenup.domain.platform.repository.SchoolModuleRepository;
import com.wissenup.domain.platform.service.ModuleService;
import com.wissenup.domain.platform.service.PlatformAuditService;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final SchoolModuleRepository schoolModuleRepository;
    private final PlatformAuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> listAllModules() {
        return moduleRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> listEnabledModulesForSchool(Long organizationId) {
        return schoolModuleRepository.findByOrganizationIdAndIsEnabled(organizationId, true).stream()
            .map(schoolModule -> {
                Optional<Module> module = moduleRepository.findById(schoolModule.getModule_id());
                if (module.isPresent()) {
                    return convertToDto(module.get(), true);
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isModuleEnabledForSchool(Long organizationId, String moduleCode) {
        Optional<Module> module = moduleRepository.findByCode(moduleCode);
        if (module.isEmpty()) {
            return false;
        }

        return schoolModuleRepository.existsByOrganizationIdAndModuleIdAndIsEnabled(
            organizationId, module.get().getModule_id(), true);
    }

    @Override
    public void enableModuleForSchool(Long organizationId, String moduleCode, Long superAdminId) {
        Module module = moduleRepository.findByCode(moduleCode)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleCode));

        Optional<SchoolModule> existing = schoolModuleRepository.findByOrganizationIdAndModuleId(organizationId, module.getModule_id());

        SchoolModule schoolModule;
        if (existing.isPresent()) {
            schoolModule = existing.get();
            schoolModule.setIs_enabled(true);
            schoolModule.setEnabled_at(LocalDateTime.now());
            schoolModuleRepository.save(schoolModule);
        } else {
            schoolModule = SchoolModule.builder()
                .organization_id(organizationId)
                .module_id(module.getModule_id())
                .is_enabled(true)
                .enabled_at(LocalDateTime.now())
                .build();
            schoolModuleRepository.save(schoolModule);
        }

        auditService.log("MODULE_ENABLED", "MODULE", module.getModule_id(), organizationId,
            Map.of("moduleCode", moduleCode), superAdminId);
    }

    @Override
    public void disableModuleForSchool(Long organizationId, String moduleCode, Long superAdminId) {
        Module module = moduleRepository.findByCode(moduleCode)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleCode));

        SchoolModule schoolModule = schoolModuleRepository.findByOrganizationIdAndModuleId(organizationId, module.getModule_id())
            .orElseThrow(() -> new ResourceNotFoundException("Module not enabled for this school"));

        schoolModule.setIs_enabled(false);
        schoolModule.setDisabled_at(LocalDateTime.now());
        schoolModuleRepository.save(schoolModule);

        auditService.log("MODULE_DISABLED", "MODULE", module.getModule_id(), organizationId,
            Map.of("moduleCode", moduleCode), superAdminId);
    }

    private ModuleDto convertToDto(Module module) {
        return convertToDto(module, false);
    }

    private ModuleDto convertToDto(Module module, boolean isEnabled) {
        return ModuleDto.builder()
            .moduleId(module.getModule_id())
            .code(module.getCode())
            .name(module.getName())
            .description(module.getDescription())
            .icon(module.getIcon())
            .displayOrder(module.getDisplay_order())
            .isActive(module.getIs_active())
            .isEnabledForSchool(isEnabled)
            .build();
    }
}
