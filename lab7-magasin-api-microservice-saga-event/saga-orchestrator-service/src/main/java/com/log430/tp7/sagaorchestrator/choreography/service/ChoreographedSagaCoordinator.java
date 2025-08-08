package com.log430.tp7.sagaorchestrator.choreography.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationCoordinator;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;

/**
 * Service for managing choreographed saga coordination.
 * Handles saga state tracking, timeout detection, and compensation coordination
 * without central orchestration.
 */
@Service
public class ChoreographedSagaCoordinator {
    
    private static final Logger log = LoggerFactory.getLogger(ChoreographedSagaCoordinator.class);
    private static final String NO_SAGA_FOUND_MSG = "No saga found for correlation ID: {}";
    
    private final ChoreographedSagaRepository sagaRepository;
    private final CompensationCoordinator compensationCoordinator;
    
    public ChoreographedSagaCoordinator(ChoreographedSagaRepository sagaRepository,
                                      CompensationCoordinator compensationCoordinator) {
        this.sagaRepository = sagaRepository;
        this.compensationCoordinator = compensationCoordinator;
    }
    
    /**
     * Initiates a new choreographed saga with default timeout.
     */
    @Transactional
    public ChoreographedSagaState initiateSaga(String correlationId, String sagaType, String sagaData) {
        return createSaga(correlationId, sagaType, sagaData, 30); // Default 30-minute timeout
    }
    
    /**
     * Initiates a new choreographed saga with custom timeout.
     */
    @Transactional
    public ChoreographedSagaState initiateSagaWithTimeout(String correlationId, String sagaType, String sagaData, int timeoutMinutes) {
        return createSaga(correlationId, sagaType, sagaData, timeoutMinutes);
    }
    
    /**
     * Internal method to create saga instance.
     */
    private ChoreographedSagaState createSaga(String correlationId, String sagaType, String sagaData, int timeoutMinutes) {
        String sagaId = UUID.randomUUID().toString();
        
        log.info("Initiating choreographed saga: sagaId={}, correlationId={}, type={}", 
                sagaId, correlationId, sagaType);
        
        ChoreographedSagaState sagaState = new ChoreographedSagaState(sagaId, correlationId, sagaType);
        sagaState.setSagaData(sagaData);
        sagaState.setTimeoutAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        
        sagaState = sagaRepository.save(sagaState);
        
        log.info("Choreographed saga initiated successfully: sagaId={}, correlationId={}", 
                sagaId, correlationId);
        
        return sagaState;
    }
    
    /**
     * Records a successful step completion for a saga.
     */
    @Transactional
    public ChoreographedSagaState recordStepCompleted(String correlationId, String stepName, String stepData) {
        Optional<ChoreographedSagaState> sagaOpt = sagaRepository.findByCorrelationId(correlationId);
        
        if (sagaOpt.isEmpty()) {
            log.warn(NO_SAGA_FOUND_MSG, correlationId);
            return null;
        }
        
        ChoreographedSagaState saga = sagaOpt.get();
        
        log.info("Recording step completion: sagaId={}, correlationId={}, step={}", 
                saga.getSagaId(), correlationId, stepName);
        
        saga.markStepCompleted(stepName);
        
        // Check if this is the final step to complete the saga
        if (isLastStep(saga, stepName)) {
            saga.markCompleted();
            log.info("Saga completed: sagaId={}, correlationId={}", saga.getSagaId(), correlationId);
        } else {
            saga.setStatus(ChoreographedSagaStatus.IN_PROGRESS);
        }
        
        saga = sagaRepository.save(saga);
        
        log.info("Step completion recorded: sagaId={}, step={}, status={}", 
                saga.getSagaId(), stepName, saga.getStatus());
        
        return saga;
    }
    
