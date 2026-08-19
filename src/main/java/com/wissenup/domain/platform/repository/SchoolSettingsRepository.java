package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {

    @Query("SELECT s FROM SchoolSettings s WHERE s.organization_id = :organizationId")
    Optional<SchoolSettings> findByOrganization_id(@Param("organizationId") Long organizationId);
}
