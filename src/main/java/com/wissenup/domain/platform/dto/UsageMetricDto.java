package com.wissenup.domain.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageMetricDto {
    private Long metricId;
    private Long organizationId;
    private LocalDate date;
    private Integer studentCount;
    private Integer staffCount;
    private Integer userCount;
    private Integer storageUsedMb;
    private Long apiCalls;
}
