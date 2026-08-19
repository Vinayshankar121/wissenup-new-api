package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.ClassSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    /**
     * Find all subjects for a class
     */
    List<ClassSubject> findAllByOrganizationIdAndClassId(Long organizationId, Long classId);

    Page<ClassSubject> findAllByOrganizationIdAndClassId(Long organizationId, Long classId, Pageable pageable);

    /**
     * Check if subject is assigned to class
     */
    Optional<ClassSubject> findByOrganizationIdAndClassIdAndSubjectId(Long organizationId, Long classId, Long subjectId);

    /**
     * Count subjects for a class
     */
    long countByOrganizationIdAndClassId(Long organizationId, Long classId);
}
