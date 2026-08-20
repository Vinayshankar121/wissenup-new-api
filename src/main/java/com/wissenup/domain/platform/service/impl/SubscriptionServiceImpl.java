package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.platform.dto.SubscriptionPlanDto;
import com.wissenup.domain.platform.entity.SchoolSubscription;
import com.wissenup.domain.platform.entity.SubscriptionPlan;
import com.wissenup.domain.platform.entity.UsageMetric;
import com.wissenup.domain.platform.repository.SchoolSubscriptionRepository;
import com.wissenup.domain.platform.repository.SubscriptionPlanRepository;
import com.wissenup.domain.platform.repository.UsageMetricRepository;
import com.wissenup.domain.platform.service.SubscriptionService;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final UsageMetricRepository usageMetricRepository;

    @Override
    public List<SubscriptionPlanDto> listPlans() {
        return planRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public SubscriptionPlanDto getPlan(Long planId) {
        return convertToDto(findPlan(planId));
    }

    @Override
    @Transactional
    public SubscriptionPlanDto createPlan(SubscriptionPlanDto request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        applyRequest(request, plan);
        plan.setCreated_at(LocalDateTime.now());
        return convertToDto(planRepository.save(plan));
    }

    @Override
    @Transactional
    public SubscriptionPlanDto updatePlan(Long planId, SubscriptionPlanDto request) {
        SubscriptionPlan plan = findPlan(planId);
        applyRequest(request, plan);
        plan.setUpdated_at(LocalDateTime.now());
        return convertToDto(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        SubscriptionPlan plan = findPlan(planId);
        plan.setIs_active(false);
        plan.setUpdated_at(LocalDateTime.now());
        planRepository.save(plan);
    }

    @Override
    public SubscriptionPlanDto getPlanByCode(String code) {
        SubscriptionPlan plan = planRepository.findByCodeAndIsActive(code, true)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + code));
        return convertToDto(plan);
    }

    @Override
    public boolean canAddStudents(Long organizationId, int studentCountIncrease) {
        SchoolSubscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        SubscriptionPlan plan = planRepository.findById(subscription.getPlan_id())
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // Get current student count from usage metrics
        UsageMetric latestMetric = usageMetricRepository.findLatestByOrganizationId(organizationId)
            .orElse(UsageMetric.builder()
                .student_count(0)
                .build());

        int currentStudents = latestMetric.getStudent_count() != null ? latestMetric.getStudent_count() : 0;
        return (currentStudents + studentCountIncrease) <= plan.getMax_students();
    }

    @Override
    public boolean canAddStaff(Long organizationId, int staffCountIncrease) {
        SchoolSubscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        SubscriptionPlan plan = planRepository.findById(subscription.getPlan_id())
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // Get current staff count from usage metrics
        UsageMetric latestMetric = usageMetricRepository.findLatestByOrganizationId(organizationId)
            .orElse(UsageMetric.builder()
                .staff_count(0)
                .build());

        int currentStaff = latestMetric.getStaff_count() != null ? latestMetric.getStaff_count() : 0;
        return (currentStaff + staffCountIncrease) <= plan.getMax_staff();
    }

    @Override
    public boolean isTrialExpiringSoon(Long organizationId) {
        SchoolSubscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (subscription.getTrial_ends_at() == null) {
            return false; // Not a trial subscription
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysFromNow = now.plusDays(7);

        return subscription.getTrial_ends_at().isBefore(sevenDaysFromNow) &&
            subscription.getTrial_ends_at().isAfter(now);
    }

    private SubscriptionPlanDto convertToDto(SubscriptionPlan plan) {
        return SubscriptionPlanDto.builder()
            .planId(plan.getPlanId())
            .code(plan.getCode())
            .name(plan.getName())
            .description(plan.getDescription())
            .maxStudents(plan.getMax_students())
            .maxStaff(plan.getMax_staff())
            .maxUsers(plan.getMax_users())
            .storageGb(plan.getStorage_gb())
            .pricePerMonth(plan.getPrice_per_month())
            .trialDays(plan.getTrial_days())
            .isActive(plan.getIs_active())
            .build();
    }

    private SubscriptionPlan findPlan(Long planId) {
        return planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    private void applyRequest(SubscriptionPlanDto request, SubscriptionPlan plan) {
        plan.setCode(request.getCode());
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setMax_students(request.getMaxStudents());
        plan.setMax_staff(request.getMaxStaff());
        plan.setMax_users(request.getMaxUsers());
        plan.setStorage_gb(request.getStorageGb());
        plan.setPrice_per_month(request.getPricePerMonth());
        plan.setTrial_days(request.getTrialDays());
        plan.setIs_active(request.getIsActive() == null || request.getIsActive());
    }
}
