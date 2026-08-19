package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.PlatformAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlatformAuditLogRepository extends JpaRepository<PlatformAuditLog, Long> {

    Page<PlatformAuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<PlatformAuditLog> findByAction(String action, Pageable pageable);

    @Query("SELECT p FROM PlatformAuditLog p WHERE p.super_admin_id = :superAdminId ORDER BY p.timestamp DESC")
    Page<PlatformAuditLog> findBySuper_admin_id(Long superAdminId, Pageable pageable);

    @Query("SELECT p FROM PlatformAuditLog p WHERE p.organization_id = :organizationId ORDER BY p.timestamp DESC")
    Page<PlatformAuditLog> findByOrganization_id(Long organizationId, Pageable pageable);

    List<PlatformAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    long countByAction(String action);
}
