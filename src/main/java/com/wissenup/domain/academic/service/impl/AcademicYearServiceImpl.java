package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import com.wissenup.domain.academic.entity.AcademicYear;
import com.wissenup.domain.academic.entity.ClassSubject;
import com.wissenup.domain.academic.entity.Section;
import com.wissenup.domain.academic.mapper.AcademicYearMapper;
import com.wissenup.domain.academic.repository.AcademicYearRepository;
import com.wissenup.domain.academic.repository.ClassRepository;
import com.wissenup.domain.academic.repository.ClassSubjectRepository;
import com.wissenup.domain.academic.repository.SectionRepository;
import com.wissenup.domain.academic.service.AcademicYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository repository;
    private final AcademicYearMapper mapper;
    private final ClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final ClassSubjectRepository classSubjectRepository;

    @Override
    public AcademicYearDto createAcademicYear(CreateAcademicYearRequest request, Long organizationId, Long userId) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        repository.findActiveByOrganization(organizationId).ifPresent(currentActive -> {
            currentActive.setIsActive(false);
            currentActive.setStatus(AcademicYear.AcademicYearStatus.CLOSED);
            currentActive.setUpdatedBy(userId);
            currentActive.setUpdatedAt(LocalDateTime.now());
            // Flush before inserting the new active year so the partial unique index
            // never observes two active rows in the same organization.
            repository.saveAndFlush(currentActive);
        });

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

    @Override
    public void copyStructure(Long targetAcademicYearId, Long sourceAcademicYearId,
                              Long organizationId, Long userId) {
        if (Objects.equals(targetAcademicYearId, sourceAcademicYearId)) {
            throw new ConflictException("Source and target academic years must be different");
        }

        AcademicYear source = repository.findById(sourceAcademicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Source academic year", sourceAcademicYearId));
        AcademicYear target = repository.findById(targetAcademicYearId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Target academic year", targetAcademicYearId));
        validateOrganizationAccess(source.getOrganizationId(), organizationId);
        validateOrganizationAccess(target.getOrganizationId(), organizationId);

        if (!classRepository.findAllByOrganizationIdAndAcademicYearId(organizationId, targetAcademicYearId).isEmpty()) {
            throw new ConflictException("Target academic year already has a class structure");
        }

        List<com.wissenup.domain.academic.entity.Class> sourceClasses =
                classRepository.findAllByOrganizationIdAndAcademicYearIdOrderByLevel(organizationId, sourceAcademicYearId);
        for (com.wissenup.domain.academic.entity.Class sourceClass : sourceClasses) {
            com.wissenup.domain.academic.entity.Class copiedClass = classRepository.save(
                    com.wissenup.domain.academic.entity.Class.builder()
                            .organizationId(organizationId)
                            .academicYearId(targetAcademicYearId)
                            .name(sourceClass.getName())
                            .code(sourceClass.getCode())
                            .level(sourceClass.getLevel())
                            .status(sourceClass.getStatus())
                            .createdBy(userId)
                            .build());
            for (Section sourceSection : sectionRepository
                    .findAllByOrganizationIdAndClassIdOrderByName(organizationId, sourceClass.getClassId())) {
                sectionRepository.save(Section.builder()
                        .organizationId(organizationId)
                        .classId(copiedClass.getClassId())
                        .name(sourceSection.getName())
                        .capacity(sourceSection.getCapacity())
                        .currentStrength(0)
                        .status(sourceSection.getStatus())
                        .createdBy(userId)
                        .build());
            }

            for (ClassSubject sourceMapping : classSubjectRepository
                    .findAllByOrganizationIdAndClassId(organizationId, sourceClass.getClassId())) {
                classSubjectRepository.save(ClassSubject.builder()
                        .organizationId(organizationId)
                        .classId(copiedClass.getClassId())
                        .subjectId(sourceMapping.getSubjectId())
                        .isCompulsory(sourceMapping.getIsCompulsory())
                        .status(sourceMapping.getStatus())
                        .createdBy(userId)
                        .build());
            }
        }
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

