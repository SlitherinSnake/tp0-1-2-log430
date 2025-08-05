package com.log430.tp6.sagaorchestrator.service;


import com.log430.tp6.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp6.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp6.sagaorchestrator.model.SagaExecution;
import com.log430.tp6.sagaorchestrator.model.SagaState;
import com.log430.tp6.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for executing compensation actions when sagas fail or timeout.
 * Handles rollback operations in reverse order of saga execution.
 */
@Service
public class SagaCompensationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaCompensationService.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    private final ServiceClientWrapper serviceClientWrapper;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;
    
    public SagaCompensationService(
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
     * Executes compensation actions for a failed or timed-out saga.
     * Performs rollback operations in reverse order of saga execution.
     * 
     * @param saga the saga requiring compensation
     * @param reason the reason for compensation (e.g., "TIMEOUT", "PAYMENT_FAILED")
     */
    @Transactional
    public void executeCompensation(SagaExecution saga, String reason) {
        String sagaId = saga.getSagaId();
        SagaState currentState = saga.getCurrentState();
        
        logger.info("Starting compensation for saga: sagaId={}, state={}, reason={}", 
                   sagaId, currentState, reason);
        
        try {
            // Transition to compensation state
            saga.transitionTo(SagaState.STOCK_RELEASING);
            sagaExecutionRepository.save(saga);
            
            // Log compensation start
            sagaEventLogger.logCompensationStarted(sagaId, saga.getCustomerId(), 
                                                 currentState.toString(), reason);
            
            // Execute compensation actions based on how far the saga progressed
            boolean compensationSuccessful = true;
            
            // If we have a stock reservation, release it
            if (saga.getStockReservationId() != null) {
                compensationSuccessful &= releaseStockReservation(saga);
            }
            
            // Note: Payment reversal would be handled here in a real system
            // For this implementation, we assume payments are handled externally
            if (saga.getPaymentTransactionId() != null) {
                logger.info("Payment transaction {} exists for saga {} - " +
                           "external payment reversal may be required", 
                           saga.getPaymentTransactionId(), sagaId);
                
                // Log that payment reversal might be needed
                sagaEventLogger.logCompensationAction(sagaId, "payment_reversal_noted", 
                                                    "Payment transaction noted for potential reversal", true);
            }
            
            // Mark saga as failed
            saga.transitionTo(SagaState.SALE_FAILED);
            if (saga.getErrorMessage() == null || saga.getErrorMessage().isEmpty()) {
                saga.setErrorMessage("Saga failed due to: " + reason);
            }
            sagaExecutionRepository.save(saga);
            
            // Log compensation completion
            if (compensationSuccessful) {
                sagaEventLogger.logCompensationCompleted(sagaId, saga.getCustomerId(), reason, true);
                sagaMetrics.incrementCompensationSuccess();
                logger.info("Compensation completed successfully for saga: sagaId={}", sagaId);
            } else {
                sagaEventLogger.logCompensationCompleted(sagaId, saga.getCustomerId(), reason, false);
                sagaMetrics.incrementCompensationFailure();
                logger.warn("Compensation completed with some failures for saga: sagaId={}", sagaId);
            }
            
        } catch (Exception e) {
            logger.error("Compensation failed for saga: sagaId={}, error={}", sagaId, e.getMessage(), e);
            
            // Record compensation failure
            sagaMetrics.incrementCompensationFailure();
            sagaEventLogger.logCompensationCompleted(sagaId, saga.getCustomerId(), reason, false);
            
            // Ensure saga is marked as failed even if compensation fails
            try {
                saga.transitionTo(SagaState.SALE_FAILED);
                saga.setErrorMessage("Compensation failed: " + e.getMessage());
                sagaExecutionRepository.save(saga);
            } catch (Exception saveError) {
                logger.error("Failed to save saga after compensation failure: sagaId={}", sagaId, saveError);
            }
            
            throw new RuntimeException("Compensation failed for saga: " + sagaId, e);
        }
    }
    
    /**
     * Releases stock reservation as part of compensation.
     * 
     * @param saga the saga with stock reservation to release
     * @return true if stock release was successful, false otherwise
     */
    private boolean releaseStockReservation(SagaExecution saga) {
        String sagaId = saga.getSagaId();
        String reservationId = saga.getStockReservationId();
        
        logger.info("Releasing stock reservation: sagaId={}, reservationId={}", sagaId, reservationId);
        
        try {
            // Call inventory service to release the stock reservation with circuit breaker
            serviceClientWrapper.releaseStockSync(reservationId);
            
            // Log successful stock release
            sagaEventLogger.logCompensationAction(sagaId, "stock_release", 
                                                "Stock reservation released successfully", true);
            
            logger.info("Stock reservation released successfully: sagaId={}, reservationId={}", 
                       sagaId, reservationId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to release stock reservation: sagaId={}, reservationId={}, error={}", 
                        sagaId, reservationId, e.getMessage(), e);
            
            // Log failed stock release
            sagaEventLogger.logCompensationAction(sagaId, "stock_release", 
                                                "Failed to release stock reservation: " + e.getMessage(), false);
            
            // Record the error but don't fail the entire compensation
            sagaMetrics.recordError("StockReleaseFailure", "compensation");
            
            return false;
        }
    }
    
    /**
     * Executes compensation for a specific saga by ID.
     * Convenience method for external triggers.
     * 
     * @param sagaId the ID of the saga to compensate
     * @param reason the reason for compensation
     * @throws RuntimeException if saga not found or compensation fails
     */
    @Transactional
    public void executeCompensationById(String sagaId, String reason) {
        SagaExecution saga = sagaExecutionRepository.findById(sagaId)
            .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
        
        executeCompensation(saga, reason);
    }
    
    /**
     * Checks if a saga requires compensation based on its current state.
     * 
     * @param saga the saga to check
     * @return true if compensation is needed
     */
    public boolean requiresCompensation(SagaExecution saga) {
        // Compensation is needed if saga has progressed beyond initial state
        // and is not already in a final state
        return !saga.isInFinalState() && 
               saga.getCurrentState() != SagaState.SALE_INITIATED &&
               (saga.getStockReservationId() != null || saga.getPaymentTransactionId() != null);
    }
    
    /**
     * Gets compensation status for a saga.
     * 
     * @param saga the saga to check
     * @return compensation status description
     */
    public String getCompensationStatus(SagaExecution saga) {
        if (saga.getCurrentState() == SagaState.SALE_FAILED) {
            return "Compensation completed - saga failed";
        } else if (saga.getCurrentState() == SagaState.STOCK_RELEASING) {
            return "Compensation in progress";
        } else if (requiresCompensation(saga)) {
            return "Compensation required";
        } else {
            return "No compensation needed";
        }
    }
}