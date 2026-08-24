package com.wissenup.domain.identity.service;

import com.wissenup.domain.identity.entity.Role;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.student.entity.Parent;
import com.wissenup.domain.student.repository.ParentRepository;
import com.wissenup.domain.student.repository.StudentParentRepository;
import com.wissenup.domain.student.repository.StudentRepository;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentAccountService {
    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final RoleRepository roles;
    private final ParentRepository parents;
    private final StudentParentRepository studentParents;
    private final StudentRepository students;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User provision(Parent parent, String rawPassword, Long createdBy) {
        if (parent.getEmail() == null || parent.getEmail().isBlank() || rawPassword == null || rawPassword.isBlank()) return null;

        String email = parent.getEmail().trim().toLowerCase();
        User user = users.findByEmail(email).orElse(null);
        if (user != null && !user.getOrganizationId().equals(parent.getOrganizationId())) {
            throw ConflictException.duplicate("User", "email", email);
        }
        if (user == null) {
            user = users.save(User.builder()
                .organizationId(parent.getOrganizationId()).email(email).phoneNumber(parent.getPhoneNumber())
                .password(passwordEncoder.encode(rawPassword)).status("ACTIVE").createdBy(createdBy).build());
        }

        Role role = roles.findByCode("PARENT")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "PARENT"));
        if (!userRoles.existsByUserIdAndRoleId(user.getUserId(), role.getRoleId())) {
            userRoles.save(UserRole.builder().userId(user.getUserId()).roleId(role.getRoleId())
                .status("ACTIVE").createdBy(createdBy).build());
        }
        parent.setUserId(user.getUserId());
        parents.save(parent);
        return user;
    }

    /** Activates accounts for parent records created before login provisioning existed. */
    @Transactional
    public User provisionLegacyIfCredentialsMatch(String email, String password) {
        Parent parent = parents.findFirstByEmailIgnoreCase(email).orElse(null);
        if (parent == null || parent.getUserId() != null) return null;
        boolean dobMatches = studentParents.findAllByParentId(parent.getParentId()).stream()
            .map(link -> students.findById(link.getStudentId()).orElse(null))
            .anyMatch(student -> student != null && student.getDateOfBirth().toString().equals(password));
        return dobMatches ? provision(parent, password, parent.getCreatedBy()) : null;
    }
}
