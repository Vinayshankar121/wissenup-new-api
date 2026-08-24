package com.wissenup.domain.student.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentRegistrationRequest {
    @NotBlank private String parentPhoneNumber;
    @NotBlank private String relationship;
    @NotBlank private String admissionNo;
    @NotBlank private String firstName;
    private String lastName;
    @NotBlank private String gender;
    @NotNull private LocalDate dateOfBirth;
    private String password;
    private String bloodGroup;
    @NotNull private LocalDate admissionDate;
    @Valid @NotNull private ParentData parent;
    @Valid @NotNull private EnrollmentData enrollment;

    @Data public static class ParentData {
        @NotBlank private String firstName;
        private String lastName;
        @NotBlank private String phoneNumber;
        @Email private String email;
        private String password;
        private String occupation;
        private String status;
        private Long addressId;
        @Valid private AddressData address;
    }
    @Data public static class AddressData {
        private String addressLine1; private String addressLine2; private String locality;
        private String city; private String state; private String country; private String zipCode; private String status;
    }
    @Data public static class EnrollmentData {
        @NotNull @Positive private Long academicYearId;
        @NotNull @Positive private Long classId;
        @NotNull @Positive private Long sectionId;
        private String rollNo;
        @NotNull private LocalDate enrollmentDate;
        private String promotionStatus;
        private String status;
    }
}
