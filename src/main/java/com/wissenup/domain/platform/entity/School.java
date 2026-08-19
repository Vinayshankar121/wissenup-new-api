package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schoolId;

    @Column(name = "organization_id", nullable = false, unique = true)
    private Long organizationId;  // Points to organizations table (organization_id)

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    private String website;

    private String logo_path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SchoolStatus status = SchoolStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean is_active = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean is_trial = false;

    private LocalDateTime trial_ends_at;

    @Column(nullable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    private Long created_by;

    private Long updated_by;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

    // Compatibility accessors for legacy camelCase call sites
    public String getLogoPath() { return logo_path; }
    public Boolean getIsActive() { return is_active; }
    public void setIsActive(Boolean isActive) { this.is_active = isActive; }
    public Boolean getIsTrial() { return is_trial; }
    public void setIsTrial(Boolean isTrial) { this.is_trial = isTrial; }
    public LocalDateTime getTrialEndsAt() { return trial_ends_at; }
    public void setTrialEndsAt(LocalDateTime trialEndsAt) { this.trial_ends_at = trialEndsAt; }
    public LocalDateTime getCreatedAt() { return created_at; }
    public LocalDateTime getUpdatedAt() { return updated_at; }
    public void setUpdatedBy(Long updatedBy) { this.updated_by = updatedBy; }

    public void setStatus(String status) {
        this.status = SchoolStatus.valueOf(status);
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public static class SchoolBuilder {
        public SchoolBuilder isActive(Boolean isActive) { return is_active(isActive); }
        public SchoolBuilder isTrial(Boolean isTrial) { return is_trial(isTrial); }
        public SchoolBuilder trialEndsAt(LocalDateTime trialEndsAt) { return trial_ends_at(trialEndsAt); }
        public SchoolBuilder createdBy(Long createdBy) { return created_by(createdBy); }
        public SchoolBuilder updatedBy(Long updatedBy) { return updated_by(updatedBy); }
    }
}
