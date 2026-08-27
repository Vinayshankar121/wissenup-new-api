package com.wissenup.domain.attendance.service;

import com.wissenup.domain.attendance.dto.*;
import com.wissenup.domain.attendance.entity.*;
import com.wissenup.domain.attendance.repository.*;
import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.communication.repository.CalendarEventRepository;
import com.wissenup.domain.staff.entity.Staff;
import com.wissenup.domain.staff.repository.*;
import com.wissenup.domain.student.repository.StudentEnrollmentRepository;
import com.wissenup.domain.student.repository.ParentRepository;
import com.wissenup.domain.student.repository.StudentParentRepository;
import com.wissenup.domain.student.repository.StudentRepository;
import com.wissenup.shared.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class AttendanceService {
 private final AttendanceSessionRepository sessions; private final StudentAttendanceRepository records;
 private final StaffRepository staff; private final StaffClassAssignmentRepository classAssignments;
 private final StaffSubjectAssignmentRepository subjectAssignments; private final StudentEnrollmentRepository enrollments;
 private final RoleRepository roles;
 private final CalendarEventRepository calendar;
 private final ParentRepository parents; private final StudentParentRepository studentParents; private final StudentRepository students;

 @Transactional(readOnly=true)
 public List<AttendanceSession> search(Long org,Long userId,Long roleId,Long year,Long classId,Long sectionId,LocalDate date,String type){
  Actor actor=actor(org,userId,roleId);
  return sessions.findAllByOrganizationIdOrderByAttendanceDateDescCreatedAtDesc(org).stream()
   .filter(s->year==null||year.equals(s.getAcademicYearId())).filter(s->classId==null||classId.equals(s.getClassId()))
   .filter(s->sectionId==null||sectionId.equals(s.getSectionId())).filter(s->date==null||date.equals(s.getAttendanceDate()))
   .filter(s->type==null||type.equalsIgnoreCase(s.getSessionType())).filter(s->canView(actor,s)).toList();
 }

 public AttendanceSession create(Long org,Long userId,Long roleId,AttendanceSessionRequest r){
  requireWorkingDay(org,r.getAttendanceDate());
  Actor a=actor(org,userId,roleId); requireMarkerAssignment(a,r.getAcademicYearId(),r.getClassId(),r.getSectionId());
  var existing=sessions.findAllByOrganizationIdAndAcademicYearIdAndClassIdAndSectionIdAndAttendanceDateAndSessionTypeOrderByCreatedAtDesc(
   org,r.getAcademicYearId(),r.getClassId(),r.getSectionId(),r.getAttendanceDate(),normalizeType(r.getSessionType()));
  if(!existing.isEmpty())throw new ConflictException("Attendance already exists for this class, date, and session");
  return sessions.save(AttendanceSession.builder().organizationId(org).academicYearId(r.getAcademicYearId()).classId(r.getClassId())
   .sectionId(r.getSectionId()).attendanceDate(r.getAttendanceDate()).sessionType(normalizeType(r.getSessionType())).status("DRAFT")
   .markedByUserId(userId).markedByStaffId(a.staffId()).markedByRole(a.role()).remarks(r.getRemarks()).build());
 }

 public AttendanceSession update(Long id,Long org,Long userId,Long roleId,AttendanceSessionRequest r){
  requireWorkingDay(org,r.getAttendanceDate());
  AttendanceSession s=get(id,org); Actor a=actor(org,userId,roleId); requireOwner(a,s); requireEditable(s);
  if(!Objects.equals(s.getAcademicYearId(),r.getAcademicYearId())||!Objects.equals(s.getClassId(),r.getClassId())||!Objects.equals(s.getSectionId(),r.getSectionId()))
   throw new ValidationException("An attendance session's class cannot be changed");
  s.setAttendanceDate(r.getAttendanceDate());s.setSessionType(normalizeType(r.getSessionType()));s.setRemarks(r.getRemarks());
  return finalizeSubmission(s,a);
 }

 public AttendanceSession saveBulk(Long org,Long userId,Long roleId,BulkAttendanceRequest r){
  AttendanceSession s=get(r.getAttendanceSessionId(),org); Actor a=actor(org,userId,roleId); requireOwner(a,s); requireEditable(s);
  requireWorkingDay(org,s.getAttendanceDate());
  Set<Long> unique=new HashSet<>();
  for(var row:r.getStudents()){
   if(!unique.add(row.getStudentId()))throw new ValidationException("A student cannot appear twice in attendance");
   if(!enrollments.existsByStudentIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(row.getStudentId(),s.getAcademicYearId(),s.getClassId(),s.getSectionId(),"ACTIVE"))
    throw new ValidationException("Student "+row.getStudentId()+" is not actively enrolled in this class and section");
   String value=normalizeAttendance(row.getAttendanceStatus());
   StudentAttendance record=records.findByOrganizationIdAndAttendanceSessionIdAndStudentId(org,s.getId(),row.getStudentId()).orElseGet(()->
    StudentAttendance.builder().organizationId(org).attendanceSessionId(s.getId()).studentId(row.getStudentId()).createdBy(userId).build());
   record.setAttendanceStatus(value);record.setRemarks(row.getRemarks());records.save(record);
  }
  return finalizeSubmission(s,a);
 }

 @Transactional(readOnly=true) public List<StudentAttendance> records(Long id,Long org,Long userId,Long roleId){
  AttendanceSession s=get(id,org);Actor a=actor(org,userId,roleId);if(!canView(a,s))throw new AccessDeniedException("You cannot view this attendance session");
  return records.findAllByOrganizationIdAndAttendanceSessionIdOrderByStudentId(org,id);
 }

 @Transactional(readOnly=true) public List<StudentAttendance> studentHistory(Long studentId,Long org,Long userId,Long roleId){
  students.findByStudentIdAndOrganizationId(studentId,org)
   .orElseThrow(()->ResourceNotFoundException.notFound("Student",studentId));
  String role=roles.findById(roleId).map(v->v.getCode().toUpperCase()).orElse("");
  if("PARENT".equals(role)){
   var parent=parents.findByUserIdAndOrganizationId(userId,org)
    .orElseThrow(()->new AccessDeniedException("No parent profile is linked to this account"));
   if(!studentParents.existsByOrganizationIdAndParentIdAndStudentId(org,parent.getParentId(),studentId))
    throw new AccessDeniedException("You can only view attendance for your linked children");
  }else if(!Set.of("SCHOOL_ADMIN","SUPER_ADMIN").contains(role)){
   throw new AccessDeniedException("You cannot view this student's attendance history");
  }
  return records.findApprovedHistory(org,studentId);
 }

 public AttendanceSession approve(Long id,Long org,Long userId,Long roleId){return action(id,org,userId,roleId,true,null);}
 public AttendanceSession reject(Long id,Long org,Long userId,Long roleId,String reason){
  if(reason==null||reason.isBlank())throw new ValidationException("A rejection reason is required");return action(id,org,userId,roleId,false,reason.trim());
 }
 public void delete(Long id,Long org,Long userId,Long roleId){
  AttendanceSession s=get(id,org);Actor a=actor(org,userId,roleId);
  if(!a.admin())throw new AccessDeniedException("Only a school admin can delete attendance");
  sessions.delete(s);
 }
 private AttendanceSession action(Long id,Long org,Long userId,Long roleId,boolean approve,String reason){
  AttendanceSession s=get(id,org);Actor a=actor(org,userId,roleId);
  if(!"PENDING_APPROVAL".equals(s.getStatus()))throw new IllegalStateException("Attendance has already been actioned or is not pending approval");
  if(!(a.admin()||isClassTeacher(a,s)))throw new AccessDeniedException("Only the assigned class teacher or school admin can action attendance");
  s.setStatus(approve?"APPROVED":"REJECTED");s.setActionedByUserId(userId);s.setActionedByStaffId(a.staffId());s.setActionedByRole(a.role());
  s.setActionedAt(LocalDateTime.now());s.setRejectionReason(approve?null:reason);return sessions.save(s);
 }

 private AttendanceSession finalizeSubmission(AttendanceSession s,Actor a){
  boolean autoApprove=a.admin()||isClassTeacher(a,s);s.setStatus(autoApprove?"APPROVED":"PENDING_APPROVAL");s.setSubmittedAt(LocalDateTime.now());
  s.setRejectionReason(null);s.setActionedByUserId(autoApprove?a.userId():null);s.setActionedByStaffId(autoApprove?a.staffId():null);
  s.setActionedByRole(autoApprove?a.role():null);s.setActionedAt(autoApprove?LocalDateTime.now():null);return sessions.save(s);
 }
 private void requireMarkerAssignment(Actor a,Long year,Long classId,Long sectionId){
  if(a.admin())return;if(a.staffId()==null)throw new AccessDeniedException("No staff profile is linked to this account");
  boolean assigned=classAssignments.existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(a.org(),a.staffId(),year,classId,sectionId,"ACTIVE")
   ||subjectAssignments.existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(a.org(),a.staffId(),year,classId,sectionId,"ACTIVE");
  if(!assigned)throw new AccessDeniedException("You are not assigned to this class and section");
 }
 private boolean isClassTeacher(Actor a,AttendanceSession s){return a.staffId()!=null&&classAssignments.existsByOrganizationIdAndStaffIdAndAcademicYearIdAndClassIdAndSectionIdAndStatusIgnoreCase(a.org(),a.staffId(),s.getAcademicYearId(),s.getClassId(),s.getSectionId(),"ACTIVE");}
 private boolean canView(Actor a,AttendanceSession s){return a.admin()||Objects.equals(a.userId(),s.getMarkedByUserId())||isClassTeacher(a,s);}
 private void requireOwner(Actor a,AttendanceSession s){if(!Objects.equals(a.userId(),s.getMarkedByUserId())&&!a.admin())throw new AccessDeniedException("Only the attendance marker can edit this session");}
 private void requireEditable(AttendanceSession s){if(!Set.of("DRAFT","REJECTED").contains(s.getStatus()))throw new IllegalStateException("Attendance is locked while pending or after approval");}
 private AttendanceSession get(Long id,Long org){return sessions.findByIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Attendance session",id));}
 private Actor actor(Long org,Long userId,Long roleId){
  String role=roles.findById(roleId).map(v->v.getCode().toUpperCase()).orElse("");Long staffId=staff.findByUserIdAndOrganizationId(userId,org).map(Staff::getStaffId).orElse(null);
  return new Actor(org,userId,staffId,role,Set.of("SCHOOL_ADMIN","SUPER_ADMIN").contains(role));
 }
 private String normalizeType(String value){return value.trim().toUpperCase();}
 private void requireWorkingDay(Long org,LocalDate date){if(calendar.isHoliday(org,date))throw new ValidationException("Attendance cannot be marked on a school holiday");}
 private String normalizeAttendance(String value){String v=value.trim().toUpperCase();if(!Set.of("PRESENT","ABSENT","LATE","LEAVE").contains(v))throw new ValidationException("Invalid attendance status: "+value);return v;}
 private record Actor(Long org,Long userId,Long staffId,String role,boolean admin){}
}
