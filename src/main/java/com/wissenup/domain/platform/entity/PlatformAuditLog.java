package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "platform_audit_logs", indexes = {
    @Index(columnList = "super_admin_id"),
    @Index(columnList = "action"),
    @Index(columnList = "entity_type"),
    @Index(columnList = "timestamp"),
    @Index(columnList = "organization_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long log_id;

    @Column(name = "super_admin_id")
    private Long super_admin_id;

    @Column(nullable = false)
    private String action;  // SCHOOL_CREATED, PLAN_CHANGED, IMPERSONATION_START, etc.

    private String entity_type;  // SCHOOL, PLAN, MODULE, etc.

    private Long entity_id;

    private Long organization_id;  // For school-specific audits

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;  // Flexible details in JSON

    private String ip_address;

    private String user_agent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Compatibility accessors for legacy camelCase call sites
    public Long getLogId() { return log_id; }
    public Long getSuperAdminId() { return super_admin_id; }
    public String getEntityType() { return entity_type; }
    public Long getEntityId() { return entity_id; }
    public Long getOrganizationId() { return organization_id; }
    public String getIpAddress() { return ip_address; }
    public String getUserAgent() { return user_agent; }

    public static class PlatformAuditLogBuilder {
        public PlatformAuditLogBuilder superAdminId(Long superAdminId) { return super_admin_id(superAdminId); }
        public PlatformAuditLogBuilder entityType(String entityType) { return entity_type(entityType); }
        public PlatformAuditLogBuilder entityId(Long entityId) { return entity_id(entityId); }
        public PlatformAuditLogBuilder organizationId(Long organizationId) { return organization_id(organizationId); }
        public PlatformAuditLogBuilder ipAddress(String ipAddress) { return ip_address(ipAddress); }
        public PlatformAuditLogBuilder userAgent(String userAgent) { return user_agent(userAgent); }
    }
}
