package com.wissenup.domain.staff.dto;
import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalDate; import java.math.BigDecimal; import java.util.List;
@Data public class StaffRequest {
 @NotBlank private String employeeCode; @NotBlank private String firstName; private String lastName; private String gender; private LocalDate dateOfBirth;
 @NotNull private LocalDate joiningDate; private String qualification; private BigDecimal experience; @NotBlank @Email private String email; @NotBlank private String phoneNumber;
 @NotNull private Long departmentId; @NotNull private Long designationId; @NotBlank private String roleName; private String password;
 private String status; private AddressData address; private List<AssignmentData> subjectAssignments; private AssignmentData classTeacherAssignment;
 @Data public static class AddressData {private String addressLine1,addressLine2,locality,city,state,country,zipCode;}
 @Data public static class AssignmentData {private Long academicYearId,classId,sectionId,subjectId;}
}
