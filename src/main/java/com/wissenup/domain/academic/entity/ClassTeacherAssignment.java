package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Maps a teacher to a class for a specific academic year
 * Used for:
 * - Attendance: teacher marks attendance for their class
 * - Timetable: teacher's schedule shows their classes
 * - Marks: teacher enters marks for their class-subjects
 *
 * A teacher can be assigned to multiple classes (e.g., teach Math in 9-A and 9-B)
 */
@Entity
@Table(name = "class_teacher_assignments", indexes = {
    @Index(name = "idx_cta_organization_id", columnList = "organization_id"),
    @Index(name = "idx_cta_class_id", columnList = "class_id"),
    @Index(name = "idx_cta_staff_id", columnList = "staff_id"),
    @Index(name = "idx_cta_subject_id", columnList = "subject_id"),
    @Index(name = "idx_cta_unique", columnList = "organization_id, class_id, subject_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassTeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_teacher_assignment_id")
    private Long classTeacherAssignmentId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * Class ID
     */
    @Column(name = "class_id", nullable = false)
    private Long classId;

    /**
     * Staff/Teacher ID
     */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    /**
     * Subject ID (teacher teaches this subject in this class)
     */
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /**
     * Is this teacher the class advisor/class teacher?
     * Only one primary teacher per class
     */
    @Column(name = "is_class_teacher", nullable = false)
    private Boolean isClassTeacher = false;

    /**
     * Status: ACTIVE, INACTIVE
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ClassTeacherStatus status = ClassTeacherStatus.ACTIVE;

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
        if (isClassTeacher == null) {
            isClassTeacher = false;
        }
        if (status == null) {
            status = ClassTeacherStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ClassTeacherStatus {
        ACTIVE, INACTIVE
    }
}
