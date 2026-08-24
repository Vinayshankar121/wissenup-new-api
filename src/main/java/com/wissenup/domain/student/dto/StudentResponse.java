package com.wissenup.domain.student.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
@Data @Builder
public class StudentResponse {
    private Long studentId; private Long organizationId; private String admissionNo;
    private String firstName; private String lastName; private String gender;
    private LocalDate dateOfBirth; private String bloodGroup; private LocalDate admissionDate; private String status;
    private Long academicYearId; private Long classId; private Long sectionId; private String rollNo; private LocalDate enrollmentDate;
    private Long parentId; private String parentName; private String parentMobile; private String parentEmail;
    private String parentRelationship; private String parentOccupation; private Long parentAddressId;
}
