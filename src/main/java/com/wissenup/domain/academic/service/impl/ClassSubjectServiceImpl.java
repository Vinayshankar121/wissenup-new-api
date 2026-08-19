package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.ClassSubjectDto;
import com.wissenup.domain.academic.dto.CreateClassSubjectRequest;
import com.wissenup.domain.academic.entity.ClassSubject;
import com.wissenup.domain.academic.mapper.ClassSubjectMapper;
import com.wissenup.domain.academic.repository.ClassSubjectRepository;
import com.wissenup.domain.academic.service.ClassSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassSubjectServiceImpl implements ClassSubjectService {

    private final ClassSubjectRepository repository;
    private final ClassSubjectMapper mapper;

    @Override
    public ClassSubjectDto createClassSubject(CreateClassSubjectRequest request, Long organizationId, Long userId) {
        ClassSubject entity = mapper.toEntity(request, organizationId);
        entity.setCreatedBy(userId);
        ClassSubject saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public ClassSubjectDto getClassSubject(Long classSubjectId, Long organizationId) {
        ClassSubject entity = repository.findById(classSubjectId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("ClassSubject", classSubjectId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public void deleteClassSubject(Long classSubjectId, Long organizationId, Long userId) {
        ClassSubject entity = repository.findById(classSubjectId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("ClassSubject", classSubjectId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(classSubjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassSubjectDto> listByClass(Long organizationId, Long classId, Pageable pageable) {
        Page<ClassSubject> page = repository.findAllByOrganizationIdAndClassId(organizationId, classId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSubjectDto> getAllByClass(Long organizationId, Long classId) {
        List<ClassSubject> subjects = repository.findAllByOrganizationIdAndClassId(organizationId, classId);
        return subjects.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubjectInClass(Long organizationId, Long classId, Long subjectId) {
        return repository.findByOrganizationIdAndClassIdAndSubjectId(organizationId, classId, subjectId).isPresent();
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

