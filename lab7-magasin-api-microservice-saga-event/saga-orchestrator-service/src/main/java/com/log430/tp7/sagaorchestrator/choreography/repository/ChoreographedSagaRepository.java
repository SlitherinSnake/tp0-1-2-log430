package com.log430.tp7.sagaorchestrator.choreography.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;

/**
 * Repository interface for choreographed saga state persistence operations.
 * Provides specialized queries for saga coordination, timeout detection, and monitoring.
 */
@Repository
public interface ChoreographedSagaRepository extends JpaRepository<ChoreographedSagaState, String> {
    
    /**
     * Find saga state by correlation ID.
     */
    Optional<ChoreographedSagaState> findByCorrelationId(String correlationId);
    
    /**
     * Find all saga states by correlation ID (in case of multiple related sagas).
     */
    List<ChoreographedSagaState> findAllByCorrelationId(String correlationId);
    
    /**
     * Find sagas by type and status.
     */
    List<ChoreographedSagaState> findBySagaTypeAndStatus(String sagaType, ChoreographedSagaStatus status);
    
    /**
     * Find sagas that require compensation.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.compensationRequired = true AND s.compensationCompleted = false")
    List<ChoreographedSagaState> findSagasRequiringCompensation();
    
    /**
     * Find active sagas (not in final state).
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.status IN ('STARTED', 'IN_PROGRESS', 'RETRYING', 'COMPENSATING')")
    List<ChoreographedSagaState> findActiveSagas();
    
    /**
     * Find timed out sagas that need attention.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.timeoutAt < :currentTime AND s.status IN ('STARTED', 'IN_PROGRESS', 'RETRYING')")
    List<ChoreographedSagaState> findTimedOutSagas(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find sagas by status with pagination.
     */
    Page<ChoreographedSagaState> findByStatusOrderByCreatedAtDesc(ChoreographedSagaStatus status, Pageable pageable);
    
    /**
     * Find sagas created within a time range.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.createdAt BETWEEN :startTime AND :endTime ORDER BY s.createdAt DESC")
    Page<ChoreographedSagaState> findByCreatedAtBetween(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        Pageable pageable);
    
    /**
     * Find sagas that have been retrying for too long.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.status = 'RETRYING' AND s.retryCount >= s.maxRetries")
    List<ChoreographedSagaState> findSagasExceedingMaxRetries();
    
    /**
     * Count sagas by status.
     */
    @Query("SELECT s.status, COUNT(s) FROM ChoreographedSagaState s GROUP BY s.status")
    List<Object[]> countSagasByStatus();
    
    /**
     * Count sagas by type.
     */
    @Query("SELECT s.sagaType, COUNT(s) FROM ChoreographedSagaState s GROUP BY s.sagaType")
    List<Object[]> countSagasByType();
    
    /**
     * Find sagas with specific step completed.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.completedSteps LIKE %:stepName%")
    List<ChoreographedSagaState> findSagasWithCompletedStep(@Param("stepName") String stepName);
    
    /**
     * Find sagas with specific step failed.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.failedSteps LIKE %:stepName%")
    List<ChoreographedSagaState> findSagasWithFailedStep(@Param("stepName") String stepName);
    
    /**
     * Find recent completed sagas for monitoring.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.status = 'COMPLETED' AND s.completedAt >= :since ORDER BY s.completedAt DESC")
    List<ChoreographedSagaState> findRecentlyCompletedSagas(@Param("since") LocalDateTime since);
    
    /**
     * Find recent failed sagas for error analysis.
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.status IN ('FAILED', 'COMPENSATED') AND s.completedAt >= :since ORDER BY s.completedAt DESC")
    List<ChoreographedSagaState> findRecentlyFailedSagas(@Param("since") LocalDateTime since);
    
    /**
     * Get saga performance metrics (average duration).
     */
    @Query(value = "SELECT s.saga_type, AVG(EXTRACT(EPOCH FROM (s.completed_at - s.created_at))) " +
           "FROM choreographed_saga_state s WHERE s.completed_at IS NOT NULL GROUP BY s.saga_type", 
           nativeQuery = true)
    List<Object[]> getSagaPerformanceMetrics();
    
    /**
     * Find sagas that need cleanup (old final states).
     */
    @Query("SELECT s FROM ChoreographedSagaState s WHERE s.status IN ('COMPLETED', 'FAILED', 'COMPENSATED') AND s.completedAt < :cutoffDate")
    List<ChoreographedSagaState> findSagasForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Check if a correlation ID has any active sagas.
     */
    @Query("SELECT COUNT(s) > 0 FROM ChoreographedSagaState s WHERE s.correlationId = :correlationId AND s.status IN ('STARTED', 'IN_PROGRESS', 'RETRYING', 'COMPENSATING')")
    boolean hasActiveSagasForCorrelation(@Param("correlationId") String correlationId);
    
    /**
     * Count sagas by status for monitoring.
     */
    long countByStatus(ChoreographedSagaStatus status);
    
    /**
     * Find sagas by specific status.
     */
    List<ChoreographedSagaState> findByStatus(ChoreographedSagaStatus status);
    
    /**
     * Find sagas created after specified time.
     */
    List<ChoreographedSagaState> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Delete old saga states for data retention.
     */
    @Query("DELETE FROM ChoreographedSagaState s WHERE s.completedAt < :cutoffDate AND s.status IN ('COMPLETED', 'FAILED', 'COMPENSATED')")
    int deleteOldSagaStates(@Param("cutoffDate") LocalDateTime cutoffDate);
}
