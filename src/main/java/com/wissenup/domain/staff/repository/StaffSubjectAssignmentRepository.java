package com.wissenup.domain.staff.repository;
import com.wissenup.domain.staff.entity.StaffSubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StaffSubjectAssignmentRepository extends JpaRepository<StaffSubjectAssignment, Long> {
    List<StaffSubjectAssignment> findAllByOrganizationIdAndStaffIdOrderByCreatedAtDesc(Long organizationId, Long staffId);
    void deleteAllByOrganizationIdAndStaffId(Long organizationId, Long staffId);
    boolean existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(Long organizationId,Long staffId,Long academicYearId,Long classId,Long sectionId,String status);
    boolean existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndSubjectIdAndStatusIgnoreCase(Long organizationId,Long staffId,Long academicYearId,Long classId,Long sectionId,Long subjectId,String status);
    Optional<StaffSubjectAssignment> findByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndSubjectId(Long organizationId,Long staffId,Long academicYearId,Long classId,Long sectionId,Long subjectId);
}
