package com.wissenup.domain.staff.repository;
import com.wissenup.domain.staff.entity.StaffClassAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StaffClassAssignmentRepository extends JpaRepository<StaffClassAssignment, Long> {
    List<StaffClassAssignment> findAllByOrganizationIdAndStaffIdOrderByCreatedAtDesc(Long organizationId, Long staffId);
    void deleteAllByOrganizationIdAndStaffId(Long organizationId, Long staffId);
    boolean existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(Long organizationId,Long staffId,Long academicYearId,Long classId,Long sectionId,String status);
    boolean existsByOrganizationIdAndStaffIdAndStatusIgnoreCase(Long organizationId,Long staffId,String status);
}
