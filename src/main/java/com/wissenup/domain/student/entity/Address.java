package com.wissenup.domain.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="addresses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long addressId;
    @Column(name="organization_id", nullable=false) private Long organizationId;
    @Column(name="address_line1", length=255) private String addressLine1;
    @Column(name="address_line2", length=255) private String addressLine2;
    @Column(length=100) private String locality;
    @Column(length=100) private String city;
    @Column(length=100) private String state;
    @Column(length=100) private String country;
    @Column(name="zip_code", length=20) private String zipCode;
    @Column(nullable=false, length=20) @Builder.Default private String status="ACTIVE";
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @PrePersist void create() { if (createdAt == null) createdAt=LocalDateTime.now(); }
}
