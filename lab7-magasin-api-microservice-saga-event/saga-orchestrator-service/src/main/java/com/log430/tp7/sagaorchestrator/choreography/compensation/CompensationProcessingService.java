package com.log430.tp7.sagaorchestrator.choreography.compensation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for processing compensation actions in choreographed sagas.
 * Runs periodically to execute pending compensation actions.
 */
@Service
@ConditionalOnProperty(name = "saga.choreographed.compensation.enabled", havingValue = "true", matchIfMissing = true)
public class CompensationProcessingService {
    
    private static final Logger log = LoggerFactory.getLogger(CompensationProcessingService.class);
    
    private final CompensationCoordinator compensationCoordinator;
    
    public CompensationProcessingService(CompensationCoordinator compensationCoordinator) {
        this.compensationCoordinator = compensationCoordinator;
    }
    
    /**
     * Processes pending compensation actions every 30 seconds.
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void processCompensationActions() {
        try {
            int pendingCount = compensationCoordinator.getPendingActionsCount();
            if (pendingCount > 0) {
                log.debug("Processing {} pending compensation actions", pendingCount);
                
                long startTime = System.currentTimeMillis();
                compensationCoordinator.processCompensationActions();
                long duration = System.currentTimeMillis() - startTime;
                
                log.debug("Compensation processing completed in {} ms", duration);
            }
        } catch (Exception e) {
            log.error("Error during compensation action processing", e);
        }
    }
    
    /**
     * Logs compensation metrics every 5 minutes for monitoring.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logCompensationMetrics() {
        try {
            int pendingCount = compensationCoordinator.getPendingActionsCount();
            
            if (pendingCount > 0) {
                log.info("Compensation metrics: {} pending actions", pendingCount);
            } else {
                log.debug("Compensation metrics: No pending actions");
            }
            
        } catch (Exception e) {
            log.error("Error logging compensation metrics", e);
        }
    }
}
