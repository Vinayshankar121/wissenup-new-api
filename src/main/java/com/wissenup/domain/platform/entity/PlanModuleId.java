package com.wissenup.domain.platform.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlanModuleId implements Serializable {
    private Long planId;
    private Long moduleId;
}
