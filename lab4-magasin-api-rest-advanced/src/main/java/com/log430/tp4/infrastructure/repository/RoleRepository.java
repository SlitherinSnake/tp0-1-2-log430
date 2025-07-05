package com.log430.tp4.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp4.domain.personnel.Role;

/**
 * Repository interface for Role entities.
 * Provides data access operations for role management.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name.
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role name exists.
     */
    boolean existsByName(String name);
}
