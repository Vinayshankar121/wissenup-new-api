package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.OnboardingRequest;
import com.wissenup.domain.platform.dto.OnboardingResponse;

/**
 * School onboarding service - TRANSACTIONAL
 *
 * This service handles the complete school setup workflow:
 * 1. Create School
 * 2. Create Admin User
 * 3. Create Academic Year
 * 4. Create Classes and Sections
 * 5. Create Subscription
 * 6. Enable Modules
 * 7. Create School Settings
 * 8. Activate School
 *
 * CRITICAL: All steps are in one transaction.
 * If ANY step fails, the entire transaction rolls back.
 * NO partially configured schools are created.
 */
public interface OnboardingService {

    /**
     * Onboard a new school - FULLY TRANSACTIONAL
     *
     * @param request - Complete onboarding data
     * @param superAdminId - Super admin performing onboarding
     * @return - Complete onboarding response with school details
     * @throws OnboardingException - If any step fails (entire transaction rolls back)
     */
    OnboardingResponse onboardSchool(OnboardingRequest request, Long superAdminId);

    /**
     * Get onboarding status for a school
     *
     * @param schoolId - School ID
     * @return - Current onboarding status
     */
    OnboardingResponse getOnboardingStatus(Long schoolId);

    /**
     * Check if onboarding can proceed for a school
     *
     * @param request - Onboarding data to validate
     * @return - true if onboarding can proceed, false otherwise
     */
    boolean canOnboard(OnboardingRequest request);
}
