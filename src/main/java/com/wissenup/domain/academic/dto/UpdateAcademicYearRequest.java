package com.wissenup.domain.academic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAcademicYearRequest {

    @NotBlank(message = "Academic year name is required")
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;
}
