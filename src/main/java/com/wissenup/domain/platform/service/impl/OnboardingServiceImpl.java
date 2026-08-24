package com.wissenup.domain.platform.service.impl;

import com.wissenup.domain.academic.entity.AcademicYear;
import com.wissenup.domain.academic.repository.AcademicYearRepository;
import com.wissenup.domain.identity.entity.Role;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.platform.dto.OnboardingRequest;
import com.wissenup.domain.platform.dto.OnboardingResponse;
import com.wissenup.domain.platform.entity.Module;
import com.wissenup.domain.platform.entity.School;
import com.wissenup.domain.platform.entity.SchoolModule;
import com.wissenup.domain.platform.entity.SchoolSettings;
import com.wissenup.domain.platform.entity.SchoolStatus;
import com.wissenup.domain.platform.entity.SchoolSubscription;
import com.wissenup.domain.platform.entity.SubscriptionPlan;
import com.wissenup.domain.platform.exception.OnboardingException;
import com.wissenup.domain.platform.repository.ModuleRepository;
import com.wissenup.domain.platform.repository.PlanModuleRepository;
import com.wissenup.domain.platform.repository.SchoolModuleRepository;
import com.wissenup.domain.platform.repository.SchoolRepository;
import com.wissenup.domain.platform.repository.SchoolSettingsRepository;
import com.wissenup.domain.platform.repository.SchoolSubscriptionRepository;
import com.wissenup.domain.platform.repository.SubscriptionPlanRepository;
import com.wissenup.domain.platform.service.OnboardingService;
import com.wissenup.domain.platform.service.OnboardingEmailService;
import com.wissenup.domain.platform.service.PlatformAuditService;
import com.wissenup.domain.shared.entity.Organization;
import com.wissenup.domain.shared.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingServiceImpl implements OnboardingService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] PASSWORD_ALPHABET =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();

    private final OrganizationRepository organizationRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SchoolSubscriptionRepository schoolSubscriptionRepository;
    private final ModuleRepository moduleRepository;
    private final PlanModuleRepository planModuleRepository;
    private final SchoolModuleRepository schoolModuleRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final PlatformAuditService auditService;
    private final OnboardingEmailService onboardingEmailService;
    private final BCryptPasswordEncoder bcryptEncoder;

    @Override
    @Transactional
    public OnboardingResponse onboardSchool(OnboardingRequest request, Long superAdminId) {
        try {
            if (schoolRepository.findByEmail(request.getSchool().getEmail()).isPresent()) {
                throw new OnboardingException(
                    "A school with email " + request.getSchool().getEmail() + " is already onboarded");
            }

            // STEP 1: Create Organization
            Organization organization = Organization.builder()
                .name(generateOrganizationName(request.getSchool().getEmail()))
                .status("ACTIVE")
                .createdBy(superAdminId)
                .build();
            organization = organizationRepository.save(organization);

            // STEP 2: Create School
            School school = School.builder()
                .organizationId(organization.getOrganizationId())
                .name(request.getSchool().getName())
                .organization_type(request.getSchool().getOrganizationType() == null
                    ? "SCHOOL" : request.getSchool().getOrganizationType())
                .registration_number(request.getSchool().getRegistrationNumber())
                .email(request.getSchool().getEmail())
                .phone(request.getSchool().getPhone())
                .address(request.getSchool().getAddress())
                .city(request.getSchool().getCity())
                .state(request.getSchool().getState())
                .zipCode(request.getSchool().getZipCode())
                .country(request.getSchool().getCountry())
                .website(request.getSchool().getWebsite())
                .status(SchoolStatus.ONBOARDING)
                .is_active(false)
                .is_trial(false)
                .createdBy(superAdminId)
                .build();
            school = schoolRepository.save(school);

            // STEP 3: Create Admin User
            String temporaryPassword = request.getAdminUser().getPassword();
            if (temporaryPassword == null || temporaryPassword.isBlank()) {
                temporaryPassword = generateTemporaryPassword();
            }
            User adminUser = User.builder()
                .organizationId(school.getOrganizationId())
                .email(request.getAdminUser().getEmail())
                .phoneNumber(request.getAdminUser().getPhone())
                .password(bcryptEncoder.encode(temporaryPassword))
                .status("ACTIVE")
                .createdBy(superAdminId)
                .build();
            adminUser = userRepository.save(adminUser);

            // STEP 4: Assign SCHOOL_ADMIN role
            Role schoolAdminRole = roleRepository.findByCode("SCHOOL_ADMIN")
                .orElseThrow(() -> new OnboardingException("SCHOOL_ADMIN role not found"));

            UserRole userRole = UserRole.builder()
                .userId(adminUser.getUserId())
                .roleId(schoolAdminRole.getRoleId())
                .status("ACTIVE")
                .createdBy(superAdminId)
                .build();
            userRoleRepository.save(userRole);

            // STEP 5: Create Academic Year (current year)
            AcademicYear academicYear = AcademicYear.builder()
                .organizationId(school.getOrganizationId())
                .name(request.getAcademicYear().getName())
                .startDate(request.getAcademicYear().getStartDate())
                .endDate(request.getAcademicYear().getEndDate())
                .isActive(true)
                .status(AcademicYear.AcademicYearStatus.ACTIVE)
                .createdBy(superAdminId)
                .build();
            academicYear = academicYearRepository.save(academicYear);

            // Classes and sections are created later by the organization admin.

            // STEP 7: Create Subscription
            String planCode = request.getSubscriptionPlan().getCode();
            SubscriptionPlan plan = subscriptionPlanRepository.findByCodeAndIsActive(planCode, true)
                .orElseThrow(() -> new OnboardingException("Subscription plan not found: " + planCode));

            SchoolSubscription subscription = SchoolSubscription.builder()
                .organization_id(school.getOrganizationId())
                .plan_id(plan.getPlanId())
                .started_at(LocalDateTime.now())
                .ends_at(LocalDateTime.now().plusDays(plan.getDuration_days()))
                .is_active(true)
                .build();

            // Calculate trial end date if plan is trial
            if (plan.isTrial() && plan.getTrial_days() != null) {
                LocalDateTime trialEndsAt = LocalDateTime.now().plusDays(plan.getTrial_days());
                subscription.setTrial_ends_at(trialEndsAt);
                school.setIs_trial(true);
                school.setTrial_ends_at(trialEndsAt);
            }

            subscription = schoolSubscriptionRepository.save(subscription);

            // STEP 8: Enable Modules Per Plan
            List<Long> planModuleIds = planModuleRepository.findAllByPlanId(plan.getPlanId()).stream()
                .map(planModule -> planModule.getModuleId())
                .toList();
            List<Module> planModules = moduleRepository.findAllById(planModuleIds).stream()
                .filter(module -> Boolean.TRUE.equals(module.getIs_active()))
                .toList();
            if (planModules.isEmpty()) {
                throw new OnboardingException("No active modules are configured for onboarding");
            }
            for (Module module : planModules) {
                SchoolModule schoolModule = SchoolModule.builder()
                    .organization_id(school.getOrganizationId())
                    .module_id(module.getModule_id())
                    .is_enabled(true)
                    .enabled_at(LocalDateTime.now())
                    .build();
                schoolModuleRepository.save(schoolModule);
            }

            // STEP 9: Create School Settings
            SchoolSettings settings = SchoolSettings.builder()
                .organization_id(school.getOrganizationId())
                .school_name(school.getName())
                .address(school.getAddress())
                .phone(school.getPhone())
                .email(school.getEmail())
                .website(school.getWebsite())
                .timezone(request.getSchoolSettings().getTimezone() != null
                    ? request.getSchoolSettings().getTimezone() : "UTC")
                .currency(request.getSchoolSettings().getCurrency() != null
                    ? request.getSchoolSettings().getCurrency() : "USD")
                .date_format(request.getSchoolSettings().getDateFormat() != null
                    ? request.getSchoolSettings().getDateFormat() : "DD/MM/YYYY")
                .notification_email_enabled(true)
                .notification_sms_enabled(false)
                .notification_whatsapp_enabled(false)
                .academic_session_start_month(request.getSchoolSettings().getAcademicSessionStartMonth() != null
                    ? request.getSchoolSettings().getAcademicSessionStartMonth() : 6)
                .academic_session_end_month(request.getSchoolSettings().getAcademicSessionEndMonth() != null
                    ? request.getSchoolSettings().getAcademicSessionEndMonth() : 5)
                .build();
            schoolSettingsRepository.save(settings);

            // STEP 10: Activate School
            school.setStatus(SchoolStatus.ACTIVE);
            school.setIs_active(true);
            school.setUpdated_by(superAdminId);
            school = schoolRepository.save(school);

            // STEP 11: Create Audit Log
            auditService.log("SCHOOL_ONBOARDED", "SCHOOL", school.getSchoolId(),
                school.getOrganizationId(),
                Map.of(
                    "schoolName", school.getName(),
                    "plan", planCode,
                    "adminEmail", adminUser.getEmail(),
                    "modulesEnabled", planModules.size(),
                    "academicYearId", academicYear.getAcademicYearId(),
                    "classesCreated", 0,
                    "sectionsCreated", 0
                ),
                superAdminId);

            onboardingEmailService.sendCredentials(
                adminUser.getEmail(), school.getName(), temporaryPassword);

            // Return success
            return OnboardingResponse.builder()
                .schoolId(school.getSchoolId())
                .organizationId(school.getOrganizationId())
                .schoolName(school.getName())
                .adminUserEmail(adminUser.getEmail())
                .subscriptionPlan(plan.getCode())
                .enabledModules(planModules.stream().map(Module::getCode).collect(Collectors.toList()))
                .status("ACTIVE")
                .message("School onboarded successfully")
                .completedAt(LocalDateTime.now())
                .build();

        } catch (OnboardingException e) {
            // Explicit onboarding exception - log and rethrow
            auditService.log("SCHOOL_ONBOARDING_FAILED", "SCHOOL", null, null,
                Map.of("reason", e.getMessage(), "type", "OnboardingException"),
                superAdminId);
            throw e;
        } catch (Exception e) {
            // Unexpected exception - log and wrap in OnboardingException
            // Transaction will automatically rollback - NO partial data created
            auditService.log("SCHOOL_ONBOARDING_FAILED", "SCHOOL", null, null,
                Map.of("reason", e.getMessage(), "type", e.getClass().getSimpleName()),
                superAdminId);
            throw new OnboardingException("School onboarding failed: " + e.getMessage(), e);
        }
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            password.append(PASSWORD_ALPHABET[SECURE_RANDOM.nextInt(PASSWORD_ALPHABET.length)]);
        }
        return password.toString();
    }

    @Override
    public OnboardingResponse getOnboardingStatus(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new OnboardingException("School not found"));
        SchoolSubscription subscription = schoolSubscriptionRepository.findByOrganizationId(school.getOrganizationId())
            .orElseThrow(() -> new OnboardingException("Subscription not found"));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlan_id())
            .orElseThrow(() -> new OnboardingException("Plan not found"));

        List<String> enabledModules = schoolModuleRepository.findByOrganizationIdAndIsEnabled(school.getOrganizationId(), true)
            .stream()
            .map(SchoolModule::getModule_id)
            .map(moduleId -> moduleRepository.findById(moduleId).map(Module::getCode).orElse("UNKNOWN"))
            .collect(Collectors.toList());

        return OnboardingResponse.builder()
            .schoolId(school.getSchoolId())
            .organizationId(school.getOrganizationId())
            .schoolName(school.getName())
            .adminUserEmail("") // Would need to fetch from user table
            .subscriptionPlan(plan.getCode())
            .enabledModules(enabledModules)
            .status(school.getStatus() != null ? school.getStatus().name() : null)
            .message("School status retrieved")
            .build();
    }

    @Override
    public boolean canOnboard(OnboardingRequest request) {
        // Check if school email already exists
        if (schoolRepository.findByEmail(request.getSchool().getEmail()).isPresent()) {
            return false;
        }

        // Check if subscription plan exists
        if (subscriptionPlanRepository.findByCode(request.getSubscriptionPlan().getCode()).isEmpty()) {
            return false;
        }

        // Check if SCHOOL_ADMIN role exists
        if (roleRepository.findByCode("SCHOOL_ADMIN").isEmpty()) {
            return false;
        }

        return true;
    }

    private String generateOrganizationName(String email) {
        // Generate organization name from email domain
        // e.g., school@example.com -> example-school-1234
        String domain = email.substring(email.indexOf('@') + 1);
        String baseName = domain.replace('.', '-') + "-" + System.currentTimeMillis();
        return baseName.substring(0, Math.min(baseName.length(), 255));
    }
}
