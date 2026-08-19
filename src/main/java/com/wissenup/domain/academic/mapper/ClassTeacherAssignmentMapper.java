package com.wissenup.domain.academic.mapper;

import com.wissenup.domain.academic.dto.ClassTeacherAssignmentDto;
import com.wissenup.domain.academic.dto.CreateClassTeacherAssignmentRequest;
import com.wissenup.domain.academic.entity.ClassTeacherAssignment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClassTeacherAssignmentMapper {

    public ClassTeacherAssignmentDto toDto(ClassTeacherAssignment entity) {
        if (entity == null) {
            return null;
        }

        return ClassTeacherAssignmentDto.builder()
                .classTeacherAssignmentId(entity.getClassTeacherAssignmentId())
                .organizationId(entity.getOrganizationId())
                .classId(entity.getClassId())
                .staffId(entity.getStaffId())
                .subjectId(entity.getSubjectId())
                .isClassTeacher(entity.getIsClassTeacher())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ClassTeacherAssignment toEntity(CreateClassTeacherAssignmentRequest request, Long organizationId) {
        if (request == null) {
            return null;
        }

        return ClassTeacherAssignment.builder()
                .organizationId(organizationId)
                .classId(request.getClassId())
                .staffId(request.getStaffId())
                .subjectId(request.getSubjectId())
                .isClassTeacher(request.getIsClassTeacher() != null ? request.getIsClassTeacher() : false)
                .status(ClassTeacherAssignment.ClassTeacherStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
