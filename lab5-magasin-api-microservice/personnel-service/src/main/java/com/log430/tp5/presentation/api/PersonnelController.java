package com.log430.tp5.presentation.api;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp5.application.service.PersonnelService;
import com.log430.tp5.domain.personnel.Personnel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API controller for personnel management.
 * Provides endpoints for personnel operations.
 */
@Tag(name = "Personnel", description = "Gestion du personnel et des utilisateurs")
@RestController
@RequestMapping("/api/personnel")
@CrossOrigin(origins = "*")
public class PersonnelController {
    
    private static final Logger log = LoggerFactory.getLogger(PersonnelController.class);
    
    private final PersonnelService personnelService;
    
    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }
    
    /**
     * Get all active personnel.
     */
    @Operation(summary = "Lister tout le personnel actif", description = "Retourne tout le personnel actif.")
    @GetMapping
    public ResponseEntity<List<Personnel>> getAllActivePersonnel() {
        log.info("API call: getAllActivePersonnel");
        try {
            List<Personnel> personnel = personnelService.getAllActivePersonnel();
            log.info("Found {} active personnel", personnel.size());
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            log.error("Error in getAllActivePersonnel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personnel by ID.
     */
    @Operation(summary = "Obtenir un personnel par ID", description = "Retourne un personnel par son identifiant.")
    @GetMapping("/{id}")
    public ResponseEntity<Personnel> getPersonnelById(@PathVariable Long id) {
        log.info("API call: getPersonnelById with id {}", id);
        try {
            return personnelService.getPersonnelById(id)
                    .map(personnel -> ResponseEntity.ok(personnel))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in getPersonnelById: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new personnel.
     */
    @Operation(summary = "Créer un personnel", description = "Crée un nouveau personnel.")
    @PostMapping
    public ResponseEntity<Personnel> createPersonnel(@RequestBody CreatePersonnelRequest request) {
        log.info("API call: createPersonnel with name {}", request.nom());
        try {
            Personnel personnel = personnelService.createPersonnel(
                    request.nom(),
                    request.identifiant()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(personnel);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request in createPersonnel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error in createPersonnel: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create personnel with login access.
     */
    @Operation(summary = "Créer un personnel avec accès", description = "Crée un nouveau personnel avec accès de connexion.")
    @PostMapping("/with-login")
    public ResponseEntity<Personnel> createPersonnelWithLogin(@RequestBody CreatePersonnelWithLoginRequest request) {
        log.info("API call: createPersonnelWithLogin with name {} and username {}", request.nom(), request.username());
        try {
            Personnel personnel = personnelService.createPersonnelWithLogin(
                    request.nom(),
                    request.identifiant(),
                    request.username(),
                    request.password()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(personnel);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request in createPersonnelWithLogin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error in createPersonnelWithLogin: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update personnel information.
     */
    @Operation(summary = "Mettre à jour un personnel", description = "Met à jour les informations d'un personnel.")
    @PutMapping("/{id}")
    public ResponseEntity<Personnel> updatePersonnel(
            @PathVariable Long id,
            @RequestBody UpdatePersonnelRequest request) {
        log.info("API call: updatePersonnel with id {}", id);
        try {
            Personnel personnel = personnelService.updatePersonnel(
                    id,
                    request.nom(),
                    request.identifiant(),
                    request.username()
            );
            return ResponseEntity.ok(personnel);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request in updatePersonnel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error in updatePersonnel: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Set password for personnel.
     */
    @Operation(summary = "Définir le mot de passe", description = "Définit ou change le mot de passe d'un personnel.")
    @PatchMapping("/{id}/password")
    public ResponseEntity<Personnel> setPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("API call: setPassword for personnel {}", id);
        try {
            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Personnel personnel = personnelService.setPassword(id, password);
            return ResponseEntity.ok(personnel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in setPassword: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Find personnel by username.
     */
    @Operation(summary = "Rechercher par nom d'utilisateur", description = "Trouve un personnel par son nom d'utilisateur.")
    @GetMapping("/by-username")
    public ResponseEntity<Personnel> findByUsername(@RequestParam String username) {
        log.info("API call: findByUsername with username {}", username);
        try {
            return personnelService.findByUsername(username)
                    .map(personnel -> ResponseEntity.ok(personnel))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in findByUsername: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Find personnel by identifiant.
     */
    @Operation(summary = "Rechercher par identifiant", description = "Trouve un personnel par son identifiant d'employé.")
    @GetMapping("/by-identifiant")
    public ResponseEntity<Personnel> findByIdentifiant(@RequestParam String identifiant) {
        log.info("API call: findByIdentifiant with identifiant {}", identifiant);
        try {
            return personnelService.findByIdentifiant(identifiant)
                    .map(personnel -> ResponseEntity.ok(personnel))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in findByIdentifiant: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personnel with login access.
     */
    @Operation(summary = "Lister le personnel avec accès", description = "Retourne le personnel ayant un accès de connexion.")
    @GetMapping("/with-login")
    public ResponseEntity<List<Personnel>> getPersonnelWithLoginAccess() {
        log.info("API call: getPersonnelWithLoginAccess");
        try {
            List<Personnel> personnel = personnelService.getPersonnelWithLoginAccess();
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            log.error("Error in getPersonnelWithLoginAccess: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Find personnel by role.
     */
    @Operation(summary = "Rechercher par rôle", description = "Trouve le personnel ayant un rôle spécifique.")
    @GetMapping("/by-role")
    public ResponseEntity<List<Personnel>> findByRoleName(@RequestParam String roleName) {
        log.info("API call: findByRoleName with role {}", roleName);
        try {
            List<Personnel> personnel = personnelService.findByRoleName(roleName);
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            log.error("Error in findByRoleName: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Deactivate personnel.
     */
    @Operation(summary = "Désactiver un personnel", description = "Désactive un personnel par son identifiant.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePersonnel(@PathVariable Long id) {
        log.info("API call: deactivatePersonnel with id {}", id);
        try {
            personnelService.deactivatePersonnel(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in deactivatePersonnel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Activate personnel.
     */
    @Operation(summary = "Activer un personnel", description = "Active un personnel par son identifiant.")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePersonnel(@PathVariable Long id) {
        log.info("API call: activatePersonnel with id {}", id);
        try {
            personnelService.activatePersonnel(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in activatePersonnel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all personnel (including inactive ones).
     */
    @Operation(summary = "Lister tout le personnel", description = "Retourne tout le personnel, actif et inactif.")
    @GetMapping("/all")
    public ResponseEntity<List<Personnel>> getAllPersonnel() {
        log.info("API call: getAllPersonnel");
        try {
            List<Personnel> personnel = personnelService.getAllPersonnel();
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            log.error("Error in getAllPersonnel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Check if personnel is active.
     */
    @Operation(summary = "Vérifier si personnel est actif", description = "Vérifie si un personnel est actif.")
    @GetMapping("/{id}/active")
    public ResponseEntity<Map<String, Boolean>> isPersonnelActive(@PathVariable Long id) {
        log.info("API call: isPersonnelActive with id {}", id);
        try {
            boolean isActive = personnelService.isPersonnelActive(id);
            return ResponseEntity.ok(Map.of("active", isActive));
        } catch (Exception e) {
            log.error("Error in isPersonnelActive: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personnel statistics.
     */
    @Operation(summary = "Obtenir les statistiques du personnel", description = "Retourne le nombre total et actif de personnel.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPersonnelStats() {
        log.info("API call: getPersonnelStats");
        try {
            long totalPersonnel = personnelService.getPersonnelCount();
            long activePersonnel = personnelService.getActivePersonnelCount();
            
            Map<String, Object> stats = Map.of(
                "totalPersonnel", totalPersonnel,
                "activePersonnel", activePersonnel,
                "inactivePersonnel", totalPersonnel - activePersonnel
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error in getPersonnelStats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Simple test endpoint to verify API is working.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("API call: test endpoint");
        Map<String, Object> response = Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "Personnel API is working"
        );
        return ResponseEntity.ok(response);
    }
    
    // Request DTOs
    public record CreatePersonnelRequest(
            String nom,
            String identifiant
    ) {}
    
    public record CreatePersonnelWithLoginRequest(
            String nom,
            String identifiant,
            String username,
            String password
    ) {}
    
    public record UpdatePersonnelRequest(
            String nom,
            String identifiant,
            String username
    ) {}
}
