package com.log430.tp4.presentation.api;

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

/**
 * REST API controller for inventory management.
 * Provides endpoints for inventory operations.
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all active inventory items.
     */
    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        log.info("API call: getAllItems");
        List<InventoryItem> items = inventoryService.getAllActiveItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get inventory item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        log.info("API call: getItemById with id {}", id);
        return inventoryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new inventory item.
     */
    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody CreateItemRequest request) {
        log.info("API call: createItem with name {}", request.nom());
        try {
            InventoryItem item = inventoryService.createItem(
                    request.nom(),
                    request.categorie(),
                    request.prix(),
                    request.stockCentral()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update inventory item.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(
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
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update stock levels.
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<InventoryItem> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer newStock = request.get("stock");
            if (newStock == null) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.updateStock(id, newStock);
            return ResponseEntity.ok(item);
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
    public ResponseEntity<InventoryItem> increaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("API call: increaseStock for id {} by {}", id, request.get("quantity"));
        try {
            Integer quantity = request.get("quantity");
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.increaseStock(id, quantity);
            return ResponseEntity.ok(item);
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
    public ResponseEntity<InventoryItem> decreaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("API call: decreaseStock for id {} by {}", id, request.get("quantity"));
        try {
            Integer quantity = request.get("quantity");
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.decreaseStock(id, quantity);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get items needing restock.
     */
    @GetMapping("/restock-needed")
    public ResponseEntity<List<InventoryItem>> getItemsNeedingRestock() {
        List<InventoryItem> items = inventoryService.getItemsNeedingRestock();
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by category.
     */
    @GetMapping("/category/{categorie}")
    public ResponseEntity<List<InventoryItem>> getItemsByCategory(@PathVariable String categorie) {
        List<InventoryItem> items = inventoryService.getItemsByCategory(categorie);
        return ResponseEntity.ok(items);
    }

    /**
     * Search items by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String name) {
        List<InventoryItem> items = inventoryService.searchItemsByName(name);
        return ResponseEntity.ok(items);
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateItem(@PathVariable Long id) {
        try {
            inventoryService.deactivateItem(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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
