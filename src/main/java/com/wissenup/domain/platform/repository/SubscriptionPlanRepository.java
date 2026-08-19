package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    @Query("SELECT s FROM SubscriptionPlan s WHERE s.is_active = :isActive")
    List<SubscriptionPlan> findByIs_active(@Param("isActive") Boolean isActive);

    @Query("SELECT s FROM SubscriptionPlan s WHERE s.code = :code AND s.is_active = :isActive")
    Optional<SubscriptionPlan> findByCodeAndIs_active(@Param("code") String code, @Param("isActive") Boolean isActive);

    default Optional<SubscriptionPlan> findByCodeAndIsActive(String code, Boolean isActive) {
        return findByCodeAndIs_active(code, isActive);
    }
}
