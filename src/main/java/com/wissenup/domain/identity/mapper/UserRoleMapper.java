package com.wissenup.domain.identity.mapper;

import com.wissenup.domain.identity.dto.CreateUserRoleRequest;
import com.wissenup.domain.identity.dto.UserRoleDto;
import com.wissenup.domain.identity.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleDto toDto(UserRole userRole) {
        if (userRole == null) return null;
        return UserRoleDto.builder()
            .userRoleId(userRole.getUserRoleId())
            .userId(userRole.getUserId())
            .roleId(userRole.getRoleId())
            .status(userRole.getStatus())
            .createdAt(userRole.getCreatedAt())
            .createdBy(userRole.getCreatedBy())
            .build();
    }

    public UserRole toEntity(CreateUserRoleRequest request) {
        if (request == null) return null;
        return UserRole.builder()
            .userId(request.getUserId())
            .roleId(request.getRoleId())
            .createdBy(request.getCreatedBy())
            .build();
    }
}
