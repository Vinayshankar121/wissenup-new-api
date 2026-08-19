package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import com.wissenup.domain.academic.entity.AcademicYear;
import com.wissenup.domain.academic.mapper.AcademicYearMapper;
import com.wissenup.domain.academic.repository.AcademicYearRepository;
import com.wissenup.domain.academic.service.AcademicYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository repository;
    private final AcademicYearMapper mapper;

    @Override
    public AcademicYearDto createAcademicYear(CreateAcademicYearRequest request, Long organizationId, Long userId) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        AcademicYear entity = mapper.toEntity(request, organizationId);
        entity.setCreatedBy(userId);

        AcademicYear saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public AcademicYearDto getAcademicYear(Long academicYearId, Long organizationId) {
        AcademicYear entity = repository.findById(academicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("AcademicYear", academicYearId));

        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public AcademicYearDto updateAcademicYear(Long academicYearId, UpdateAcademicYearRequest request,
                                              Long organizationId, Long userId) {
        AcademicYear entity = repository.findById(academicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("AcademicYear", academicYearId));

        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        if (request.getStartDate() != null && request.getEndDate() != null &&
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        mapper.updateEntity(request, entity);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());

        AcademicYear saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void deleteAcademicYear(Long academicYearId, Long organizationId, Long userId) {
        AcademicYear entity = repository.findById(academicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("AcademicYear", academicYearId));

        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        if (entity.getIsActive()) {
            throw new ConflictException("Cannot delete an active academic year");
        }

        repository.deleteById(academicYearId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicYearDto> listAcademicYears(Long organizationId, Pageable pageable) {
        Page<AcademicYear> page = repository.findAllByOrganizationId(organizationId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearDto getActiveAcademicYear(Long organizationId) {
        Optional<AcademicYear> optional = repository.findActiveByOrganization(organizationId);
        return optional.map(mapper::toDto).orElse(null);
    }

    @Override
    public void activateAcademicYear(Long academicYearId, Long organizationId, Long userId) {
        AcademicYear entity = repository.findById(academicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("AcademicYear", academicYearId));

        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        Optional<AcademicYear> activeYear = repository.findActiveByOrganization(organizationId);
        if (activeYear.isPresent() && !activeYear.get().getAcademicYearId().equals(academicYearId)) {
            AcademicYear currentActive = activeYear.get();
            currentActive.setIsActive(false);
            currentActive.setStatus(AcademicYear.AcademicYearStatus.CLOSED);
            currentActive.setUpdatedBy(userId);
            currentActive.setUpdatedAt(LocalDateTime.now());
            repository.save(currentActive);
        }

        entity.setIsActive(true);
        entity.setStatus(AcademicYear.AcademicYearStatus.ACTIVE);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    public void deactivateAcademicYear(Long academicYearId, Long organizationId, Long userId) {
        AcademicYear entity = repository.findById(academicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("AcademicYear", academicYearId));

        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        entity.setIsActive(false);
        entity.setStatus(AcademicYear.AcademicYearStatus.CLOSED);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

