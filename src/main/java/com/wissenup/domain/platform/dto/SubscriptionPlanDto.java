package com.wissenup.domain.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDto {
    private Long planId;
    private String code;
    private String name;
    private String description;
    private Integer maxStudents;
    private Integer maxStaff;
    private Integer maxUsers;
    private Integer storageGb;
    private BigDecimal pricePerMonth;
    private BigDecimal yearlyPrice;
    private Integer durationDays;
    private Integer gracePeriodDays;
    private Integer trialDays;
    private Boolean isActive;
    private List<Long> moduleIds;
}
