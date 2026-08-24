package com.wissenup.domain.staff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_subject_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffSubjectAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_subject_assignment_id") private Long staffSubjectAssignmentId;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Column(name = "staff_id", nullable = false) private Long staffId;
    @Column(name = "academic_year_id", nullable = false) private Long academicYearId;
    @Column(name = "class_id", nullable = false) private Long classId;
    @Column(name = "section_id", nullable = false) private Long sectionId;
    @Column(name = "subject_id", nullable = false) private Long subjectId;
    @Column(nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @PrePersist void prePersist() { if (status == null) status = "ACTIVE"; if (createdAt == null) createdAt = LocalDateTime.now(); }
}
