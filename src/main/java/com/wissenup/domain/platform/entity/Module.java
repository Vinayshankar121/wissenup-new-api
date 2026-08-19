package com.wissenup.domain.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long module_id;

    @Column(unique = true, nullable = false)
    private String code;  // ACADEMIC, STUDENTS, ATTENDANCE, FEES, EXAMS, TIMETABLE, TRANSPORT, LIBRARY, HR

    @Column(nullable = false)
    private String name;

    private String description;

    private String icon;

    private Integer display_order;

    @Column(nullable = false)
    @Builder.Default
    private Boolean is_active = true;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    // Compatibility accessors for legacy camelCase call sites
    public Long getModuleId() { return module_id; }
    public Integer getDisplayOrder() { return display_order; }
    public Boolean getIsActive() { return is_active; }
}
