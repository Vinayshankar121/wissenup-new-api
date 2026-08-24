package com.wissenup.domain.staff.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="departments") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Department {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long departmentId;
 @Column(name="organization_id",nullable=false) private Long organizationId;
 @Column(nullable=false) private String name; @Column(name="department_type") private String departmentType;
 @Builder.Default private String status="ACTIVE"; @Column(name="created_at") private LocalDateTime createdAt; @Column(name="created_by") private Long createdBy;
 @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();}
}
