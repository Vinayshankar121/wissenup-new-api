package com.wissenup.domain.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingRequest {

    @NotNull(message = "School data required")
    private SchoolData school;

    @NotNull(message = "Admin user data required")
    private AdminUserData adminUser;

    @NotNull(message = "Academic year data required")
    private AcademicYearData academicYear;

    @NotNull(message = "Subscription plan required")
    private SubscriptionData subscriptionPlan;

    @NotNull(message = "School settings required")
    private SchoolSettingsData schoolSettings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchoolData {
        @NotBlank(message = "School name required")
        private String name;

        @Email(message = "Invalid school email")
        @NotBlank(message = "School email required")
        private String email;

        private String phone;
        private String address;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String website;
        private String organizationType;
        private String registrationNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminUserData {
        @NotBlank(message = "First name required")
        private String firstName;

        private String lastName;

        @Email(message = "Invalid email")
        @NotBlank(message = "Email required")
        private String email;

        private String phone;

        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcademicYearData {
        @NotBlank(message = "Academic year name required")
        private String name;

        @NotNull(message = "Start date required")
        private LocalDate startDate;

        @NotNull(message = "End date required")
        private LocalDate endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionData {
        @NotBlank(message = "Subscription plan code required")
        private String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchoolSettingsData {
        private String timezone;
        private String currency;
        private String dateFormat;
        private Integer academicSessionStartMonth;
        private Integer academicSessionEndMonth;
    }
}
