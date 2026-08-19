package com.wissenup.domain.platform.service;

import com.wissenup.domain.platform.dto.UsageMetricDto;

/**
 * Usage tracking service for monitoring school resource consumption.
 */
public interface UsageService {

    /**
     * Record daily usage metrics for a school.
     *
     * @param organizationId - School organization ID
     * @param studentCount - Current student count
     * @param staffCount - Current staff count
     * @param userCount - Current user count
     * @param storageUsedMb - Storage used in MB
     * @param apiCalls - API calls made
     */
    void recordDailyMetrics(Long organizationId, int studentCount, int staffCount, int userCount,
                           int storageUsedMb, long apiCalls);

    /**
     * Get latest usage metrics for a school.
     *
     * @param organizationId - School organization ID
     * @return - Latest usage metric DTO
     */
    UsageMetricDto getLatestMetrics(Long organizationId);

    /**
     * Get total usage across all schools.
     *
     * @return - Total usage metric DTO
     */
    UsageMetricDto getTotalUsageAcrossAllSchools();
}
