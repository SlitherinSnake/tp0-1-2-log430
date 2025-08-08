package com.log430.tp7.presentation.api;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.log430.tp7.application.service.EventDrivenInventoryService;
import com.log430.tp7.application.service.InventoryQueryService;
import com.log430.tp7.application.service.InventoryService;
import com.log430.tp7.application.service.SagaInventoryService;
import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.presentation.api.dto.InventoryItemDto;
import com.log430.tp7.presentation.api.dto.StockReservationRequest;
import com.log430.tp7.presentation.api.dto.StockReservationResponse;
import com.log430.tp7.presentation.api.dto.StockVerificationRequest;
import com.log430.tp7.presentation.api.dto.StockVerificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    private final SagaInventoryService sagaInventoryService;
    private final EventDrivenInventoryService eventDrivenInventoryService;
    private final InventoryQueryService inventoryQueryService;

    public InventoryController(InventoryService inventoryService, SagaInventoryService sagaInventoryService,
                             EventDrivenInventoryService eventDrivenInventoryService,
                             InventoryQueryService inventoryQueryService) {
        this.inventoryService = inventoryService;
        this.sagaInventoryService = sagaInventoryService;
        this.eventDrivenInventoryService = eventDrivenInventoryService;
        this.inventoryQueryService = inventoryQueryService;
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
        try {
            return inventoryService.getItemById(id)
                    .map(item -> ResponseEntity.ok(InventoryItemDto.fromEntity(item)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in getItemById: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
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
        log.info("API call: updateItem with id {} and name {}", id, request.nom());
        try {
            InventoryItem item = inventoryService.updateItem(
                    id,
                    request.nom(),
                    request.categorie(),
                    request.prix(),
                    request.description()
            );
            log.info("Successfully updated item with id {}", id);
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (IllegalArgumentException e) {
            log.error("Item not found for update: id {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating item with id {}: {}", id, e.getMessage(), e);
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
        log.info("API call: updateStock for id {} with new stock {}", id, request.get("stock"));
        try {
            Integer newStock = request.get("stock");
            if (newStock == null) {
                log.error("Stock value is null for update request on item {}", id);
                return ResponseEntity.badRequest().build();
            }
            InventoryItem item = inventoryService.updateStock(id, newStock);
            log.info("Successfully updated stock for item {} to {}", id, newStock);
            return ResponseEntity.ok(InventoryItemDto.fromEntity(item));
        } catch (IllegalArgumentException e) {
            log.error("Item not found for stock update: id {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating stock for item {}: {}", id, e.getMessage(), e);
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
        log.info("API call: getItemsNeedingRestock");
        try {
            List<InventoryItem> items = inventoryService.getItemsNeedingRestock();
            log.info("Found {} items needing restock", items.size());
            List<InventoryItemDto> dtos = items.stream()
                    .map(InventoryItemDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error in getItemsNeedingRestock: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get items by category.
     */
    @GetMapping("/category/{categorie}")
    public ResponseEntity<List<InventoryItemDto>> getItemsByCategory(@PathVariable String categorie) {
        log.info("API call: getItemsByCategory with category {}", categorie);
        try {
            List<InventoryItem> items = inventoryService.getItemsByCategory(categorie);
            log.info("Found {} items for category {}", items.size(), categorie);
            List<InventoryItemDto> dtos = items.stream()
                    .map(InventoryItemDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error in getItemsByCategory for category {}: {}", categorie, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search items by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<InventoryItemDto>> searchItems(@RequestParam String name) {
        log.info("API call: searchItems with name '{}'", name);
        try {
            List<InventoryItem> items = inventoryService.searchItemsByName(name);
            log.info("Found {} items matching name '{}'", items.size(), name);
            List<InventoryItemDto> dtos = items.stream()
                    .map(InventoryItemDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error in searchItems for name '{}': {}", name, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all distinct categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.info("API call: getCategories");
        try {
            List<String> categories = inventoryService.getDistinctCategories();
            log.info("Found {} distinct categories", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Error in getCategories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get total inventory value.
     */
    @GetMapping("/total-value")
    public ResponseEntity<Map<String, Double>> getTotalInventoryValue() {
        log.info("API call: getTotalInventoryValue");
        try {
            Double totalValue = inventoryService.calculateTotalInventoryValue();
            log.info("Calculated total inventory value: {}", totalValue);
            return ResponseEntity.ok(Map.of("totalValue", totalValue != null ? totalValue : 0.0));
        } catch (Exception e) {
            log.error("Error calculating total inventory value: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deactivate inventory item.
     */
    @Operation(summary = "Supprimer (désactiver) un produit", description = "Désactive un article d'inventaire par son identifiant.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateItem(@PathVariable Long id) {
        log.info("API call: deactivateItem with id {}", id);
        try {
            inventoryService.deactivateItem(id);
            log.info("Successfully deactivated item with id {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Item not found for deactivation: id {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deactivating item with id {}: {}", id, e.getMessage(), e);
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
            "message", "Inventory API is working"
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

    /**
     * Get all inventory items (alternative endpoint for frontend compatibility).
     */
    @GetMapping("/items")
    public ResponseEntity<List<InventoryItemDto>> getItems() {
        log.info("API call: getItems");
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
            log.error("Error in getItems: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== CQRS QUERY ENDPOINTS ==========

    /**
     * Get inventory summary statistics using CQRS read model.
     */
    @Operation(summary = "Obtenir le résumé de l'inventaire", description = "Retourne les statistiques de l'inventaire via le modèle de lecture CQRS.")
    @GetMapping("/cqrs/summary")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        log.info("CQRS API call: getInventorySummary");
        try {
            InventoryQueryService.InventorySummary summary = inventoryQueryService.getInventorySummary();
            
            Map<String, Object> response = Map.of(
                "totalItems", summary.getTotalItems(),
                "totalStock", summary.getTotalStock(),
                "reservedStock", summary.getReservedStock(),
                "availableStock", summary.getAvailableStock(),
                "totalValue", inventoryQueryService.calculateTotalInventoryValue(),
                "availableValue", inventoryQueryService.calculateTotalAvailableValue(),
                "reservedValue", inventoryQueryService.calculateTotalReservedValue()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getInventorySummary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get items with reservations using CQRS read model.
     */
    @Operation(summary = "Obtenir les articles avec réservations", description = "Retourne les articles ayant des réservations actives via le modèle de lecture CQRS.")
    @GetMapping("/cqrs/with-reservations")
    public ResponseEntity<List<Map<String, Object>>> getItemsWithReservations() {
        log.info("CQRS API call: getItemsWithReservations");
        try {
            var items = inventoryQueryService.getItemsWithReservations();
            
            List<Map<String, Object>> response = items.stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("inventoryItemId", item.getInventoryItemId());
                    map.put("nom", item.getNom());
                    map.put("categorie", item.getCategorie());
                    map.put("prix", item.getPrix());
                    map.put("stockCentral", item.getStockCentral());
                    map.put("stockReserved", item.getStockReserved());
                    map.put("stockAvailable", item.getStockAvailable());
                    map.put("totalValue", item.getTotalValue());
                    map.put("needsRestock", item.isNeedsRestock());
                    return map;
                })
                .toList();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getItemsWithReservations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get reservation summary for a transaction using CQRS read model.
     */
    @Operation(summary = "Obtenir le résumé des réservations pour une transaction", description = "Retourne le résumé des réservations pour une transaction via le modèle de lecture CQRS.")
    @GetMapping("/cqrs/reservations/transaction/{transactionId}/summary")
    public ResponseEntity<Map<String, Object>> getReservationSummaryForTransaction(@PathVariable String transactionId) {
        log.info("CQRS API call: getReservationSummaryForTransaction for transaction: {}", transactionId);
        try {
            InventoryQueryService.ReservationSummary summary = 
                inventoryQueryService.getReservationSummaryForTransaction(transactionId);
            
            Map<String, Object> response = Map.of(
                "transactionId", transactionId,
                "activeCount", summary.getActiveCount(),
                "activeQuantity", summary.getActiveQuantity(),
                "confirmedCount", summary.getConfirmedCount(),
                "confirmedQuantity", summary.getConfirmedQuantity(),
                "releasedCount", summary.getReleasedCount(),
                "releasedQuantity", summary.getReleasedQuantity(),
                "totalCount", summary.getTotalCount(),
                "totalQuantity", summary.getTotalQuantity()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getReservationSummaryForTransaction for transaction: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active reservations for a transaction using CQRS read model.
     */
    @Operation(summary = "Obtenir les réservations actives pour une transaction", description = "Retourne les réservations actives pour une transaction via le modèle de lecture CQRS.")
    @GetMapping("/cqrs/reservations/transaction/{transactionId}/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveReservationsForTransaction(@PathVariable String transactionId) {
        log.info("CQRS API call: getActiveReservationsForTransaction for transaction: {}", transactionId);
        try {
            var reservations = inventoryQueryService.getActiveReservationsForTransaction(transactionId);
            
            List<Map<String, Object>> response = reservations.stream()
                .map(reservation -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reservationId", reservation.getReservationId());
                    map.put("inventoryItemId", reservation.getInventoryItemId());
                    map.put("transactionId", reservation.getTransactionId());
                    map.put("quantity", reservation.getQuantity());
                    map.put("status", reservation.getStatus().toString());
                    map.put("createdAt", reservation.getCreatedAt());
                    map.put("expiresAt", reservation.getExpiresAt());
                    map.put("correlationId", reservation.getCorrelationId() != null ? reservation.getCorrelationId() : "");
                    return map;
                })
                .toList();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getActiveReservationsForTransaction for transaction: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get reservations expiring soon using CQRS read model.
     */
    @Operation(summary = "Obtenir les réservations expirant bientôt", description = "Retourne les réservations qui expirent dans les prochaines minutes via le modèle de lecture CQRS.")
    @GetMapping("/cqrs/reservations/expiring-soon")
    public ResponseEntity<List<Map<String, Object>>> getReservationsExpiringSoon(
            @RequestParam(defaultValue = "30") int minutesAhead) {
        log.info("CQRS API call: getReservationsExpiringSoon with minutesAhead: {}", minutesAhead);
        try {
            var reservations = inventoryQueryService.getReservationsExpiringSoon(minutesAhead);
            
            List<Map<String, Object>> response = reservations.stream()
                .map(reservation -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reservationId", reservation.getReservationId());
                    map.put("inventoryItemId", reservation.getInventoryItemId());
                    map.put("transactionId", reservation.getTransactionId());
                    map.put("quantity", reservation.getQuantity());
                    map.put("expiresAt", reservation.getExpiresAt());
                    map.put("minutesUntilExpiry", java.time.Duration.between(
                        java.time.LocalDateTime.now(), 
                        reservation.getExpiresAt()
                    ).toMinutes());
                    return map;
                })
                .toList();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getReservationsExpiringSoon: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== EVENT-DRIVEN INVENTORY ENDPOINTS ==========

    /**
     * Reserve inventory with event publishing.
     */
    @Operation(summary = "Réserver l'inventaire avec événements", description = "Réserve l'inventaire et publie des événements appropriés.")
    @PostMapping("/events/reserve")
    public ResponseEntity<Map<String, Object>> reserveInventoryWithEvents(
            @RequestBody ReserveInventoryRequest request) {
        log.info("Event-driven API call: reserveInventoryWithEvents for item {} with quantity {} for transaction {}", 
                request.inventoryItemId(), request.quantity(), request.transactionId());
        
        try {
            String reservationId = eventDrivenInventoryService.reserveInventory(
                    request.inventoryItemId(), 
                    request.transactionId(), 
                    request.quantity(), 
                    request.correlationId()
            );
            
            Map<String, Object> response;
            if (reservationId != null) {
                response = Map.of(
                    "success", true,
                    "reservationId", reservationId,
                    "inventoryItemId", request.inventoryItemId(),
                    "quantity", request.quantity(),
                    "transactionId", request.transactionId(),
                    "message", "Inventory reserved successfully"
                );
                log.info("Inventory reservation successful: reservationId={}", reservationId);
                return ResponseEntity.ok(response);
            } else {
                response = Map.of(
                    "success", false,
                    "inventoryItemId", request.inventoryItemId(),
                    "quantity", request.quantity(),
                    "transactionId", request.transactionId(),
                    "message", "Inventory reservation failed - insufficient stock or item unavailable"
                );
                log.warn("Inventory reservation failed for transaction {}", request.transactionId());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in reserveInventoryWithEvents for transaction {}: {}", 
                     request.transactionId(), e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "inventoryItemId", request.inventoryItemId(),
                "quantity", request.quantity(),
                "transactionId", request.transactionId(),
                "message", "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Release inventory reservation with event publishing.
     */
    @Operation(summary = "Libérer une réservation avec événements", description = "Libère une réservation d'inventaire et publie des événements appropriés.")
    @PostMapping("/events/release")
    public ResponseEntity<Map<String, Object>> releaseInventoryWithEvents(
            @RequestBody ReleaseInventoryRequest request) {
        log.info("Event-driven API call: releaseInventoryWithEvents for reservation {} with reason '{}'", 
                request.reservationId(), request.reason());
        
        try {
            boolean success = eventDrivenInventoryService.releaseReservation(
                    request.reservationId(), 
                    request.reason(), 
                    request.correlationId()
            );
            
            Map<String, Object> response = Map.of(
                "success", success,
                "reservationId", request.reservationId(),
                "reason", request.reason(),
                "message", success ? "Reservation released successfully" : "Failed to release reservation"
            );
            
            if (success) {
                log.info("Inventory reservation released successfully: {}", request.reservationId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to release inventory reservation: {}", request.reservationId());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in releaseInventoryWithEvents for reservation {}: {}", 
                     request.reservationId(), e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "reservationId", request.reservationId(),
                "reason", request.reason(),
                "message", "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Confirm inventory reservation with event publishing.
     */
    @Operation(summary = "Confirmer une réservation avec événements", description = "Confirme une réservation d'inventaire et réduit le stock réel.")
    @PostMapping("/events/confirm")
    public ResponseEntity<Map<String, Object>> confirmInventoryWithEvents(
            @RequestBody ConfirmInventoryRequest request) {
        log.info("Event-driven API call: confirmInventoryWithEvents for reservation {}", 
                request.reservationId());
        
        try {
            boolean success = eventDrivenInventoryService.confirmReservation(
                    request.reservationId(), 
                    request.correlationId()
            );
            
            Map<String, Object> response = Map.of(
                "success", success,
                "reservationId", request.reservationId(),
                "message", success ? "Reservation confirmed successfully" : "Failed to confirm reservation"
            );
            
            if (success) {
                log.info("Inventory reservation confirmed successfully: {}", request.reservationId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to confirm inventory reservation: {}", request.reservationId());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in confirmInventoryWithEvents for reservation {}: {}", 
                     request.reservationId(), e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "reservationId", request.reservationId(),
                "message", "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== SAGA ENDPOINTS ==========

    /**
     * Verify stock availability for saga operations.
     */
    @Operation(summary = "Vérifier la disponibilité du stock", description = "Vérifie si le stock est suffisant pour une opération saga.")
    @PostMapping("/api/v1/inventory/verify-stock")
    public ResponseEntity<StockVerificationResponse> verifyStock(@Valid @RequestBody StockVerificationRequest request) {
        log.info("Saga API call: verifyStock for product {} with quantity {} for saga {}", 
            request.productId(), request.quantity(), request.sagaId());
        
        try {
            StockVerificationResponse response = sagaInventoryService.verifyStock(
                request.productId(), request.quantity(), request.sagaId()
            );
            
            if (response.available()) {
                log.info("Stock verification successful for saga {}", request.sagaId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Stock verification failed for saga {}: {}", request.sagaId(), response.message());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in verifyStock for saga {}: {}", request.sagaId(), e.getMessage(), e);
            StockVerificationResponse errorResponse = StockVerificationResponse.failure(
                request.productId(), request.quantity(), 0, 
                "Internal server error", request.sagaId()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Reserve stock for saga operations.
     */
    @Operation(summary = "Réserver du stock", description = "Réserve du stock pour une opération saga.")
    @PostMapping("/api/v1/inventory/reserve-stock")
    public ResponseEntity<StockReservationResponse> reserveStock(@Valid @RequestBody StockReservationRequest request) {
        log.info("Saga API call: reserveStock for product {} with quantity {} for saga {}", 
            request.productId(), request.quantity(), request.sagaId());
        
        try {
            StockReservationResponse response = sagaInventoryService.reserveStock(
                request.productId(), request.quantity(), request.sagaId()
            );
            
            if (response.success()) {
                log.info("Stock reservation successful for saga {} - reservationId: {}", 
                    request.sagaId(), response.reservationId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Stock reservation failed for saga {}: {}", request.sagaId(), response.message());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in reserveStock for saga {}: {}", request.sagaId(), e.getMessage(), e);
            StockReservationResponse errorResponse = StockReservationResponse.failure(
                request.productId(), request.quantity(), request.sagaId(), 
                "Internal server error"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Release stock reservation (compensation action).
     */
    @Operation(summary = "Libérer une réservation", description = "Libère une réservation de stock pour compensation saga.")
    @DeleteMapping("/api/v1/inventory/reservations/{reservationId}")
    public ResponseEntity<Map<String, Object>> releaseReservation(@PathVariable String reservationId) {
        log.info("Saga API call: releaseReservation for reservationId {}", reservationId);
        
        try {
            boolean success = sagaInventoryService.releaseReservation(reservationId);
            
            Map<String, Object> response = Map.of(
                "success", success,
                "reservationId", reservationId,
                "message", success ? "Reservation released successfully" : "Failed to release reservation"
            );
            
            if (success) {
                log.info("Stock reservation released successfully: {}", reservationId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to release stock reservation: {}", reservationId);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in releaseReservation for reservationId {}: {}", reservationId, e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "reservationId", reservationId,
                "message", "Internal server error"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== END SAGA ENDPOINTS ==========

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

    // Event-driven inventory request DTOs
    public record ReserveInventoryRequest(
            Long inventoryItemId,
            String transactionId,
            Integer quantity,
            String correlationId
    ) {}

    public record ReleaseInventoryRequest(
            String reservationId,
            String reason,
            String correlationId
    ) {}

    public record ConfirmInventoryRequest(
            String reservationId,
            String correlationId
    ) {}
}
