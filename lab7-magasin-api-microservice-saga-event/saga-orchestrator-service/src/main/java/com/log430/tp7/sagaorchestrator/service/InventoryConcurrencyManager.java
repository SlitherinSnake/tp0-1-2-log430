package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.dto.StockReservationResponse;
import com.log430.tp7.sagaorchestrator.dto.StockVerificationRequest;
import com.log430.tp7.sagaorchestrator.dto.StockVerificationResponse;
import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service responsible for managing concurrent inventory operations and preventing race conditions
 * during stock verification and reservation processes.
 */
@Service
public class InventoryConcurrencyManager {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryConcurrencyManager.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    private final ServiceClientWrapper serviceClientWrapper;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;
    
    // Product-level locks for inventory operations
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> productLocks = new ConcurrentHashMap<>();
    
    // Maximum retry attempts for inventory operations
    private static final int MAX_INVENTORY_RETRY_ATTEMPTS = 5;
    private static final long INVENTORY_RETRY_DELAY_MS = 200;
    
    public InventoryConcurrencyManager(
            SagaExecutionRepository sagaExecutionRepository,
            ServiceClientWrapper serviceClientWrapper,
            SagaMetrics sagaMetrics,
            SagaEventLogger sagaEventLogger) {
        this.sagaExecutionRepository = sagaExecutionRepository;
        this.serviceClientWrapper = serviceClientWrapper;
        this.sagaMetrics = sagaMetrics;
        this.sagaEventLogger = sagaEventLogger;
    }
    
    /**
     * Safely verifies stock availability with concurrent access protection.
     * Uses read locks to allow multiple concurrent verifications while preventing
     * conflicts with reservation operations.
     * 
     * @param productId the product identifier
     * @param quantity the requested quantity
     * @param sagaId the saga identifier
     * @return stock verification response
     * @throws RuntimeException if verification fails after retries
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockVerificationResponse verifyStockWithConcurrencyProtection(String productId, Integer quantity, String sagaId) {
        ReentrantReadWriteLock productLock = getProductLock(productId);
        
        logger.debug("Acquiring read lock for stock verification: productId={}, sagaId={}", productId, sagaId);
        
        productLock.readLock().lock();
        try {
            sagaEventLogger.logDebugInfo(sagaId, "inventory_lock_acquisition", 
                                       "Read lock acquired for stock verification", 
                                       java.util.Map.of("productId", productId, "lockType", "READ"));
            
            // Check for concurrent reserving sagas that might affect availability
            List<SagaExecution> reservingSagas = sagaExecutionRepository.findStockReservingSagasWithLock(productId);
            
            if (!reservingSagas.isEmpty()) {
                logger.warn("Concurrent stock reserving sagas detected during verification: productId={}, " +
                           "sagaId={}, concurrentSagas={}", productId, sagaId, reservingSagas.size());
                
                sagaMetrics.recordError("ConcurrentReservingDetected", "stock_verification");
            }
            
            // Perform stock verification with retry logic
            return performStockVerificationWithRetry(productId, quantity, sagaId);
            
        } finally {
            productLock.readLock().unlock();
            sagaEventLogger.logDebugInfo(sagaId, "inventory_lock_release", 
                                       "Read lock released for stock verification", 
                                       java.util.Map.of("productId", productId, "lockType", "READ"));
            
            logger.debug("Released read lock for stock verification: productId={}, sagaId={}", productId, sagaId);
        }
    }
    
    /**
     * Safely reserves stock with exclusive access protection.
     * Uses write locks to ensure atomic reservation operations and prevent
     * race conditions between multiple sagas.
     * 
     * @param productId the product identifier
     * @param quantity the quantity to reserve
     * @param sagaId the saga identifier
     * @param customerId the customer identifier
     * @return stock reservation response
     * @throws RuntimeException if reservation fails after retries
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StockReservationResponse reserveStockWithConcurrencyProtection(String productId, Integer quantity, 
                                                                         String sagaId, String customerId) {
        ReentrantReadWriteLock productLock = getProductLock(productId);
        
        logger.debug("Acquiring write lock for stock reservation: productId={}, sagaId={}", productId, sagaId);
        
        productLock.writeLock().lock();
        try {
            sagaEventLogger.logDebugInfo(sagaId, "inventory_lock_acquisition", 
                                       "Write lock acquired for stock reservation", 
                                       java.util.Map.of("productId", productId, "lockType", "WRITE"));
            
            // Double-check for concurrent sagas before proceeding
            long concurrentCount = sagaExecutionRepository.countConcurrentActiveSagas(customerId, productId, sagaId);
            
            if (concurrentCount > 0) {
                logger.warn("Concurrent active sagas detected during reservation: productId={}, sagaId={}, " +
                           "customerId={}, concurrentCount={}", productId, sagaId, customerId, concurrentCount);
                
                // Apply business rules to determine if reservation should proceed
                if (!shouldProceedWithReservation(productId, sagaId, customerId, concurrentCount)) {
                    sagaMetrics.recordError("ReservationBlockedByConcurrency", "stock_reservation");
                    throw new RuntimeException("Stock reservation blocked due to concurrent saga operations");
                }
            }
            
            // Perform stock reservation with retry logic
            return performStockReservationWithRetry(productId, quantity, sagaId, customerId);
            
        } finally {
            productLock.writeLock().unlock();
            sagaEventLogger.logDebugInfo(sagaId, "inventory_lock_release", 
                                       "Write lock released for stock reservation", 
                                       java.util.Map.of("productId", productId, "lockType", "WRITE"));
            
            logger.debug("Released write lock for stock reservation: productId={}, sagaId={}", productId, sagaId);
        }
    }
    
    /**
     * Gets or creates a product-level lock for inventory operations.
     * 
     * @param productId the product identifier
     * @return read-write lock for the product
     */
    private ReentrantReadWriteLock getProductLock(String productId) {
        return productLocks.computeIfAbsent(productId, k -> new ReentrantReadWriteLock(true)); // Fair lock
    }
    
