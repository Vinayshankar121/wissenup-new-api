package com.wissenup.domain.staff.repository;
import com.wissenup.domain.staff.entity.Designation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DesignationRepository extends JpaRepository<Designation,Long>{List<Designation> findAllByOrganizationIdOrderByName(Long organizationId); Optional<Designation> findByDesignationIdAndOrganizationId(Long id,Long orgId);}
