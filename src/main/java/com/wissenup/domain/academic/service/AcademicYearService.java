package com.wissenup.domain.academic.service;

import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AcademicYearService {

    AcademicYearDto createAcademicYear(CreateAcademicYearRequest request, Long organizationId, Long userId);

    AcademicYearDto getAcademicYear(Long academicYearId, Long organizationId);

    AcademicYearDto updateAcademicYear(Long academicYearId, UpdateAcademicYearRequest request, Long organizationId, Long userId);

    void deleteAcademicYear(Long academicYearId, Long organizationId, Long userId);

    Page<AcademicYearDto> listAcademicYears(Long organizationId, Pageable pageable);

    AcademicYearDto getActiveAcademicYear(Long organizationId);

    void activateAcademicYear(Long academicYearId, Long organizationId, Long userId);

    void deactivateAcademicYear(Long academicYearId, Long organizationId, Long userId);
}
