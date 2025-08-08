package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.inventory.readmodel.InventoryReadModel;
import com.log430.tp7.domain.inventory.readmodel.InventoryReservationReadModel;
import com.log430.tp7.infrastructure.repository.InventoryReadModelRepository;
import com.log430.tp7.infrastructure.repository.InventoryReservationReadModelRepository;

/**
 * Query service for inventory read operations using CQRS read models.
 * Provides optimized read-only operations separated from command operations.
 */
@Service
@Transactional(readOnly = true)
public class InventoryQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryQueryService.class);
    
    private final InventoryReadModelRepository inventoryReadModelRepository;
    private final InventoryReservationReadModelRepository reservationReadModelRepository;
    
    public InventoryQueryService(InventoryReadModelRepository inventoryReadModelRepository,
                                InventoryReservationReadModelRepository reservationReadModelRepository) {
        this.inventoryReadModelRepository = inventoryReadModelRepository;
        this.reservationReadModelRepository = reservationReadModelRepository;
    }
    
    // ========== INVENTORY QUERIES ==========
    
    /**
     * Get all active inventory items (optimized read).
     */
    public List<InventoryReadModel> getAllActiveItems() {
        logger.debug("Querying all active inventory items from read model");
        return inventoryReadModelRepository.findByIsActiveTrueOrderByNomAsc();
    }
    
    /**
     * Get inventory item by ID (optimized read).
     */
    public Optional<InventoryReadModel> getItemById(Long id) {
        logger.debug("Querying inventory item by id: {} from read model", id);
        return inventoryReadModelRepository.findById(id);
    }
    
    /**
     * Get items by category (optimized read).
     */
    public List<InventoryReadModel> getItemsByCategory(String category) {
        logger.debug("Querying inventory items by category: {} from read model", category);
        return inventoryReadModelRepository.findByCategorieAndIsActiveTrueOrderByNomAsc(category);
    }
    
    /**
     * Search items by name (optimized read).
     */
    public List<InventoryReadModel> searchItemsByName(String name) {
        logger.debug("Searching inventory items by name: {} from read model", name);
        return inventoryReadModelRepository.findByNomContainingIgnoreCaseAndIsActiveTrueOrderByNomAsc(name);
    }
    
    /**
     * Get items needing restock (optimized read).
     */
    public List<InventoryReadModel> getItemsNeedingRestock() {
        logger.debug("Querying items needing restock from read model");
        return inventoryReadModelRepository.findByNeedsRestockTrueAndIsActiveTrueOrderByNomAsc();
    }
    
    /**
     * Get items with low stock (optimized read).
     */
    public List<InventoryReadModel> getLowStockItems() {
        logger.debug("Querying low stock items from read model");
        return inventoryReadModelRepository.findLowStockItems();
    }
    
    /**
     * Get out of stock items (optimized read).
     */
    public List<InventoryReadModel> getOutOfStockItems() {
        logger.debug("Querying out of stock items from read model");
        return inventoryReadModelRepository.findOutOfStockItems();
    }
    
    /**
     * Get items with reservations (optimized read).
     */
    public List<InventoryReadModel> getItemsWithReservations() {
        logger.debug("Querying items with reservations from read model");
        return inventoryReadModelRepository.findItemsWithReservations();
    }
    
    /**
     * Get distinct categories (optimized read).
     */
    public List<String> getDistinctCategories() {
        logger.debug("Querying distinct categories from read model");
        return inventoryReadModelRepository.findDistinctCategories();
    }
    
    /**
     * Calculate total inventory value (optimized read).
     */
    public Double calculateTotalInventoryValue() {
        logger.debug("Calculating total inventory value from read model");
        return inventoryReadModelRepository.calculateTotalInventoryValue();
    }
    
    /**
     * Calculate total available value (optimized read).
     */
    public Double calculateTotalAvailableValue() {
        logger.debug("Calculating total available value from read model");
        return inventoryReadModelRepository.calculateTotalAvailableValue();
    }
    
    /**
     * Calculate total reserved value (optimized read).
     */
    public Double calculateTotalReservedValue() {
        logger.debug("Calculating total reserved value from read model");
        return inventoryReadModelRepository.calculateTotalReservedValue();
    }
    
    /**
     * Get inventory summary statistics (optimized read).
     */
    public InventorySummary getInventorySummary() {
        logger.debug("Getting inventory summary from read model");
        
        Object[] summary = inventoryReadModelRepository.getInventorySummary();
        if (summary != null && summary.length >= 4) {
            return new InventorySummary(
                ((Number) summary[0]).longValue(),  // count
                ((Number) summary[1]).intValue(),   // total stock
                ((Number) summary[2]).intValue(),   // reserved stock
                ((Number) summary[3]).intValue()    // available stock
            );
        }
        
        return new InventorySummary(0L, 0, 0, 0);
    }
    
    // ========== RESERVATION QUERIES ==========
    
    /**
     * Get active reservations for a transaction (optimized read).
     */
    public List<InventoryReservationReadModel> getActiveReservationsForTransaction(String transactionId) {
        logger.debug("Querying active reservations for transaction: {} from read model", transactionId);
        return reservationReadModelRepository.findByTransactionIdAndStatus(
            transactionId, InventoryReservationReadModel.ReservationStatus.ACTIVE
        );
    }
    
    /**
     * Get all reservations for a transaction (optimized read).
     */
    public List<InventoryReservationReadModel> getAllReservationsForTransaction(String transactionId) {
        logger.debug("Querying all reservations for transaction: {} from read model", transactionId);
        return reservationReadModelRepository.findByTransactionIdOrderByCreatedAtAsc(transactionId);
    }
    
    /**
     * Get active reservations for an inventory item (optimized read).
     */
    public List<InventoryReservationReadModel> getActiveReservationsForItem(Long inventoryItemId) {
        logger.debug("Querying active reservations for item: {} from read model", inventoryItemId);
        return reservationReadModelRepository.findByInventoryItemIdAndStatus(
            inventoryItemId, InventoryReservationReadModel.ReservationStatus.ACTIVE
        );
    }
    
    /**
     * Get all reservations for an inventory item (optimized read).
     */
    public List<InventoryReservationReadModel> getAllReservationsForItem(Long inventoryItemId) {
        logger.debug("Querying all reservations for item: {} from read model", inventoryItemId);
        return reservationReadModelRepository.findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId);
    }
    
    /**
     * Get expired active reservations (optimized read).
     */
    public List<InventoryReservationReadModel> getExpiredActiveReservations() {
        logger.debug("Querying expired active reservations from read model");
        return reservationReadModelRepository.findExpiredActiveReservations(LocalDateTime.now());
    }
    
    /**
     * Get reservations expiring soon (optimized read).
     */
    public List<InventoryReservationReadModel> getReservationsExpiringSoon(int minutesAhead) {
        logger.debug("Querying reservations expiring in {} minutes from read model", minutesAhead);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonThreshold = now.plusMinutes(minutesAhead);
        return reservationReadModelRepository.findReservationsExpiringSoon(now, soonThreshold);
    }
    
    /**
     * Get recent reservations (optimized read).
     */
    public List<InventoryReservationReadModel> getRecentReservations(int hoursBack) {
        logger.debug("Querying reservations from last {} hours from read model", hoursBack);
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return reservationReadModelRepository.findRecentReservations(since);
    }
    
    /**
     * Get reservation by ID (optimized read).
     */
    public Optional<InventoryReservationReadModel> getReservationById(String reservationId) {
        logger.debug("Querying reservation by id: {} from read model", reservationId);
        return reservationReadModelRepository.findById(reservationId);
    }
    
    /**
     * Calculate active reserved quantity for an item (optimized read).
     */
    public Integer calculateActiveReservedQuantity(Long inventoryItemId) {
        logger.debug("Calculating active reserved quantity for item: {} from read model", inventoryItemId);
        return reservationReadModelRepository.calculateActiveReservedQuantity(inventoryItemId, LocalDateTime.now());
    }
    
    /**
     * Get reservation summary for an item (optimized read).
     */
    public ReservationSummary getReservationSummaryForItem(Long inventoryItemId) {
        logger.debug("Getting reservation summary for item: {} from read model", inventoryItemId);
        
        List<Object[]> results = reservationReadModelRepository.getReservationSummaryForItem(inventoryItemId);
        return buildReservationSummary(results);
    }
    
    /**
     * Get reservation summary for a transaction (optimized read).
     */
    public ReservationSummary getReservationSummaryForTransaction(String transactionId) {
        logger.debug("Getting reservation summary for transaction: {} from read model", transactionId);
        
        List<Object[]> results = reservationReadModelRepository.getReservationSummaryForTransaction(transactionId);
        return buildReservationSummary(results);
    }
    
    // ========== HELPER METHODS ==========
    
    private ReservationSummary buildReservationSummary(List<Object[]> results) {
        int activeCount = 0, activeQuantity = 0;
        int confirmedCount = 0, confirmedQuantity = 0;
        int releasedCount = 0, releasedQuantity = 0;
        
        for (Object[] result : results) {
            InventoryReservationReadModel.ReservationStatus status = 
                (InventoryReservationReadModel.ReservationStatus) result[0];
            int count = ((Number) result[1]).intValue();
            int quantity = ((Number) result[2]).intValue();
            
            switch (status) {
                case ACTIVE:
                    activeCount = count;
                    activeQuantity = quantity;
                    break;
                case CONFIRMED:
                    confirmedCount = count;
                    confirmedQuantity = quantity;
                    break;
                case RELEASED:
                    releasedCount = count;
                    releasedQuantity = quantity;
                    break;
            }
        }
        
        return new ReservationSummary(
            activeCount, activeQuantity,
            confirmedCount, confirmedQuantity,
            releasedCount, releasedQuantity
        );
    }
    
    // ========== DATA CLASSES ==========
    
    /**
     * Summary of inventory statistics.
     */
    public static class InventorySummary {
        private final long totalItems;
        private final int totalStock;
        private final int reservedStock;
        private final int availableStock;
        
        public InventorySummary(long totalItems, int totalStock, int reservedStock, int availableStock) {
            this.totalItems = totalItems;
            this.totalStock = totalStock;
            this.reservedStock = reservedStock;
            this.availableStock = availableStock;
        }
        
        // Getters
        public long getTotalItems() { return totalItems; }
        public int getTotalStock() { return totalStock; }
        public int getReservedStock() { return reservedStock; }
        public int getAvailableStock() { return availableStock; }
        
        @Override
        public String toString() {
            return String.format("InventorySummary{totalItems=%d, totalStock=%d, reservedStock=%d, availableStock=%d}", 
                               totalItems, totalStock, reservedStock, availableStock);
        }
    }
    
    /**
     * Summary of reservation statistics.
     */
    public static class ReservationSummary {
        private final int activeCount;
        private final int activeQuantity;
        private final int confirmedCount;
        private final int confirmedQuantity;
        private final int releasedCount;
        private final int releasedQuantity;
        
        public ReservationSummary(int activeCount, int activeQuantity, int confirmedCount, int confirmedQuantity,
                                 int releasedCount, int releasedQuantity) {
            this.activeCount = activeCount;
            this.activeQuantity = activeQuantity;
            this.confirmedCount = confirmedCount;
            this.confirmedQuantity = confirmedQuantity;
            this.releasedCount = releasedCount;
            this.releasedQuantity = releasedQuantity;
        }
        
        // Getters
        public int getActiveCount() { return activeCount; }
        public int getActiveQuantity() { return activeQuantity; }
        public int getConfirmedCount() { return confirmedCount; }
        public int getConfirmedQuantity() { return confirmedQuantity; }
        public int getReleasedCount() { return releasedCount; }
        public int getReleasedQuantity() { return releasedQuantity; }
        public int getTotalCount() { return activeCount + confirmedCount + releasedCount; }
        public int getTotalQuantity() { return activeQuantity + confirmedQuantity + releasedQuantity; }
        
        @Override
        public String toString() {
            return String.format("ReservationSummary{active=%d(%d), confirmed=%d(%d), released=%d(%d)}", 
                               activeCount, activeQuantity, confirmedCount, confirmedQuantity, releasedCount, releasedQuantity);
        }
    }
}