package com.wissenup.domain.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="student_enrollments", uniqueConstraints =
    @UniqueConstraint(name="uq_student_enrollment_year", columnNames={"student_id","academic_year_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentEnrollment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long enrollmentId;
    @Column(name="organization_id", nullable=false) private Long organizationId;
    @Column(name="student_id", nullable=false) private Long studentId;
    @Column(name="academic_year_id", nullable=false) private Long academicYearId;
    @Column(name="class_id", nullable=false) private Long classId;
    @Column(name="section_id", nullable=false) private Long sectionId;
    @Column(name="roll_no", length=30) private String rollNo;
    @Column(name="enrollment_date", nullable=false) private LocalDate enrollmentDate;
    @Column(name="promotion_status", length=30) private String promotionStatus;
    @Column(nullable=false, length=20) @Builder.Default private String status="ACTIVE";
}
