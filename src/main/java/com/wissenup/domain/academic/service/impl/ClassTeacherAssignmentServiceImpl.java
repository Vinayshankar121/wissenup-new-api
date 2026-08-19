package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.ClassTeacherAssignmentDto;
import com.wissenup.domain.academic.dto.CreateClassTeacherAssignmentRequest;
import com.wissenup.domain.academic.entity.ClassTeacherAssignment;
import com.wissenup.domain.academic.mapper.ClassTeacherAssignmentMapper;
import com.wissenup.domain.academic.repository.ClassTeacherAssignmentRepository;
import com.wissenup.domain.academic.service.ClassTeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassTeacherAssignmentServiceImpl implements ClassTeacherAssignmentService {

    private final ClassTeacherAssignmentRepository repository;
    private final ClassTeacherAssignmentMapper mapper;

    @Override
    public ClassTeacherAssignmentDto createAssignment(CreateClassTeacherAssignmentRequest request,
                                                      Long organizationId, Long userId) {
        ClassTeacherAssignment entity = mapper.toEntity(request, organizationId);
        entity.setCreatedBy(userId);
        ClassTeacherAssignment saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public ClassTeacherAssignmentDto getAssignment(Long assignmentId, Long organizationId) {
        ClassTeacherAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("ClassTeacherAssignment", assignmentId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public void deleteAssignment(Long assignmentId, Long organizationId, Long userId) {
        ClassTeacherAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("ClassTeacherAssignment", assignmentId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassTeacherAssignmentDto> listByClass(Long organizationId, Long classId, Pageable pageable) {
        Page<ClassTeacherAssignment> page = repository.findAllByOrganizationIdAndClassId(organizationId, classId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassTeacherAssignmentDto> getAllByClass(Long organizationId, Long classId) {
        List<ClassTeacherAssignment> assignments = repository.findAllByOrganizationIdAndClassId(organizationId, classId);
        return assignments.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassTeacherAssignmentDto> getAllByTeacher(Long organizationId, Long staffId) {
        List<ClassTeacherAssignment> assignments = repository.findAllByOrganizationIdAndStaffId(organizationId, staffId);
        return assignments.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClassTeacherAssignmentDto> getClassTeacher(Long organizationId, Long classId) {
        return repository.findByOrganizationIdAndClassIdAndIsClassTeacher(organizationId, classId, true)
                .map(mapper::toDto);
    }

    @Override
    public void assignClassTeacher(Long organizationId, Long classId, Long staffId, Long userId) {
        repository.findByOrganizationIdAndClassIdAndIsClassTeacher(organizationId, classId, true)
                .ifPresent(existing -> {
                    existing.setIsClassTeacher(false);
                    existing.setUpdatedBy(userId);
                    existing.setUpdatedAt(LocalDateTime.now());
                    repository.save(existing);
                });

        repository.findByOrganizationIdAndClassIdAndStaffId(organizationId, classId, staffId)
                .ifPresent(assignment -> {
                    assignment.setIsClassTeacher(true);
                    assignment.setUpdatedBy(userId);
                    assignment.setUpdatedAt(LocalDateTime.now());
                    repository.save(assignment);
                });
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

