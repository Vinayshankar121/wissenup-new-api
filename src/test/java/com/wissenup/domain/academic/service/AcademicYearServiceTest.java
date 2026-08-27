package com.wissenup.domain.academic.service;

import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.domain.academic.dto.AcademicYearDto;
import com.wissenup.domain.academic.dto.CreateAcademicYearRequest;
import com.wissenup.domain.academic.dto.UpdateAcademicYearRequest;
import com.wissenup.domain.academic.entity.AcademicYear;
import com.wissenup.domain.academic.entity.AcademicYear.AcademicYearStatus;
import com.wissenup.domain.academic.mapper.AcademicYearMapper;
import com.wissenup.domain.academic.repository.AcademicYearRepository;
import com.wissenup.domain.academic.service.impl.AcademicYearServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicYearService Tests")
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository repository;

    @Mock
    private AcademicYearMapper mapper;

    @InjectMocks
    private AcademicYearServiceImpl service;

    private Long organizationId = 1L;
    private Long userId = 100L;
    private Long academicYearId = 1L;

    private CreateAcademicYearRequest createRequest;
    private UpdateAcademicYearRequest updateRequest;
    private AcademicYear entity;
    private AcademicYearDto dto;

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        createRequest = CreateAcademicYearRequest.builder()
                .name("2024-2025")
                .description("Academic Year 2024-2025")
                .startDate(startDate)
                .endDate(endDate)
                .build();

        updateRequest = UpdateAcademicYearRequest.builder()
                .name("2024-2025 (Updated)")
                .description("Updated description")
                .startDate(startDate)
                .endDate(endDate)
                .build();

        entity = AcademicYear.builder()
                .academicYearId(academicYearId)
                .organizationId(organizationId)
                .name("2024-2025")
                .description("Academic Year 2024-2025")
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .status(AcademicYearStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();

        dto = AcademicYearDto.builder()
                .academicYearId(academicYearId)
                .organizationId(organizationId)
                .name("2024-2025")
                .description("Academic Year 2024-2025")
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .status("ACTIVE")
                .createdAt(entity.getCreatedAt())
                .createdBy(userId)
                .build();
    }

    // =========================================================================
    // CREATE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should create academic year successfully")
    void testCreateAcademicYear_Success() {
        // Arrange
        when(repository.findActiveByOrganization(organizationId)).thenReturn(Optional.empty());
        when(mapper.toEntity(createRequest, organizationId)).thenReturn(entity);
        when(repository.save(any(AcademicYear.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        AcademicYearDto result = service.createAcademicYear(createRequest, organizationId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(academicYearId, result.getAcademicYearId());
        assertEquals("2024-2025", result.getName());
        assertTrue(result.getIsActive());
        verify(repository, times(1)).save(any(AcademicYear.class));
    }

    @Test
    @DisplayName("Should close the current active year before creating a new active year")
    void testCreateAcademicYear_ClosesCurrentActiveYear() {
        AcademicYear currentActive = AcademicYear.builder()
                .academicYearId(2L)
                .organizationId(organizationId)
                .name("2023-2024")
                .isActive(true)
                .status(AcademicYearStatus.ACTIVE)
                .build();

        when(repository.findActiveByOrganization(organizationId)).thenReturn(Optional.of(currentActive));
        when(repository.saveAndFlush(currentActive)).thenReturn(currentActive);
        when(mapper.toEntity(createRequest, organizationId)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.createAcademicYear(createRequest, organizationId, userId);

        assertFalse(currentActive.getIsActive());
        assertEquals(AcademicYearStatus.CLOSED, currentActive.getStatus());
        assertEquals(userId, currentActive.getUpdatedBy());
        verify(repository).saveAndFlush(currentActive);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("Should fail when start date is after end date")
    void testCreateAcademicYear_InvalidDateRange() {
        // Arrange
        CreateAcademicYearRequest invalidRequest = CreateAcademicYearRequest.builder()
                .name("Invalid Year")
                .startDate(LocalDate.of(2025, 5, 31))
                .endDate(LocalDate.of(2024, 6, 1))
                .build();

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> service.createAcademicYear(invalidRequest, organizationId, userId));
        verify(repository, never()).save(any(AcademicYear.class));
    }

    // =========================================================================
    // READ TESTS
    // =========================================================================

    @Test
    @DisplayName("Should get academic year by ID")
    void testGetAcademicYear_Success() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        AcademicYearDto result = service.getAcademicYear(academicYearId, organizationId);

        // Assert
        assertNotNull(result);
        assertEquals(academicYearId, result.getAcademicYearId());
        verify(repository, times(1)).findById(academicYearId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when academic year not found")
    void testGetAcademicYear_NotFound() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> service.getAcademicYear(academicYearId, organizationId));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when organization mismatch")
    void testGetAcademicYear_OrganizationMismatch() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.getAcademicYear(academicYearId, 999L)); // Different org
        verify(mapper, never()).toDto(any());
    }

    // =========================================================================
    // LIST TESTS
    // =========================================================================

    @Test
    @DisplayName("Should list academic years with pagination")
    void testListAcademicYears_Success() {
        // Arrange
        AcademicYear year1 = entity;
        AcademicYear year2 = AcademicYear.builder()
                .academicYearId(2L)
                .organizationId(organizationId)
                .name("2025-2026")
                .isActive(false)
                .build();

        Page<AcademicYear> page = new PageImpl<>(Arrays.asList(year1, year2));
        Pageable pageable = PageRequest.of(0, 25);

        when(repository.findAllByOrganizationId(organizationId, pageable)).thenReturn(page);
        when(mapper.toDto(year1)).thenReturn(dto);
        when(mapper.toDto(year2)).thenReturn(dto);

        // Act
        Page<AcademicYearDto> result = service.listAcademicYears(organizationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(repository, times(1)).findAllByOrganizationId(organizationId, pageable);
    }

    @Test
    @DisplayName("Should get active academic year")
    void testGetActiveAcademicYear_Success() {
        // Arrange
        when(repository.findActiveByOrganization(organizationId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        AcademicYearDto result = service.getActiveAcademicYear(organizationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsActive());
        assertEquals("ACTIVE", result.getStatus());
    }

    // =========================================================================
    // UPDATE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should update academic year successfully")
    void testUpdateAcademicYear_Success() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity));
        when(repository.save(any(AcademicYear.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        AcademicYearDto result = service.updateAcademicYear(academicYearId, updateRequest, organizationId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(academicYearId, result.getAcademicYearId());
        verify(repository, times(1)).save(any(AcademicYear.class));
    }

    // =========================================================================
    // DELETE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should delete academic year successfully")
    void testDeleteAcademicYear_Success() {
        // Arrange
        AcademicYear inactiveYear = AcademicYear.builder()
                .academicYearId(academicYearId)
                .organizationId(organizationId)
                .name("2023-2024")
                .isActive(false)
                .build();

        when(repository.findById(academicYearId)).thenReturn(Optional.of(inactiveYear));

        // Act
        service.deleteAcademicYear(academicYearId, organizationId, userId);

        // Assert
        verify(repository, times(1)).deleteById(academicYearId);
    }

    @Test
    @DisplayName("Should fail to delete active academic year")
    void testDeleteAcademicYear_ActiveYear() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity)); // isActive = true

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> service.deleteAcademicYear(academicYearId, organizationId, userId));
        verify(repository, never()).deleteById(anyLong());
    }

    // =========================================================================
    // ACTIVATE/DEACTIVATE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should activate academic year and deactivate others")
    void testActivateAcademicYear_Success() {
        // Arrange
        AcademicYear newYear = AcademicYear.builder()
                .academicYearId(2L)
                .organizationId(organizationId)
                .name("2025-2026")
                .isActive(false)
                .status(AcademicYearStatus.CLOSED)
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(newYear));
        when(repository.findActiveByOrganization(organizationId)).thenReturn(Optional.of(entity));
        when(repository.save(any(AcademicYear.class))).thenReturn(newYear);

        // Act
        service.activateAcademicYear(2L, organizationId, userId);

        // Assert
        assertTrue(newYear.getIsActive());
        assertEquals(AcademicYearStatus.ACTIVE, newYear.getStatus());
        assertFalse(entity.getIsActive());
        assertEquals(AcademicYearStatus.CLOSED, entity.getStatus());
        verify(repository, times(2)).save(any(AcademicYear.class));
    }

    @Test
    @DisplayName("Should deactivate academic year")
    void testDeactivateAcademicYear_Success() {
        // Arrange
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity));
        when(repository.save(any(AcademicYear.class))).thenReturn(entity);

        // Act
        service.deactivateAcademicYear(academicYearId, organizationId, userId);

        // Assert
        assertFalse(entity.getIsActive());
        assertEquals(AcademicYearStatus.CLOSED, entity.getStatus());
        verify(repository, times(1)).save(any(AcademicYear.class));
    }

    // =========================================================================
    // MULTI-TENANCY TESTS
    // =========================================================================

    @Test
    @DisplayName("Should enforce organization isolation - cannot access different org's data")
    void testOrganizationIsolation() {
        // Arrange
        Long differentOrgId = 999L;
        when(repository.findById(academicYearId)).thenReturn(Optional.of(entity)); // orgId=1

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.getAcademicYear(academicYearId, differentOrgId)); // trying to access with different org
    }

    @Test
    @DisplayName("Should prevent accessing another organization's active year")
    void testMultiTenant_PreventCrossOrgAccess() {
        // Arrange
        AcademicYear orgBYear = AcademicYear.builder()
                .academicYearId(academicYearId)
                .organizationId(999L) // Different organization
                .name("2024-2025")
                .isActive(true)
                .build();

        when(repository.findById(academicYearId)).thenReturn(Optional.of(orgBYear));

        // Act & Assert - School A trying to get School B's year
        assertThrows(AccessDeniedException.class,
                () -> service.getAcademicYear(academicYearId, 1L)); // organizationId = 1
    }
}
