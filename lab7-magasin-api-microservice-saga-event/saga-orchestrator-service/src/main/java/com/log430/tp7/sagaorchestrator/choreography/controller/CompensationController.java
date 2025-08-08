package com.log430.tp7.sagaorchestrator.choreography.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationAction;
import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationCoordinator;

/**
 * REST controller for compensation management in choreographed sagas.
 */
@RestController
@RequestMapping("/api/saga/choreographed/compensation")
public class CompensationController {
    
    private static final Logger log = LoggerFactory.getLogger(CompensationController.class);
    
    private final CompensationCoordinator compensationCoordinator;
    
    public CompensationController(CompensationCoordinator compensationCoordinator) {
        this.compensationCoordinator = compensationCoordinator;
    }
    
    /**
     * Triggers compensation for a specific saga.
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerCompensation(@RequestBody CompensationTriggerRequest request) {
        log.info("Triggering compensation: sagaId={}, failedStep={}", 
                request.sagaId(), request.failedStep());
        
        try {
            compensationCoordinator.triggerCompensation(
                request.sagaId(), 
                request.correlationId(), 
                request.failedStep(), 
                request.errorMessage()
            );
            
            log.info("Compensation triggered successfully: sagaId={}", request.sagaId());
            return ResponseEntity.ok("Compensation triggered successfully");
            
        } catch (Exception e) {
            log.error("Failed to trigger compensation: sagaId={}", request.sagaId(), e);
            return ResponseEntity.internalServerError().body("Failed to trigger compensation: " + e.getMessage());
        }
    }
    
    /**
     * Gets compensation actions for a specific saga.
     */
    @GetMapping("/saga/{sagaId}")
    public ResponseEntity<List<CompensationAction>> getCompensationActions(@PathVariable String sagaId) {
        log.debug("Getting compensation actions for saga: {}", sagaId);
        
        try {
            List<CompensationAction> actions = compensationCoordinator.getCompensationActions(sagaId);
            log.debug("Found {} compensation actions for saga: {}", actions.size(), sagaId);
            return ResponseEntity.ok(actions);
            
        } catch (Exception e) {
            log.error("Failed to get compensation actions: sagaId={}", sagaId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets the count of pending compensation actions.
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Integer> getPendingActionsCount() {
        try {
            int count = compensationCoordinator.getPendingActionsCount();
            log.debug("Pending compensation actions count: {}", count);
            return ResponseEntity.ok(count);
            
        } catch (Exception e) {
            log.error("Failed to get pending actions count", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Manually triggers compensation processing.
     */
    @PostMapping("/process")
    public ResponseEntity<String> processCompensationActions() {
        log.info("Manually triggering compensation processing");
        
        try {
            compensationCoordinator.processCompensationActions();
            log.info("Compensation processing completed");
            return ResponseEntity.ok("Compensation processing completed");
            
        } catch (Exception e) {
            log.error("Failed to process compensation actions", e);
            return ResponseEntity.internalServerError().body("Failed to process compensation actions: " + e.getMessage());
        }
    }
    
    // Request DTOs
    
    public record CompensationTriggerRequest(
        String sagaId,
        String correlationId,
        String failedStep,
        String errorMessage
    ) {}
}
