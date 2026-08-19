package com.wissenup.domain.identity.mapper;

import com.wissenup.domain.identity.dto.CreateUserRequest;
import com.wissenup.domain.identity.dto.UpdateUserRequest;
import com.wissenup.domain.identity.dto.UserDto;
import com.wissenup.domain.identity.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
            .userId(user.getUserId())
            .organizationId(user.getOrganizationId())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .createdBy(user.getCreatedBy())
            .updatedAt(user.getUpdatedAt())
            .updatedBy(user.getUpdatedBy())
            .build();
    }

    public User toEntity(CreateUserRequest request, Long organizationId) {
        if (request == null) return null;
        return User.builder()
            .organizationId(organizationId)
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .password(passwordEncoder.encode(request.getPassword()))
            .status(request.getStatus() != null ? request.getStatus() : "PENDING")
            .createdBy(request.getCreatedBy())
            .updatedBy(request.getUpdatedBy())
            .build();
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        if (request == null) return;
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getUpdatedBy() != null) {
            user.setUpdatedBy(request.getUpdatedBy());
        }
    }
}
