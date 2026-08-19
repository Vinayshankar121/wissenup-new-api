package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.ClassSubjectDto;
import com.wissenup.domain.academic.dto.CreateClassSubjectRequest;
import com.wissenup.domain.academic.entity.ClassSubject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClassSubjectMapper {

    public ClassSubjectDto toDto(ClassSubject entity) {
        if (entity == null) {
            return null;
        }

        return ClassSubjectDto.builder()
                .classSubjectId(entity.getClassSubjectId())
                .organizationId(entity.getOrganizationId())
                .classId(entity.getClassId())
                .subjectId(entity.getSubjectId())
                .isCompulsory(entity.getIsCompulsory())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ClassSubject toEntity(CreateClassSubjectRequest request, Long organizationId) {
        if (request == null) {
            return null;
        }

        return ClassSubject.builder()
                .organizationId(organizationId)
                .classId(request.getClassId())
                .subjectId(request.getSubjectId())
                .isCompulsory(request.getIsCompulsory() != null ? request.getIsCompulsory() : false)
                .status(ClassSubject.ClassSubjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
