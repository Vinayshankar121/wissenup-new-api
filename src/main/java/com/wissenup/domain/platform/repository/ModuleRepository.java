package com.wissenup.domain.platform.repository;

import com.wissenup.domain.platform.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByCode(String code);

    @Query("SELECT m FROM Module m WHERE m.is_active = :isActive")
    List<Module> findByIs_active(Boolean isActive);
}
