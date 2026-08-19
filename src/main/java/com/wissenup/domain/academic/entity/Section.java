package com.wissenup.domain.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sections", indexes = {
    @Index(name = "idx_sections_organization_id", columnList = "organization_id"),
    @Index(name = "idx_sections_class_id", columnList = "class_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    /**
     * Section name: e.g., "A", "B", "I", "II"
     * Unique within class: unique(organization_id, class_id, name)
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Class advisor/teacher ID (optional)
     */
    @Column(name = "class_teacher_id")
    private Long classTeacherId;

    /**
     * Capacity/strength
     */
    @Column(name = "capacity")
    private Integer capacity;

    /**
     * Current enrollment count (denormalized, updated when student enrolled/withdrawn)
     */
    @Column(name = "current_strength")
    private Integer currentStrength = 0;

    /**
     * Status: ACTIVE, INACTIVE
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SectionStatus status = SectionStatus.ACTIVE;

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
            status = SectionStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SectionStatus {
        ACTIVE, INACTIVE
    }
}
