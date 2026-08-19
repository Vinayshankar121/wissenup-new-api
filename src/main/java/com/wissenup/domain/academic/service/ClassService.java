package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.ClassDto;
import com.wissenup.domain.academic.dto.CreateClassRequest;
import com.wissenup.domain.academic.dto.UpdateClassRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassService {
    ClassDto createClass(CreateClassRequest request, Long organizationId, Long userId);
    ClassDto getClass(Long classId, Long organizationId);
    ClassDto updateClass(Long classId, UpdateClassRequest request, Long organizationId, Long userId);
    void deleteClass(Long classId, Long organizationId, Long userId);
    Page<ClassDto> listClasses(Long organizationId, Long academicYearId, Pageable pageable);
    List<ClassDto> listAllClasses(Long organizationId, Long academicYearId);
}
