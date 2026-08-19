package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.CreateSectionRequest;
import com.wissenup.domain.academic.dto.SectionDto;
import com.wissenup.domain.academic.dto.UpdateSectionRequest;
import com.wissenup.domain.academic.entity.Section;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SectionMapper {

    public SectionDto toDto(Section entity) {
        if (entity == null) {
            return null;
        }

        return SectionDto.builder()
                .sectionId(entity.getSectionId())
                .organizationId(entity.getOrganizationId())
                .classId(entity.getClassId())
                .name(entity.getName())
                .classTeacherId(entity.getClassTeacherId())
                .capacity(entity.getCapacity())
                .currentStrength(entity.getCurrentStrength())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public Section toEntity(CreateSectionRequest request, Long organizationId, Long classId) {
        if (request == null) {
            return null;
        }

        return Section.builder()
                .organizationId(organizationId)
                .classId(classId)
                .name(request.getName())
                .capacity(request.getCapacity())
                .currentStrength(0)
                .status(Section.SectionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(UpdateSectionRequest request, Section entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setCapacity(request.getCapacity());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
