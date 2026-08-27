package com.wissenup.domain.attendance.repository;
import com.wissenup.domain.attendance.entity.StudentAttendance; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import java.util.*;
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance,Long> {
 List<StudentAttendance> findAllByOrganizationIdAndAttendanceSessionIdOrderByStudentId(Long org,Long sessionId);
 Optional<StudentAttendance> findByOrganizationIdAndAttendanceSessionIdAndStudentId(Long org,Long sessionId,Long studentId);
 @Query("select r from StudentAttendance r join AttendanceSession s on s.id=r.attendanceSessionId where r.organizationId=:org and r.studentId=:studentId and s.organizationId=:org and s.status='APPROVED' order by s.attendanceDate desc, r.createdAt desc")
 List<StudentAttendance> findApprovedHistory(Long org,Long studentId);
}
