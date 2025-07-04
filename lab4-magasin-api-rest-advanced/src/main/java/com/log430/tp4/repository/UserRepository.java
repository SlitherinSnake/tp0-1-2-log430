package com.log430.tp4.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.log430.tp4.model.User;

/**
 * Interface d’accès aux données des utilisateurs.
 * Inclut les opérations CRUD et deux méthodes utiles :
 * - findByUsername : récupération d’un utilisateur pour l’authentification
 * - existsByUsername : vérification de l’unicité du login
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Recherche un utilisateur par son login
    Optional<User> findByUsername(String username);
    // Vérifie si un login est déjà utilisé
    Boolean existsByUsername(String username);
} 