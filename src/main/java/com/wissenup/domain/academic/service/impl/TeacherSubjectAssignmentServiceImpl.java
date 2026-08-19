package com.wissenup.domain.academic.service.impl;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.CreateTeacherSubjectAssignmentRequest;
import com.wissenup.domain.academic.dto.TeacherSubjectAssignmentDto;
import com.wissenup.domain.academic.entity.TeacherSubjectAssignment;
import com.wissenup.domain.academic.mapper.TeacherSubjectAssignmentMapper;
import com.wissenup.domain.academic.repository.TeacherSubjectAssignmentRepository;
import com.wissenup.domain.academic.service.TeacherSubjectAssignmentService;
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
public class TeacherSubjectAssignmentServiceImpl implements TeacherSubjectAssignmentService {

    private final TeacherSubjectAssignmentRepository repository;
    private final TeacherSubjectAssignmentMapper mapper;

    @Override
    public TeacherSubjectAssignmentDto createAssignment(CreateTeacherSubjectAssignmentRequest request,
                                                        Long organizationId, Long userId) {
        TeacherSubjectAssignment entity = mapper.toEntity(request, organizationId);
        entity.setCreatedBy(userId);
        TeacherSubjectAssignment saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public TeacherSubjectAssignmentDto getAssignment(Long assignmentId, Long organizationId) {
        TeacherSubjectAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("TeacherSubjectAssignment", assignmentId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        return mapper.toDto(entity);
    }

    @Override
    public void deleteAssignment(Long assignmentId, Long organizationId, Long userId) {
        TeacherSubjectAssignment entity = repository.findById(assignmentId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("TeacherSubjectAssignment", assignmentId));
        validateOrganizationAccess(entity.getOrganizationId(), organizationId);
        repository.deleteById(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherSubjectAssignmentDto> listByStaff(Long organizationId, Long staffId, Pageable pageable) {
        Page<TeacherSubjectAssignment> page = repository.findAllByOrganizationIdAndStaffId(organizationId, staffId, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSubjectAssignmentDto> getAllByStaff(Long organizationId, Long staffId) {
        List<TeacherSubjectAssignment> assignments = repository.findAllByOrganizationIdAndStaffId(organizationId, staffId);
        return assignments.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canTeachSubject(Long organizationId, Long staffId, Long subjectId) {
        return repository.findByOrganizationIdAndStaffIdAndSubjectId(organizationId, staffId, subjectId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSubjectAssignmentDto> getTeachersForSubject(Long organizationId, Long subjectId) {
        List<TeacherSubjectAssignment> assignments = repository.findAllByOrganizationIdAndSubjectId(organizationId, subjectId);
        return assignments.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    private void validateOrganizationAccess(Long entityOrgId, Long requestOrgId) {
        if (!entityOrgId.equals(requestOrgId)) {
            throw new AccessDeniedException("Access denied: organization mismatch");
        }
    }
}

