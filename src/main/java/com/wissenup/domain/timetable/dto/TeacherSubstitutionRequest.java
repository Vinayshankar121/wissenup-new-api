package com.wissenup.domain.timetable.dto; import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalDate;
@Data public class TeacherSubstitutionRequest { @NotNull private Long timetableDetailId; @NotNull private LocalDate substitutionDate; @NotNull private Long substituteStaffId; @Size(max=255) private String reason; }
