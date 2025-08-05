package com.log430.tp7.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp7.domain.personnel.Personnel;

/**
 * Repository interface for Personnel entities.
 * Provides data access operations for personnel management.
 */
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    /**
     * Find all active personnel.
     */
    List<Personnel> findByIsActiveTrue();

    /**
     * Find personnel by username for authentication.
     */
    Optional<Personnel> findByUsernameAndIsActiveTrue(String username);

    /**
     * Find personnel by employee identifier.
     */
    Optional<Personnel> findByIdentifiantAndIsActiveTrue(String identifiant);

    /**
     * Find personnel with login access.
     */
    @Query("SELECT p FROM Personnel p WHERE p.username IS NOT NULL AND p.password IS NOT NULL AND p.isActive = true")
    List<Personnel> findPersonnelWithLoginAccess();

    /**
     * Find personnel by role name.
     */
    @Query("SELECT p FROM Personnel p JOIN p.roles r WHERE r.name = :roleName AND p.isActive = true")
    List<Personnel> findByRoleName(@Param("roleName") String roleName);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if identifiant exists.
     */
    boolean existsByIdentifiant(String identifiant);
}
