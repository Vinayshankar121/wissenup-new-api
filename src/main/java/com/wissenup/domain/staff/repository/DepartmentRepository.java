package com.wissenup.domain.staff.repository;
import com.wissenup.domain.staff.entity.Department; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DepartmentRepository extends JpaRepository<Department,Long>{List<Department> findAllByOrganizationIdOrderByName(Long organizationId); Optional<Department> findByDepartmentIdAndOrganizationId(Long id,Long orgId);}
