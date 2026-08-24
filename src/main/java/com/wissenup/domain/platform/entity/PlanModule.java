package com.wissenup.domain.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_modules")
@IdClass(PlanModuleId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanModule {
    @Id
    @Column(name = "plan_id")
    private Long planId;

    @Id
    @Column(name = "module_id")
    private Long moduleId;
}
