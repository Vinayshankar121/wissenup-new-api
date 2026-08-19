package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.UsageMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageMetricRepository extends JpaRepository<UsageMetric, Long> {

    /**
     * Find usage metrics for an organization by date range.
     */
    List<UsageMetric> findByOrganizationIdAndDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate);

    /**
     * Find latest usage metric for an organization.
     */
    @Query(value = "SELECT * FROM usage_metrics WHERE organization_id = :organizationId ORDER BY date DESC LIMIT 1",
            nativeQuery = true)
    Optional<UsageMetric> findLatestByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Sum all student counts across all organizations.
     */
    @Query(value = "SELECT COALESCE(SUM(student_count), 0) FROM usage_metrics", nativeQuery = true)
    long sumStudentCount();

    /**
     * Sum all staff counts across all organizations.
     */
    @Query(value = "SELECT COALESCE(SUM(staff_count), 0) FROM usage_metrics", nativeQuery = true)
    long sumStaffCount();

    /**
     * Sum all storage used across all organizations (in MB).
     */
    @Query(value = "SELECT COALESCE(SUM(storage_used_mb), 0) FROM usage_metrics", nativeQuery = true)
    long sumStorageUsed();
}
