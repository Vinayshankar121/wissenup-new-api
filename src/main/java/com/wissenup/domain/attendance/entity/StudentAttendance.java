package com.wissenup.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="student_attendance")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentAttendance {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="student_attendance_id") private Long id;
 @Column(name="organization_id",nullable=false) private Long organizationId;
 @Column(name="attendance_session_id",nullable=false) private Long attendanceSessionId;
 @Column(name="student_id",nullable=false) private Long studentId;
 @Column(name="attendance_status",nullable=false) private String attendanceStatus;
 private String remarks;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @Column(name="created_by",nullable=false) private Long createdBy;
 @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();if(updatedAt==null)updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
}
