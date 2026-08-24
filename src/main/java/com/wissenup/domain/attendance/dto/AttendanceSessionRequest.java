package com.wissenup.domain.attendance.dto;
import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalDate;
@Data public class AttendanceSessionRequest {
 @NotNull private Long academicYearId; @NotNull private Long classId; @NotNull private Long sectionId;
 @NotNull private LocalDate attendanceDate; @NotBlank private String sessionType; private String remarks;
}
