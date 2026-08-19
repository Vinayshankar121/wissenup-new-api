package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.ClassTeacherAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassTeacherAssignmentRepository extends JpaRepository<ClassTeacherAssignment, Long> {

    /**
     * Find all teachers assigned to a class
     */
    List<ClassTeacherAssignment> findAllByOrganizationIdAndClassId(Long organizationId, Long classId);

    Page<ClassTeacherAssignment> findAllByOrganizationIdAndClassId(Long organizationId, Long classId, Pageable pageable);

    /**
     * Find a teacher's subject assignment in a class
     */
    Optional<ClassTeacherAssignment> findByOrganizationIdAndClassIdAndStaffIdAndSubjectId(
        Long organizationId, Long classId, Long staffId, Long subjectId);

    /**
     * Find all classes a teacher teaches
     */
    List<ClassTeacherAssignment> findAllByOrganizationIdAndStaffId(Long organizationId, Long staffId);

    Optional<ClassTeacherAssignment> findByOrganizationIdAndClassIdAndStaffId(Long organizationId, Long classId, Long staffId);

    /**
     * Find class teacher (advisor)
     */
    Optional<ClassTeacherAssignment> findByOrganizationIdAndClassIdAndIsClassTeacher(Long organizationId, Long classId, Boolean isClassTeacher);

    /**
     * Check if teacher teaches a subject in a class
     */
    boolean existsByOrganizationIdAndClassIdAndStaffIdAndSubjectId(Long organizationId, Long classId, Long staffId, Long subjectId);
}
