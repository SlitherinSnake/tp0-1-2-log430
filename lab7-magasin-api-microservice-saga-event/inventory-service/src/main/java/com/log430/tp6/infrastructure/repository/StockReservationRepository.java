package com.log430.tp7.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp7.domain.inventory.StockReservation;
import jakarta.persistence.LockModeType;

/**
 * Repository interface for StockReservation entities.
 * Provides data access operations for stock reservation management.
 */
public interface StockReservationRepository extends JpaRepository<StockReservation, String> {

    /**
     * Find active reservations by saga ID.
     */
    List<StockReservation> findBySagaIdAndStatus(String sagaId, StockReservation.ReservationStatus status);

    /**
     * Find active reservations for a product.
     */
    List<StockReservation> findByProductIdAndStatus(String productId, StockReservation.ReservationStatus status);

    /**
     * Find expired reservations that need cleanup.
     */
    @Query("SELECT r FROM StockReservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<StockReservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Calculate total reserved quantity for a product.
     */
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM StockReservation r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.expiresAt > :now")
    Integer calculateReservedQuantity(@Param("productId") String productId, @Param("now") LocalDateTime now);

    /**
     * Find reservation by reservation ID and saga ID for security.
     */
    Optional<StockReservation> findByReservationIdAndSagaId(String reservationId, String sagaId);
    
    /**
     * Find and lock active reservations for a product with pessimistic write lock.
     * Prevents concurrent modifications during stock reservation operations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM StockReservation r WHERE r.productId = :productId AND r.status = 'ACTIVE' " +
           "AND r.expiresAt > :now ORDER BY r.createdAt ASC")
    List<StockReservation> findActiveReservationsWithLock(@Param("productId") String productId, 
                                                          @Param("now") LocalDateTime now);
    
    /**
     * Atomically create a stock reservation with race condition protection.
     * Uses database constraints to prevent duplicate reservations.
     */
    @Modifying
    @Query("INSERT INTO StockReservation (reservationId, productId, quantity, sagaId, status, createdAt, expiresAt) " +
           "SELECT :reservationId, :productId, :quantity, :sagaId, 'ACTIVE', :createdAt, :expiresAt " +
           "WHERE NOT EXISTS (SELECT 1 FROM StockReservation r WHERE r.sagaId = :sagaId AND r.productId = :productId " +
           "AND r.status = 'ACTIVE')")
    int createReservationIfNotExists(@Param("reservationId") String reservationId,
                                    @Param("productId") String productId,
                                    @Param("quantity") Integer quantity,
                                    @Param("sagaId") String sagaId,
                                    @Param("createdAt") LocalDateTime createdAt,
                                    @Param("expiresAt") LocalDateTime expiresAt);
    
    /**
     * Calculate total reserved quantity with pessimistic read lock for consistency.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM StockReservation r WHERE r.productId = :productId " +
           "AND r.status = 'ACTIVE' AND r.expiresAt > :now")
    Integer calculateReservedQuantityWithLock(@Param("productId") String productId, @Param("now") LocalDateTime now);
    
    /**
     * Find and lock reservation for atomic status updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM StockReservation r WHERE r.reservationId = :reservationId")
    Optional<StockReservation> findByIdWithLock(@Param("reservationId") String reservationId);
    
    /**
     * Count concurrent active reservations for the same product by different sagas.
     */
    @Query("SELECT COUNT(r) FROM StockReservation r WHERE r.productId = :productId AND r.sagaId != :excludeSagaId " +
           "AND r.status = 'ACTIVE' AND r.expiresAt > :now")
    long countConcurrentActiveReservations(@Param("productId") String productId, 
                                          @Param("excludeSagaId") String excludeSagaId,
                                          @Param("now") LocalDateTime now);
}