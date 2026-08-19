package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.ClassDto;
import com.wissenup.domain.academic.dto.CreateClassRequest;
import com.wissenup.domain.academic.dto.UpdateClassRequest;
import com.wissenup.domain.academic.entity.Class;
import com.wissenup.domain.academic.mapper.ClassMapper;
import com.wissenup.domain.academic.repository.ClassRepository;
import com.wissenup.domain.academic.service.ClassService;
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
public class ClassServiceImpl implements ClassService {

    private final ClassRepository repository;
    private final ClassMapper mapper;

    @Override
    public ClassDto createClass(CreateClassRequest request, Long organizationId, Long userId) {
        Class entity = mapper.toEntity(request, organizationId, request.getAcademicYearId());
        entity.setCreatedBy(userId);
        Class saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public ClassDto getClass(Long classId, Long organizationId) {
        Class entity = repository.findById(classId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Class", classId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public ClassDto updateClass(Long classId, UpdateClassRequest request, Long organizationId, Long userId) {
        Class entity = repository.findById(classId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Class", classId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);

        mapper.updateEntity(request, entity);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());

        Class saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void deleteClass(Long classId, Long organizationId, Long userId) {
        Class entity = repository.findById(classId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("Class", classId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(classId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassDto> listClasses(Long organizationId, Long academicYearId, Pageable pageable) {
        Page<Class> page = repository.findAllByOrganizationIdAndAcademicYearId(organizationId, academicYearId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> listAllClasses(Long organizationId, Long academicYearId) {
        List<Class> classes = repository.findAllByOrganizationIdAndAcademicYearId(organizationId, academicYearId);
        return classes.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

