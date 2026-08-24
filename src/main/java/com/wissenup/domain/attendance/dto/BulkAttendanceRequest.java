package com.wissenup.domain.attendance.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.util.List;
@Data public class BulkAttendanceRequest {
 @NotNull private Long attendanceSessionId; @NotEmpty @Valid private List<Row> students;
 @Data public static class Row { @NotNull private Long studentId; @NotBlank private String attendanceStatus; private String remarks; }
}
