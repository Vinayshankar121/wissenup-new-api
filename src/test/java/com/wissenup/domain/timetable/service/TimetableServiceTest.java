package com.wissenup.domain.timetable.service;
import com.wissenup.domain.timetable.dto.TimetablePeriodRequest; import com.wissenup.domain.timetable.entity.TimetablePeriod; import com.wissenup.domain.timetable.repository.*;
import com.wissenup.domain.academic.repository.*; import com.wissenup.domain.identity.entity.Role; import com.wissenup.domain.identity.repository.RoleRepository; import com.wissenup.domain.staff.repository.*;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*; import org.springframework.security.access.AccessDeniedException; import java.time.LocalTime; import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TimetableServiceTest {
 @Mock TimetablePeriodRepository periods;@Mock ClassTimetableRepository headers;@Mock TimetableDetailRepository details;@Mock TeacherSubstitutionRepository substitutions;@Mock AcademicYearRepository years;@Mock ClassRepository classes;@Mock SectionRepository sections;@Mock ClassSubjectRepository classSubjects;@Mock StaffRepository staff;@Mock StaffSubjectAssignmentRepository staffSubjects;@Mock RoleRepository roles;@InjectMocks TimetableService service;
 @Test void schoolAdminCanCreatePeriod(){when(roles.findById(2L)).thenReturn(Optional.of(Role.builder().code("SCHOOL_ADMIN").build()));when(periods.save(any())).thenAnswer(i->i.getArgument(0));TimetablePeriod saved=service.savePeriod(null,4L,10L,2L,request());assertEquals("Period 1",saved.getPeriodName());assertEquals(4L,saved.getOrganizationId());}
 @Test void teacherCannotModifyTimetable(){when(roles.findById(3L)).thenReturn(Optional.of(Role.builder().code("TEACHER").build()));assertThrows(AccessDeniedException.class,()->service.savePeriod(null,4L,10L,3L,request()));verify(periods,never()).save(any());}
 private TimetablePeriodRequest request(){TimetablePeriodRequest r=new TimetablePeriodRequest();r.setPeriodName("Period 1");r.setStartTime(LocalTime.of(9,0));r.setEndTime(LocalTime.of(9,45));r.setIsBreak(false);r.setDisplayOrder(1);return r;}
}
