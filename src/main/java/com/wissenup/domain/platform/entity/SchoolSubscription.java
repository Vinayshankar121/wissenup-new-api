package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_subscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = "organization_id")  // One active subscription per school
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscription_id;

    @Column(name = "organization_id", nullable = false)
    private Long organization_id;  // Points to organizations.organization_id

    @Column(name = "plan_id", nullable = false)
    private Long plan_id;

    @Column(nullable = false)
    private LocalDateTime started_at;

    private LocalDateTime ends_at;  // NULL = indefinite/active

    private LocalDateTime trial_ends_at;  // For TRIAL plan expiry

    @Column(nullable = false)
    @Builder.Default
    private Boolean is_active = true;

    @Column(nullable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

    // Convenience methods
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        if (trial_ends_at != null) {
            return now.isAfter(trial_ends_at) && trial_ends_at.equals(ends_at);
        }
        if (ends_at != null) {
            return now.isAfter(ends_at);
        }
        return false;
    }

    public boolean isTrialExpiring() {
        if (trial_ends_at == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningThreshold = trial_ends_at.minusDays(7);
        return now.isAfter(warningThreshold) && now.isBefore(trial_ends_at);
    }

    // Compatibility accessors for legacy camelCase call sites
    public Long getPlanId() { return plan_id; }
    public LocalDateTime getTrialEndsAt() { return trial_ends_at; }
    public void setTrialEndsAt(LocalDateTime trialEndsAt) { this.trial_ends_at = trialEndsAt; }

    public static class SchoolSubscriptionBuilder {
        public SchoolSubscriptionBuilder organizationId(Long organizationId) { return organization_id(organizationId); }
        public SchoolSubscriptionBuilder planId(Long planId) { return plan_id(planId); }
        public SchoolSubscriptionBuilder startedAt(LocalDateTime startedAt) { return started_at(startedAt); }
        public SchoolSubscriptionBuilder isActive(Boolean isActive) { return is_active(isActive); }
    }
}
