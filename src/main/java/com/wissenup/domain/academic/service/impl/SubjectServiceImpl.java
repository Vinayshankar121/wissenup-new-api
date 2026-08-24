package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.CreateSubjectRequest;
import com.wissenup.domain.academic.dto.SubjectDto;
import com.wissenup.domain.academic.dto.UpdateSubjectRequest;
import com.wissenup.domain.academic.entity.Subject;
import com.wissenup.domain.academic.mapper.SubjectMapper;
import com.wissenup.domain.academic.repository.SubjectRepository;
import com.wissenup.domain.academic.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository repository;
    private final SubjectMapper mapper;

    @Override
    public SubjectDto createSubject(CreateSubjectRequest request, Long organizationId, Long userId) {
        validateUniqueSubject(request.getName(), request.getCode(), organizationId, null);
        Subject entity = mapper.toEntity(request, organizationId);
        entity.setCreatedBy(userId);
        Subject saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SubjectDto getSubject(Long subjectId, Long organizationId) {
        Subject entity = repository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Subject", subjectId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public SubjectDto updateSubject(Long subjectId, UpdateSubjectRequest request, Long organizationId, Long userId) {
        Subject entity = repository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Subject", subjectId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        validateUniqueSubject(request.getName(), request.getCode(), organizationId, subjectId);

        mapper.updateEntity(request, entity);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());

        Subject saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void deleteSubject(Long subjectId, Long organizationId, Long userId) {
        Subject entity = repository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Subject", subjectId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectDto> listSubjects(Long organizationId, Pageable pageable) {
        Page<Subject> page = repository.findAllByOrganizationId(organizationId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDto getSubjectByName(String name, Long organizationId) {
        return repository.findByOrganizationIdAndName(organizationId, name)
                .map(mapper::toDto)
                .orElse(null);
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }

    private void validateUniqueSubject(String name, String code, Long organizationId, Long currentSubjectId) {
        repository.findByOrganizationIdAndName(organizationId, name.trim())
                .filter(subject -> !subject.getSubjectId().equals(currentSubjectId))
                .ifPresent(subject -> {
                    throw ConflictException.duplicate("Subject", "name", name);
                });
        repository.findByOrganizationIdAndCode(organizationId, code.trim().toUpperCase())
                .filter(subject -> !subject.getSubjectId().equals(currentSubjectId))
                .ifPresent(subject -> {
                    throw ConflictException.duplicate("Subject", "code", code);
                });
    }
}

