package com.log430.tp7.sagaorchestrator.choreography.compensation;

/**
 * Status enumeration for compensation actions in choreographed sagas.
 */
public enum CompensationStatus {
    
    /**
     * Compensation action is pending execution.
     */
    PENDING("Compensation action is waiting to be executed"),
    
    /**
     * Compensation action is currently being executed.
     */
    IN_PROGRESS("Compensation action is being executed"),
    
    /**
     * Compensation action completed successfully.
     */
    COMPLETED("Compensation action completed successfully"),
    
    /**
     * Compensation action failed and may be retried.
     */
    FAILED("Compensation action failed"),
    
    /**
     * Compensation action was skipped (e.g., no compensation needed).
     */
    SKIPPED("Compensation action was skipped"),
    
    /**
     * Compensation action was cancelled before execution.
     */
    CANCELLED("Compensation action was cancelled");
    
    private final String description;
    
    CompensationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the status represents a terminal state (no further processing needed).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == SKIPPED || this == CANCELLED;
    }
    
    /**
     * Checks if the status allows for retry attempts.
     */
    public boolean canRetry() {
        return this == FAILED;
    }
    
    /**
     * Checks if the status indicates an active compensation.
     */
    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS;
    }
    
    /**
     * Gets valid transition states from current status.
     */
    public CompensationStatus[] getValidTransitions() {
        return switch (this) {
            case PENDING -> new CompensationStatus[]{IN_PROGRESS, CANCELLED, SKIPPED};
            case IN_PROGRESS -> new CompensationStatus[]{COMPLETED, FAILED, CANCELLED};
            case FAILED -> new CompensationStatus[]{IN_PROGRESS, CANCELLED, SKIPPED};
            case COMPLETED, SKIPPED, CANCELLED -> new CompensationStatus[]{}; // Terminal states
        };
    }
    
    /**
     * Validates if transition to target status is allowed.
     */
    public boolean canTransitionTo(CompensationStatus targetStatus) {
        CompensationStatus[] validTransitions = getValidTransitions();
        for (CompensationStatus valid : validTransitions) {
            if (valid == targetStatus) {
                return true;
            }
        }
        return false;
    }
}
