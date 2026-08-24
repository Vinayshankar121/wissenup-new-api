package com.wissenup.domain.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolDto {
    private Long schoolId;
    private Long organizationId;
    private String name;
    private String organizationType;
    private String registrationNumber;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String website;
    private String logoPath;
    private String status;
    private Boolean isActive;
    private Boolean isTrial;
    private LocalDateTime trialEndsAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String adminEmail;
    private Long subscriptionId;
    private Long planId;
    private String planCode;
    private String planName;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer planDurationDays;
    private Integer gracePeriodDays;
    private LocalDateTime subscriptionStart;
    private LocalDateTime subscriptionEnd;
    private LocalDateTime subscriptionTrialEnd;
    private String subscriptionStatus;
    private Boolean autoRenew;
}
