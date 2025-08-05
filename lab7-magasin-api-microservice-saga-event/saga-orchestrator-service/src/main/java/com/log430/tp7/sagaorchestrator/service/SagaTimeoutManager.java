package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.model.SagaState;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for managing saga timeouts and executing automatic compensation.
 * Runs scheduled cleanup tasks to detect and handle timed-out sagas.
 */
@Service
public class SagaTimeoutManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaTimeoutManager.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    private final SagaCompensationService sagaCompensationService;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;
    
    // Configurable timeout values from application.yml
    @Value("${saga.timeout.default:300000}") // 5 minutes default
    private long defaultTimeoutMs;
    
    @Value("${saga.timeout.stock-verification:30000}") // 30 seconds
    private long stockVerificationTimeoutMs;
    
    @Value("${saga.timeout.stock-reservation:30000}") // 30 seconds
    private long stockReservationTimeoutMs;
    
    @Value("${saga.timeout.payment-processing:60000}") // 1 minute
    private long paymentProcessingTimeoutMs;
    
    @Value("${saga.timeout.order-confirmation:30000}") // 30 seconds
    private long orderConfirmationTimeoutMs;
    
    // State-specific timeout mapping
    private Map<SagaState, Long> stateTimeouts;
    
    public SagaTimeoutManager(
            SagaExecutionRepository sagaExecutionRepository,
            SagaCompensationService sagaCompensationService,
            SagaMetrics sagaMetrics,
            SagaEventLogger sagaEventLogger) {
        this.sagaExecutionRepository = sagaExecutionRepository;
        this.sagaCompensationService = sagaCompensationService;
        this.sagaMetrics = sagaMetrics;
        this.sagaEventLogger = sagaEventLogger;
    }
    
    /**
     * Initialize state-specific timeout mappings after properties are injected.
     */
    @jakarta.annotation.PostConstruct
    public void initializeTimeouts() {
        stateTimeouts = Map.of(
            SagaState.STOCK_VERIFYING, stockVerificationTimeoutMs,
            SagaState.STOCK_RESERVING, stockReservationTimeoutMs,
            SagaState.PAYMENT_PROCESSING, paymentProcessingTimeoutMs,
            SagaState.ORDER_CONFIRMING, orderConfirmationTimeoutMs
        );
        
        logger.info("Saga timeout manager initialized with timeouts: " +
                   "default={}ms, stock-verification={}ms, stock-reservation={}ms, " +
                   "payment-processing={}ms, order-confirmation={}ms",
                   defaultTimeoutMs, stockVerificationTimeoutMs, stockReservationTimeoutMs,
                   paymentProcessingTimeoutMs, orderConfirmationTimeoutMs);
    }
    
    /**
     * Scheduled task that runs every 30 seconds to detect and handle timed-out sagas.
     * Uses fixed delay to ensure previous execution completes before starting next one.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    @Transactional
    public void handleTimeouts() {
        logger.debug("Starting saga timeout detection and cleanup");
        
        try {
            // Find all active sagas that might be timed out
            List<SagaExecution> activeSagas = sagaExecutionRepository.findActiveSagas();
            
            int timeoutCount = 0;
            LocalDateTime now = LocalDateTime.now();
            
            for (SagaExecution saga : activeSagas) {
                if (isSagaTimedOut(saga, now)) {
                    logger.warn("Detected timed-out saga: sagaId={}, state={}, lastUpdated={}", 
                               saga.getSagaId(), saga.getCurrentState(), saga.getUpdatedAt());
                    
                    executeTimeoutCompensation(saga);
                    timeoutCount++;
                }
            }
            
            if (timeoutCount > 0) {
                logger.info("Processed {} timed-out sagas", timeoutCount);
                sagaMetrics.recordTimeoutCleanup(timeoutCount);
            }
            
        } catch (Exception e) {
            logger.error("Error during saga timeout handling", e);
            sagaMetrics.recordError("TimeoutHandlingError", "timeout_management");
        }
    }
    
    /**
     * Determines if a saga has timed out based on its current state and last update time.
     * 
     * @param saga the saga to check
     * @param now the current timestamp
     * @return true if the saga has timed out
     */
    private boolean isSagaTimedOut(SagaExecution saga, LocalDateTime now) {
        if (saga.isInFinalState()) {
            return false; // Final states don't timeout
        }
        
        // Get state-specific timeout or use default
        long timeoutMs = stateTimeouts.getOrDefault(saga.getCurrentState(), defaultTimeoutMs);
        
        // Calculate timeout threshold
        LocalDateTime timeoutThreshold = saga.getUpdatedAt().plusNanos(timeoutMs * 1_000_000);
        
        boolean isTimedOut = now.isAfter(timeoutThreshold);
        
        if (isTimedOut) {
            logger.debug("Saga timeout detected: sagaId={}, state={}, lastUpdated={}, timeoutMs={}, threshold={}", 
                        saga.getSagaId(), saga.getCurrentState(), saga.getUpdatedAt(), 
                        timeoutMs, timeoutThreshold);
        }
        
        return isTimedOut;
    }
    
    /**
     * Executes timeout compensation for a timed-out saga.
     * Logs the timeout event and delegates to the compensation service.
     * 
     * @param saga the timed-out saga
     */
    @Transactional
    public void executeTimeoutCompensation(SagaExecution saga) {
        String sagaId = saga.getSagaId();
        SagaState currentState = saga.getCurrentState();
        
        logger.warn("Executing timeout compensation for saga: sagaId={}, state={}", sagaId, currentState);
        
        try {
            // Update saga with timeout error message
            String timeoutMessage = String.format("Saga timed out in state %s after %d minutes", 
                                                 currentState, getTimeoutMinutes(currentState));
            saga.setErrorMessage(timeoutMessage);
            
            // Log timeout event with structured logging
            sagaEventLogger.logSagaTimeout(sagaId, saga.getCustomerId(), currentState.toString(), 
                                         timeoutMessage, getTimeoutMinutes(currentState));
            
            // Execute compensation based on current state
            sagaCompensationService.executeCompensation(saga, "TIMEOUT");
            
            // Record timeout metrics
            sagaMetrics.incrementSagaTimeout(currentState.toString());
            
            logger.info("Timeout compensation completed for saga: sagaId={}", sagaId);
            
        } catch (Exception e) {
            logger.error("Failed to execute timeout compensation for saga: sagaId={}, error={}", 
                        sagaId, e.getMessage(), e);
            
            // Record compensation failure
            sagaMetrics.recordError("TimeoutCompensationFailed", "timeout_management");
            
            // Ensure saga is marked as failed even if compensation fails
            try {
                saga.transitionTo(SagaState.SALE_FAILED);
                saga.setErrorMessage("Timeout compensation failed: " + e.getMessage());
                sagaExecutionRepository.save(saga);
            } catch (Exception saveError) {
                logger.error("Failed to save saga after timeout compensation failure: sagaId={}", 
                           sagaId, saveError);
            }
        }
    }
    
    /**
     * Gets the timeout duration in minutes for a given state.
     * 
     * @param state the saga state
     * @return timeout duration in minutes
     */
    private long getTimeoutMinutes(SagaState state) {
        long timeoutMs = stateTimeouts.getOrDefault(state, defaultTimeoutMs);
        return timeoutMs / (60 * 1000); // Convert to minutes
    }
    
    /**
     * Manually triggers timeout detection and cleanup.
     * Useful for testing or manual intervention.
     * 
     * @return number of sagas that were timed out and compensated
     */
    public int manualTimeoutCleanup() {
        logger.info("Manual timeout cleanup triggered");
        
        List<SagaExecution> activeSagas = sagaExecutionRepository.findActiveSagas();
        LocalDateTime now = LocalDateTime.now();
        int timeoutCount = 0;
        
        for (SagaExecution saga : activeSagas) {
            if (isSagaTimedOut(saga, now)) {
                executeTimeoutCompensation(saga);
                timeoutCount++;
            }
        }
        
        logger.info("Manual timeout cleanup completed: {} sagas processed", timeoutCount);
        return timeoutCount;
    }
    
    /**
     * Gets current timeout configuration for monitoring and debugging.
     * 
     * @return map of timeout configurations
     */
    public Map<String, Long> getTimeoutConfiguration() {
        return Map.of(
            "default", defaultTimeoutMs,
            "stock-verification", stockVerificationTimeoutMs,
            "stock-reservation", stockReservationTimeoutMs,
            "payment-processing", paymentProcessingTimeoutMs,
            "order-confirmation", orderConfirmationTimeoutMs
        );
    }
    
    /**
     * Finds sagas that are approaching timeout (within 80% of timeout duration).
     * Useful for proactive monitoring and alerting.
     * 
     * @return list of sagas approaching timeout
     */
    public List<SagaExecution> findSagasApproachingTimeout() {
        List<SagaExecution> activeSagas = sagaExecutionRepository.findActiveSagas();
        LocalDateTime now = LocalDateTime.now();
        
        return activeSagas.stream()
            .filter(saga -> {
                long timeoutMs = stateTimeouts.getOrDefault(saga.getCurrentState(), defaultTimeoutMs);
                long warningThresholdMs = (long) (timeoutMs * 0.8); // 80% of timeout
                LocalDateTime warningThreshold = saga.getUpdatedAt().plusNanos(warningThresholdMs * 1_000_000);
                return now.isAfter(warningThreshold) && !isSagaTimedOut(saga, now);
            })
            .toList();
    }
}