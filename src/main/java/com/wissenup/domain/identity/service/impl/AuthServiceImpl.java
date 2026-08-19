package com.wissenup.domain.identity.service.impl;

import com.wissenup.domain.identity.dto.LoginResponse;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.exception.AuthenticationException;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.identity.service.AuthService;
import com.wissenup.domain.identity.service.OtpService;
import com.wissenup.shared.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository,
                          OtpService otpService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @Override
    public void initiateLogin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(AuthenticationException::invalidCredentials);

        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("Login attempt for inactive user: {}", email);
            throw AuthenticationException.invalidCredentials();
        }

        otpService.generateAndSendOtp(email);
        log.info("OTP sent to: {}", email);
    }

    @Override
    public LoginResponse verifyOtpAndLogin(String email, String otp) {
        otpService.verifyOtp(email, otp);
        otpService.clearOtp(email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(AuthenticationException::invalidCredentials);

        if (!"ACTIVE".equals(user.getStatus())) {
            throw AuthenticationException.invalidCredentials();
        }

        Optional<UserRole> userRole = userRoleRepository.findByUserId(user.getUserId());
        Long roleId = userRole.map(UserRole::getRoleId).orElse(null);

        String token = jwtService.generateToken(user.getUserId(), user.getOrganizationId(), roleId, user.getEmail());

        log.info("User logged in: {}", email);

        return LoginResponse.builder()
            .token(token)
            .userId(user.getUserId())
            .organizationId(user.getOrganizationId())
            .roleId(roleId)
            .email(user.getEmail())
            .build();
    }

    @Override
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(AuthenticationException::invalidCredentials);

        if (!"ACTIVE".equals(user.getStatus())) {
            throw AuthenticationException.invalidCredentials();
        }

        otpService.generateAndSendOtp(email);
        log.info("OTP resent to: {}", email);
    }
}
