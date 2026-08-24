package com.wissenup.domain.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "students", uniqueConstraints =
    @UniqueConstraint(name = "uq_students_org_admission", columnNames = {"organization_id", "admission_no"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long studentId;
    @Column(name="organization_id", nullable=false) private Long organizationId;
    @Column(name="admission_no", nullable=false, length=50) private String admissionNo;
    @Column(name="first_name", nullable=false, length=100) private String firstName;
    @Column(name="last_name", length=100) private String lastName;
    @Column(nullable=false, length=20) private String gender;
    @Column(name="date_of_birth", nullable=false) private LocalDate dateOfBirth;
    @Column(name="blood_group", length=10) private String bloodGroup;
    @Column(name="admission_date", nullable=false) private LocalDate admissionDate;
    @Column(nullable=false, length=20) @Builder.Default private String status = "ACTIVE";
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="created_by", nullable=false) private Long createdBy;
    @PrePersist void create() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