    /**
     * Performs stock verification with retry logic for transient failures.
     */
    private StockVerificationResponse performStockVerificationWithRetry(String productId, Integer quantity, String sagaId) {
        int attempts = 0;
        
        while (attempts < MAX_INVENTORY_RETRY_ATTEMPTS) {
            try {
                StockVerificationRequest request = new StockVerificationRequest(productId, quantity, sagaId);
                return serviceClientWrapper.verifyStockSync(productId, quantity, sagaId);
                
            } catch (Exception e) {
                attempts++;
                
                logger.warn("Stock verification attempt failed: productId={}, sagaId={}, attempt={}/{}, error={}", 
                           productId, sagaId, attempts, MAX_INVENTORY_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempts >= MAX_INVENTORY_RETRY_ATTEMPTS) {
                    sagaMetrics.recordError("StockVerificationRetryExhausted", "stock_verification");
                    throw new RuntimeException("Stock verification failed after " + MAX_INVENTORY_RETRY_ATTEMPTS + 
                                             " attempts: " + e.getMessage(), e);
                }
                
                // Wait before retrying with exponential backoff
                try {
                    Thread.sleep(INVENTORY_RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying stock verification", ie);
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in stock verification retry logic");
    }
    
    /**
     * Performs stock reservation with retry logic for transient failures.
     */
    private StockReservationResponse performStockReservationWithRetry(String productId, Integer quantity, 
                                                                     String sagaId, String customerId) {
        int attempts = 0;
        
        while (attempts < MAX_INVENTORY_RETRY_ATTEMPTS) {
            try {
                return serviceClientWrapper.reserveStockSync(productId, quantity, sagaId, customerId);
                
            } catch (Exception e) {
                attempts++;
                
                logger.warn("Stock reservation attempt failed: productId={}, sagaId={}, attempt={}/{}, error={}", 
                           productId, sagaId, attempts, MAX_INVENTORY_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempts >= MAX_INVENTORY_RETRY_ATTEMPTS) {
                    sagaMetrics.recordError("StockReservationRetryExhausted", "stock_reservation");
                    throw new RuntimeException("Stock reservation failed after " + MAX_INVENTORY_RETRY_ATTEMPTS + 
                                             " attempts: " + e.getMessage(), e);
                }
                
                // Wait before retrying with exponential backoff
                try {
                    Thread.sleep(INVENTORY_RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying stock reservation", ie);
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in stock reservation retry logic");
    }
    
    /**
     * Determines if a saga should proceed with stock reservation based on business rules.
     * 
     * @param productId the product identifier
     * @param sagaId the current saga identifier
     * @param customerId the customer identifier
     * @param concurrentCount the number of concurrent sagas
     * @return true if reservation should proceed
     */
    private boolean shouldProceedWithReservation(String productId, String sagaId, String customerId, long concurrentCount) {
        // Business rule: Allow up to 3 concurrent reservations per product
        if (concurrentCount > 3) {
            logger.warn("Too many concurrent reservations for product: productId={}, sagaId={}, concurrentCount={}", 
                       productId, sagaId, concurrentCount);
            return false;
        }
        
        // Business rule: Check if current saga is the oldest among concurrent sagas
        List<SagaExecution> concurrentSagas = sagaExecutionRepository
            .findActiveByCustomerAndProductWithLock(customerId, productId);
        
        return concurrentSagas.stream()
            .filter(saga -> !saga.getSagaId().equals(sagaId))
            .allMatch(saga -> {
                // Find current saga in the list
                SagaExecution currentSaga = concurrentSagas.stream()
                    .filter(s -> s.getSagaId().equals(sagaId))
                    .findFirst()
                    .orElse(null);
                
                if (currentSaga == null) {
                    return false; // Current saga not found, don't proceed
                }
                
                // Current saga can proceed if it's older or equal in age
                return currentSaga.getCreatedAt().isBefore(saga.getCreatedAt()) ||
                       currentSaga.getCreatedAt().equals(saga.getCreatedAt());
            });
    }
    
    /**
     * Releases stock reservation with concurrent access protection.
     * 
     * @param reservationId the reservation identifier
     * @param sagaId the saga identifier
     * @return true if release was successful
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean releaseStockReservationSafely(String reservationId, String sagaId) {
        try {
            // Note: This would typically call the inventory service to release the reservation
            // For now, we'll use the service client wrapper
            serviceClientWrapper.releaseStockReservationSync(reservationId, sagaId);
            
            sagaEventLogger.logDebugInfo(sagaId, "stock_release", 
                                       "Stock reservation released successfully", 
                                       java.util.Map.of("reservationId", reservationId));
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to release stock reservation: reservationId={}, sagaId={}, error={}", 
                        reservationId, sagaId, e.getMessage());
            
            sagaMetrics.recordError("StockReleaseFailure", "stock_release");
            return false;
        }
    }
    
    /**
     * Gets statistics about inventory concurrency operations.
     * 
     * @return statistics map
     */
    public java.util.Map<String, Object> getInventoryConcurrencyStatistics() {
        int activeProductLocks = productLocks.size();
        
        // Count read and write locks currently held
        int activeReadLocks = 0;
        int activeWriteLocks = 0;
        
        for (ReentrantReadWriteLock lock : productLocks.values()) {
            if (lock.getReadLockCount() > 0) {
                activeReadLocks++;
            }
            if (lock.isWriteLocked()) {
                activeWriteLocks++;
            }
        }
        
        return java.util.Map.of(
            "activeProductLocks", activeProductLocks,
            "activeReadLocks", activeReadLocks,
            "activeWriteLocks", activeWriteLocks,
            "timestamp", LocalDateTime.now()
        );
    }
    
    /**
     * Cleans up unused product locks to prevent memory leaks.
     */
    public void cleanupUnusedLocks() {
        productLocks.entrySet().removeIf(entry -> {
            ReentrantReadWriteLock lock = entry.getValue();
            boolean isUnused = !lock.isWriteLocked() && lock.getReadLockCount() == 0 && !lock.hasQueuedThreads();
            
            if (isUnused) {
                logger.debug("Cleaning up unused product lock: productId={}", entry.getKey());
            }
            
            return isUnused;
        });
    }
}