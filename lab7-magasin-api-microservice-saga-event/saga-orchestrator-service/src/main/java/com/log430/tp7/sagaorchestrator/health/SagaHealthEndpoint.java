package com.log430.tp7.sagaorchestrator.health;

import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health endpoint for saga-specific health information
 */
@RestController
@RequestMapping("/actuator/saga-health")
public class SagaHealthEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaHealthEndpoint.class);
    
    private final SagaExecutionRepository sagaExecutionRepository;
    
    @Autowired
    public SagaHealthEndpoint(SagaExecutionRepository sagaExecutionRepository) {
        this.sagaExecutionRepository = sagaExecutionRepository;
    }
    
    /**
     * Provides saga-specific health information
     */
    @GetMapping
    public Map<String, Object> getSagaHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connectivity
            long activeSagaCount = sagaExecutionRepository.findActiveSagas().size();
            long totalSagaCount = sagaExecutionRepository.count();
            
            health.put("status", "UP");
            health.put("activeSagas", activeSagaCount);
            health.put("totalSagas", totalSagaCount);
            health.put("completedSagas", totalSagaCount - activeSagaCount);
            
            // Add warnings if needed
            if (activeSagaCount > 1000) {
                health.put("warning", "High number of active sagas: " + activeSagaCount);
            }
            
            logger.debug("Saga health check completed: activeSagas={}, totalSagas={}", 
                        activeSagaCount, totalSagaCount);
            
        } catch (Exception e) {
            logger.error("Saga health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}