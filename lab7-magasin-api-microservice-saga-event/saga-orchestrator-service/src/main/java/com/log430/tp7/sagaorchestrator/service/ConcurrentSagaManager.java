package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.model.SagaState;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing concurrent saga operations and handling race conditions.
 * Implements database-level locking and optimistic locking for saga state updates.
 */
@Service
public class ConcurrentSagaManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentSagaManager.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;
    
    // In-memory locks for customer-product combinations to prevent race conditions
    private final ConcurrentHashMap<String, ReentrantLock> customerProductLocks = new ConcurrentHashMap<>();
    
    // Maximum retry attempts for optimistic locking conflicts
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;
    
    public ConcurrentSagaManager(
            SagaExecutionRepository sagaExecutionRepository,
            SagaMetrics sagaMetrics,
            SagaEventLogger sagaEventLogger) {
        this.sagaExecutionRepository = sagaExecutionRepository;
        this.sagaMetrics = sagaMetrics;
        this.sagaEventLogger = sagaEventLogger;
    }
    
    /**
     * Safely updates saga state with optimistic locking and retry logic.
     * Handles OptimisticLockException by retrying the operation.
     * 
     * @param sagaId the saga identifier
     * @param newState the new state to transition to
     * @param updateAction additional update action to perform
     * @return updated saga execution
     * @throws RuntimeException if all retry attempts fail
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SagaExecution updateSagaStateWithRetry(String sagaId, SagaState newState, 
                                                  SagaUpdateAction updateAction) {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                // Fetch the latest version of the saga
                Optional<SagaExecution> sagaOpt = sagaExecutionRepository.findById(sagaId);
                if (sagaOpt.isEmpty()) {
                    throw new RuntimeException("Saga not found: " + sagaId);
                }
                
                SagaExecution saga = sagaOpt.get();
                SagaState previousState = saga.getCurrentState();
                
                // Check if state transition is valid
                if (!saga.canTransitionTo(newState)) {
                    logger.warn("Invalid state transition attempted: sagaId={}, from={}, to={}", 
                               sagaId, previousState, newState);
                    throw new RuntimeException("Invalid state transition from " + previousState + " to " + newState);
                }
                
                // Perform the state transition
                saga.transitionTo(newState);
                
                // Execute additional update action if provided
                if (updateAction != null) {
                    updateAction.execute(saga);
                }
                
                // Save with optimistic locking
                SagaExecution updatedSaga = sagaExecutionRepository.save(saga);
                
                // Log successful state transition
                sagaEventLogger.logStateTransition(sagaId, previousState, newState, "concurrent_update");
                sagaMetrics.recordStateTransition(previousState, newState);
                
                logger.debug("Saga state updated successfully: sagaId={}, from={}, to={}, attempt={}", 
                           sagaId, previousState, newState, attempts + 1);
                
                return updatedSaga;
                
            } catch (OptimisticLockException | OptimisticLockingFailureException e) {
                attempts++;
                
                logger.warn("Optimistic lock conflict for saga state update: sagaId={}, attempt={}/{}, error={}", 
                           sagaId, attempts, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    sagaMetrics.recordError("OptimisticLockFailure", "concurrent_update");
                    sagaEventLogger.logSagaError(sagaId, "OptimisticLockFailure", "concurrent_update", 
                                               "Failed to update saga state after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                    
                    throw new RuntimeException("Failed to update saga state after " + MAX_RETRY_ATTEMPTS + 
                                             " attempts due to concurrent modifications", e);
                }
                
                // Wait before retrying with exponential backoff
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry saga update", ie);
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in saga state update retry logic");
    }
    
    /**
     * Checks for concurrent sagas for the same customer and product combination.
     * Prevents race conditions in inventory management.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @param excludeSagaId saga ID to exclude from the check (current saga)
     * @return true if concurrent sagas exist
     */
    @Transactional(readOnly = true)
    public boolean hasConcurrentSagas(String customerId, String productId, String excludeSagaId) {
        List<SagaExecution> activeSagas = sagaExecutionRepository.findActiveSagas();
        
        long concurrentCount = activeSagas.stream()
            .filter(saga -> !saga.getSagaId().equals(excludeSagaId))
            .filter(saga -> saga.getCustomerId().equals(customerId))
            .filter(saga -> saga.getProductId().equals(productId))
            .count();
        
        if (concurrentCount > 0) {
            logger.warn("Concurrent sagas detected: customerId={}, productId={}, count={}, excludeSagaId={}", 
                       customerId, productId, concurrentCount, excludeSagaId);
            
            sagaMetrics.recordError("ConcurrentSagaDetected", "race_condition_check");
        }
        
        return concurrentCount > 0;
    }
    
    /**
     * Acquires a lock for customer-product combination to prevent race conditions.
     * Uses in-memory locks to serialize access to the same customer-product combination.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @param sagaId the saga identifier for logging
     * @return lock key for later release
     */
    public String acquireCustomerProductLock(String customerId, String productId, String sagaId) {
        String lockKey = customerId + ":" + productId;
        
        ReentrantLock lock = customerProductLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        logger.debug("Acquiring customer-product lock: customerId={}, productId={}, sagaId={}", 
                    customerId, productId, sagaId);
        
        try {
            lock.lock();
            
            logger.debug("Customer-product lock acquired: customerId={}, productId={}, sagaId={}", 
                        customerId, productId, sagaId);
            
            sagaEventLogger.logDebugInfo(sagaId, "lock_acquisition", 
                                       "Customer-product lock acquired", 
                                       java.util.Map.of("lockKey", lockKey));
            
            return lockKey;
            
        } catch (Exception e) {
            logger.error("Failed to acquire customer-product lock: customerId={}, productId={}, sagaId={}, error={}", 
                        customerId, productId, sagaId, e.getMessage());
            
            sagaMetrics.recordError("LockAcquisitionFailure", "race_condition_prevention");
            throw new RuntimeException("Failed to acquire customer-product lock", e);
        }
    }
    
    /**
     * Releases the customer-product lock.
     * 
     * @param lockKey the lock key returned by acquireCustomerProductLock
     * @param sagaId the saga identifier for logging
     */
    public void releaseCustomerProductLock(String lockKey, String sagaId) {
        ReentrantLock lock = customerProductLocks.get(lockKey);
        
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                
                logger.debug("Customer-product lock released: lockKey={}, sagaId={}", lockKey, sagaId);
                
                sagaEventLogger.logDebugInfo(sagaId, "lock_release", 
                                           "Customer-product lock released", 
                                           java.util.Map.of("lockKey", lockKey));
                
                // Clean up unused locks to prevent memory leaks
                if (!lock.hasQueuedThreads()) {
                    customerProductLocks.remove(lockKey);
                }
                
            } catch (Exception e) {
                logger.error("Error releasing customer-product lock: lockKey={}, sagaId={}, error={}", 
                           lockKey, sagaId, e.getMessage());
                
                sagaMetrics.recordError("LockReleaseFailure", "race_condition_prevention");
            }
        } else {
            logger.warn("Attempted to release lock not held by current thread: lockKey={}, sagaId={}", 
                       lockKey, sagaId);
        }
    }
    
    /**
     * Finds and handles potential race conditions in active sagas.
     * This method can be called periodically to detect and resolve conflicts.
     * 
     * @return number of potential race conditions detected
     */
    @Transactional(readOnly = true)
    public int detectAndLogRaceConditions() {
        List<SagaExecution> potentialConflicts = sagaExecutionRepository.findPotentialRaceConditions();
        
        if (!potentialConflicts.isEmpty()) {
            logger.warn("Detected {} potential race conditions in active sagas", potentialConflicts.size());
            
            for (SagaExecution saga : potentialConflicts) {
                sagaEventLogger.logSagaError(saga.getSagaId(), "PotentialRaceCondition", "race_condition_detection",
                                           "Multiple active sagas detected for customer-product combination: " +
                                           saga.getCustomerId() + ":" + saga.getProductId(), null);
            }
            
            sagaMetrics.recordError("RaceConditionDetected", "race_condition_detection");
        }
        
        return potentialConflicts.size();
    }
    
    /**
     * Validates that a saga can proceed without causing race conditions.
     * Checks for concurrent sagas and validates current state.
     * 
     * @param saga the saga to validate
     * @return true if saga can proceed safely
     */
    public boolean validateSagaCanProceed(SagaExecution saga) {
        // Check if saga is in a valid state to proceed
        if (saga.isInFinalState()) {
            logger.debug("Saga is in final state, cannot proceed: sagaId={}, state={}", 
                        saga.getSagaId(), saga.getCurrentState());
            return false;
        }
        
        // Check for concurrent sagas
        if (hasConcurrentSagas(saga.getCustomerId(), saga.getProductId(), saga.getSagaId())) {
            logger.warn("Concurrent sagas detected, saga cannot proceed safely: sagaId={}, customerId={}, productId={}", 
                       saga.getSagaId(), saga.getCustomerId(), saga.getProductId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Safely updates saga state using pessimistic locking for critical operations.
     * Prevents concurrent modifications during state transitions.
     * 
     * @param sagaId the saga identifier
     * @param newState the new state to transition to
     * @param updateAction additional update action to perform
     * @return updated saga execution
     * @throws RuntimeException if saga not found or update fails
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SagaExecution updateSagaStateWithPessimisticLock(String sagaId, SagaState newState, 
                                                           SagaUpdateAction updateAction) {
        // Use pessimistic write lock to prevent concurrent modifications
        Optional<SagaExecution> sagaOpt = sagaExecutionRepository.findByIdWithLock(sagaId);
        if (sagaOpt.isEmpty()) {
            throw new RuntimeException("Saga not found: " + sagaId);
        }
        
        SagaExecution saga = sagaOpt.get();
        SagaState previousState = saga.getCurrentState();
        
        // Check if state transition is valid
        if (!saga.canTransitionTo(newState)) {
            logger.warn("Invalid state transition attempted: sagaId={}, from={}, to={}", 
                       sagaId, previousState, newState);
            throw new RuntimeException("Invalid state transition from " + previousState + " to " + newState);
        }
        
        // Perform the state transition
        saga.transitionTo(newState);
        
        // Execute additional update action if provided
        if (updateAction != null) {
            updateAction.execute(saga);
        }
        
        // Save with pessimistic lock already acquired
        SagaExecution updatedSaga = sagaExecutionRepository.save(saga);
        
        // Log successful state transition
        sagaEventLogger.logStateTransition(sagaId, previousState, newState, "pessimistic_lock_update");
        sagaMetrics.recordStateTransition(previousState, newState);
        
        logger.debug("Saga state updated with pessimistic lock: sagaId={}, from={}, to={}", 
                   sagaId, previousState, newState);
        
        return updatedSaga;
    }
    
    /**
     * Handles race conditions during stock reservation by checking for concurrent sagas.
     * Implements first-come-first-served policy for stock reservations.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @param sagaId the current saga identifier
     * @return true if saga can proceed with stock reservation
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean handleStockReservationRaceCondition(String customerId, String productId, String sagaId) {
        // Get lock for customer-product combination
        String lockKey = acquireCustomerProductLock(customerId, productId, sagaId);
        
        try {
            // Find concurrent active sagas with pessimistic lock
            List<SagaExecution> concurrentSagas = sagaExecutionRepository
                .findActiveByCustomerAndProductWithLock(customerId, productId);
            
            // Filter out current saga and check for conflicts
            List<SagaExecution> conflictingSagas = concurrentSagas.stream()
                .filter(saga -> !saga.getSagaId().equals(sagaId))
                .filter(saga -> saga.getCurrentState() == SagaState.STOCK_VERIFYING || 
                               saga.getCurrentState() == SagaState.STOCK_RESERVING)
                .toList();
            
            if (!conflictingSagas.isEmpty()) {
                // Find the earliest saga (first-come-first-served)
                SagaExecution earliestSaga = conflictingSagas.stream()
                    .min((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                    .orElse(null);
                
                // Current saga can proceed only if it's the earliest
                Optional<SagaExecution> currentSaga = concurrentSagas.stream()
                    .filter(saga -> saga.getSagaId().equals(sagaId))
                    .findFirst();
                
                if (currentSaga.isPresent() && earliestSaga != null) {
                    boolean canProceed = currentSaga.get().getCreatedAt().isBefore(earliestSaga.getCreatedAt()) ||
                                        currentSaga.get().getCreatedAt().equals(earliestSaga.getCreatedAt());
                    
                    if (!canProceed) {
                        logger.warn("Stock reservation race condition detected - saga must wait: sagaId={}, " +
                                   "customerId={}, productId={}, conflictingSagas={}", 
                                   sagaId, customerId, productId, conflictingSagas.size());
                        
                        sagaEventLogger.logSagaError(sagaId, "StockReservationRaceCondition", "race_condition_handling",
                                                   "Saga blocked due to concurrent stock reservation conflict", null);
                        sagaMetrics.recordError("StockReservationRaceCondition", "race_condition_handling");
                    }
                    
                    return canProceed;
                }
            }
            
            // No conflicts found, saga can proceed
            return true;
            
        } finally {
            releaseCustomerProductLock(lockKey, sagaId);
        }
    }
    
    /**
     * Validates and handles concurrent saga execution for inventory management.
     * Implements business rules for handling multiple sagas accessing the same inventory.
     * 
     * @param saga the saga to validate
     * @return validation result with recommendations
     */
    @Transactional(readOnly = true)
    public ConcurrencyValidationResult validateConcurrentInventoryAccess(SagaExecution saga) {
        // Count concurrent sagas for the same product
        long concurrentCount = sagaExecutionRepository.countConcurrentActiveSagas(
            saga.getCustomerId(), saga.getProductId(), saga.getSagaId());
        
        if (concurrentCount == 0) {
            return new ConcurrencyValidationResult(true, "No concurrent sagas detected", null);
        }
        
        // Check if current saga should be prioritized based on business rules
        List<SagaExecution> concurrentSagas = sagaExecutionRepository
            .findActiveByCustomerAndProductWithLock(saga.getCustomerId(), saga.getProductId());
        
        // Apply business rules for prioritization
        boolean shouldProceed = applyConcurrencyBusinessRules(saga, concurrentSagas);
        
        String message = shouldProceed ? 
            "Saga can proceed despite concurrent access" : 
            "Saga should wait due to concurrent access";
        
        List<String> recommendations = generateConcurrencyRecommendations(saga, concurrentSagas);
        
        return new ConcurrencyValidationResult(shouldProceed, message, recommendations);
    }
    
    /**
     * Applies business rules for handling concurrent saga execution.
     * 
     * @param currentSaga the current saga
     * @param concurrentSagas list of concurrent sagas
     * @return true if current saga should proceed
     */
    private boolean applyConcurrencyBusinessRules(SagaExecution currentSaga, List<SagaExecution> concurrentSagas) {
        // Rule 1: First-come-first-served based on creation time
        boolean isEarliest = concurrentSagas.stream()
            .filter(saga -> !saga.getSagaId().equals(currentSaga.getSagaId()))
            .allMatch(saga -> currentSaga.getCreatedAt().isBefore(saga.getCreatedAt()) ||
                             currentSaga.getCreatedAt().equals(saga.getCreatedAt()));
        
        if (isEarliest) {
            return true;
        }
        
        // Rule 2: Priority based on saga state (more advanced states have priority)
        int currentStateOrder = getStateOrder(currentSaga.getCurrentState());
        boolean hasHigherPriority = concurrentSagas.stream()
            .filter(saga -> !saga.getSagaId().equals(currentSaga.getSagaId()))
            .allMatch(saga -> currentStateOrder >= getStateOrder(saga.getCurrentState()));
        
        return hasHigherPriority;
    }
    
    /**
     * Gets the order/priority of a saga state for concurrency resolution.
     */
    private int getStateOrder(SagaState state) {
        return switch (state) {
            case SALE_INITIATED -> 1;
            case STOCK_VERIFYING -> 2;
            case STOCK_RESERVING -> 3;
            case PAYMENT_PROCESSING -> 4;
            case ORDER_CONFIRMING -> 5;
            case STOCK_RELEASING -> 6;
            case SALE_CONFIRMED, SALE_FAILED -> 7;
        };
    }
    
    /**
     * Generates recommendations for handling concurrent saga execution.
     */
    private List<String> generateConcurrencyRecommendations(SagaExecution currentSaga, 
                                                           List<SagaExecution> concurrentSagas) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (concurrentSagas.size() > 3) {
            recommendations.add("High concurrency detected - consider implementing queue-based processing");
        }
        
        long oldSagas = concurrentSagas.stream()
            .filter(saga -> saga.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5)))
            .count();
        
        if (oldSagas > 0) {
            recommendations.add("Old sagas detected - consider timeout cleanup");
        }
        
        boolean hasStuckSagas = concurrentSagas.stream()
            .anyMatch(saga -> saga.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(2)));
        
        if (hasStuckSagas) {
            recommendations.add("Stuck sagas detected - investigate potential deadlocks");
        }
        
        return recommendations;
    }
    
    /**
     * Monitors and reports on concurrent saga execution patterns.
     * 
     * @return monitoring report
     */
    @Transactional(readOnly = true)
    public ConcurrencyMonitoringReport generateConcurrencyReport() {
        List<SagaExecution> activeSagas = sagaExecutionRepository.findActiveSagas();
        
        // Group by customer-product combinations
        java.util.Map<String, List<SagaExecution>> groupedSagas = activeSagas.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                saga -> saga.getCustomerId() + ":" + saga.getProductId()));
        
        // Find high-concurrency combinations
        List<String> highConcurrencyCombinations = groupedSagas.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(java.util.Map.Entry::getKey)
            .toList();
        
        // Calculate statistics
        int totalActiveSagas = activeSagas.size();
        int concurrentCombinations = highConcurrencyCombinations.size();
        int activeLocks = customerProductLocks.size();
        
        return new ConcurrencyMonitoringReport(
            totalActiveSagas,
            concurrentCombinations,
            activeLocks,
            highConcurrencyCombinations,
            LocalDateTime.now()
        );
    }
    
    /**
     * Gets statistics about concurrent saga operations for monitoring.
     * 
     * @return statistics map
     */
    public java.util.Map<String, Object> getConcurrencyStatistics() {
        int activeLocks = customerProductLocks.size();
        int potentialConflicts = detectAndLogRaceConditions();
        ConcurrencyMonitoringReport report = generateConcurrencyReport();
        
        return java.util.Map.of(
            "activeLocks", activeLocks,
            "potentialConflicts", potentialConflicts,
            "totalActiveSagas", report.totalActiveSagas(),
            "concurrentCombinations", report.concurrentCombinations(),
            "timestamp", LocalDateTime.now()
        );
    }
    
    /**
     * Functional interface for saga update actions.
     */
    @FunctionalInterface
    public interface SagaUpdateAction {
        void execute(SagaExecution saga);
    }
    
    /**
     * Result of concurrency validation.
     */
    public record ConcurrencyValidationResult(
        boolean canProceed,
        String message,
        List<String> recommendations
    ) {}
    
    /**
     * Monitoring report for concurrent saga execution.
     */
    public record ConcurrencyMonitoringReport(
        int totalActiveSagas,
        int concurrentCombinations,
        int activeLocks,
        List<String> highConcurrencyCombinations,
        LocalDateTime timestamp
    ) {}
}