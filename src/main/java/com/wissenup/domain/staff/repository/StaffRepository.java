package com.wissenup.domain.staff.repository;
import com.wissenup.domain.staff.entity.Staff; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface StaffRepository extends JpaRepository<Staff,Long>{List<Staff> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId); Optional<Staff> findByStaffIdAndOrganizationId(Long id,Long orgId); Optional<Staff> findByUserIdAndOrganizationId(Long userId,Long orgId); boolean existsByOrganizationIdAndEmployeeCode(Long orgId,String code);}
