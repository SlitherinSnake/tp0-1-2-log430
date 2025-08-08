package com.log430.tp7.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp7.domain.inventory.readmodel.InventoryReservationReadModel;

/**
 * Repository interface for InventoryReservationReadModel entities.
 * Provides optimized read operations for inventory reservation queries.
 */
public interface InventoryReservationReadModelRepository extends JpaRepository<InventoryReservationReadModel, String> {
    
    /**
     * Find active reservations by transaction ID.
     */
    List<InventoryReservationReadModel> findByTransactionIdAndStatus(String transactionId, 
                                                                     InventoryReservationReadModel.ReservationStatus status);
    
    /**
     * Find active reservations for an inventory item.
     */
    List<InventoryReservationReadModel> findByInventoryItemIdAndStatus(Long inventoryItemId, 
                                                                       InventoryReservationReadModel.ReservationStatus status);
    
    /**
     * Find all reservations for a transaction (any status).
     */
    List<InventoryReservationReadModel> findByTransactionIdOrderByCreatedAtAsc(String transactionId);
    
    /**
     * Find all reservations for an inventory item (any status).
     */
    List<InventoryReservationReadModel> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);
    
    /**
     * Find expired active reservations.
     */
    @Query("SELECT r FROM InventoryReservationReadModel r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<InventoryReservationReadModel> findExpiredActiveReservations(@Param("now") LocalDateTime now);
    
    /**
     * Calculate total reserved quantity for an inventory item.
     */
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM InventoryReservationReadModel r " +
           "WHERE r.inventoryItemId = :inventoryItemId AND r.status = 'ACTIVE' AND r.expiresAt > :now")
    Integer calculateActiveReservedQuantity(@Param("inventoryItemId") Long inventoryItemId, @Param("now") LocalDateTime now);
    
    /**
     * Find reservations by correlation ID.
     */
    List<InventoryReservationReadModel> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
    
    /**
     * Find recent reservations (within last N hours).
     */
    @Query("SELECT r FROM InventoryReservationReadModel r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<InventoryReservationReadModel> findRecentReservations(@Param("since") LocalDateTime since);
    
    /**
     * Count reservations by status.
     */
    @Query("SELECT r.status, COUNT(r) FROM InventoryReservationReadModel r GROUP BY r.status")
    List<Object[]> countReservationsByStatus();
    
    /**
     * Find reservations that will expire soon.
     */
    @Query("SELECT r FROM InventoryReservationReadModel r WHERE r.status = 'ACTIVE' AND r.expiresAt BETWEEN :now AND :soonThreshold ORDER BY r.expiresAt ASC")
    List<InventoryReservationReadModel> findReservationsExpiringSoon(@Param("now") LocalDateTime now, 
                                                                     @Param("soonThreshold") LocalDateTime soonThreshold);
    
    /**
     * Get reservation summary for an inventory item.
     */
    @Query("SELECT r.status, COUNT(r), COALESCE(SUM(r.quantity), 0) FROM InventoryReservationReadModel r " +
           "WHERE r.inventoryItemId = :inventoryItemId GROUP BY r.status")
    List<Object[]> getReservationSummaryForItem(@Param("inventoryItemId") Long inventoryItemId);
    
    /**
     * Get reservation summary for a transaction.
     */
    @Query("SELECT r.status, COUNT(r), COALESCE(SUM(r.quantity), 0) FROM InventoryReservationReadModel r " +
           "WHERE r.transactionId = :transactionId GROUP BY r.status")
    List<Object[]> getReservationSummaryForTransaction(@Param("transactionId") String transactionId);
    
    /**
     * Find long-running active reservations.
     */
    @Query("SELECT r FROM InventoryReservationReadModel r WHERE r.status = 'ACTIVE' AND r.createdAt < :threshold ORDER BY r.createdAt ASC")
    List<InventoryReservationReadModel> findLongRunningActiveReservations(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Find reservations by inventory item and date range.
     */
    @Query("SELECT r FROM InventoryReservationReadModel r WHERE r.inventoryItemId = :inventoryItemId " +
           "AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<InventoryReservationReadModel> findReservationsByItemAndDateRange(@Param("inventoryItemId") Long inventoryItemId,
                                                                           @Param("startDate") LocalDateTime startDate,
                                                                           @Param("endDate") LocalDateTime endDate);
}