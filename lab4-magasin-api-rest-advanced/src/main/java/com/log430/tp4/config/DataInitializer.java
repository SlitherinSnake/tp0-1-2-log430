package com.log430.tp4.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp4.model.Role;
import com.log430.tp4.model.Role.ERole;
import com.log430.tp4.model.User;
import com.log430.tp4.repository.RoleRepository;
import com.log430.tp4.repository.UserRepository;

/**
 * DataInitializer lance du « code d’initialisation » au démarrage de l’application.
 * Objectif : s’assurer qu’il existe toujours au moins trois rôles (ADMIN, EMPLOYEE, VIEWER)
 * et deux comptes utilisateurs de base (admin / user).
 *
 * Spring Boot exécute la méthode run() juste après avoir lancé le serveur.
 * Si tout est déjà présent dans la base, rien n’est créé ; 
 * sinon, le composant ajoute automatiquement ce qu’il manque.
 */
@Component // Rend la classe détectable par Spring (injection automatique)
public class DataInitializer implements CommandLineRunner {

    /*  Dépendances injectées automatiquement par Spring  */

    @Autowired
    private RoleRepository roleRepository; // Accès aux rôles en base

    @Autowired
    private UserRepository userRepository; // Accès aux comptes utilisateurs

    @Autowired
    private PasswordEncoder passwordEncoder; // Outil pour chiffrer les mots de passe

    /**
     * Méthode exécutée une seule fois, au lancement.
     * Les trois appels se font dans un ordre logique :
     * 1) création des rôles s’ils n’existent pas,
     * 2) création d’un compte admin,
     * 3) création d’un compte utilisateur « standard ».
     */
    @Override
    @Transactional // Garantit que chaque bloc crée tout ou rien (cohérence base)
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initRoles();
        // Create admin user if it doesn't exist
        createAdminIfNotFound();
        // Create regular user if it doesn't exist
        createRegularUserIfNotFound();
    }

    /** Ajoute les trois rôles de base si la table des rôles est vide. */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            Role empRole = new Role(ERole.ROLE_EMPLOYEE);
            Role viewerRole = new Role(ERole.ROLE_VIEWER);

            roleRepository.save(adminRole);
            roleRepository.save(empRole);
            roleRepository.save(viewerRole);
            
            System.out.println("Rôles initialisés avec succès");
        }
    }

    /** Crée le compte « admin » avec le rôle ADMIN si aucun n’existe déjà. */
    @Transactional
    private void createAdminIfNotFound() {
        if (!userRepository.existsByUsername("admin")) {
            try {
                // Création du compte et chiffrement du mot de passe
                User admin = new User(
                        "admin",
                        passwordEncoder.encode("admin123")
                );

                // Récupère le rôle ADMIN depuis la base
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Erreur : le rôle d’administrateur n’est pas trouvé."));
                
                // Associe le rôle au compte avant enregistrement
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                
                // Set le role pour l'admin user
                admin.setRoles(roles);
                
                // Sauvegarde en base
                userRepository.save(admin);
                
                System.out.println("L'utilisateur administrateur a été créé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de la création de l'utilisateur administrateur : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /** Crée le compte « user » avec le rôle VIEWER si aucun n’existe déjà. */
    @Transactional
    private void createRegularUserIfNotFound() {
        if (!userRepository.existsByUsername("user")) {
            try {
                // Crée un nouveau utilisateur régulier
                User regularUser = new User(
                        "user",
                        passwordEncoder.encode("user123")
                );

                // Trouve le viewer role
                Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                        .orElseThrow(() -> new RuntimeException("Erreur : le rôle de spectateur est introuvable."));
                
                // Crée un ensemble avec le role viewer
                Set<Role> roles = new HashSet<>();
                roles.add(viewerRole);
                
                // Set le role pour l'utilisateur régulier
                regularUser.setRoles(roles);
                
                // Sauvegarde l'utilisateur régulier
                userRepository.save(regularUser);
                
                System.out.println("Utilisateur régulier créé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de la création d'un utilisateur régulier : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 