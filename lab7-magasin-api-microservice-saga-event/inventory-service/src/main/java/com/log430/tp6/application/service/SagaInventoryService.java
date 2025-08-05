package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.domain.inventory.StockReservation;
import com.log430.tp7.infrastructure.repository.InventoryItemRepository;
import com.log430.tp7.infrastructure.repository.StockReservationRepository;
import com.log430.tp7.presentation.api.dto.StockReservationResponse;
import com.log430.tp7.presentation.api.dto.StockVerificationResponse;

/**
 * Application service for saga-specific inventory operations.
 * Handles stock verification, reservation, and release for distributed transactions.
 */
@Service
@Transactional
public class SagaInventoryService {
    private static final Logger log = LoggerFactory.getLogger(SagaInventoryService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;

    public SagaInventoryService(
            InventoryItemRepository inventoryItemRepository,
            StockReservationRepository stockReservationRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockReservationRepository = stockReservationRepository;
    }

    /**
     * Verify if sufficient stock is available for the requested quantity.
     */
    @Transactional(readOnly = true)
    public StockVerificationResponse verifyStock(String productId, Integer quantity, String sagaId) {
        log.info("Verifying stock for product {} with quantity {} for saga {}", productId, quantity, sagaId);
        
        try {
            // Convert productId to Long (assuming it's the inventory item ID)
            Long itemId = Long.parseLong(productId);
            
            Optional<InventoryItem> itemOpt = inventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return StockVerificationResponse.failure(
                    productId, quantity, 0, "Product not found", sagaId
                );
            }
            
            InventoryItem item = itemOpt.get();
            if (!item.isActive()) {
                log.warn("Product is inactive: {}", productId);
                return StockVerificationResponse.failure(
                    productId, quantity, 0, "Product is inactive", sagaId
                );
            }
            
            // Calculate available stock (current stock - active reservations)
            Integer currentStock = item.getStockCentral();
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantity(
                productId, LocalDateTime.now()
            );
            Integer availableStock = currentStock - reservedQuantity;
            
            if (availableStock >= quantity) {
                log.info("Stock verification successful for product {} - available: {}, requested: {}", 
                    productId, availableStock, quantity);
                return StockVerificationResponse.success(productId, quantity, availableStock, sagaId);
            } else {
                log.warn("Insufficient stock for product {} - available: {}, requested: {}", 
                    productId, availableStock, quantity);
                return StockVerificationResponse.failure(
                    productId, quantity, availableStock, "Insufficient stock", sagaId
                );
            }
            
        } catch (NumberFormatException e) {
            log.error("Invalid product ID format: {}", productId);
            return StockVerificationResponse.failure(
                productId, quantity, 0, "Invalid product ID format", sagaId
            );
        } catch (Exception e) {
            log.error("Error verifying stock for product {}: {}", productId, e.getMessage(), e);
            return StockVerificationResponse.failure(
                productId, quantity, 0, "Internal error during stock verification", sagaId
            );
        }
    }

    /**
     * Reserve stock for a saga transaction.
     */
    public StockReservationResponse reserveStock(String productId, Integer quantity, String sagaId) {
        log.info("Reserving stock for product {} with quantity {} for saga {}", productId, quantity, sagaId);
        
        try {
            // First verify stock is still available
            StockVerificationResponse verification = verifyStock(productId, quantity, sagaId);
            if (!verification.available()) {
                log.warn("Cannot reserve stock - verification failed: {}", verification.message());
                return StockReservationResponse.failure(productId, quantity, sagaId, verification.message());
            }
            
            // Create reservation
            String reservationId = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30); // 30 minute expiration
            
            StockReservation reservation = new StockReservation(reservationId, productId, quantity, sagaId);
            reservation.setExpiresAt(expiresAt);
            
            stockReservationRepository.save(reservation);
            
            log.info("Stock reserved successfully - reservationId: {}, product: {}, quantity: {}, saga: {}", 
                reservationId, productId, quantity, sagaId);
            
            return StockReservationResponse.success(reservationId, productId, quantity, sagaId, expiresAt);
            
        } catch (Exception e) {
            log.error("Error reserving stock for product {}: {}", productId, e.getMessage(), e);
            return StockReservationResponse.failure(
                productId, quantity, sagaId, "Internal error during stock reservation"
            );
        }
    }

    /**
     * Release a stock reservation (compensation action).
     */
    public boolean releaseReservation(String reservationId) {
        log.info("Releasing stock reservation: {}", reservationId);
        
        try {
            Optional<StockReservation> reservationOpt = stockReservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                log.warn("Reservation not found: {}", reservationId);
                return false;
            }
            
            StockReservation reservation = reservationOpt.get();
            if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
                log.warn("Reservation is not active: {} - status: {}", reservationId, reservation.getStatus());
                return false;
            }
            
            reservation.release();
            stockReservationRepository.save(reservation);
            
            log.info("Stock reservation released successfully: {}", reservationId);
            return true;
            
        } catch (Exception e) {
            log.error("Error releasing reservation {}: {}", reservationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Confirm a stock reservation and reduce actual stock (final step).
     */
    public boolean confirmReservation(String reservationId) {
        log.info("Confirming stock reservation: {}", reservationId);
        
        try {
            Optional<StockReservation> reservationOpt = stockReservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                log.warn("Reservation not found: {}", reservationId);
                return false;
            }
            
            StockReservation reservation = reservationOpt.get();
            if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
                log.warn("Reservation is not active: {} - status: {}", reservationId, reservation.getStatus());
                return false;
            }
            
            // Reduce actual stock
            Long itemId = Long.parseLong(reservation.getProductId());
            Optional<InventoryItem> itemOpt = inventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                log.error("Product not found when confirming reservation: {}", reservation.getProductId());
                return false;
            }
            
            InventoryItem item = itemOpt.get();
            item.reduceStock(reservation.getQuantity());
            inventoryItemRepository.save(item);
            
            // Mark reservation as confirmed
            reservation.confirm();
            stockReservationRepository.save(reservation);
            
            log.info("Stock reservation confirmed successfully: {} - reduced stock by {}", 
                reservationId, reservation.getQuantity());
            return true;
            
        } catch (Exception e) {
            log.error("Error confirming reservation {}: {}", reservationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clean up expired reservations (scheduled task).
     */
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Cleaning up expired reservations");
        
        try {
            var expiredReservations = stockReservationRepository.findExpiredReservations(LocalDateTime.now());
            
            for (StockReservation reservation : expiredReservations) {
                reservation.release();
                stockReservationRepository.save(reservation);
                log.info("Released expired reservation: {}", reservation.getReservationId());
            }
            
            log.info("Cleaned up {} expired reservations", expiredReservations.size());
            
        } catch (Exception e) {
            log.error("Error cleaning up expired reservations: {}", e.getMessage(), e);
        }
    }
}