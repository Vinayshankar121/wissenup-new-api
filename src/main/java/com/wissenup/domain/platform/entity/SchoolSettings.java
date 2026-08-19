package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = "organization_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settings_id;

    @Column(name = "organization_id", nullable = false)
    private Long organization_id;

    private String logo_path;

    private String school_name;

    private String address;

    private String phone;

    private String email;

    private String website;

    @Builder.Default
    private String timezone = "UTC";

    @Builder.Default
    private String currency = "USD";

    @Builder.Default
    private String date_format = "DD/MM/YYYY";

    private String receipt_logo_path;

    private String receipt_footer;

    @Builder.Default
    private Boolean notification_email_enabled = true;

    @Builder.Default
    private Boolean notification_sms_enabled = false;

    @Builder.Default
    private Boolean notification_whatsapp_enabled = false;

    @Builder.Default
    private Integer academic_session_start_month = 6;  // June

    @Builder.Default
    private Integer academic_session_end_month = 5;    // May

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

    public static class SchoolSettingsBuilder {
        public SchoolSettingsBuilder organizationId(Long organizationId) { return organization_id(organizationId); }
        public SchoolSettingsBuilder schoolName(String schoolName) { return school_name(schoolName); }
        public SchoolSettingsBuilder dateFormat(String dateFormat) { return date_format(dateFormat); }
        public SchoolSettingsBuilder notificationEmailEnabled(Boolean enabled) { return notification_email_enabled(enabled); }
        public SchoolSettingsBuilder notificationSmsEnabled(Boolean enabled) { return notification_sms_enabled(enabled); }
        public SchoolSettingsBuilder notificationWhatsappEnabled(Boolean enabled) { return notification_whatsapp_enabled(enabled); }
        public SchoolSettingsBuilder academicSessionStartMonth(Integer month) { return academic_session_start_month(month); }
        public SchoolSettingsBuilder academicSessionEndMonth(Integer month) { return academic_session_end_month(month); }
    }
}
