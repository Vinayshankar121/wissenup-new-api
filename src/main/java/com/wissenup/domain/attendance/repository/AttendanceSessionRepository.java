package com.wissenup.domain.attendance.repository;
import com.wissenup.domain.attendance.entity.AttendanceSession; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession,Long> {
 Optional<AttendanceSession> findByIdAndOrganizationId(Long id,Long org);
 List<AttendanceSession> findAllByOrganizationIdOrderByAttendanceDateDescCreatedAtDesc(Long org);
 List<AttendanceSession> findAllByOrganizationIdAndAcademicYearIdAndClassIdAndSectionIdAndAttendanceDateAndSessionTypeOrderByCreatedAtDesc(Long org,Long year,Long classId,Long sectionId,LocalDate date,String type);
}
