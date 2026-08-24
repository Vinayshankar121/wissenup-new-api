package com.wissenup.domain.timetable.dto; import jakarta.validation.constraints.*; import lombok.Data;
@Data public class TimetableDetailRequest { @NotNull private Long classTimetableId; @NotBlank private String dayOfWeek; @NotNull private Long periodId; @NotNull private Long subjectId; @NotNull private Long staffId; @Size(max=100) private String roomNumber; }
