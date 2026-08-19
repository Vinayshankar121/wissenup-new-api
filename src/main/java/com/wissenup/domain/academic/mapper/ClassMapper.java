package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.ClassDto;
import com.wissenup.domain.academic.dto.CreateClassRequest;
import com.wissenup.domain.academic.dto.UpdateClassRequest;
import com.wissenup.domain.academic.entity.Class;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClassMapper {

    public ClassDto toDto(Class entity) {
        if (entity == null) {
            return null;
        }

        return ClassDto.builder()
                .classId(entity.getClassId())
                .organizationId(entity.getOrganizationId())
                .academicYearId(entity.getAcademicYearId())
                .name(entity.getName())
                .code(entity.getCode())
                .level(entity.getLevel())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public Class toEntity(CreateClassRequest request, Long organizationId, Long academicYearId) {
        if (request == null) {
            return null;
        }

        return Class.builder()
                .organizationId(organizationId)
                .academicYearId(academicYearId)
                .name(request.getName())
                .code(request.getCode())
                .level(request.getLevel())
                .status(Class.ClassStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(UpdateClassRequest request, Class entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setLevel(request.getLevel());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
