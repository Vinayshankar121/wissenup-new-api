package com.wissenup.domain.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassTeacherAssignmentDto {

    private Long classTeacherAssignmentId;
    private Long organizationId;
    private Long classId;
    private Long staffId;
    private Long subjectId;
    private Boolean isClassTeacher;
    private String status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
