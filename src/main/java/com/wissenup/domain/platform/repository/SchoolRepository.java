package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.School;
import com.wissenup.domain.platform.entity.SchoolStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findByOrganizationId(Long organizationId);

    Optional<School> findByEmail(String email);

    Page<School> findAll(Pageable pageable);

    Page<School> findByStatus(SchoolStatus status, Pageable pageable);

    @Query("SELECT s FROM School s WHERE s.is_active = :isActive")
    Page<School> findByIs_active(@Param("isActive") Boolean isActive, Pageable pageable);

    List<School> findByStatus(SchoolStatus status);

    @Query("SELECT s FROM School s WHERE s.is_active = :isActive")
    List<School> findByIs_active(@Param("isActive") Boolean isActive);

    long countByStatus(SchoolStatus status);

    @Query("SELECT COUNT(s) FROM School s WHERE s.is_active = :isActive")
    long countByIs_active(@Param("isActive") Boolean isActive);

    @Query("SELECT s FROM School s WHERE s.is_trial = :isTrial AND s.trial_ends_at < :now")
    List<School> findByIs_trialAndTrial_ends_at_Before(@Param("isTrial") Boolean isTrial,
                                                       @Param("now") LocalDateTime now);
}
