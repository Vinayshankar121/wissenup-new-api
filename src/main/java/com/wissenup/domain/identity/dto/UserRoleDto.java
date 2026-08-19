package com.wissenup.domain.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDto {

    private Long userRoleId;
    private Long userId;
    private Long roleId;
    private String status;
    private LocalDateTime createdAt;
    private Long createdBy;
}
