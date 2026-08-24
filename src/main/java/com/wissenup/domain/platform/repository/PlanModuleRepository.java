package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.PlanModule;
import com.wissenup.domain.platform.entity.PlanModuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanModuleRepository extends JpaRepository<PlanModule, PlanModuleId> {
    List<PlanModule> findAllByPlanId(Long planId);
}