    /**
     * Records a step failure for a saga.
     */
    @Transactional
    public ChoreographedSagaState recordStepFailed(String correlationId, String stepName, String errorMessage) {
        Optional<ChoreographedSagaState> sagaOpt = sagaRepository.findByCorrelationId(correlationId);
        
        if (sagaOpt.isEmpty()) {
            log.warn(NO_SAGA_FOUND_MSG, correlationId);
            return null;
        }
        
        ChoreographedSagaState saga = sagaOpt.get();
        
        log.warn("Recording step failure: sagaId={}, correlationId={}, step={}, error={}", 
                saga.getSagaId(), correlationId, stepName, errorMessage);
        
        saga.markStepFailed(stepName, errorMessage);
        
        if (saga.getStatus() == ChoreographedSagaStatus.COMPENSATING) {
            log.warn("Saga compensation started: sagaId={}, step={}", saga.getSagaId(), stepName);
            // Trigger compensation through coordinator
            compensationCoordinator.triggerCompensation(saga.getSagaId(), saga.getCorrelationId(), stepName, errorMessage);
        } else if (saga.getStatus() == ChoreographedSagaStatus.FAILED) {
            log.error("Saga failed: sagaId={}, error={}", saga.getSagaId(), errorMessage);
        }
        
        saga = sagaRepository.save(saga);
        
        log.warn("Step failure recorded: sagaId={}, step={}, status={}", 
                saga.getSagaId(), stepName, saga.getStatus());
        
        return saga;
    }
    
    /**
     * Records compensation completion for a saga.
     */
    @Transactional
    public ChoreographedSagaState recordCompensationCompleted(String correlationId, String stepName) {
        Optional<ChoreographedSagaState> sagaOpt = sagaRepository.findByCorrelationId(correlationId);
        
        if (sagaOpt.isEmpty()) {
            log.warn(NO_SAGA_FOUND_MSG, correlationId);
            return null;
        }
        
        ChoreographedSagaState saga = sagaOpt.get();
        
        log.info("Recording compensation completion: sagaId={}, correlationId={}, step={}", 
                saga.getSagaId(), correlationId, stepName);
        
        saga.markCompensationCompleted();
        
        saga = sagaRepository.save(saga);
        
        log.info("Compensation completion recorded: sagaId={}, status={}", 
                saga.getSagaId(), saga.getStatus());
        
        return saga;
    }
    
    /**
     * Handles saga timeout detection and processing.
     */
    @Transactional
    public void handleTimeouts() {
        List<ChoreographedSagaState> timedOutSagas = sagaRepository.findTimedOutSagas(LocalDateTime.now());
        
        log.info("Processing {} timed out sagas", timedOutSagas.size());
        
        for (ChoreographedSagaState saga : timedOutSagas) {
            log.warn("Saga timed out: sagaId={}, correlationId={}, type={}", 
                    saga.getSagaId(), saga.getCorrelationId(), saga.getSagaType());
            
            saga.setStatus(ChoreographedSagaStatus.TIMED_OUT);
            saga.setCompensationRequired(true);
            saga.setErrorMessage("Saga timed out after " + 
                                LocalDateTime.now().minusMinutes(30) + " minutes");
            
            sagaRepository.save(saga);
        }
    }
    
    /**
     * Gets saga state by correlation ID.
     */
    public Optional<ChoreographedSagaState> getSagaByCorrelationId(String correlationId) {
        return sagaRepository.findByCorrelationId(correlationId);
    }
    
    /**
     * Gets all active sagas.
     */
    public List<ChoreographedSagaState> getActiveSagas() {
        return sagaRepository.findActiveSagas();
    }
    
    /**
     * Gets sagas requiring compensation.
     */
    public List<ChoreographedSagaState> getSagasRequiringCompensation() {
        return sagaRepository.findSagasRequiringCompensation();
    }
    
    /**
     * Gets saga statistics for monitoring.
     */
    public SagaStatistics getSagaStatistics() {
        List<Object[]> statusCounts = sagaRepository.countSagasByStatus();
        List<Object[]> typeCounts = sagaRepository.countSagasByType();
        List<Object[]> performanceMetrics = sagaRepository.getSagaPerformanceMetrics();
        
        return new SagaStatistics(statusCounts, typeCounts, performanceMetrics);
    }
    
    /**
     * Determines if the completed step is the last step in the saga.
     * This is saga-type specific logic.
     */
    private boolean isLastStep(ChoreographedSagaState saga, String stepName) {
        // This would be implemented based on your specific saga types
        // For now, using simple logic based on step names
        String sagaType = saga.getSagaType();
        
        if ("ORDER_PROCESSING".equals(sagaType)) {
            return "OrderDelivered".equals(stepName) || "OrderFulfilled".equals(stepName);
        } else if ("PAYMENT_PROCESSING".equals(sagaType)) {
            return "PaymentProcessed".equals(stepName);
        }
        
        // Default: check if we have completed all expected steps
        return stepName != null && (
            stepName.contains("Completed") || 
            stepName.contains("Delivered") || 
            stepName.contains("Fulfilled")
        );
    }
}
