package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.CreateSectionRequest;
import com.wissenup.domain.academic.dto.SectionDto;
import com.wissenup.domain.academic.dto.UpdateSectionRequest;
import com.wissenup.domain.academic.entity.Section;
import com.wissenup.domain.academic.mapper.SectionMapper;
import com.wissenup.domain.academic.repository.SectionRepository;
import com.wissenup.domain.academic.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository repository;
    private final SectionMapper mapper;

    @Override
    public SectionDto createSection(CreateSectionRequest request, Long organizationId, Long userId) {
        Section entity = mapper.toEntity(request, organizationId, request.getClassId());
        entity.setCreatedBy(userId);
        Section saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SectionDto getSection(Long sectionId, Long organizationId) {
        Section entity = repository.findById(sectionId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Section", sectionId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public SectionDto updateSection(Long sectionId, UpdateSectionRequest request, Long organizationId, Long userId) {
        Section entity = repository.findById(sectionId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Section", sectionId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        mapper.updateEntity(request, entity);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());

        Section saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void deleteSection(Long sectionId, Long organizationId, Long userId) {
        Section entity = repository.findById(sectionId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Section", sectionId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SectionDto> listSectionsByClass(Long organizationId, Long classId, Pageable pageable) {
        Page<Section> page = repository.findAllByOrganizationIdAndClassId(organizationId, classId, pageable);
        return page.map(mapper::toDto);
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

