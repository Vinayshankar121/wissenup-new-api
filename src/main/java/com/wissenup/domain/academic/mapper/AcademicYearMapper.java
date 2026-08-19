package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import com.wissenup.domain.academic.entity.AcademicYear;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AcademicYearMapper {

    public AcademicYearDto toDto(AcademicYear entity) {
        if (entity == null) {
            return null;
        }

        return AcademicYearDto.builder()
                .academicYearId(entity.getAcademicYearId())
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public AcademicYear toEntity(CreateAcademicYearRequest request, Long organizationId) {
        if (request == null) {
            return null;
        }

        AcademicYear entity = AcademicYear.builder()
                .organizationId(organizationId)
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .status(AcademicYear.AcademicYearStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return entity;
    }

    public void updateEntity(UpdateAcademicYearRequest request, AcademicYear entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
