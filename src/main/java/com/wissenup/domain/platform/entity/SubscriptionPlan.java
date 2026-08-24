package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(unique = true, nullable = false)
    private String code;  // FREE, TRIAL, BASIC, STANDARD, PREMIUM

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer max_students;

    @Column(nullable = false)
    private Integer max_staff;

    @Column(nullable = false)
    private Integer max_users;

    @Column(nullable = false)
    private Integer storage_gb;

    private BigDecimal price_per_month;

    private BigDecimal yearly_price;

    @Column(nullable = false)
    @Builder.Default
    private Integer duration_days = 365;

    @Column(nullable = false)
    @Builder.Default
    private Integer grace_period_days = 7;

    private Integer trial_days;  // NULL for non-trial plans, e.g., 30 for TRIAL plan

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
    public boolean isTrial() {
        return trial_days != null && trial_days > 0;
    }

    public boolean isFreePlan() {
        return "FREE".equalsIgnoreCase(code);
    }

    public boolean isPremium() {
        return "PREMIUM".equalsIgnoreCase(code);
    }

    // Compatibility accessors for legacy camelCase call sites
    public Integer getMaxStudents() { return max_students; }
    public Integer getMaxStaff() { return max_staff; }
    public Integer getMaxUsers() { return max_users; }
    public Integer getStorageGb() { return storage_gb; }
    public BigDecimal getPricePerMonth() { return price_per_month; }
    public Integer getTrialDays() { return trial_days; }
    public Boolean getIsActive() { return is_active; }
}
