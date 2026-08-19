package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subjects_organization_id", columnList = "organization_id"),
    @Index(name = "idx_subjects_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * Subject name: e.g., "Mathematics", "English", "Science"
     * Unique within school: unique(organization_id, name)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Subject code for quick reference
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Subject type: CORE, ELECTIVE, CO_CURRICULAR
     */
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SubjectType type = SubjectType.CORE;

    /**
     * Description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Marks/Credit hours
     */
    @Column(name = "max_marks")
    private Integer maxMarks = 100;

    /**
     * Status: ACTIVE, INACTIVE
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SubjectStatus status = SubjectStatus.ACTIVE;

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
        if (type == null) {
            type = SubjectType.CORE;
        }
        if (status == null) {
            status = SubjectStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SubjectType {
        CORE,
        ELECTIVE,
        CO_CURRICULAR
    }

    public enum SubjectStatus {
        ACTIVE, INACTIVE
    }
}
