package com.log430.tp7.sagaorchestrator.choreography.compensation;

import java.time.LocalDateTime;

/**
 * Represents a compensation action that needs to be executed
 * when a choreographed saga step fails or times out.
 */
public class CompensationAction {
    
    private final String actionId;
    private final String sagaId;
    private final String correlationId;
    private final String stepName;
    private final String serviceName;
    private final String compensationEndpoint;
    private final String compensationPayload;
    private final int priority;
    private final LocalDateTime createdAt;
    private LocalDateTime executeAfter;
    private CompensationStatus status;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime lastAttempt;
    
    // Private constructor - use Builder
    private CompensationAction(Builder builder) {
        this.actionId = builder.actionId;
        this.sagaId = builder.sagaId;
        this.correlationId = builder.correlationId;
        this.stepName = builder.stepName;
        this.serviceName = builder.serviceName;
        this.compensationEndpoint = builder.compensationEndpoint;
        this.compensationPayload = builder.compensationPayload;
        this.priority = builder.priority;
        this.createdAt = LocalDateTime.now();
        this.executeAfter = builder.executeAfter != null ? builder.executeAfter : LocalDateTime.now();
        this.status = CompensationStatus.PENDING;
        this.retryCount = 0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getActionId() { return actionId; }
    public String getSagaId() { return sagaId; }
    public String getCorrelationId() { return correlationId; }
    public String getStepName() { return stepName; }
    public String getServiceName() { return serviceName; }
    public String getCompensationEndpoint() { return compensationEndpoint; }
    public String getCompensationPayload() { return compensationPayload; }
    public int getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExecuteAfter() { return executeAfter; }
    public CompensationStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getLastAttempt() { return lastAttempt; }
    
    // Status management
    public void markInProgress() {
        this.status = CompensationStatus.IN_PROGRESS;
        this.lastAttempt = LocalDateTime.now();
    }
    
    public void markCompleted() {
        this.status = CompensationStatus.COMPLETED;
    }
    
    public void markFailed(String errorMessage) {
        this.status = CompensationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.lastAttempt = LocalDateTime.now();
    }
    
    public void markSkipped(String reason) {
        this.status = CompensationStatus.SKIPPED;
        this.errorMessage = reason;
    }
    
    public boolean isReadyForExecution() {
        return status == CompensationStatus.PENDING && 
               LocalDateTime.now().isAfter(executeAfter);
    }
    
    public boolean canRetry(int maxRetries) {
        return status == CompensationStatus.FAILED && 
               retryCount < maxRetries;
    }
    
    public boolean isTerminal() {
        return status == CompensationStatus.COMPLETED || 
               status == CompensationStatus.SKIPPED ||
               (status == CompensationStatus.FAILED && retryCount >= 3); // Default max retries
    }
    
    @Override
    public String toString() {
        return String.format("CompensationAction{actionId='%s', sagaId='%s', stepName='%s', " +
                           "serviceName='%s', status=%s, retryCount=%d}",
                           actionId, sagaId, stepName, serviceName, status, retryCount);
    }
    
    /**
     * Builder class for CompensationAction.
     */
    public static class Builder {
        private String actionId;
        private String sagaId;
        private String correlationId;
        private String stepName;
        private String serviceName;
        private String compensationEndpoint;
        private String compensationPayload;
        private int priority = 0;
        private LocalDateTime executeAfter;
        
        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }
        
        public Builder sagaId(String sagaId) {
            this.sagaId = sagaId;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder compensationEndpoint(String compensationEndpoint) {
            this.compensationEndpoint = compensationEndpoint;
            return this;
        }
        
        public Builder compensationPayload(String compensationPayload) {
            this.compensationPayload = compensationPayload;
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder executeAfter(LocalDateTime executeAfter) {
            this.executeAfter = executeAfter;
            return this;
        }
        
        public CompensationAction build() {
            if (actionId == null || sagaId == null || correlationId == null || 
                stepName == null || serviceName == null || compensationEndpoint == null) {
                throw new IllegalArgumentException("Required fields cannot be null");
            }
            return new CompensationAction(this);
        }
    }
}
