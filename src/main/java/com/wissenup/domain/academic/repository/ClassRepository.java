package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {

    /**
     * Find all classes for an academic year
     */
    List<Class> findAllByOrganizationIdAndAcademicYearIdOrderByLevel(Long organizationId, Long academicYearId);

    Page<Class> findAllByOrganizationIdAndAcademicYearId(Long organizationId, Long academicYearId, Pageable pageable);

    List<Class> findAllByOrganizationIdAndAcademicYearId(Long organizationId, Long academicYearId);

    /**
     * Find class by name within organization and academic year
     */
    Optional<Class> findByOrganizationIdAndAcademicYearIdAndName(Long organizationId, Long academicYearId, String name);

    /**
     * Find class by code
     */
    Optional<Class> findByOrganizationIdAndCode(Long organizationId, String code);
}
