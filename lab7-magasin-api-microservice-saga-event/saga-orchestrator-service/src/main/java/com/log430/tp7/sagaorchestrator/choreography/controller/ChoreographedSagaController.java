package com.log430.tp7.sagaorchestrator.choreography.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.service.ChoreographedSagaCoordinator;
import com.log430.tp7.sagaorchestrator.choreography.service.SagaStatistics;

/**
 * REST controller for choreographed saga management and monitoring.
 */
@RestController
@RequestMapping("/api/saga/choreographed")
public class ChoreographedSagaController {
    
    private static final Logger log = LoggerFactory.getLogger(ChoreographedSagaController.class);
    
    private final ChoreographedSagaCoordinator sagaCoordinator;
    
    public ChoreographedSagaController(ChoreographedSagaCoordinator sagaCoordinator) {
        this.sagaCoordinator = sagaCoordinator;
    }
    
    /**
     * Initiates a new choreographed saga.
     */
    @PostMapping("/initiate")
    public ResponseEntity<ChoreographedSagaState> initiateSaga(@RequestBody SagaRequest request) {
        log.info("Initiating choreographed saga: correlationId={}, type={}", 
                request.correlationId(), request.sagaType());
        
        try {
            ChoreographedSagaState saga = sagaCoordinator.initiateSaga(
                request.correlationId(), 
                request.sagaType(), 
                request.sagaData()
            );
            
            log.info("Choreographed saga initiated: sagaId={}", saga.getSagaId());
            return ResponseEntity.ok(saga);
            
        } catch (Exception e) {
            log.error("Failed to initiate choreographed saga: correlationId={}", 
                     request.correlationId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Records a step completion.
     */
    @PostMapping("/step/completed")
    public ResponseEntity<ChoreographedSagaState> recordStepCompleted(@RequestBody StepCompletionRequest request) {
        log.info("Recording step completion: correlationId={}, step={}", 
                request.correlationId(), request.stepName());
        
        try {
            ChoreographedSagaState saga = sagaCoordinator.recordStepCompleted(
                request.correlationId(), 
                request.stepName(), 
                request.stepData()
            );
            
            if (saga == null) {
                log.warn("No saga found for step completion: correlationId={}", request.correlationId());
                return ResponseEntity.notFound().build();
            }
            
            log.info("Step completion recorded: sagaId={}, status={}", saga.getSagaId(), saga.getStatus());
            return ResponseEntity.ok(saga);
            
        } catch (Exception e) {
            log.error("Failed to record step completion: correlationId={}, step={}", 
                     request.correlationId(), request.stepName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Records a step failure.
     */
    @PostMapping("/step/failed")
    public ResponseEntity<ChoreographedSagaState> recordStepFailed(@RequestBody StepFailureRequest request) {
        log.warn("Recording step failure: correlationId={}, step={}, error={}", 
                request.correlationId(), request.stepName(), request.errorMessage());
        
        try {
            ChoreographedSagaState saga = sagaCoordinator.recordStepFailed(
                request.correlationId(), 
                request.stepName(), 
                request.errorMessage()
            );
            
            if (saga == null) {
                log.warn("No saga found for step failure: correlationId={}", request.correlationId());
                return ResponseEntity.notFound().build();
            }
            
            log.warn("Step failure recorded: sagaId={}, status={}", saga.getSagaId(), saga.getStatus());
            return ResponseEntity.ok(saga);
            
        } catch (Exception e) {
            log.error("Failed to record step failure: correlationId={}, step={}", 
                     request.correlationId(), request.stepName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Records compensation completion.
     */
    @PostMapping("/compensation/completed")
    public ResponseEntity<ChoreographedSagaState> recordCompensationCompleted(@RequestBody CompensationRequest request) {
        log.info("Recording compensation completion: correlationId={}, step={}", 
                request.correlationId(), request.stepName());
        
        try {
            ChoreographedSagaState saga = sagaCoordinator.recordCompensationCompleted(
                request.correlationId(), 
                request.stepName()
            );
            
            if (saga == null) {
                log.warn("No saga found for compensation completion: correlationId={}", request.correlationId());
                return ResponseEntity.notFound().build();
            }
            
            log.info("Compensation completion recorded: sagaId={}, status={}", saga.getSagaId(), saga.getStatus());
            return ResponseEntity.ok(saga);
            
        } catch (Exception e) {
            log.error("Failed to record compensation completion: correlationId={}, step={}", 
                     request.correlationId(), request.stepName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets saga state by correlation ID.
     */
    @GetMapping("/saga/{correlationId}")
    public ResponseEntity<ChoreographedSagaState> getSaga(@PathVariable String correlationId) {
        log.debug("Getting saga by correlation ID: {}", correlationId);
        
        try {
            Optional<ChoreographedSagaState> saga = sagaCoordinator.getSagaByCorrelationId(correlationId);
            
            if (saga.isEmpty()) {
                log.debug("No saga found for correlation ID: {}", correlationId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(saga.get());
            
        } catch (Exception e) {
            log.error("Failed to get saga: correlationId={}", correlationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets all active sagas.
     */
    @GetMapping("/active")
    public ResponseEntity<List<ChoreographedSagaState>> getActiveSagas() {
        log.debug("Getting all active sagas");
        
        try {
            List<ChoreographedSagaState> activeSagas = sagaCoordinator.getActiveSagas();
            log.debug("Found {} active sagas", activeSagas.size());
            return ResponseEntity.ok(activeSagas);
            
        } catch (Exception e) {
            log.error("Failed to get active sagas", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets sagas requiring compensation.
     */
    @GetMapping("/compensation-required")
    public ResponseEntity<List<ChoreographedSagaState>> getSagasRequiringCompensation() {
        log.debug("Getting sagas requiring compensation");
        
        try {
            List<ChoreographedSagaState> compensationSagas = sagaCoordinator.getSagasRequiringCompensation();
            log.debug("Found {} sagas requiring compensation", compensationSagas.size());
            return ResponseEntity.ok(compensationSagas);
            
        } catch (Exception e) {
            log.error("Failed to get sagas requiring compensation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets saga statistics for monitoring.
     */
    @GetMapping("/statistics")
    public ResponseEntity<SagaStatistics> getSagaStatistics() {
        log.debug("Getting saga statistics");
        
        try {
            SagaStatistics statistics = sagaCoordinator.getSagaStatistics();
            log.debug("Retrieved saga statistics: {} status counts, {} type counts", 
                     statistics.getStatusCounts().size(), statistics.getTypeCounts().size());
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Failed to get saga statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Triggers timeout processing for sagas.
     */
    @PostMapping("/timeouts/process")
    public ResponseEntity<String> processTimeouts() {
        log.info("Processing saga timeouts");
        
        try {
            sagaCoordinator.handleTimeouts();
            log.info("Saga timeout processing completed");
            return ResponseEntity.ok("Timeout processing completed");
            
        } catch (Exception e) {
            log.error("Failed to process saga timeouts", e);
            return ResponseEntity.internalServerError().body("Failed to process timeouts: " + e.getMessage());
        }
    }
    
    // Request DTOs
    
    public record SagaRequest(
        String correlationId,
        String sagaType,
        String sagaData
    ) {}
    
    public record StepCompletionRequest(
        String correlationId,
        String stepName,
        String stepData
    ) {}
    
    public record StepFailureRequest(
        String correlationId,
        String stepName,
        String errorMessage
    ) {}
    
    public record CompensationRequest(
        String correlationId,
        String stepName
    ) {}
}
