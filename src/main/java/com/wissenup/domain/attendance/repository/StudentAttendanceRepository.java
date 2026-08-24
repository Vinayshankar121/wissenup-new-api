package com.wissenup.domain.attendance.repository;
import com.wissenup.domain.attendance.entity.StudentAttendance; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance,Long> {
 List<StudentAttendance> findAllByOrganizationIdAndAttendanceSessionIdOrderByStudentId(Long org,Long sessionId);
 Optional<StudentAttendance> findByOrganizationIdAndAttendanceSessionIdAndStudentId(Long org,Long sessionId,Long studentId);
}
