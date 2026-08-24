package com.wissenup.domain.student.repository;
import com.wissenup.domain.student.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment,Long> {
    Optional<StudentEnrollment> findFirstByStudentIdOrderByEnrollmentIdDesc(Long studentId);
    void deleteAllByStudentId(Long studentId);
    boolean existsByStudentIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(Long studentId,Long academicYearId,Long classId,Long sectionId,String status);
    List<StudentEnrollment> findAllByOrganizationIdAndClassIdAndSectionIdAndStatusIgnoreCaseOrderByRollNoAsc(Long organizationId,Long classId,Long sectionId,String status);
    List<StudentEnrollment> findAllByOrganizationIdAndAcademicYearIdAndClassIdAndStatusIgnoreCase(Long organizationId,Long academicYearId,Long classId,String status);
    Optional<StudentEnrollment> findByOrganizationIdAndStudentIdAndAcademicYearIdAndClassIdAndStatusIgnoreCase(Long organizationId,Long studentId,Long academicYearId,Long classId,String status);
}
