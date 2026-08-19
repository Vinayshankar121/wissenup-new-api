package com.wissenup.domain.identity.service.impl;

import com.wissenup.domain.identity.dto.CreateUserRequest;
import com.wissenup.domain.identity.dto.UpdateUserRequest;
import com.wissenup.domain.identity.dto.UserDto;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.mapper.UserMapper;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.service.UserService;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(CreateUserRequest request, Long organizationId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ConflictException.duplicate("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request, organizationId);
        User savedUser = userRepository.save(user);
        log.info("User created: {} in organization: {}", savedUser.getEmail(), organizationId);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId, Long organizationId) {
        User user = userRepository.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(Long organizationId) {
        List<User> users = userRepository.findAllByOrganizationId(organizationId);
        return users.stream()
            .map(userMapper::toDto)
            .toList();
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUserRequest request, Long organizationId) {
        User user = userRepository.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        userMapper.updateEntity(user, request);
        User updatedUser = userRepository.save(user);
        log.info("User updated: {} in organization: {}", updatedUser.getEmail(), organizationId);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId, Long organizationId) {
        User user = userRepository.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        userRepository.delete(user);
        log.info("User deleted: {} from organization: {}", user.getEmail(), organizationId);
    }
}
