package com.log430.tp7.sagaorchestrator.choreography.compensation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;

/**
 * Service for coordinating compensation actions in choreographed sagas.
 * Manages the execution of compensation logic when saga steps fail or timeout.
 */
@Service
public class CompensationCoordinator {
    
    private static final Logger log = LoggerFactory.getLogger(CompensationCoordinator.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MINUTES = 5;
    private static final String INVENTORY_SERVICE = "inventory-service";
    private static final String PAYMENT_SERVICE = "payment-service";
    private static final String STORE_SERVICE = "store-service";
    
    private final ChoreographedSagaRepository sagaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // In-memory queues for compensation actions (could be replaced with persistent storage)
    private final Queue<CompensationAction> pendingActions = new ConcurrentLinkedQueue<>();
    private final Map<String, CompensationAction> actionRegistry = new ConcurrentHashMap<>();
    
    public CompensationCoordinator(ChoreographedSagaRepository sagaRepository, 
                                 RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.sagaRepository = sagaRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Triggers compensation for a failed saga step.
     */
    public void triggerCompensation(String sagaId, String correlationId, String failedStep, String errorMessage) {
        log.warn("Triggering compensation for saga: sagaId={}, failedStep={}, error={}", 
                sagaId, failedStep, errorMessage);
        
        try {
            Optional<ChoreographedSagaState> sagaOpt = sagaRepository.findByCorrelationId(correlationId);
            if (sagaOpt.isEmpty()) {
                log.error("Cannot trigger compensation - saga not found: correlationId={}", correlationId);
                return;
            }
            
            ChoreographedSagaState saga = sagaOpt.get();
            List<CompensationAction> compensationActions = buildCompensationPlan(saga);
            
            if (compensationActions.isEmpty()) {
                log.info("No compensation actions required for saga: sagaId={}", sagaId);
                return;
            }
            
            log.info("Generated {} compensation actions for saga: sagaId={}", compensationActions.size(), sagaId);
            
            // Queue compensation actions for execution
            for (CompensationAction action : compensationActions) {
                queueCompensationAction(action);
            }
            
        } catch (Exception e) {
            log.error("Failed to trigger compensation for saga: sagaId={}", sagaId, e);
        }
    }
    
    /**
     * Builds a compensation plan based on completed steps that need to be undone.
     */
    private List<CompensationAction> buildCompensationPlan(ChoreographedSagaState saga) {
        List<CompensationAction> actions = new ArrayList<>();
        
        try {
            List<String> completedStepsList = parseCompletedSteps(saga.getCompletedSteps());
            
            log.debug("Building compensation plan for saga: sagaId={}, completedSteps={}", 
                     saga.getSagaId(), completedStepsList);
            
            // Create compensation actions for all completed steps (in reverse order)
            List<String> stepsToCompensate = completedStepsList.reversed();
            
            int priority = 1;
            for (String stepName : stepsToCompensate) {
                CompensationDefinition definition = getCompensationDefinition(stepName);
                if (definition != null) {
                    String actionId = UUID.randomUUID().toString();
                    
                    CompensationAction action = CompensationAction.builder()
                        .actionId(actionId)
                        .sagaId(saga.getSagaId())
                        .correlationId(saga.getCorrelationId())
                        .stepName(stepName)
                        .serviceName(definition.getServiceName())
                        .compensationEndpoint(definition.getEndpoint())
                        .compensationPayload(buildCompensationPayload(saga, stepName))
                        .priority(priority++)
                        .build();
                    
                    actions.add(action);
                    log.debug("Created compensation action: actionId={}, step={}, service={}", 
                             actionId, stepName, definition.getServiceName());
                } else {
                    log.warn("No compensation definition found for step: {}", stepName);
                }
            }
        } catch (Exception e) {
            log.error("Failed to build compensation plan for saga: sagaId={}", saga.getSagaId(), e);
        }
        
        return actions;
    }
    
    /**
     * Parses completed steps from JSON string.
     */
    private List<String> parseCompletedSteps(String completedStepsJson) {
        if (completedStepsJson == null || completedStepsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(completedStepsJson, typeRef);
        } catch (Exception e) {
            log.error("Failed to parse completed steps JSON: {}", completedStepsJson, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets compensation definition for a specific step.
     * This would typically be loaded from configuration or a registry.
     */
    private CompensationDefinition getCompensationDefinition(String stepName) {
        // Static compensation definitions for demo purposes
        // In production, this would be loaded from configuration
        Map<String, CompensationDefinition> definitions = Map.of(
            "InventoryReserved", new CompensationDefinition(INVENTORY_SERVICE, "/api/inventory/release"),
            "PaymentProcessed", new CompensationDefinition(PAYMENT_SERVICE, "/api/payment/refund"),
            "OrderCreated", new CompensationDefinition(STORE_SERVICE, "/api/orders/cancel"),
            "StockReserved", new CompensationDefinition(INVENTORY_SERVICE, "/api/stock/release"),
            "PaymentAuthorized", new CompensationDefinition(PAYMENT_SERVICE, "/api/payment/cancel-authorization")
        );
        
        return definitions.get(stepName);
    }
    
    /**
     * Builds compensation payload for a specific step.
     */
    private String buildCompensationPayload(ChoreographedSagaState saga, String stepName) {
        // Build JSON payload based on saga data and step
        // This is a simplified implementation
        return String.format("""
            {
                "sagaId": "%s",
                "correlationId": "%s",
                "stepName": "%s",
                "reason": "Saga compensation",
                "timestamp": "%s"
            }
            """, saga.getSagaId(), saga.getCorrelationId(), stepName, LocalDateTime.now());
    }
    
    /**
     * Queues a compensation action for execution.
     */
    public void queueCompensationAction(CompensationAction action) {
        pendingActions.offer(action);
        actionRegistry.put(action.getActionId(), action);
        
        log.info("Queued compensation action: actionId={}, step={}, priority={}", 
                action.getActionId(), action.getStepName(), action.getPriority());
    }
    
    /**
     * Processes pending compensation actions.
     */
    public void processCompensationActions() {
        if (pendingActions.isEmpty()) {
            return;
        }
        
        log.debug("Processing {} pending compensation actions", pendingActions.size());
        
        List<CompensationAction> actionsToProcess = new ArrayList<>();
        
        // Collect actions ready for execution
        while (!pendingActions.isEmpty()) {
            CompensationAction action = pendingActions.poll();
            if (action != null && action.isReadyForExecution()) {
                actionsToProcess.add(action);
            } else if (action != null) {
                // Re-queue if not ready yet
                pendingActions.offer(action);
            }
        }
        
        // Sort by priority (lower number = higher priority)
        actionsToProcess.sort(Comparator.comparingInt(CompensationAction::getPriority));
        
        // Execute actions
        for (CompensationAction action : actionsToProcess) {
            executeCompensationAction(action);
        }
    }
    
    /**
     * Executes a single compensation action.
     */
    private void executeCompensationAction(CompensationAction action) {
        log.info("Executing compensation action: actionId={}, step={}, service={}", 
                action.getActionId(), action.getStepName(), action.getServiceName());
        
        action.markInProgress();
        
        try {
            // Prepare HTTP request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Correlation-ID", action.getCorrelationId());
            headers.set("X-Saga-ID", action.getSagaId());
            
            HttpEntity<String> request = new HttpEntity<>(action.getCompensationPayload(), headers);
            
            // Execute compensation call
            String url = buildServiceUrl(action.getServiceName(), action.getCompensationEndpoint());
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                action.markCompleted();
                log.info("Compensation action completed successfully: actionId={}", action.getActionId());
            } else {
                String errorMsg = "HTTP " + response.getStatusCode() + ": " + response.getBody();
                handleCompensationFailure(action, errorMsg);
            }
            
        } catch (Exception e) {
            handleCompensationFailure(action, e.getMessage());
        }
    }
    
    /**
     * Handles compensation action failure.
     */
    private void handleCompensationFailure(CompensationAction action, String errorMessage) {
        log.error("Compensation action failed: actionId={}, error={}", action.getActionId(), errorMessage);
        
        action.markFailed(errorMessage);
        
        if (action.canRetry(MAX_RETRIES)) {
            // Schedule retry with delay
            LocalDateTime retryTime = LocalDateTime.now().plusMinutes((long) RETRY_DELAY_MINUTES * action.getRetryCount());
            CompensationAction retryAction = CompensationAction.builder()
                .actionId(action.getActionId())
                .sagaId(action.getSagaId())
                .correlationId(action.getCorrelationId())
                .stepName(action.getStepName())
                .serviceName(action.getServiceName())
                .compensationEndpoint(action.getCompensationEndpoint())
                .compensationPayload(action.getCompensationPayload())
                .priority(action.getPriority())
                .executeAfter(retryTime)
                .build();
            
            queueCompensationAction(retryAction);
            log.info("Scheduled compensation retry: actionId={}, retryTime={}", 
                    action.getActionId(), retryTime);
        } else {
            log.error("Compensation action exceeded max retries: actionId={}", action.getActionId());
        }
    }
    
    /**
     * Builds service URL for compensation calls.
     */
    private String buildServiceUrl(String serviceName, String endpoint) {
        // This would use service discovery in production
        String baseUrl = getServiceBaseUrl(serviceName);
        return baseUrl + endpoint;
    }
    
    /**
     * Gets base URL for a service (simplified for demo).
     */
    private String getServiceBaseUrl(String serviceName) {
        // In production, this would use service discovery
        Map<String, String> serviceUrls = Map.of(
            INVENTORY_SERVICE, "http://inventory-service:8080",
            PAYMENT_SERVICE, "http://payment-service:8080",
            STORE_SERVICE, "http://store-service:8080"
        );
        
        return serviceUrls.getOrDefault(serviceName, "http://localhost:8080");
    }
    
    /**
     * Gets pending compensation actions count.
     */
    public int getPendingActionsCount() {
        return pendingActions.size();
    }
    
    /**
     * Gets all compensation actions for a saga.
     */
    public List<CompensationAction> getCompensationActions(String sagaId) {
        return actionRegistry.values().stream()
            .filter(action -> sagaId.equals(action.getSagaId()))
            .sorted(Comparator.comparingInt(CompensationAction::getPriority))
            .toList();
    }
    
    /**
     * Inner class for compensation definitions.
     */
    private static class CompensationDefinition {
        private final String serviceName;
        private final String endpoint;
        
        public CompensationDefinition(String serviceName, String endpoint) {
            this.serviceName = serviceName;
            this.endpoint = endpoint;
        }
        
        public String getServiceName() { return serviceName; }
        public String getEndpoint() { return endpoint; }
    }
}
