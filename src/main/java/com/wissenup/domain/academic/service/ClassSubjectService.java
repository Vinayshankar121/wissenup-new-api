package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.ClassSubjectDto;
import com.wissenup.domain.academic.dto.CreateClassSubjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassSubjectService {
    ClassSubjectDto createClassSubject(CreateClassSubjectRequest request, Long organizationId, Long userId);
    ClassSubjectDto getClassSubject(Long classSubjectId, Long organizationId);
    void deleteClassSubject(Long classSubjectId, Long organizationId, Long userId);
    Page<ClassSubjectDto> listByClass(Long organizationId, Long classId, Pageable pageable);
    List<ClassSubjectDto> getAllByClass(Long organizationId, Long classId);
    boolean isSubjectInClass(Long organizationId, Long classId, Long subjectId);
}
