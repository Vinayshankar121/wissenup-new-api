package com.wissenup.domain.identity.service.impl;

import com.wissenup.domain.identity.dto.LoginResponse;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.exception.AuthenticationException;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.student.repository.ParentRepository;
import com.wissenup.domain.identity.service.AuthService;
import com.wissenup.domain.identity.service.OtpService;
import com.wissenup.domain.identity.service.ParentAccountService;
import com.wissenup.shared.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ParentAccountService parentAccountService;
    private final RoleRepository roleRepository;
    private final ParentRepository parentRepository;

    public AuthServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository,
                          OtpService otpService, JwtService jwtService, PasswordEncoder passwordEncoder,
                          ParentAccountService parentAccountService, RoleRepository roleRepository,
                          ParentRepository parentRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.parentAccountService = parentAccountService;
        this.roleRepository = roleRepository;
        this.parentRepository = parentRepository;
    }

    @Override
    public void initiateLogin(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseGet(() -> parentAccountService.provisionLegacyIfCredentialsMatch(normalizedEmail, password));

        if (user == null) throw AuthenticationException.invalidCredentials();

        if (!"ACTIVE".equals(user.getStatus()) || !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login attempt for inactive user: {}", email);
            throw AuthenticationException.invalidCredentials();
        }

        otpService.generateAndSendOtp(normalizedEmail);
        log.info("OTP sent to: {}", normalizedEmail);
    }

    @Override
    public LoginResponse verifyOtpAndLogin(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        otpService.verifyOtp(normalizedEmail, otp);
        otpService.clearOtp(normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(AuthenticationException::invalidCredentials);

        if (!"ACTIVE".equals(user.getStatus())) {
            throw AuthenticationException.invalidCredentials();
        }

        Optional<UserRole> userRole = userRoleRepository.findByUserId(user.getUserId());
        Long roleId = userRole.map(UserRole::getRoleId).orElse(null);
        String role = roleId == null ? null : roleRepository.findById(roleId).map(r -> r.getCode()).orElse(null);
        Long parentId = parentRepository.findByUserIdAndOrganizationId(user.getUserId(), user.getOrganizationId())
            .map(parent -> parent.getParentId()).orElse(null);

        String token = jwtService.generateToken(user.getUserId(), user.getOrganizationId(), roleId, user.getEmail());

        log.info("User logged in: {}", email);

        return LoginResponse.builder()
            .token(token)
            .userId(user.getUserId())
            .organizationId(user.getOrganizationId())
            .roleId(roleId)
            .role(role)
            .parentId(parentId)
            .email(user.getEmail())
            .build();
    }

    @Override
    public void resendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(AuthenticationException::invalidCredentials);

        if (!"ACTIVE".equals(user.getStatus())) {
            throw AuthenticationException.invalidCredentials();
        }

        otpService.generateAndSendOtp(normalizedEmail);
        log.info("OTP resent to: {}", normalizedEmail);
    }
}
