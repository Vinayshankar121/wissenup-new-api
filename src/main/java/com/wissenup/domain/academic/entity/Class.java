package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes", indexes = {
    @Index(name = "idx_classes_organization_id", columnList = "organization_id"),
    @Index(name = "idx_classes_academic_year_id", columnList = "academic_year_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    /**
     * Class name: e.g., "10", "IX-A", "Grade 9"
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Class code for quick reference
     */
    @Column(name = "code", length = 50)
    private String code;

    /**
     * Class numeric level: 1-12 (or grade equivalent)
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * Description/notes
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Status: ACTIVE, INACTIVE, ARCHIVED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

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
            status = ClassStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Class Status Enum
     */
    public enum ClassStatus {
        ACTIVE,      // Current year, can enroll students
        INACTIVE,    // Temporarily disabled
        ARCHIVED     // Old class (kept for audit trail)
    }
}
