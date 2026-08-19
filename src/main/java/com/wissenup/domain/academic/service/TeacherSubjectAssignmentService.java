package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.CreateTeacherSubjectAssignmentRequest;
import com.wissenup.domain.academic.dto.TeacherSubjectAssignmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeacherSubjectAssignmentService {
    TeacherSubjectAssignmentDto createAssignment(CreateTeacherSubjectAssignmentRequest request, Long organizationId, Long userId);
    TeacherSubjectAssignmentDto getAssignment(Long assignmentId, Long organizationId);
    void deleteAssignment(Long assignmentId, Long organizationId, Long userId);
    Page<TeacherSubjectAssignmentDto> listByStaff(Long organizationId, Long staffId, Pageable pageable);
    List<TeacherSubjectAssignmentDto> getAllByStaff(Long organizationId, Long staffId);
    boolean canTeachSubject(Long organizationId, Long staffId, Long subjectId);
    List<TeacherSubjectAssignmentDto> getTeachersForSubject(Long organizationId, Long subjectId);
}
