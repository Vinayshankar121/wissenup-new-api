package com.wissenup.domain.timetable.dto; import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalTime;
@Data public class TimetablePeriodRequest { @NotBlank private String periodName; @NotNull private LocalTime startTime; @NotNull private LocalTime endTime; @NotNull private Boolean isBreak; @NotNull @Positive private Integer displayOrder; }
