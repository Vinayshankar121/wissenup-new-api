package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.SubscriptionPlanDto;

import java.util.List;

/**
 * Subscription plan service for retrieving and validating plans.
 */
public interface SubscriptionService {

    List<SubscriptionPlanDto> listPlans();

    SubscriptionPlanDto getPlan(Long planId);

    SubscriptionPlanDto createPlan(SubscriptionPlanDto request);

    SubscriptionPlanDto updatePlan(Long planId, SubscriptionPlanDto request);

    void deletePlan(Long planId);

    /**
     * Get subscription plan by code.
     *
     * @param code - Plan code (FREE, TRIAL, BASIC, STANDARD, PREMIUM)
     * @return - Plan DTO
     */
    SubscriptionPlanDto getPlanByCode(String code);

    /**
     * Check if school can add more students (against plan limit).
     *
     * @param organizationId - School organization ID
     * @param studentCountIncrease - Number of students being added
     * @return - true if within limit, false otherwise
     */
    boolean canAddStudents(Long organizationId, int studentCountIncrease);

    /**
     * Check if school can add more staff (against plan limit).
     *
     * @param organizationId - School organization ID
     * @param staffCountIncrease - Number of staff being added
     * @return - true if within limit, false otherwise
     */
    boolean canAddStaff(Long organizationId, int staffCountIncrease);

    /**
     * Check if school is on a trial plan and if trial is expiring soon.
     *
     * @param organizationId - School organization ID
     * @return - true if trial expires within 7 days
     */
    boolean isTrialExpiringSoon(Long organizationId);
}
