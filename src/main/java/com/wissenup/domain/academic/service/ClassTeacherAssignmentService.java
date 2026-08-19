package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.ClassTeacherAssignmentDto;
import com.wissenup.domain.academic.dto.CreateClassTeacherAssignmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClassTeacherAssignmentService {
    ClassTeacherAssignmentDto createAssignment(CreateClassTeacherAssignmentRequest request, Long organizationId, Long userId);
    ClassTeacherAssignmentDto getAssignment(Long assignmentId, Long organizationId);
    void deleteAssignment(Long assignmentId, Long organizationId, Long userId);
    Page<ClassTeacherAssignmentDto> listByClass(Long organizationId, Long classId, Pageable pageable);
    List<ClassTeacherAssignmentDto> getAllByClass(Long organizationId, Long classId);
    List<ClassTeacherAssignmentDto> getAllByTeacher(Long organizationId, Long staffId);
    Optional<ClassTeacherAssignmentDto> getClassTeacher(Long organizationId, Long classId);
    void assignClassTeacher(Long organizationId, Long classId, Long staffId, Long userId);
}
