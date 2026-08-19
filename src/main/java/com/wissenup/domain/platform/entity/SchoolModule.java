package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_modules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "module_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long school_module_id;

    @Column(name = "organization_id", nullable = false)
    private Long organization_id;

    @Column(name = "module_id", nullable = false)
    private Long module_id;

    @Column(nullable = false)
    @Builder.Default
    private Boolean is_enabled = true;

    private LocalDateTime enabled_at;

    private LocalDateTime disabled_at;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (is_enabled) {
            enabled_at = LocalDateTime.now();
        }
    }

    // Compatibility accessors for legacy camelCase call sites
    public Long getModuleId() { return module_id; }
    public void setIsEnabled(Boolean isEnabled) { this.is_enabled = isEnabled; }
    public void setEnabledAt(LocalDateTime enabledAt) { this.enabled_at = enabledAt; }
    public void setDisabledAt(LocalDateTime disabledAt) { this.disabled_at = disabledAt; }

    public static class SchoolModuleBuilder {
        public SchoolModuleBuilder organizationId(Long organizationId) { return organization_id(organizationId); }
        public SchoolModuleBuilder moduleId(Long moduleId) { return module_id(moduleId); }
        public SchoolModuleBuilder isEnabled(Boolean isEnabled) { return is_enabled(isEnabled); }
        public SchoolModuleBuilder enabledAt(LocalDateTime enabledAt) { return enabled_at(enabledAt); }
        public SchoolModuleBuilder disabledAt(LocalDateTime disabledAt) { return disabled_at(disabledAt); }
    }
}
