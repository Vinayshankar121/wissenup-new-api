package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.SchoolSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, Long> {

    @Query("SELECT s FROM SchoolSubscription s WHERE s.organization_id = :organizationId")
    Optional<SchoolSubscription> findByOrganization_id(@Param("organizationId") Long organizationId);

    @Query("SELECT s FROM SchoolSubscription s WHERE s.organization_id = :organizationId AND s.is_active = :isActive")
    Optional<SchoolSubscription> findByOrganization_idAndIs_active(@Param("organizationId") Long organizationId,
                                                                  @Param("isActive") Boolean isActive);

    default Optional<SchoolSubscription> findByOrganizationId(Long organizationId) {
        return findByOrganization_id(organizationId);
    }

    default Optional<SchoolSubscription> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive) {
        return findByOrganization_idAndIs_active(organizationId, isActive);
    }
}
