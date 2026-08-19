package com.wissenup.domain.identity.repository;

import com.wissenup.domain.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (across all organizations).
     * Email is globally unique.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Find user by ID and organization (tenant isolation).
     */
    Optional<User> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    /**
     * Get all users in an organization.
     */
    List<User> findAllByOrganizationId(Long organizationId);
}
