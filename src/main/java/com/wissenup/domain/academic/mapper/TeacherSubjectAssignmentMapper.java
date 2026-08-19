package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.CreateTeacherSubjectAssignmentRequest;
import com.wissenup.domain.academic.dto.TeacherSubjectAssignmentDto;
import com.wissenup.domain.academic.entity.TeacherSubjectAssignment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TeacherSubjectAssignmentMapper {

    public TeacherSubjectAssignmentDto toDto(TeacherSubjectAssignment entity) {
        if (entity == null) {
            return null;
        }

        return TeacherSubjectAssignmentDto.builder()
                .teacherSubjectAssignmentId(entity.getTeacherSubjectAssignmentId())
                .organizationId(entity.getOrganizationId())
                .staffId(entity.getStaffId())
                .subjectId(entity.getSubjectId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public TeacherSubjectAssignment toEntity(CreateTeacherSubjectAssignmentRequest request, Long organizationId) {
        if (request == null) {
            return null;
        }

        return TeacherSubjectAssignment.builder()
                .organizationId(organizationId)
                .staffId(request.getStaffId())
                .subjectId(request.getSubjectId())
            .status(TeacherSubjectAssignment.TeacherSubjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
