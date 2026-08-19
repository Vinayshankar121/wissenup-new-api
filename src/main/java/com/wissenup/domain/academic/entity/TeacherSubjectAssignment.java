package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Teacher can teach specific subjects
 * Used to validate marks entry: teacher can only enter marks for assigned subjects
 */
@Entity
@Table(name = "teacher_subject_assignments", indexes = {
    @Index(name = "idx_tsa_organization_id", columnList = "organization_id"),
    @Index(name = "idx_tsa_staff_id", columnList = "staff_id"),
    @Index(name = "idx_tsa_subject_id", columnList = "subject_id"),
    @Index(name = "idx_tsa_unique", columnList = "organization_id, staff_id, subject_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_subject_assignment_id")
    private Long teacherSubjectAssignmentId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * Staff/Teacher ID
     */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    /**
     * Subject ID that teacher is qualified to teach
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /**
     * Status: ACTIVE, INACTIVE
     * Inactive when teacher is no longer qualified or has left
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TeacherSubjectStatus status = TeacherSubjectStatus.ACTIVE;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TeacherSubjectStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TeacherSubjectStatus {
        ACTIVE, INACTIVE
    }
}
