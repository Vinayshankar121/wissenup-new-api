package com.wissenup.domain.staff.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="staff") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Staff {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long staffId;
 @Column(name="organization_id",nullable=false) private Long organizationId; @Column(name="user_id",nullable=false) private Long userId;
 @Column(name="department_id",nullable=false) private Long departmentId; @Column(name="designation_id",nullable=false) private Long designationId; @Column(name="address_id") private Long addressId;
 @Column(name="employee_code",nullable=false) private String employeeCode; @Column(name="first_name",nullable=false) private String firstName; @Column(name="last_name") private String lastName;
 private String gender; @Column(name="date_of_birth") private LocalDate dateOfBirth; @Column(name="joining_date",nullable=false) private LocalDate joiningDate;
 private String qualification; private BigDecimal experience; private String email; @Column(name="phone_number") private String phoneNumber; @Column(name="role_name") private String roleName;
 @Builder.Default private String status="ACTIVE"; @Column(name="created_at") private LocalDateTime createdAt; @Column(name="created_by") private Long createdBy;
 @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();}
}
