package com.wissenup.domain.student.repository;
import com.wissenup.domain.student.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface StudentParentRepository extends JpaRepository<StudentParent,Long> {
    Optional<StudentParent> findFirstByStudentIdOrderByIsPrimaryDesc(Long studentId);
    List<StudentParent> findAllByParentId(Long parentId);
    boolean existsByOrganizationIdAndParentIdAndStudentId(Long organizationId,Long parentId,Long studentId);
    void deleteAllByStudentId(Long studentId);
}
