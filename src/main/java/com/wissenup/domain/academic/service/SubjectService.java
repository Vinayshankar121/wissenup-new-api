package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.CreateSubjectRequest;
import com.wissenup.domain.academic.dto.SubjectDto;
import com.wissenup.domain.academic.dto.UpdateSubjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectService {
    SubjectDto createSubject(CreateSubjectRequest request, Long organizationId, Long userId);
    SubjectDto getSubject(Long subjectId, Long organizationId);
    SubjectDto updateSubject(Long subjectId, UpdateSubjectRequest request, Long organizationId, Long userId);
    void deleteSubject(Long subjectId, Long organizationId, Long userId);
    Page<SubjectDto> listSubjects(Long organizationId, Pageable pageable);
    SubjectDto getSubjectByName(String name, Long organizationId);
}
