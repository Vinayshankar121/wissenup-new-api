package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Junction table: Class has many Subjects
 * Represents which subjects are taught in a class
 */
@Entity
@Table(name = "class_subjects", indexes = {
    @Index(name = "idx_class_subjects_organization_id", columnList = "organization_id"),
    @Index(name = "idx_class_subjects_class_id", columnList = "class_id"),
    @Index(name = "idx_class_subjects_subject_id", columnList = "subject_id"),
    @Index(name = "idx_class_subjects_unique", columnList = "organization_id, class_id, subject_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_subject_id")
    private Long classSubjectId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /**
     * Is this subject compulsory for this class?
     */
    @Column(name = "is_compulsory", nullable = false)
    private Boolean isCompulsory = true;

    /**
     * Status: ACTIVE, INACTIVE
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ClassSubjectStatus status = ClassSubjectStatus.ACTIVE;

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
        if (isCompulsory == null) {
            isCompulsory = true;
        }
        if (status == null) {
            status = ClassSubjectStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ClassSubjectStatus {
        ACTIVE, INACTIVE
    }
}
