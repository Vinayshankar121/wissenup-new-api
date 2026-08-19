package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    /**
     * Find the active academic year for an organization
     * Only one should exist per organization
     */
    @Query("SELECT a FROM AcademicYear a WHERE a.organizationId = :organizationId AND a.isActive = true")
    Optional<AcademicYear> findActiveByOrganization(Long organizationId);

    /**
     * Find all academic years for an organization
     */
    List<AcademicYear> findAllByOrganizationIdOrderByStartDateDesc(Long organizationId);

    Page<AcademicYear> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Find by name within organization (unique constraint)
     */
    Optional<AcademicYear> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Find all active years (regardless of organization)
     */
    List<AcademicYear> findAllByIsActive(Boolean isActive);
}
