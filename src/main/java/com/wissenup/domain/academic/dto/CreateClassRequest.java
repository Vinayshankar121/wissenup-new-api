package com.wissenup.domain.academic.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRequest {

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotBlank(message = "Class name is required")
    private String name;

    @NotBlank(message = "Class code is required")
    private String code;

    @NotNull(message = "Level is required")
    @Min(value = 1, message = "Level must be between 1 and 12")
    @Max(value = 12, message = "Level must be between 1 and 12")
    private Integer level;
}
