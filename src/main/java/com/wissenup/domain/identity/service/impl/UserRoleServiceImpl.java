package com.wissenup.domain.identity.service.impl;

import com.wissenup.domain.identity.dto.CreateUserRoleRequest;
import com.wissenup.domain.identity.dto.UserRoleDto;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.mapper.UserRoleMapper;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.identity.service.UserRoleService;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserRoleMapper userRoleMapper;

    public UserRoleServiceImpl(UserRoleRepository userRoleRepository, UserRepository userRepository,
                              UserRoleMapper userRoleMapper) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public UserRoleDto assignRoleToUser(CreateUserRoleRequest request, Long organizationId) {
        User user = userRepository.findByUserIdAndOrganizationId(request.getUserId(), organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", request.getUserId()));

        if (userRoleRepository.existsByUserIdAndRoleId(request.getUserId(), request.getRoleId())) {
            throw ConflictException.duplicate("UserRole", "user-role",
                String.format("userId=%d, roleId=%d", request.getUserId(), request.getRoleId()));
        }

        UserRole userRole = userRoleMapper.toEntity(request);
        UserRole savedUserRole = userRoleRepository.save(userRole);
        log.info("Role {} assigned to user {} in organization: {}",
            request.getRoleId(), request.getUserId(), organizationId);
        return userRoleMapper.toDto(savedUserRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleDto> getUserRoles(Long userId, Long organizationId) {
        userRepository.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        return userRoles.stream()
            .map(userRoleMapper::toDto)
            .toList();
    }

    @Override
    public void removeRoleFromUser(Long userRoleId, Long organizationId) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("UserRole", userRoleId));

        User user = userRepository.findByUserIdAndOrganizationId(userRole.getUserId(), organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userRole.getUserId()));

        userRoleRepository.delete(userRole);
        log.info("Role {} removed from user {} in organization: {}",
            userRole.getRoleId(), userRole.getUserId(), organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, Long roleId, Long organizationId) {
        userRepository.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        return userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
    }
}
