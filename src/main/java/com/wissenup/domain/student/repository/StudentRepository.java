package com.wissenup.domain.student.repository;
import com.wissenup.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    Optional<Student> findByStudentIdAndOrganizationId(Long id, Long organizationId);
    Optional<Student> findByOrganizationIdAndAdmissionNo(Long organizationId, String admissionNo);
    boolean existsByOrganizationIdAndAdmissionNo(Long organizationId, String admissionNo);
}
