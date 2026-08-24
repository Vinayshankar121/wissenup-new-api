package com.wissenup.domain.staff.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="designations") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Designation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long designationId;
 @Column(name="organization_id",nullable=false) private Long organizationId; @Column(name="department_id",nullable=false) private Long departmentId;
 @Column(nullable=false) private String name; private String description; @Builder.Default private String status="ACTIVE";
 @Column(name="created_at") private LocalDateTime createdAt; @Column(name="created_by") private Long createdBy;
 @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();}
}
