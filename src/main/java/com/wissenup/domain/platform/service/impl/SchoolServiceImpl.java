package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.platform.dto.SchoolDto;
import com.wissenup.domain.platform.entity.School;
import com.wissenup.domain.platform.entity.SchoolStatus;
import com.wissenup.domain.platform.entity.SchoolSubscription;
import com.wissenup.domain.platform.entity.SubscriptionPlan;
import com.wissenup.domain.platform.repository.SchoolRepository;
import com.wissenup.domain.platform.repository.SchoolSubscriptionRepository;
import com.wissenup.domain.platform.repository.SubscriptionPlanRepository;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.platform.service.PlatformAuditService;
import com.wissenup.domain.platform.service.SchoolService;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolSubscriptionRepository schoolSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final PlatformAuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolDto> listSchools(Pageable pageable) {
        Page<School> schools = schoolRepository.findAll(pageable);
        return convertToDto(schools);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolDto getSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));
        return convertToDto(school);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolDto getSchoolByOrganizationId(Long organizationId) {
        School school = schoolRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "School not found for organization: " + organizationId));
        return convertToDto(school);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolDto getSchoolByEmail(String email) {
        School school = schoolRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("School not found with email: " + email));
        return convertToDto(school);
    }

    @Override
    public void activateSchool(Long schoolId, Long superAdminId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        school.setStatus(SchoolStatus.ACTIVE);
        school.setIs_active(true);
        school.setUpdated_by(superAdminId);
        schoolRepository.save(school);
    }

    @Override
    public void deactivateSchool(Long schoolId, Long superAdminId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        school.setStatus(SchoolStatus.INACTIVE);
        school.setIs_active(false);
        school.setUpdated_by(superAdminId);
        schoolRepository.save(school);
    }

    @Override
    public void updateStatus(Long schoolId, String status, Long superAdminId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        String oldStatus = school.getStatus().name();
        school.setStatus(SchoolStatus.valueOf(status));
        school.setUpdated_by(superAdminId);
        schoolRepository.save(school);

        auditService.log("SCHOOL_STATUS_CHANGED", "SCHOOL", schoolId, school.getOrganizationId(),
            Map.of("oldStatus", oldStatus, "newStatus", status), superAdminId);
    }

    private Page<SchoolDto> convertToDto(Page<School> schools) {
        return new PageImpl<>(
            schools.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()),
            schools.getPageable(),
            schools.getTotalElements()
        );
    }

    private SchoolDto convertToDto(School school) {
        SchoolDto dto = SchoolDto.builder()
            .schoolId(school.getSchoolId())
            .organizationId(school.getOrganizationId())
            .name(school.getName())
            .email(school.getEmail())
            .phone(school.getPhone())
            .address(school.getAddress())
            .city(school.getCity())
            .state(school.getState())
            .zipCode(school.getZipCode())
            .country(school.getCountry())
            .website(school.getWebsite())
            .logoPath(school.getLogo_path())
            .status(school.getStatus() != null ? school.getStatus().name() : null)
            .isActive(school.getIs_active())
            .isTrial(school.getIs_trial())
            .trialEndsAt(school.getTrial_ends_at())
            .createdAt(school.getCreated_at())
            .updatedAt(school.getUpdated_at())
            .build();

        userRepository.findAllByOrganizationId(school.getOrganizationId()).stream()
            .findFirst()
            .ifPresent(user -> dto.setAdminEmail(user.getEmail()));

        schoolSubscriptionRepository.findByOrganizationId(school.getOrganizationId())
            .ifPresent(subscription -> enrichSubscription(dto, subscription));
        return dto;
    }

    private void enrichSubscription(SchoolDto dto, SchoolSubscription subscription) {
        dto.setSubscriptionId(subscription.getSubscription_id());
        dto.setPlanId(subscription.getPlan_id());
        dto.setSubscriptionStart(subscription.getStarted_at());
        dto.setSubscriptionEnd(subscription.getEnds_at());
        dto.setSubscriptionTrialEnd(subscription.getTrial_ends_at());
        dto.setAutoRenew(false);
        dto.setSubscriptionStatus(Boolean.TRUE.equals(subscription.getIs_active()) && !subscription.isExpired()
            ? "ACTIVE" : "INACTIVE");

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlan_id()).orElse(null);
        if (plan != null) {
            dto.setPlanCode(plan.getCode());
            dto.setPlanName(plan.getName());
            dto.setMonthlyPrice(plan.getPrice_per_month());
        }
    }
}
