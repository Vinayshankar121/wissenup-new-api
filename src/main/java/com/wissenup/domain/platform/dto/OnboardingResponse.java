package com.wissenup.domain.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponse {
    private Long schoolId;
    private Long organizationId;
    private String schoolName;
    private String adminUserEmail;
    private String subscriptionPlan;
    private List<String> enabledModules;
    private String status;
    private String message;
    private LocalDateTime completedAt;
}
