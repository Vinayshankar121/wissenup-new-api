package com.wissenup.domain.identity.repository;

import com.wissenup.domain.identity.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Find roles for a user.
     * A user can have multiple roles (typically one primary).
     */
    Optional<UserRole> findByUserId(Long userId);

    /**
     * Find all roles for a user.
     */
    List<UserRole> findAllByUserId(Long userId);

    /**
     * Check if a user-role assignment exists.
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Find by user and role.
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
}
