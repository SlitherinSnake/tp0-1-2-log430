package com.log430.tp7.sagaorchestrator.choreography.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entity representing a choreographed saga state for distributed transaction coordination.
 * Unlike orchestrated sagas, this tracks the distributed state across multiple services
 * without a central orchestrator, using event-driven coordination.
 */
@Entity
@Table(name = "choreographed_saga_state", indexes = {
    @Index(name = "idx_correlation_id", columnList = "correlationId"),
    @Index(name = "idx_saga_type", columnList = "sagaType"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_timeout_at", columnList = "timeoutAt")
})
public class ChoreographedSagaState {
    
    @Id
    @Column(name = "saga_id", length = 36)
    private String sagaId;
    
    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;
    
    @Column(name = "saga_type", nullable = false, length = 100)
    private String sagaType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ChoreographedSagaStatus status;
    
    @Column(name = "current_step", length = 100)
    private String currentStep;
    
    @Column(name = "completed_steps", columnDefinition = "TEXT")
    private String completedSteps; // JSON array of completed step names
    
    @Column(name = "failed_steps", columnDefinition = "TEXT")
    private String failedSteps; // JSON array of failed step names
    
    @Column(name = "compensation_required", nullable = false)
    private Boolean compensationRequired = false;
    
    @Column(name = "compensation_completed", nullable = false)
    private Boolean compensationCompleted = false;
    
    @Column(name = "saga_data", columnDefinition = "TEXT")
    private String sagaData; // JSON data containing saga context
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Version
    private Long version;
    
    // Constructors
    public ChoreographedSagaState() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ChoreographedSagaStatus.STARTED;
    }
    
    public ChoreographedSagaState(String sagaId, String correlationId, String sagaType) {
        this();
        this.sagaId = sagaId;
        this.correlationId = correlationId;
        this.sagaType = sagaType;
        this.timeoutAt = LocalDateTime.now().plusMinutes(30); // Default 30-minute timeout
    }
    
    // Business methods
    public void markStepCompleted(String stepName) {
        this.currentStep = stepName;
        this.updatedAt = LocalDateTime.now();
        addToCompletedSteps(stepName);
    }
    
    public void markStepFailed(String stepName, String errorMessage) {
        this.currentStep = stepName;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
        addToFailedSteps(stepName);
        
        if (shouldTriggerCompensation()) {
            this.compensationRequired = true;
            this.status = ChoreographedSagaStatus.COMPENSATING;
        } else if (canRetry()) {
            this.retryCount++;
            this.status = ChoreographedSagaStatus.RETRYING;
        } else {
            this.status = ChoreographedSagaStatus.FAILED;
        }
    }
    
    public void markCompleted() {
        this.status = ChoreographedSagaStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markFailed(String errorMessage) {
        this.status = ChoreographedSagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markCompensationCompleted() {
        this.compensationCompleted = true;
        this.status = ChoreographedSagaStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isTimedOut() {
        return timeoutAt != null && LocalDateTime.now().isAfter(timeoutAt);
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public boolean isInFinalState() {
        return status == ChoreographedSagaStatus.COMPLETED || 
               status == ChoreographedSagaStatus.FAILED || 
               status == ChoreographedSagaStatus.COMPENSATED;
    }
    
    private boolean shouldTriggerCompensation() {
        // Trigger compensation if we've exhausted retries or have critical failures
        return !canRetry() || (completedSteps != null && !completedSteps.isEmpty());
    }
    
    private void addToCompletedSteps(String stepName) {
        if (completedSteps == null || completedSteps.isEmpty()) {
            completedSteps = "[\"" + stepName + "\"]";
        } else {
            // Simple JSON array manipulation - in production, use proper JSON library
            completedSteps = completedSteps.substring(0, completedSteps.length() - 1) + 
                           ",\"" + stepName + "\"]";
        }
    }
    
    private void addToFailedSteps(String stepName) {
        if (failedSteps == null || failedSteps.isEmpty()) {
            failedSteps = "[\"" + stepName + "\"]";
        } else {
            failedSteps = failedSteps.substring(0, failedSteps.length() - 1) + 
                         ",\"" + stepName + "\"]";
        }
    }
    
    // Getters and Setters
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getSagaType() { return sagaType; }
    public void setSagaType(String sagaType) { this.sagaType = sagaType; }
    
    public ChoreographedSagaStatus getStatus() { return status; }
    public void setStatus(ChoreographedSagaStatus status) { this.status = status; }
    
    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    
    public String getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(String completedSteps) { this.completedSteps = completedSteps; }
    
    public String getFailedSteps() { return failedSteps; }
    public void setFailedSteps(String failedSteps) { this.failedSteps = failedSteps; }
    
    public Boolean getCompensationRequired() { return compensationRequired; }
    public void setCompensationRequired(Boolean compensationRequired) { this.compensationRequired = compensationRequired; }
    
    public Boolean getCompensationCompleted() { return compensationCompleted; }
    public void setCompensationCompleted(Boolean compensationCompleted) { this.compensationCompleted = compensationCompleted; }
    
    public String getSagaData() { return sagaData; }
    public void setSagaData(String sagaData) { this.sagaData = sagaData; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getTimeoutAt() { return timeoutAt; }
    public void setTimeoutAt(LocalDateTime timeoutAt) { this.timeoutAt = timeoutAt; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoreographedSagaState that = (ChoreographedSagaState) o;
        return Objects.equals(sagaId, that.sagaId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sagaId);
    }
    
    @Override
    public String toString() {
        return String.format("ChoreographedSagaState{sagaId='%s', correlationId='%s', " +
                           "sagaType='%s', status=%s, currentStep='%s', createdAt=%s}",
                           sagaId, correlationId, sagaType, status, currentStep, createdAt);
    }
}
