package com.wissenup.domain.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="student_parents", uniqueConstraints =
    @UniqueConstraint(name="uq_student_parent", columnNames={"student_id","parent_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentParent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long studentParentId;
    @Column(name="organization_id", nullable=false) private Long organizationId;
    @Column(name="student_id", nullable=false) private Long studentId;
    @Column(name="parent_id", nullable=false) private Long parentId;
    @Column(nullable=false, length=20) private String relationship;
    @Column(name="is_primary", nullable=false) @Builder.Default private Boolean isPrimary=true;
}
