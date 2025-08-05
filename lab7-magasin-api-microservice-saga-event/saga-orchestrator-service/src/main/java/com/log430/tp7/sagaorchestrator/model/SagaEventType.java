package com.log430.tp7.sagaorchestrator.model;

/**
 * Enumeration of all possible saga event types for audit trail.
 * Used to categorize and filter saga events for monitoring and debugging.
 */
public enum SagaEventType {
    
    /**
     * Event fired when a new saga is initiated.
     */
    SAGA_STARTED,
    
    /**
     * Event fired when saga transitions from one state to another.
     */
    STATE_TRANSITION,
    
    /**
     * Event fired when a service call is initiated.
     */
    SERVICE_CALL_STARTED,
    
    /**
     * Event fired when a service call completes successfully.
     */
    SERVICE_CALL_COMPLETED,
    
    /**
     * Event fired when a service call fails.
     */
    SERVICE_CALL_FAILED,
    
    /**
     * Event fired when compensation logic starts.
     */
    COMPENSATION_STARTED,
    
    /**
     * Event fired when compensation logic completes.
     */
    COMPENSATION_COMPLETED,
    
    /**
     * Event fired when the entire saga completes (success or failure).
     */
    SAGA_COMPLETED,
    
    /**
     * Event fired when an error occurs during saga execution.
     */
    ERROR,
    
    /**
     * Event fired for timeout scenarios.
     */
    TIMEOUT,
    
    /**
     * Event fired for retry attempts.
     */
    RETRY,
    
    /**
     * Event fired for custom business logic events.
     */
    CUSTOM
}