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

import com.log430.tp5.application.service.StoreService;
import com.log430.tp5.domain.store.Store;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API controller for store management.
 * Provides endpoints for store operations.
 */
@Tag(name = "Magasins", description = "Gestion des magasins")
@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {
    
    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Get all active stores.
     */
    @Operation(summary = "Lister tous les magasins actifs", description = "Retourne tous les magasins actifs.")
    @GetMapping
    public ResponseEntity<List<Store>> getAllActiveStores() {
        log.info("API call: getAllActiveStores");
        try {
            List<Store> stores = storeService.getAllActiveStores();
            log.info("Found {} active stores", stores.size());
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            log.error("Error in getAllActiveStores: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get store by ID.
     */
    @Operation(summary = "Obtenir un magasin par ID", description = "Retourne un magasin par son identifiant.")
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        log.info("API call: getStoreById with id {}", id);
        try {
            return storeService.getStoreById(id)
                    .map(store -> ResponseEntity.ok(store))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in getStoreById: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new store.
     */
    @Operation(summary = "Créer un magasin", description = "Crée un nouveau magasin.")
    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody CreateStoreRequest request) {
        log.info("API call: createStore with name {}", request.nom());
        try {
            Store store = storeService.createStore(
                    request.nom(),
                    request.quartier(),
                    request.adresse(),
                    request.telephone()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(store);
        } catch (Exception e) {
            log.error("Error in createStore: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update store information.
     */
    @Operation(summary = "Mettre à jour un magasin", description = "Met à jour les informations d'un magasin.")
    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(
            @PathVariable Long id,
            @RequestBody UpdateStoreRequest request) {
        log.info("API call: updateStore with id {}", id);
        try {
            Store store = storeService.updateStore(
                    id,
                    request.nom(),
                    request.quartier(),
                    request.adresse(),
                    request.telephone()
            );
            return ResponseEntity.ok(store);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in updateStore: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Find store by name.
     */
    @Operation(summary = "Rechercher un magasin par nom", description = "Trouve un magasin par son nom exact.")
    @GetMapping("/by-name")
    public ResponseEntity<Store> findStoreByName(@RequestParam String nom) {
        log.info("API call: findStoreByName with name {}", nom);
        try {
            return storeService.findStoreByName(nom)
                    .map(store -> ResponseEntity.ok(store))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in findStoreByName: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Find stores by quartier.
     */
    @Operation(summary = "Rechercher des magasins par quartier", description = "Trouve tous les magasins d'un quartier.")
    @GetMapping("/by-quartier")
    public ResponseEntity<List<Store>> findStoresByQuartier(@RequestParam String quartier) {
        log.info("API call: findStoresByQuartier with quartier {}", quartier);
        try {
            List<Store> stores = storeService.findStoresByQuartier(quartier);
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            log.error("Error in findStoresByQuartier: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search stores by name containing text.
     */
    @Operation(summary = "Rechercher des magasins", description = "Recherche des magasins dont le nom contient le texte spécifié.")
    @GetMapping("/search")
    public ResponseEntity<List<Store>> searchStoresByName(@RequestParam String nom) {
        log.info("API call: searchStoresByName with name containing '{}'", nom);
        try {
            List<Store> stores = storeService.searchStoresByName(nom);
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            log.error("Error in searchStoresByName: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deactivate store.
     */
    @Operation(summary = "Désactiver un magasin", description = "Désactive un magasin par son identifiant.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateStore(@PathVariable Long id) {
        log.info("API call: deactivateStore with id {}", id);
        try {
            storeService.deactivateStore(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in deactivateStore: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate store.
     */
    @Operation(summary = "Activer un magasin", description = "Active un magasin par son identifiant.")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateStore(@PathVariable Long id) {
        log.info("API call: activateStore with id {}", id);
        try {
            storeService.activateStore(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in activateStore: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all stores (including inactive ones).
     */
    @Operation(summary = "Lister tous les magasins", description = "Retourne tous les magasins, actifs et inactifs.")
    @GetMapping("/all")
    public ResponseEntity<List<Store>> getAllStores() {
        log.info("API call: getAllStores");
        try {
            List<Store> stores = storeService.getAllStores();
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            log.error("Error in getAllStores: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if store is active.
     */
    @Operation(summary = "Vérifier si un magasin est actif", description = "Vérifie si un magasin est actif.")
    @GetMapping("/{id}/active")
    public ResponseEntity<Map<String, Boolean>> isStoreActive(@PathVariable Long id) {
        log.info("API call: isStoreActive with id {}", id);
        try {
            boolean isActive = storeService.isStoreActive(id);
            return ResponseEntity.ok(Map.of("active", isActive));
        } catch (Exception e) {
            log.error("Error in isStoreActive: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get store statistics.
     */
    @Operation(summary = "Obtenir les statistiques des magasins", description = "Retourne le nombre total et actif de magasins.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStoreStats() {
        log.info("API call: getStoreStats");
        try {
            long totalStores = storeService.getStoreCount();
            long activeStores = storeService.getActiveStoreCount();
            
            Map<String, Object> stats = Map.of(
                "totalStores", totalStores,
                "activeStores", activeStores,
                "inactiveStores", totalStores - activeStores
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error in getStoreStats: {}", e.getMessage(), e);
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
            "message", "Store API is working"
        );
        return ResponseEntity.ok(response);
    }

    // Request DTOs
    public record CreateStoreRequest(
            String nom,
            String quartier,
            String adresse,
            String telephone
    ) {}

    public record UpdateStoreRequest(
            String nom,
            String quartier,
            String adresse,
            String telephone
    ) {}
}
