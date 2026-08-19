package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_years", indexes = {
    @Index(name = "idx_academic_years_organization_id", columnList = "organization_id"),
    @Index(name = "idx_academic_years_start_end", columnList = "start_date, end_date"),
    @Index(name = "idx_academic_years_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academic_year_id")
    private Long academicYearId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * Name/Label: e.g., "2024-2025", "AY-2024-25"
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Descriptive text
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Academic year start date
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Academic year end date
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Only one active academic year per organization
     * Deactivate others before activating new one
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    /**
     * Status: ACTIVE, CLOSED, ARCHIVED
     * ACTIVE: Current year
     * CLOSED: Completed, data locked
     * ARCHIVED: Old years (kept for audit trail)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AcademicYearStatus status = AcademicYearStatus.ACTIVE;

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
            status = AcademicYearStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Academic Year Status Enum
     */
    public enum AcademicYearStatus {
        ACTIVE,      // Current/upcoming year
        CLOSED,      // Completed, locked
        ARCHIVED     // Old years
    }
}
