package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.SchoolModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolModuleRepository extends JpaRepository<SchoolModule, Long> {

    @Query("SELECT sm FROM SchoolModule sm WHERE sm.organization_id = :organizationId")
    List<SchoolModule> findByOrganization_id(Long organizationId);

    @Query("SELECT sm FROM SchoolModule sm WHERE sm.organization_id = :organizationId AND sm.is_enabled = :isEnabled")
    List<SchoolModule> findByOrganization_idAndIs_enabled(Long organizationId, Boolean isEnabled);

    @Query("SELECT sm FROM SchoolModule sm WHERE sm.organization_id = :organizationId AND sm.module_id = :moduleId")
    Optional<SchoolModule> findByOrganization_idAndModule_id(Long organizationId, Long moduleId);

    @Query("SELECT CASE WHEN COUNT(sm) > 0 THEN true ELSE false END FROM SchoolModule sm WHERE sm.organization_id = :organizationId AND sm.module_id = :moduleId AND sm.is_enabled = :isEnabled")
    boolean existsByOrganization_idAndModule_idAndIs_enabled(Long organizationId, Long moduleId, Boolean isEnabled);

    // Camel case aliases for convenience
    default List<SchoolModule> findByOrganizationIdAndIsEnabled(Long organizationId, Boolean isEnabled) {
        return findByOrganization_idAndIs_enabled(organizationId, isEnabled);
    }

    default Optional<SchoolModule> findByOrganizationIdAndModuleId(Long organizationId, Long moduleId) {
        return findByOrganization_idAndModule_id(organizationId, moduleId);
    }

    default boolean existsByOrganizationIdAndModuleIdAndIsEnabled(Long organizationId, Long moduleId, Boolean isEnabled) {
        return existsByOrganization_idAndModule_idAndIs_enabled(organizationId, moduleId, isEnabled);
    }
}
