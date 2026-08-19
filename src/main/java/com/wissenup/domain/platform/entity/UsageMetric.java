package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metric_id;

    @Column(name = "organization_id", nullable = false)
    private Long organization_id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Builder.Default
    private Integer student_count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer staff_count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer user_count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer storage_used_mb = 0;

    @Column(nullable = false)
    @Builder.Default
    private Long api_calls = 0L;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    // Compatibility accessors for legacy camelCase call sites
    public Long getMetricId() { return metric_id; }
    public Long getOrganizationId() { return organization_id; }
    public void setOrganizationId(Long organizationId) { this.organization_id = organizationId; }
    public Integer getStudentCount() { return student_count; }
    public void setStudentCount(Integer studentCount) { this.student_count = studentCount; }
    public Integer getStaffCount() { return staff_count; }
    public void setStaffCount(Integer staffCount) { this.staff_count = staffCount; }
    public Integer getUserCount() { return user_count; }
    public void setUserCount(Integer userCount) { this.user_count = userCount; }
    public Integer getStorageUsedMb() { return storage_used_mb; }
    public void setStorageUsedMb(Integer storageUsedMb) { this.storage_used_mb = storageUsedMb; }
    public Long getApiCalls() { return api_calls; }
    public void setApiCalls(Long apiCalls) { this.api_calls = apiCalls; }

    public static class UsageMetricBuilder {
        public UsageMetricBuilder organizationId(Long organizationId) { return organization_id(organizationId); }
        public UsageMetricBuilder studentCount(Integer studentCount) { return student_count(studentCount); }
        public UsageMetricBuilder staffCount(Integer staffCount) { return staff_count(staffCount); }
        public UsageMetricBuilder userCount(Integer userCount) { return user_count(userCount); }
        public UsageMetricBuilder storageUsedMb(Integer storageUsedMb) { return storage_used_mb(storageUsedMb); }
        public UsageMetricBuilder apiCalls(Long apiCalls) { return api_calls(apiCalls); }
    }
}
