package com.wissenup.domain.timetable.dto; import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ClassTimetableRequest { @NotNull private Long academicYearId; @NotNull private Long classId; @NotNull private Long sectionId; }
