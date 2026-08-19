package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.platform.dto.UsageMetricDto;
import com.wissenup.domain.platform.entity.UsageMetric;
import com.wissenup.domain.platform.repository.UsageMetricRepository;
import com.wissenup.domain.platform.service.UsageService;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UsageServiceImpl implements UsageService {

    private final UsageMetricRepository usageMetricRepository;

    @Override
    public void recordDailyMetrics(Long organizationId, int studentCount, int staffCount, int userCount,
                                   int storageUsedMb, long apiCalls) {
        LocalDate today = LocalDate.now();

        // Try to find existing metric for today
        UsageMetric metric = usageMetricRepository
            .findLatestByOrganizationId(organizationId)
            .filter(m -> m.getDate().equals(today))
            .orElse(null);

        if (metric == null) {
            // Create new metric for today
            metric = UsageMetric.builder()
                .organizationId(organizationId)
                .date(today)
                .studentCount(studentCount)
                .staffCount(staffCount)
                .userCount(userCount)
                .storageUsedMb(storageUsedMb)
                .apiCalls(apiCalls)
                .build();
        } else {
            // Update existing metric for today
            metric.setStudentCount(studentCount);
            metric.setStaffCount(staffCount);
            metric.setUserCount(userCount);
            metric.setStorageUsedMb(storageUsedMb);
            metric.setApiCalls(apiCalls);
        }

        usageMetricRepository.save(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public UsageMetricDto getLatestMetrics(Long organizationId) {
        UsageMetric metric = usageMetricRepository.findLatestByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("No usage metrics found for organization: " + organizationId));
        return convertToDto(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public UsageMetricDto getTotalUsageAcrossAllSchools() {
        long totalStudents = usageMetricRepository.sumStudentCount();
        long totalStaff = usageMetricRepository.sumStaffCount();
        long totalStorageUsed = usageMetricRepository.sumStorageUsed();

        return UsageMetricDto.builder()
            .date(LocalDate.now())
            .studentCount((int) totalStudents)
            .staffCount((int) totalStaff)
            .storageUsedMb((int) totalStorageUsed)
            .build();
    }

    private UsageMetricDto convertToDto(UsageMetric metric) {
        return UsageMetricDto.builder()
            .metricId(metric.getMetricId())
            .organizationId(metric.getOrganizationId())
            .date(metric.getDate())
            .studentCount(metric.getStudentCount())
            .staffCount(metric.getStaffCount())
            .userCount(metric.getUserCount())
            .storageUsedMb(metric.getStorageUsedMb())
            .apiCalls(metric.getApiCalls())
            .build();
    }
}
