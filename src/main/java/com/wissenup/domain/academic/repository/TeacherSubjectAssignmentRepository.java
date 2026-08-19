package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.TeacherSubjectAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSubjectAssignmentRepository extends JpaRepository<TeacherSubjectAssignment, Long> {

    /**
     * Find all subjects a teacher can teach
     */
    List<TeacherSubjectAssignment> findAllByOrganizationIdAndStaffId(Long organizationId, Long staffId);

    Page<TeacherSubjectAssignment> findAllByOrganizationIdAndStaffId(Long organizationId, Long staffId, Pageable pageable);

    /**
     * Check if teacher can teach a subject
     */
    Optional<TeacherSubjectAssignment> findByOrganizationIdAndStaffIdAndSubjectId(Long organizationId, Long staffId, Long subjectId);

    /**
     * Find all teachers who teach a subject
     */
    List<TeacherSubjectAssignment> findAllByOrganizationIdAndSubjectId(Long organizationId, Long subjectId);

    /**
     * Count subjects for a teacher
     */
    long countByOrganizationIdAndStaffId(Long organizationId, Long staffId);
}
