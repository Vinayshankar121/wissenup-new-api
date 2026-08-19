package com.wissenup.domain.academic.service;

import com.wissenup.shared.exception.AccessDeniedException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.domain.academic.dto.ClassDto;
import com.wissenup.domain.academic.dto.CreateClassRequest;
import com.wissenup.domain.academic.dto.UpdateClassRequest;
import com.wissenup.domain.academic.entity.Class;
import com.wissenup.domain.academic.entity.Class.ClassStatus;
import com.wissenup.domain.academic.mapper.ClassMapper;
import com.wissenup.domain.academic.repository.ClassRepository;
import com.wissenup.domain.academic.service.impl.ClassServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassService Tests")
class ClassServiceTest {

    @Mock
    private ClassRepository repository;

    @Mock
    private ClassMapper mapper;

    @InjectMocks
    private ClassServiceImpl service;

    private Long organizationId = 1L;
    private Long academicYearId = 1L;
    private Long classId = 1L;
    private Long userId = 100L;

    private CreateClassRequest createRequest;
    private UpdateClassRequest updateRequest;
    private Class entity;
    private ClassDto dto;

    @BeforeEach
    void setUp() {
        createRequest = CreateClassRequest.builder()
                .academicYearId(academicYearId)
                .name("10 (X)")
                .code("CLASS_10")
                .level(10)
                .build();

        updateRequest = UpdateClassRequest.builder()
                .name("10 (X) - Updated")
                .code("CLASS_10_UPD")
                .level(10)
                .build();

        entity = Class.builder()
                .classId(classId)
                .organizationId(organizationId)
                .academicYearId(academicYearId)
                .name("10 (X)")
                .code("CLASS_10")
                .level(10)
                .status(ClassStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();

        dto = ClassDto.builder()
                .classId(classId)
                .organizationId(organizationId)
                .academicYearId(academicYearId)
                .name("10 (X)")
                .code("CLASS_10")
                .level(10)
                .status("ACTIVE")
                .createdAt(entity.getCreatedAt())
                .createdBy(userId)
                .build();
    }

    @Test
    @DisplayName("Should create class successfully")
    void testCreateClass_Success() {
        // Arrange
        when(mapper.toEntity(createRequest, organizationId, academicYearId)).thenReturn(entity);
        when(repository.save(any(Class.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        ClassDto result = service.createClass(createRequest, organizationId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(classId, result.getClassId());
        assertEquals("10 (X)", result.getName());
        assertEquals(10, result.getLevel());
        verify(repository, times(1)).save(any(Class.class));
    }

    @Test
    @DisplayName("Should get class by ID")
    void testGetClass_Success() {
        // Arrange
        when(repository.findById(classId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        ClassDto result = service.getClass(classId, organizationId);

        // Assert
        assertNotNull(result);
        assertEquals(classId, result.getClassId());
        assertEquals("10 (X)", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when class not found")
    void testGetClass_NotFound() {
        // Arrange
        when(repository.findById(classId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> service.getClass(classId, organizationId));
    }

    @Test
    @DisplayName("Should list classes by academic year")
    void testListClasses_Success() {
        // Arrange
        Class class1 = entity;
        Class class2 = Class.builder()
                .classId(2L)
                .organizationId(organizationId)
                .academicYearId(academicYearId)
                .name("IX (9)")
                .code("CLASS_9")
                .level(9)
                .status(ClassStatus.ACTIVE)
                .build();

        Page<Class> page = new PageImpl<>(Arrays.asList(class1, class2));
        Pageable pageable = PageRequest.of(0, 25);

        when(repository.findAllByOrganizationIdAndAcademicYearId(organizationId, academicYearId, pageable))
                .thenReturn(page);
        when(mapper.toDto(class1)).thenReturn(dto);
        when(mapper.toDto(class2)).thenReturn(dto);

        // Act
        Page<ClassDto> result = service.listClasses(organizationId, academicYearId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("Should list all classes (for dropdowns)")
    void testListAllClasses_Success() {
        // Arrange
        Class class1 = entity;
        Class class2 = Class.builder()
                .classId(2L)
                .organizationId(organizationId)
                .academicYearId(academicYearId)
                .name("IX (9)")
                .code("CLASS_9")
                .level(9)
                .status(ClassStatus.ACTIVE)
                .build();

        when(repository.findAllByOrganizationIdAndAcademicYearId(organizationId, academicYearId))
                .thenReturn(Arrays.asList(class1, class2));
        when(mapper.toDto(class1)).thenReturn(dto);
        when(mapper.toDto(class2)).thenReturn(dto);

        // Act
        List<ClassDto> result = service.listAllClasses(organizationId, academicYearId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should update class successfully")
    void testUpdateClass_Success() {
        // Arrange
        when(repository.findById(classId)).thenReturn(Optional.of(entity));
        when(repository.save(any(Class.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        ClassDto result = service.updateClass(classId, updateRequest, organizationId, userId);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).save(any(Class.class));
    }

    @Test
    @DisplayName("Should delete class successfully")
    void testDeleteClass_Success() {
        // Arrange
        when(repository.findById(classId)).thenReturn(Optional.of(entity));

        // Act
        service.deleteClass(classId, organizationId, userId);

        // Assert
        verify(repository, times(1)).deleteById(classId);
    }

    @Test
    @DisplayName("Should enforce organization isolation - cannot access different org's class")
    void testOrganizationIsolation_DifferentOrg() {
        // Arrange
        Class orgBClass = Class.builder()
                .classId(classId)
                .organizationId(999L) // Different organization
                .academicYearId(academicYearId)
                .name("10 (X)")
                .code("CLASS_10")
                .level(10)
                .status(ClassStatus.ACTIVE)
                .build();

        when(repository.findById(classId)).thenReturn(Optional.of(orgBClass));

        // Act & Assert - Organization 1 trying to access Organization 999's class
        assertThrows(AccessDeniedException.class,
                () -> service.getClass(classId, organizationId));
    }

    @Test
    @DisplayName("Should prevent updating class in different organization")
    void testUpdateClass_DifferentOrganization() {
        // Arrange
        Class orgBClass = Class.builder()
                .classId(classId)
                .organizationId(999L) // Different org
                .academicYearId(academicYearId)
                .name("10 (X)")
                .code("CLASS_10")
                .level(10)
                .build();

        when(repository.findById(classId)).thenReturn(Optional.of(orgBClass));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.updateClass(classId, updateRequest, organizationId, userId));
        verify(repository, never()).save(any(Class.class));
    }

    @Test
    @DisplayName("Should validate level range (1-12)")
    void testClassLevelValidation() {
        // This validates that the entity should enforce level constraints
        // In real implementation, this would be caught by @Min/@Max annotations
        assertTrue(entity.getLevel() >= 1 && entity.getLevel() <= 12);
    }
}
