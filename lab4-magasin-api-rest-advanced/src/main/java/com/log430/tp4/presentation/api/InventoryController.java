package com.log430.tp4.presentation.api;

import java.util.ArrayList;
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

import com.log430.tp4.application.service.InventoryService;
import com.log430.tp4.domain.inventory.InventoryItem;
import com.log430.tp4.presentation.api.dto.InventoryItemDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API controller for inventory management.
 * Provides endpoints for inventory operations.
 */
@Tag(name = "Inventaire", description = "Gestion de l'inventaire et des produits")
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private static final String QUANTITY_PARAM = "quantity";

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all active inventory items.
     */
    @Operation(summary = "Lister tous les produits", description = "Retourne tous les articles actifs de l'inventaire.")
    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAllItems() {
        log.info("API call: getAllItems");
        try {
            List<InventoryItem> items = inventoryService.getAllActiveItems();
            log.info("Found {} active inventory items", items.size());
            
            List<InventoryItemDto> dtos = items.stream()
                    .map(InventoryItemDto::fromEntity)
                    .toList();
            
            log.info("Successfully converted {} items to DTOs", dtos.size());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("Cache-Control", "no-cache")
                    .body(dtos);
        } catch (Exception e) {
            log.error("Error in getAllItems: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all inventory items as a direct list (alternative endpoint).
     */
    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemDto>> getAllItemsDirect() {
        log.info("API call: getAllItemsDirect");
        try {
            List<InventoryItem> items = inventoryService.getAllActiveItems();
            log.info("Found {} active inventory items", items.size());
            
            List<InventoryItemDto> dtos = new ArrayList<>();
            for (InventoryItem item : items) {
                dtos.add(new InventoryItemDto(item));
            }
            
            log.info("Successfully converted {} items to DTOs", dtos.size());
            return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .body(dtos);
        } catch (Exception e) {
            log.error("Error in getAllItemsDirect: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get inventory item by ID.
     */
    @Operation(summary = "Obtenir un produit par ID", description = "Retourne un article d'inventaire par son identifiant.")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getItemById(@PathVariable Long id) {
        log.info("API call: getItemById with id {}", id);
        return inventoryService.getItemById(id)
                .map(item -> ResponseEntity.ok(InventoryItemDto.fromEntity(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new inventory item.
     */
    @Operation(summary = "Créer un produit", description = "Crée un nouvel article d'inventaire.")
    @PostMapping
    public ResponseEntity<InventoryItemDto> createItem(@RequestBody CreateItemRequest request) {
        log.info("API call: createItem with name {}", request.nom());
        try {
            InventoryItem item = inventoryService.createItem(
                    request.nom(),
                    request.categorie(),
                    request.prix(),
                    request.stockCentral()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(InventoryItemDto.fromEntity(item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update inventory item.
     */
    @Operation(summary = "Mettre à jour un produit", description = "Met à jour les informations d'un article d'inventaire.")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(
            @PathVariable Long id,
            @RequestBody UpdateItemRequest request) {
        try {
            InventoryItem item = inventoryService.updateItem(
                    id,
                    request.nom(),
                    request.categorie(),
                    request.prix(),
                    request.description()
            );
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update stock levels.
     */
    @Operation(summary = "Mettre à jour le stock d'un produit", description = "Met à jour le stock central d'un article d'inventaire.")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<InventoryItemDto> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer newStock = request.get("stock");
            if (newStock == null) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.updateStock(id, newStock);
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Increase stock (receiving inventory).
     */
    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<InventoryItemDto> increaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("API call: increaseStock for id {} by {}", id, request.get(QUANTITY_PARAM));
        try {
            Integer quantity = request.get(QUANTITY_PARAM);
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.increaseStock(id, quantity);
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Decrease stock (sales or transfers).
     */
    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<InventoryItemDto> decreaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("API call: decreaseStock for id {} by {}", id, request.get(QUANTITY_PARAM));
        try {
            Integer quantity = request.get(QUANTITY_PARAM);
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.decreaseStock(id, quantity);
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get items needing restock.
     */
    @GetMapping("/restock-needed")
    public ResponseEntity<List<InventoryItemDto>> getItemsNeedingRestock() {
        List<InventoryItem> items = inventoryService.getItemsNeedingRestock();
        List<InventoryItemDto> dtos = items.stream()
                .map(InventoryItemDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get items by category.
     */
    @GetMapping("/category/{categorie}")
    public ResponseEntity<List<InventoryItemDto>> getItemsByCategory(@PathVariable String categorie) {
        List<InventoryItem> items = inventoryService.getItemsByCategory(categorie);
        List<InventoryItemDto> dtos = items.stream()
                .map(InventoryItemDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Search items by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<InventoryItemDto>> searchItems(@RequestParam String name) {
        List<InventoryItem> items = inventoryService.searchItemsByName(name);
        List<InventoryItemDto> dtos = items.stream()
                .map(InventoryItemDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all distinct categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = inventoryService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get total inventory value.
     */
    @GetMapping("/total-value")
    public ResponseEntity<Map<String, Double>> getTotalInventoryValue() {
        Double totalValue = inventoryService.calculateTotalInventoryValue();
        return ResponseEntity.ok(Map.of("totalValue", totalValue != null ? totalValue : 0.0));
    }

    /**
     * Deactivate inventory item.
     */
    @Operation(summary = "Supprimer (désactiver) un produit", description = "Désactive un article d'inventaire par son identifiant.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateItem(@PathVariable Long id) {
        try {
            inventoryService.deactivateItem(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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
            "message", "API is working"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get count of inventory items.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getItemCount() {
        log.info("API call: getItemCount");
        try {
            List<InventoryItem> items = inventoryService.getAllActiveItems();
            Map<String, Object> response = Map.of(
                "count", items.size(),
                "status", "OK"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting item count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get inventory items in batches for debugging.
     */
    @GetMapping("/batch")
    public ResponseEntity<List<InventoryItemDto>> getItemsBatch(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "5") int size) {
        log.info("API call: getItemsBatch start={}, size={}", start, size);
        try {
            List<InventoryItem> allItems = inventoryService.getAllActiveItems();
            log.info("Total items available: {}", allItems.size());
            
            int endIndex = Math.min(start + size, allItems.size());
            List<InventoryItem> batchItems = allItems.subList(start, endIndex);
            log.info("Processing batch from {} to {}, {} items", start, endIndex, batchItems.size());
            
            List<InventoryItemDto> dtos = new ArrayList<>();
            for (int i = 0; i < batchItems.size(); i++) {
                InventoryItem item = batchItems.get(i);
                try {
                    log.info("Converting item {} (ID: {})", i + start, item.getId());
                    InventoryItemDto dto = InventoryItemDto.fromEntity(item);
                    dtos.add(dto);
                } catch (Exception e) {
                    log.error("Error converting item {} (ID: {}) to DTO: {}", i + start, item.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Successfully converted {} items in batch", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error in getItemsBatch: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Request DTOs
    public record CreateItemRequest(
            String nom,
            String categorie,
            Double prix,
            Integer stockCentral
    ) {}

    public record UpdateItemRequest(
            String nom,
            String categorie,
            Double prix,
            String description
    ) {}
}
