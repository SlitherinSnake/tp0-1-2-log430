package com.log430.tp6.sagaorchestrator.repository;

import com.log430.tp6.sagaorchestrator.model.SagaExecution;
import com.log430.tp6.sagaorchestrator.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SagaExecution entity with custom query methods.
 * Provides methods for finding active sagas, timed-out sagas, and state-based queries.
 */
@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, String> {
    
    /**
     * Finds all active (non-final) sagas.
     * 
     * @return list of active saga executions
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED')")
    List<SagaExecution> findActiveSagas();
    
    /**
     * Finds sagas in a specific state.
     * 
     * @param state the saga state to filter by
     * @return list of sagas in the specified state
     */
    List<SagaExecution> findByCurrentState(SagaState state);
    
    /**
     * Finds sagas in multiple states.
     * 
     * @param states the saga states to filter by
     * @return list of sagas in any of the specified states
     */
    List<SagaExecution> findByCurrentStateIn(List<SagaState> states);
    
    /**
     * Finds sagas for a specific customer.
     * 
     * @param customerId the customer identifier
     * @return list of sagas for the customer
     */
    List<SagaExecution> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    /**
     * Finds sagas for a specific product.
     * 
     * @param productId the product identifier
     * @return list of sagas for the product
     */
    List<SagaExecution> findByProductIdOrderByCreatedAtDesc(String productId);
    
    /**
     * Finds timed-out sagas that have been running longer than the specified duration.
     * 
     * @param timeoutMinutes the timeout duration in minutes
     * @return list of timed-out saga executions
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED') " +
           "AND s.updatedAt < :timeoutThreshold")
    List<SagaExecution> findTimedOutSagas(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    /**
     * Finds sagas that have been in a specific state longer than the specified duration.
     * 
     * @param state the saga state to check
     * @param timeoutMinutes the timeout duration in minutes
     * @return list of sagas stuck in the specified state
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState = :state " +
           "AND s.updatedAt < :timeoutThreshold")
    List<SagaExecution> findSagasStuckInState(@Param("state") SagaState state, 
                                              @Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    /**
     * Finds sagas created within a specific time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of sagas created within the time range
     */
    List<SagaExecution> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Finds sagas that completed (successfully or failed) within a specific time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of completed sagas
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState IN ('SALE_CONFIRMED', 'SALE_FAILED') " +
           "AND s.updatedAt BETWEEN :startTime AND :endTime ORDER BY s.updatedAt DESC")
    List<SagaExecution> findCompletedSagasBetween(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);
    
    /**
     * Finds failed sagas within a specific time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of failed sagas
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState = 'SALE_FAILED' " +
           "AND s.updatedAt BETWEEN :startTime AND :endTime ORDER BY s.updatedAt DESC")
    List<SagaExecution> findFailedSagasBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * Finds successful sagas within a specific time range.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of successful sagas
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState = 'SALE_CONFIRMED' " +
           "AND s.updatedAt BETWEEN :startTime AND :endTime ORDER BY s.updatedAt DESC")
    List<SagaExecution> findSuccessfulSagasBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * Counts sagas by state within a specific time range.
     * 
     * @param state the saga state to count
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return count of sagas in the specified state
     */
    long countByCurrentStateAndCreatedAtBetween(SagaState state, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Counts active sagas for a specific customer.
     * 
     * @param customerId the customer identifier
     * @return count of active sagas for the customer
     */
    @Query("SELECT COUNT(s) FROM SagaExecution s WHERE s.customerId = :customerId " +
           "AND s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED')")
    long countActiveSagasForCustomer(@Param("customerId") String customerId);
    
    /**
     * Counts active sagas for a specific product.
     * 
     * @param productId the product identifier
     * @return count of active sagas for the product
     */
    @Query("SELECT COUNT(s) FROM SagaExecution s WHERE s.productId = :productId " +
           "AND s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED')")
    long countActiveSagasForProduct(@Param("productId") String productId);
    
    /**
     * Finds sagas with stock reservations that need cleanup.
     * 
     * @return list of sagas with stock reservations
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.stockReservationId IS NOT NULL " +
           "AND s.currentState IN ('STOCK_RELEASING', 'SALE_FAILED')")
    List<SagaExecution> findSagasWithStockReservations();
    
    /**
     * Finds sagas with payment transactions for reconciliation.
     * 
     * @return list of sagas with payment transactions
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.paymentTransactionId IS NOT NULL")
    List<SagaExecution> findSagasWithPaymentTransactions();
    
    /**
     * Finds the most recent saga for a customer and product combination.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @return the most recent saga, if any
     */
    Optional<SagaExecution> findFirstByCustomerIdAndProductIdOrderByCreatedAtDesc(String customerId, String productId);
    
    /**
     * Finds long-running sagas that exceed the specified duration.
     * 
     * @param durationMinutes the duration threshold in minutes
     * @return list of long-running sagas
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED') " +
           "AND s.createdAt < :durationThreshold ORDER BY s.createdAt ASC")
    List<SagaExecution> findLongRunningSagas(@Param("durationThreshold") LocalDateTime durationThreshold);
    
    /**
     * Updates the error message for a saga.
     * 
     * @param sagaId the saga identifier
     * @param errorMessage the error message to set
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.errorMessage = :errorMessage, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.sagaId = :sagaId")
    int updateErrorMessage(@Param("sagaId") String sagaId, @Param("errorMessage") String errorMessage);
    
    /**
     * Updates the stock reservation ID for a saga.
     * 
     * @param sagaId the saga identifier
     * @param stockReservationId the stock reservation ID to set
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.stockReservationId = :stockReservationId, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.sagaId = :sagaId")
    int updateStockReservationId(@Param("sagaId") String sagaId, @Param("stockReservationId") String stockReservationId);
    
    /**
     * Updates the payment transaction ID for a saga.
     * 
     * @param sagaId the saga identifier
     * @param paymentTransactionId the payment transaction ID to set
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.paymentTransactionId = :paymentTransactionId, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.sagaId = :sagaId")
    int updatePaymentTransactionId(@Param("sagaId") String sagaId, @Param("paymentTransactionId") String paymentTransactionId);
    
    /**
     * Updates the order ID for a saga.
     * 
     * @param sagaId the saga identifier
     * @param orderId the order ID to set
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.orderId = :orderId, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.sagaId = :sagaId")
    int updateOrderId(@Param("sagaId") String sagaId, @Param("orderId") String orderId);
    
    /**
     * Deletes old completed sagas beyond the retention period.
     * 
     * @param cutoffDate the date before which sagas should be deleted
     * @return number of deleted sagas
     */
    @Modifying
    @Query("DELETE FROM SagaExecution s WHERE s.currentState IN ('SALE_CONFIRMED', 'SALE_FAILED') " +
           "AND s.updatedAt < :cutoffDate")
    int deleteCompletedSagasOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Gets saga execution statistics for monitoring.
     * 
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of state counts
     */
    @Query("SELECT s.currentState, COUNT(s) FROM SagaExecution s " +
           "WHERE s.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY s.currentState")
    List<Object[]> getSagaStatistics(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Finds sagas that might have race conditions (multiple sagas for same customer/product).
     * 
     * @return list of potentially conflicting sagas
     */
    @Query("SELECT s FROM SagaExecution s WHERE s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED') " +
           "AND EXISTS (SELECT s2 FROM SagaExecution s2 WHERE s2.customerId = s.customerId " +
           "AND s2.productId = s.productId AND s2.sagaId != s.sagaId " +
           "AND s2.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED'))")
    List<SagaExecution> findPotentialRaceConditions();
    
    /**
     * Finds and locks a saga by ID with pessimistic write lock for concurrent updates.
     * Prevents concurrent modifications during critical saga state transitions.
     * 
     * @param sagaId the saga identifier
     * @return saga execution with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SagaExecution s WHERE s.sagaId = :sagaId")
    Optional<SagaExecution> findByIdWithLock(@Param("sagaId") String sagaId);
    
    /**
     * Finds and locks active sagas for a customer-product combination with pessimistic read lock.
     * Prevents race conditions during stock reservation conflicts.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @return list of active sagas with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT s FROM SagaExecution s WHERE s.customerId = :customerId AND s.productId = :productId " +
           "AND s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED') ORDER BY s.createdAt ASC")
    List<SagaExecution> findActiveByCustomerAndProductWithLock(@Param("customerId") String customerId, 
                                                               @Param("productId") String productId);
    
    /**
     * Finds and locks sagas in stock reserving state for a specific product with pessimistic write lock.
     * Used to prevent concurrent stock reservation conflicts.
     * 
     * @param productId the product identifier
     * @return list of sagas in stock reserving state with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SagaExecution s WHERE s.productId = :productId " +
           "AND s.currentState IN ('STOCK_VERIFYING', 'STOCK_RESERVING') ORDER BY s.createdAt ASC")
    List<SagaExecution> findStockReservingSagasWithLock(@Param("productId") String productId);
    
    /**
     * Atomically updates saga state with version check for optimistic locking.
     * 
     * @param sagaId the saga identifier
     * @param newState the new state to set
     * @param expectedVersion the expected version for optimistic locking
     * @return number of updated records (0 if version mismatch)
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.currentState = :newState, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.sagaId = :sagaId AND s.version = :expectedVersion")
    int updateStateWithVersionCheck(@Param("sagaId") String sagaId, 
                                   @Param("newState") SagaState newState,
                                   @Param("expectedVersion") Long expectedVersion);
    
    /**
     * Atomically updates saga state and stock reservation ID with version check.
     * 
     * @param sagaId the saga identifier
     * @param newState the new state to set
     * @param stockReservationId the stock reservation ID to set
     * @param expectedVersion the expected version for optimistic locking
     * @return number of updated records (0 if version mismatch)
     */
    @Modifying
    @Query("UPDATE SagaExecution s SET s.currentState = :newState, s.stockReservationId = :stockReservationId, " +
           "s.updatedAt = CURRENT_TIMESTAMP WHERE s.sagaId = :sagaId AND s.version = :expectedVersion")
    int updateStateAndStockReservationWithVersionCheck(@Param("sagaId") String sagaId, 
                                                       @Param("newState") SagaState newState,
                                                       @Param("stockReservationId") String stockReservationId,
                                                       @Param("expectedVersion") Long expectedVersion);
    
    /**
     * Counts concurrent active sagas for the same customer-product combination.
     * Used for race condition detection.
     * 
     * @param customerId the customer identifier
     * @param productId the product identifier
     * @param excludeSagaId saga ID to exclude from count
     * @return count of concurrent active sagas
     */
    @Query("SELECT COUNT(s) FROM SagaExecution s WHERE s.customerId = :customerId AND s.productId = :productId " +
           "AND s.sagaId != :excludeSagaId AND s.currentState NOT IN ('SALE_CONFIRMED', 'SALE_FAILED')")
    long countConcurrentActiveSagas(@Param("customerId") String customerId, 
                                   @Param("productId") String productId,
                                   @Param("excludeSagaId") String excludeSagaId);
}