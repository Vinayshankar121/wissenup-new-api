package com.wissenup.domain.staff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_class_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffClassAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_class_assignment_id") private Long staffClassAssignmentId;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Column(name = "staff_id", nullable = false) private Long staffId;
    @Column(name = "academic_year_id", nullable = false) private Long academicYearId;
    @Column(name = "class_id", nullable = false) private Long classId;
    @Column(name = "section_id", nullable = false) private Long sectionId;
    @Column(nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @PrePersist void prePersist() { if (status == null) status = "ACTIVE"; if (createdAt == null) createdAt = LocalDateTime.now(); }
}
