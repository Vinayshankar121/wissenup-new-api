package com.wissenup.domain.student.repository;
import com.wissenup.domain.student.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ParentRepository extends JpaRepository<Parent,Long> {
    Optional<Parent> findByOrganizationIdAndPhoneNumber(Long organizationId, String phoneNumber);
    Optional<Parent> findFirstByEmailIgnoreCase(String email);
    Optional<Parent> findByUserIdAndOrganizationId(Long userId, Long organizationId);
}
