package com.log430.tp7.sagaorchestrator.choreography.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for choreographed saga timeout detection and cleanup.
 * Runs periodically to identify and handle timed-out sagas.
 */
@Service
@ConditionalOnProperty(name = "saga.choreographed.timeout.enabled", havingValue = "true", matchIfMissing = true)
public class ChoreographedSagaTimeoutService {
    
    private static final Logger log = LoggerFactory.getLogger(ChoreographedSagaTimeoutService.class);
    
    private final ChoreographedSagaCoordinator sagaCoordinator;
    
    public ChoreographedSagaTimeoutService(ChoreographedSagaCoordinator sagaCoordinator) {
        this.sagaCoordinator = sagaCoordinator;
    }
    
    /**
     * Periodically checks for and processes timed-out sagas.
     * Runs every 5 minutes by default.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processTimeouts() {
        log.debug("Starting choreographed saga timeout check");
        
        try {
            long startTime = System.currentTimeMillis();
            
            sagaCoordinator.handleTimeouts();
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Choreographed saga timeout check completed in {} ms", duration);
            
        } catch (Exception e) {
            log.error("Error during choreographed saga timeout processing", e);
        }
    }
    
    /**
     * More frequent check for critical timeouts (every minute).
     * This could be used for high-priority sagas that need faster timeout detection.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    @ConditionalOnProperty(name = "saga.choreographed.timeout.critical.enabled", havingValue = "true")
    public void processCriticalTimeouts() {
        log.debug("Starting critical choreographed saga timeout check");
        
        try {
            // For now, use the same timeout logic
            // In the future, this could handle different timeout rules for critical sagas
            sagaCoordinator.handleTimeouts();
            
        } catch (Exception e) {
            log.error("Error during critical choreographed saga timeout processing", e);
        }
    }
}
