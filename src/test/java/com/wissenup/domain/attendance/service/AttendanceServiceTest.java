package com.wissenup.domain.attendance.service;

import com.wissenup.domain.attendance.dto.*; import com.wissenup.domain.attendance.entity.AttendanceSession;
import com.wissenup.domain.attendance.repository.*; import com.wissenup.domain.identity.entity.Role; import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.communication.repository.CalendarEventRepository;
import com.wissenup.domain.staff.entity.Staff; import com.wissenup.domain.staff.repository.*; import com.wissenup.domain.student.repository.StudentEnrollmentRepository;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*;
import java.time.LocalDate; import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AttendanceServiceTest {
 @Mock AttendanceSessionRepository sessions; @Mock StudentAttendanceRepository records; @Mock StaffRepository staff;
 @Mock StaffClassAssignmentRepository classAssignments; @Mock StaffSubjectAssignmentRepository subjectAssignments;
 @Mock StudentEnrollmentRepository enrollments; @Mock RoleRepository roles; @InjectMocks AttendanceService service;
 @Mock CalendarEventRepository calendar;
 private final Long org=4L,user=20L,roleId=3L,staffId=8L;

 @BeforeEach void setup(){
  lenient().when(roles.findById(roleId)).thenReturn(Optional.of(Role.builder().roleId(roleId).code("TEACHER").name("Teacher").build()));
  lenient().when(staff.findByUserIdAndOrganizationId(user,org)).thenReturn(Optional.of(Staff.builder().staffId(staffId).organizationId(org).userId(user).build()));
  lenient().when(sessions.save(any())).thenAnswer(invocation->{AttendanceSession s=invocation.getArgument(0);if(s.getId()==null)s.setId(100L);return s;});
 }

 @Test void subjectTeacherAttendanceRequiresApproval(){
  when(subjectAssignments.existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(org,staffId,3L,37L,109L,"ACTIVE")).thenReturn(true);
  when(enrollments.existsByStudentIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(501L,3L,37L,109L,"ACTIVE")).thenReturn(true);
  AttendanceSession session=service.create(org,user,roleId,request()); when(sessions.findByIdAndOrganizationId(100L,org)).thenReturn(Optional.of(session));
  AttendanceSession submitted=service.saveBulk(org,user,roleId,bulk());
  assertEquals("PENDING_APPROVAL",submitted.getStatus()); assertNull(submitted.getActionedByUserId());
 }

 @Test void classTeacherAttendanceIsApprovedWithoutApprovalQueue(){
  when(classAssignments.existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(org,staffId,3L,37L,109L,"ACTIVE")).thenReturn(true);
  when(enrollments.existsByStudentIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(501L,3L,37L,109L,"ACTIVE")).thenReturn(true);
  AttendanceSession session=service.create(org,user,roleId,request()); when(sessions.findByIdAndOrganizationId(100L,org)).thenReturn(Optional.of(session));
  AttendanceSession submitted=service.saveBulk(org,user,roleId,bulk());
  assertEquals("APPROVED",submitted.getStatus()); assertEquals(user,submitted.getActionedByUserId());
 }

 @Test void attendanceCannotBeCreatedOnSchoolHoliday(){
  when(calendar.isHoliday(org,LocalDate.of(2026,8,22))).thenReturn(true);
  var error=assertThrows(com.wissenup.shared.exception.ValidationException.class,()->service.create(org,user,roleId,request()));
  assertEquals("Attendance cannot be marked on a school holiday",error.getMessage());verify(sessions,never()).save(any());
 }

 private AttendanceSessionRequest request(){AttendanceSessionRequest r=new AttendanceSessionRequest();r.setAcademicYearId(3L);r.setClassId(37L);r.setSectionId(109L);r.setAttendanceDate(LocalDate.of(2026,8,22));r.setSessionType("MORNING");return r;}
 private BulkAttendanceRequest bulk(){BulkAttendanceRequest.Row row=new BulkAttendanceRequest.Row();row.setStudentId(501L);row.setAttendanceStatus("PRESENT");BulkAttendanceRequest r=new BulkAttendanceRequest();r.setAttendanceSessionId(100L);r.setStudents(java.util.List.of(row));return r;}
}
