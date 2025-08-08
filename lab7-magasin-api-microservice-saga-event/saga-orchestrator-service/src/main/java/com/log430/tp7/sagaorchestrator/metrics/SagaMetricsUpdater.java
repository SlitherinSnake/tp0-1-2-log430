package com.log430.tp7.sagaorchestrator.metrics;

import com.log430.tp7.sagaorchestrator.model.SagaState;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Scheduled task to update saga metrics gauges periodically
 */
@Component
public class SagaMetricsUpdater {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaMetricsUpdater.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    private final SagaMetrics sagaMetrics;
    
    public SagaMetricsUpdater(SagaExecutionRepository sagaExecutionRepository, SagaMetrics sagaMetrics) {
        this.sagaExecutionRepository = sagaExecutionRepository;
        this.sagaMetrics = sagaMetrics;
    }
    
    /**
     * Updates saga gauge metrics every 30 seconds
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    public void updateSagaMetrics() {
        try {
            logger.debug("Updating saga metrics gauges");
            
            // Update active sagas count
            long activeSagasCount = sagaExecutionRepository.findActiveSagas().size();
            sagaMetrics.updateActiveSagasCount(activeSagasCount);
            
            // Update pending stock verification count
            long pendingStockVerification = sagaExecutionRepository.findByCurrentStateIn(
                Arrays.asList(SagaState.SALE_INITIATED, SagaState.STOCK_VERIFYING)
            ).size();
            sagaMetrics.updatePendingStockVerificationCount(pendingStockVerification);
            
            // Update pending payment count
            long pendingPayment = sagaExecutionRepository.findByCurrentStateIn(
                Arrays.asList(SagaState.STOCK_RESERVING, SagaState.PAYMENT_PROCESSING)
            ).size();
            sagaMetrics.updatePendingPaymentCount(pendingPayment);
            
            logger.debug("Saga metrics updated: activeSagas={}, pendingStockVerification={}, pendingPayment={}", 
                        activeSagasCount, pendingStockVerification, pendingPayment);
            
        } catch (Exception e) {
            logger.error("Error updating saga metrics", e);
        }
    }
}