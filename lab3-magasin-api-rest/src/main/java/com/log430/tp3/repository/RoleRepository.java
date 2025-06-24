package com.log430.tp3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.log430.tp3.model.Role;
import com.log430.tp3.model.Role.ERole;

/**
 * Interface d’accès aux données des rôles.
 * Hérite des opérations CRUD standard de JpaRepository.
 * Fournit une méthode pour rechercher un rôle par son nom.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Recherche un rôle par son nom (ex. ROLE_ADMIN)
    Optional<Role> findByName(ERole name);
} 