package com.wissenup.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="attendance_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AttendanceSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="attendance_session_id") private Long id;
 @Column(name="organization_id",nullable=false) private Long organizationId;
 @Column(name="academic_year_id",nullable=false) private Long academicYearId;
 @Column(name="class_id",nullable=false) private Long classId;
 @Column(name="section_id",nullable=false) private Long sectionId;
 @Column(name="attendance_date",nullable=false) private LocalDate attendanceDate;
 @Column(name="session_type",nullable=false) private String sessionType;
 @Column(nullable=false) private String status;
 @Column(name="marked_by_user_id",nullable=false) private Long markedByUserId;
 @Column(name="marked_by_staff_id") private Long markedByStaffId;
 @Column(name="marked_by_role",nullable=false) private String markedByRole;
 private String remarks;
 @Column(name="submitted_at") private LocalDateTime submittedAt;
 @Column(name="actioned_by_user_id") private Long actionedByUserId;
 @Column(name="actioned_by_staff_id") private Long actionedByStaffId;
 @Column(name="actioned_by_role") private String actionedByRole;
 @Column(name="actioned_at") private LocalDateTime actionedAt;
 @Column(name="rejection_reason") private String rejectionReason;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @Version private Long version;
 @PrePersist void create(){if(status==null)status="DRAFT";if(createdAt==null)createdAt=LocalDateTime.now();if(updatedAt==null)updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
}
