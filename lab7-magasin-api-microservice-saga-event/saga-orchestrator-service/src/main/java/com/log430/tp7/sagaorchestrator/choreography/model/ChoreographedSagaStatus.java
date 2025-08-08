package com.log430.tp7.sagaorchestrator.choreography.model;

/**
 * Enumeration representing the status of a choreographed saga.
 * Tracks the overall state of distributed transaction coordination without a central orchestrator.
 */
public enum ChoreographedSagaStatus {
    
    /**
     * Saga has been initiated and is waiting for the first step to complete.
     */
    STARTED("Saga has been started"),
    
    /**
     * Saga is in progress with one or more steps completed successfully.
     */
    IN_PROGRESS("Saga is executing business steps"),
    
    /**
     * A step has failed and the saga is attempting to retry.
     */
    RETRYING("Saga is retrying a failed step"),
    
    /**
     * Saga has failed and compensation is in progress.
     * This status indicates that rollback operations are being executed.
     */
    COMPENSATING("Saga is executing compensation actions"),
    
    /**
     * All saga steps have completed successfully.
     * This is a final success state.
     */
    COMPLETED("Saga completed successfully"),
    
    /**
     * Saga has failed and compensation has been completed.
     * This is a final failure state with successful rollback.
     */
    COMPENSATED("Saga failed but compensation completed"),
    
    /**
     * Saga has failed and either compensation failed or was not possible.
     * This is a final failure state requiring manual intervention.
     */
    FAILED("Saga failed with incomplete compensation"),
    
    /**
     * Saga has timed out waiting for step completion.
     * May trigger automatic compensation depending on configuration.
     */
    TIMED_OUT("Saga timed out waiting for completion");
    
    private final String description;
    
    ChoreographedSagaStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status represents a final state (no further transitions expected).
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == COMPENSATED || this == FAILED;
    }
    
    /**
     * Checks if this status indicates the saga is currently active.
     */
    public boolean isActive() {
        return this == STARTED || this == IN_PROGRESS || this == RETRYING;
    }
    
    /**
     * Checks if this status indicates compensation is needed or in progress.
     */
    public boolean isCompensating() {
        return this == COMPENSATING;
    }
    
    /**
     * Checks if this status represents a successful outcome.
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Checks if this status represents a failure outcome.
     */
    public boolean isFailure() {
        return this == FAILED || this == COMPENSATED || this == TIMED_OUT;
    }
    
    /**
     * Gets the next logical status based on the current status and outcome.
     */
    public ChoreographedSagaStatus getNextStatus(boolean stepSuccessful, boolean isLastStep, boolean compensationNeeded) {
        switch (this) {
            case STARTED:
                if (stepSuccessful) {
                    return isLastStep ? COMPLETED : IN_PROGRESS;
                } else {
                    return compensationNeeded ? COMPENSATING : RETRYING;
                }
            case IN_PROGRESS:
                if (stepSuccessful) {
                    return isLastStep ? COMPLETED : IN_PROGRESS;
                } else {
                    return compensationNeeded ? COMPENSATING : RETRYING;
                }
            case RETRYING:
                if (stepSuccessful) {
                    return isLastStep ? COMPLETED : IN_PROGRESS;
                } else {
                    return COMPENSATING;
                }
            case COMPENSATING:
                return stepSuccessful ? COMPENSATED : FAILED;
            default:
                return this; // Final states don't transition
        }
    }
}
