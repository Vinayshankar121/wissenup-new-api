package com.wissenup.domain.academic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassTeacherAssignmentRequest {

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Boolean isClassTeacher;
}
