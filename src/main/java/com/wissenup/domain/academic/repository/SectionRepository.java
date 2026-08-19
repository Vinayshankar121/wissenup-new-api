package com.wissenup.domain.academic.repository;

import com.wissenup.domain.academic.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    /**
     * Find all sections for a class
     */
    List<Section> findAllByOrganizationIdAndClassIdOrderByName(Long organizationId, Long classId);

    Page<Section> findAllByOrganizationIdAndClassId(Long organizationId, Long classId, Pageable pageable);

    /**
     * Find section by class and name (unique constraint)
     */
    Optional<Section> findByOrganizationIdAndClassIdAndName(Long organizationId, Long classId, String name);
}
