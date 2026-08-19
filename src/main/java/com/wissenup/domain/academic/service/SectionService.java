package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.CreateSectionRequest;
import com.wissenup.domain.academic.dto.SectionDto;
import com.wissenup.domain.academic.dto.UpdateSectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SectionService {
    SectionDto createSection(CreateSectionRequest request, Long organizationId, Long userId);
    SectionDto getSection(Long sectionId, Long organizationId);
    SectionDto updateSection(Long sectionId, UpdateSectionRequest request, Long organizationId, Long userId);
    void deleteSection(Long sectionId, Long organizationId, Long userId);
    Page<SectionDto> listSectionsByClass(Long organizationId, Long classId, Pageable pageable);
}
