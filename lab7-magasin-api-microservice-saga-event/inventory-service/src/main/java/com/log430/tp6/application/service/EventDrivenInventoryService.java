package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.domain.inventory.StockReservation;
import com.log430.tp7.domain.inventory.events.InventoryReleased;
import com.log430.tp7.domain.inventory.events.InventoryReserved;
import com.log430.tp7.domain.inventory.events.InventoryUnavailable;
import com.log430.tp7.infrastructure.event.EventProducer;
import com.log430.tp7.infrastructure.repository.InventoryItemRepository;
import com.log430.tp7.infrastructure.repository.StockReservationRepository;

/**
 * Event-driven inventory service that publishes domain events for inventory operations.
 * Integrates with the existing inventory management while adding event publishing capabilities.
 */
@Service
@Transactional
public class EventDrivenInventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventDrivenInventoryService.class);
    
    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final EventProducer eventProducer;
    private final InventoryProjectionService projectionService;
    
    public EventDrivenInventoryService(InventoryItemRepository inventoryItemRepository,
                                     StockReservationRepository stockReservationRepository,
                                     EventProducer eventProducer,
                                     InventoryProjectionService projectionService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.eventProducer = eventProducer;
        this.projectionService = projectionService;
    }
    
    /**
     * Reserve inventory for a transaction and publish appropriate events.
     * 
     * @param inventoryItemId The inventory item ID
     * @param transactionId The transaction ID requesting the reservation
     * @param quantity The quantity to reserve
     * @param correlationId The correlation ID for event tracking
     * @return The reservation ID if successful, null if unavailable
     */
    public String reserveInventory(Long inventoryItemId, String transactionId, Integer quantity, String correlationId) {
        logger.info("Attempting to reserve inventory: itemId={}, transactionId={}, quantity={}", 
                   inventoryItemId, transactionId, quantity);
        
        try {
            InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId));
            
            // Check if item is active
            if (!item.isActive()) {
                logger.warn("Inventory item is not active: {}", inventoryItemId);
                publishInventoryUnavailable(inventoryItemId, transactionId, quantity, 0, 
                                          "Item is not active", correlationId);
                return null;
            }
            
            // Calculate available stock (current stock - reserved stock)
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantityWithLock(
                    inventoryItemId.toString(), LocalDateTime.now());
            Integer availableStock = item.getStockCentral() - reservedQuantity;
            
            // Check if sufficient stock is available
            if (availableStock < quantity) {
                logger.warn("Insufficient inventory: itemId={}, requested={}, available={}", 
                           inventoryItemId, quantity, availableStock);
                publishInventoryUnavailable(inventoryItemId, transactionId, quantity, availableStock, 
                                          "Insufficient stock", correlationId);
                return null;
            }
            
            // Create reservation
            String reservationId = UUID.randomUUID().toString();
            StockReservation reservation = new StockReservation(reservationId, inventoryItemId.toString(), 
                                                              quantity, transactionId);
            stockReservationRepository.save(reservation);
            
            // Calculate remaining stock after reservation
            Integer remainingStock = availableStock - quantity;
            
            // Publish success event
            publishInventoryReserved(inventoryItemId, transactionId, quantity, reservationId, 
                                   remainingStock, correlationId);
            
            logger.info("Successfully reserved inventory: itemId={}, reservationId={}, quantity={}", 
                       inventoryItemId, reservationId, quantity);
            
            return reservationId;
            
        } catch (Exception e) {
            logger.error("Failed to reserve inventory: itemId={}, transactionId={}", 
                        inventoryItemId, transactionId, e);
            publishInventoryUnavailable(inventoryItemId, transactionId, quantity, 0, 
                                      "System error: " + e.getMessage(), correlationId);
            return null;
        }
    }
    
    /**
     * Release a previously made reservation and publish event.
     * 
     * @param reservationId The reservation ID to release
     * @param reason The reason for releasing the reservation
     * @param correlationId The correlation ID for event tracking
     * @return true if successfully released, false otherwise
     */
    public boolean releaseReservation(String reservationId, String reason, String correlationId) {
        logger.info("Attempting to release reservation: reservationId={}, reason={}", reservationId, reason);
        
        try {
            StockReservation reservation = stockReservationRepository.findByIdWithLock(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
            
            if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
                logger.warn("Reservation is not active: reservationId={}, status={}", 
                           reservationId, reservation.getStatus());
                return false;
            }
            
            // Release the reservation
            reservation.release();
            stockReservationRepository.save(reservation);
            
            // Get current inventory item to calculate new available stock
            Long inventoryItemId = Long.parseLong(reservation.getProductId());
            InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId));
            
            // Calculate new available stock after release
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantity(
                    inventoryItemId.toString(), LocalDateTime.now());
            Integer newAvailableStock = item.getStockCentral() - reservedQuantity;
            
            // Publish release event
            publishInventoryReleased(inventoryItemId, reservation.getSagaId(), reservation.getQuantity(), 
                                   reservationId, newAvailableStock, reason, correlationId);
            
            logger.info("Successfully released reservation: reservationId={}, quantity={}", 
                       reservationId, reservation.getQuantity());
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to release reservation: reservationId={}", reservationId, e);
            return false;
        }
    }
    
    /**
     * Confirm a reservation by converting it to actual stock reduction.
     * 
     * @param reservationId The reservation ID to confirm
     * @param correlationId The correlation ID for event tracking
     * @return true if successfully confirmed, false otherwise
     */
    public boolean confirmReservation(String reservationId, String correlationId) {
        logger.info("Attempting to confirm reservation: reservationId={}", reservationId);
        
        try {
            StockReservation reservation = stockReservationRepository.findByIdWithLock(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
            
            if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
                logger.warn("Reservation is not active: reservationId={}, status={}", 
                           reservationId, reservation.getStatus());
                return false;
            }
            
            // Get inventory item and reduce actual stock
            Long inventoryItemId = Long.parseLong(reservation.getProductId());
            InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + inventoryItemId));
            
            // Reduce actual stock
            item.reduceStock(reservation.getQuantity());
            inventoryItemRepository.save(item);
            
            // Confirm the reservation
            reservation.confirm();
            stockReservationRepository.save(reservation);
            
            logger.info("Successfully confirmed reservation: reservationId={}, quantity={}", 
                       reservationId, reservation.getQuantity());
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to confirm reservation: reservationId={}", reservationId, e);
            return false;
        }
    }
    
    private void publishInventoryReserved(Long inventoryItemId, String transactionId, Integer quantity, 
                                        String reservationId, Integer remainingStock, String correlationId) {
        try {
            InventoryReserved event = new InventoryReserved(inventoryItemId, transactionId, quantity, 
                                                          reservationId, remainingStock, correlationId);
            eventProducer.publishEvent("inventory.reserved", event);
            
            // Update read models
            projectionService.handleInventoryReserved(event);
            
            logger.info("Published InventoryReserved event and updated read models: {}", event);
        } catch (Exception e) {
            logger.error("Failed to publish InventoryReserved event", e);
        }
    }
    
    private void publishInventoryUnavailable(Long inventoryItemId, String transactionId, Integer requestedQuantity, 
                                           Integer availableStock, String reason, String correlationId) {
        try {
            InventoryUnavailable event = new InventoryUnavailable(inventoryItemId, transactionId, requestedQuantity, 
                                                                availableStock, reason, correlationId);
            eventProducer.publishEvent("inventory.unavailable", event);
            
            // Update read models (if needed)
            projectionService.handleInventoryUnavailable(event);
            
            logger.info("Published InventoryUnavailable event and updated read models: {}", event);
        } catch (Exception e) {
            logger.error("Failed to publish InventoryUnavailable event", e);
        }
    }
    
    private void publishInventoryReleased(Long inventoryItemId, String transactionId, Integer quantity, 
                                        String reservationId, Integer newAvailableStock, String reason, String correlationId) {
        try {
            InventoryReleased event = new InventoryReleased(inventoryItemId, transactionId, quantity, 
                                                          reservationId, newAvailableStock, reason, correlationId);
            eventProducer.publishEvent("inventory.released", event);
            
            // Update read models
            projectionService.handleInventoryReleased(event);
            
            logger.info("Published InventoryReleased event and updated read models: {}", event);
        } catch (Exception e) {
            logger.error("Failed to publish InventoryReleased event", e);
        }
    }
}