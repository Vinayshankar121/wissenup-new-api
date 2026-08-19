package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Find all subjects for organization
     */
    List<Subject> findAllByOrganizationIdOrderByName(Long organizationId);

    Page<Subject> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Find subject by name (unique within organization)
     */
    Optional<Subject> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Find subject by code
     */
    Optional<Subject> findByOrganizationIdAndCode(Long organizationId, String code);

    /**
     * Find all core subjects
     */
    List<Subject> findAllByOrganizationIdAndType(Long organizationId, Subject.SubjectType type);
}
