package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.CreateSubjectRequest;
import com.wissenup.domain.academic.dto.SubjectDto;
import com.wissenup.domain.academic.dto.UpdateSubjectRequest;
import com.wissenup.domain.academic.entity.Subject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SubjectMapper {

    public SubjectDto toDto(Subject entity) {
        if (entity == null) {
            return null;
        }

        return SubjectDto.builder()
                .subjectId(entity.getSubjectId())
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType() != null ? entity.getType().name() : null)
                .maxMarks(entity.getMaxMarks())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public Subject toEntity(CreateSubjectRequest request, Long organizationId) {
        if (request == null) {
            return null;
        }

        return Subject.builder()
                .organizationId(organizationId)
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .type(Subject.SubjectType.CORE)
                .maxMarks(100)
                .status(Subject.SubjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(UpdateSubjectRequest request, Subject entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName().trim());
        entity.setCode(request.getCode().trim().toUpperCase());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
