package com.wissenup.domain.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="parents", uniqueConstraints =
    @UniqueConstraint(name="uq_parents_org_phone", columnNames={"organization_id","phone_number"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Parent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long parentId;
    @Column(name="organization_id", nullable=false) private Long organizationId;
    @Column(name="user_id") private Long userId;
    @Column(name="first_name", nullable=false, length=100) private String firstName;
    @Column(name="last_name", length=100) private String lastName;
    @Column(name="phone_number", nullable=false, length=30) private String phoneNumber;
    @Column(length=255) private String email;
    @Column(length=150) private String occupation;
    @Column(name="address_id") private Long addressId;
    @Column(nullable=false, length=20) @Builder.Default private String status="ACTIVE";
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="created_by", nullable=false) private Long createdBy;
    @PrePersist void create() { if (createdAt == null) createdAt=LocalDateTime.now(); }
}
