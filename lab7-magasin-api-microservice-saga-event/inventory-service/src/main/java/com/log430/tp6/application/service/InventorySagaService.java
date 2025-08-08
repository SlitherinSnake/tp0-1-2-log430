package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.domain.inventory.StockReservation;
import com.log430.tp7.domain.transaction.events.TransactionCreated;
import com.log430.tp7.infrastructure.repository.InventoryItemRepository;
import com.log430.tp7.infrastructure.repository.StockReservationRepository;

/**
 * Service that coordinates inventory operations within choreographed sagas.
 * Provides higher-level business logic for inventory reservation and compensation.
 */
@Service
@Transactional
public class InventorySagaService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventorySagaService.class);
    
    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final EventDrivenInventoryService eventDrivenInventoryService;
    
    public InventorySagaService(InventoryItemRepository inventoryItemRepository,
                               StockReservationRepository stockReservationRepository,
                               EventDrivenInventoryService eventDrivenInventoryService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.eventDrivenInventoryService = eventDrivenInventoryService;
    }
    
    /**
     * Process a transaction created event by validating inventory availability.
     * This provides early feedback in the saga workflow.
     */
    public boolean validateTransactionInventory(TransactionCreated event) {
        logger.info("Validating inventory for transaction: {}", event.getTransactionId());
        
        try {
            boolean allItemsAvailable = true;
            
            for (TransactionCreated.TransactionItemData item : event.getItems()) {
                boolean itemAvailable = validateItemAvailability(
                    item.getInventoryItemId(), 
                    item.getQuantity()
                );
                
                if (!itemAvailable) {
                    logger.warn("Insufficient inventory for item: inventoryItemId={}, requestedQuantity={}", 
                               item.getInventoryItemId(), item.getQuantity());
                    allItemsAvailable = false;
                }
            }
            
            logger.info("Inventory validation completed for transaction: {}, result: {}", 
                       event.getTransactionId(), allItemsAvailable);
            
            return allItemsAvailable;
            
        } catch (Exception e) {
            logger.error("Failed to validate inventory for transaction: {}", event.getTransactionId(), e);
            return false;
        }
    }
    
    /**
     * Reserve inventory for all items in a transaction.
     * This is typically triggered by a PaymentProcessed event.
     */
    public boolean reserveTransactionInventory(String transactionId, List<TransactionCreated.TransactionItemData> items, String correlationId) {
        logger.info("Reserving inventory for transaction: {}, itemCount: {}", transactionId, items.size());
        
        try {
            boolean allReservationsSuccessful = true;
            
            for (TransactionCreated.TransactionItemData item : items) {
                String reservationId = eventDrivenInventoryService.reserveInventory(
                    item.getInventoryItemId(),
                    transactionId,
                    item.getQuantity(),
                    correlationId
                );
                
                if (reservationId == null) {
                    logger.error("Failed to reserve inventory for item: inventoryItemId={}, quantity={}", 
                                item.getInventoryItemId(), item.getQuantity());
                    allReservationsSuccessful = false;
                    break;
                }
            }
            
            // If any reservation failed, release all successful reservations
            if (!allReservationsSuccessful) {
                logger.warn("Some inventory reservations failed, releasing all reservations for transaction: {}", transactionId);
                releaseTransactionReservations(transactionId, "Partial reservation failure", correlationId);
            }
            
            logger.info("Inventory reservation completed for transaction: {}, result: {}", 
                       transactionId, allReservationsSuccessful);
            
            return allReservationsSuccessful;
            
        } catch (Exception e) {
            logger.error("Failed to reserve inventory for transaction: {}", transactionId, e);
            // Release any partial reservations
            releaseTransactionReservations(transactionId, "Exception during reservation", correlationId);
            return false;
        }
    }
    
    /**
     * Release all inventory reservations for a transaction.
     * This is used for compensation in saga workflows.
     */
    public void releaseTransactionReservations(String transactionId, String reason, String correlationId) {
        logger.info("Releasing all inventory reservations for transaction: {}, reason: {}", transactionId, reason);
        
        try {
            List<StockReservation> activeReservations = stockReservationRepository.findBySagaIdAndStatus(
                transactionId, StockReservation.ReservationStatus.ACTIVE
            );
            
            for (StockReservation reservation : activeReservations) {
                boolean released = eventDrivenInventoryService.releaseReservation(
                    reservation.getReservationId(),
                    reason,
                    correlationId
                );
                
                if (!released) {
                    logger.error("Failed to release reservation: reservationId={}, transactionId={}", 
                                reservation.getReservationId(), transactionId);
                }
            }
            
            logger.info("Released {} inventory reservations for transaction: {}", 
                       activeReservations.size(), transactionId);
            
        } catch (Exception e) {
            logger.error("Failed to release inventory reservations for transaction: {}", transactionId, e);
        }
    }
    
    /**
     * Confirm all inventory reservations for a transaction.
     * This converts reservations to actual stock reductions.
     */
    public boolean confirmTransactionReservations(String transactionId, String correlationId) {
        logger.info("Confirming all inventory reservations for transaction: {}", transactionId);
        
        try {
            List<StockReservation> activeReservations = stockReservationRepository.findBySagaIdAndStatus(
                transactionId, StockReservation.ReservationStatus.ACTIVE
            );
            
            boolean allConfirmationsSuccessful = true;
            
            for (StockReservation reservation : activeReservations) {
                boolean confirmed = eventDrivenInventoryService.confirmReservation(
                    reservation.getReservationId(),
                    correlationId
                );
                
                if (!confirmed) {
                    logger.error("Failed to confirm reservation: reservationId={}, transactionId={}", 
                                reservation.getReservationId(), transactionId);
                    allConfirmationsSuccessful = false;
                }
            }
            
            logger.info("Confirmed {} inventory reservations for transaction: {}, result: {}", 
                       activeReservations.size(), transactionId, allConfirmationsSuccessful);
            
            return allConfirmationsSuccessful;
            
        } catch (Exception e) {
            logger.error("Failed to confirm inventory reservations for transaction: {}", transactionId, e);
            return false;
        }
    }
    
    /**
     * Get the current status of inventory reservations for a transaction.
     */
    public InventoryReservationStatus getTransactionReservationStatus(String transactionId) {
        try {
            List<StockReservation> activeReservations = stockReservationRepository.findBySagaIdAndStatus(
                transactionId, StockReservation.ReservationStatus.ACTIVE
            );
            
            List<StockReservation> confirmedReservations = stockReservationRepository.findBySagaIdAndStatus(
                transactionId, StockReservation.ReservationStatus.CONFIRMED
            );
            
            List<StockReservation> releasedReservations = stockReservationRepository.findBySagaIdAndStatus(
                transactionId, StockReservation.ReservationStatus.RELEASED
            );
            
            return new InventoryReservationStatus(
                transactionId,
                activeReservations.size(),
                confirmedReservations.size(),
                releasedReservations.size()
            );
            
        } catch (Exception e) {
            logger.error("Failed to get reservation status for transaction: {}", transactionId, e);
            return new InventoryReservationStatus(transactionId, 0, 0, 0);
        }
    }
    
    /**
     * Validate if an inventory item has sufficient stock for the requested quantity.
     */
    private boolean validateItemAvailability(Long inventoryItemId, Integer requestedQuantity) {
        try {
            Optional<InventoryItem> itemOpt = inventoryItemRepository.findById(inventoryItemId);
            if (itemOpt.isEmpty()) {
                logger.warn("Inventory item not found: {}", inventoryItemId);
                return false;
            }
            
            InventoryItem item = itemOpt.get();
            if (!item.isActive()) {
                logger.warn("Inventory item is not active: {}", inventoryItemId);
                return false;
            }
            
            // Calculate available stock (current stock - reserved stock)
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantity(
                inventoryItemId.toString(), LocalDateTime.now()
            );
            Integer availableStock = item.getStockCentral() - reservedQuantity;
            
            boolean available = availableStock >= requestedQuantity;
            logger.debug("Inventory availability check: itemId={}, requested={}, available={}, result={}", 
                        inventoryItemId, requestedQuantity, availableStock, available);
            
            return available;
            
        } catch (Exception e) {
            logger.error("Failed to validate item availability: inventoryItemId={}", inventoryItemId, e);
            return false;
        }
    }
    
    /**
     * Data class representing the status of inventory reservations for a transaction.
     */
    public static class InventoryReservationStatus {
        private final String transactionId;
        private final int activeReservations;
        private final int confirmedReservations;
        private final int releasedReservations;
        
        public InventoryReservationStatus(String transactionId, int activeReservations, 
                                        int confirmedReservations, int releasedReservations) {
            this.transactionId = transactionId;
            this.activeReservations = activeReservations;
            this.confirmedReservations = confirmedReservations;
            this.releasedReservations = releasedReservations;
        }
        
        // Getters
        public String getTransactionId() { return transactionId; }
        public int getActiveReservations() { return activeReservations; }
        public int getConfirmedReservations() { return confirmedReservations; }
        public int getReleasedReservations() { return releasedReservations; }
        public int getTotalReservations() { return activeReservations + confirmedReservations + releasedReservations; }
        
        @Override
        public String toString() {
            return String.format("InventoryReservationStatus{transactionId='%s', active=%d, confirmed=%d, released=%d}", 
                               transactionId, activeReservations, confirmedReservations, releasedReservations);
        }
    }
}