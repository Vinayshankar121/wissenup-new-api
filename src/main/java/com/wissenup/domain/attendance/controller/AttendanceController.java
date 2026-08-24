package com.wissenup.domain.attendance.controller;

import com.wissenup.domain.attendance.dto.*; import com.wissenup.domain.attendance.entity.*; import com.wissenup.domain.attendance.service.AttendanceService;
import com.wissenup.shared.dto.ApiResponse; import com.wissenup.shared.security.SecurityContextUtil; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/attendance")
public class AttendanceController {
 private final AttendanceService service;
 private Long org(){return SecurityContextUtil.getCurrentOrganizationId();} private Long user(){return SecurityContextUtil.getCurrentUserId();} private Long role(){return SecurityContextUtil.getCurrentRoleId();}
 @GetMapping({"/sessions","/sessions/search"}) public ApiResponse<List<AttendanceSession>> sessions(@RequestParam(required=false) Long organizationId,@RequestParam(required=false) Long academicYearId,@RequestParam(required=false) Long classId,@RequestParam(required=false) Long sectionId,@RequestParam(required=false) LocalDate attendanceDate,@RequestParam(required=false) String sessionType){
  SecurityContextUtil.requireOrganizationAccess(organizationId);return ApiResponse.success(service.search(org(),user(),role(),academicYearId,classId,sectionId,attendanceDate,sessionType));}
 @PostMapping("/sessions") public ApiResponse<AttendanceSession> create(@Valid @RequestBody AttendanceSessionRequest request){return ApiResponse.success("Attendance session created",service.create(org(),user(),role(),request));}
 @PutMapping("/sessions/{id}") public ApiResponse<AttendanceSession> update(@PathVariable Long id,@Valid @RequestBody AttendanceSessionRequest request){return ApiResponse.success("Attendance submitted",service.update(id,org(),user(),role(),request));}
 @PutMapping("/sessions/{id}/approve") public ApiResponse<AttendanceSession> approve(@PathVariable Long id,@RequestBody(required=false) AttendanceActionRequest request){return ApiResponse.success("Attendance approved",service.approve(id,org(),user(),role()));}
 @PutMapping("/sessions/{id}/reject") public ApiResponse<AttendanceSession> reject(@PathVariable Long id,@RequestBody AttendanceActionRequest request){return ApiResponse.success("Attendance rejected",service.reject(id,org(),user(),role(),request.getReason()));}
 @DeleteMapping("/sessions/{id}") public ApiResponse<Void> delete(@PathVariable Long id){service.delete(id,org(),user(),role());return ApiResponse.success("Attendance deleted");}
 @GetMapping("/students/session/{id}") public ApiResponse<List<StudentAttendance>> records(@PathVariable Long id){return ApiResponse.success(service.records(id,org(),user(),role()));}
 @PostMapping("/students/bulk") public ApiResponse<AttendanceSession> createBulk(@Valid @RequestBody BulkAttendanceRequest request){return ApiResponse.success("Attendance saved",service.saveBulk(org(),user(),role(),request));}
 @PutMapping("/students/bulk") public ApiResponse<AttendanceSession> updateBulk(@Valid @RequestBody BulkAttendanceRequest request){return ApiResponse.success("Attendance saved",service.saveBulk(org(),user(),role(),request));}
}
