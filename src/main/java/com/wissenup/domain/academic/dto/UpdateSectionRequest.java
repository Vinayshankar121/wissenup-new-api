package com.wissenup.domain.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSectionRequest {

    @NotBlank(message = "Section name is required")
    private String name;

    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;
}
