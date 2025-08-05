package com.log430.tp7.sagaorchestrator.exception;

/**
 * Base exception class for saga-related errors
 */
public class SagaException extends RuntimeException {
    
    private final String sagaId;
    
    public SagaException(String message) {
        super(message);
        this.sagaId = null;
    }
    
    public SagaException(String message, String sagaId) {
        super(message);
        this.sagaId = sagaId;
    }
    
    public SagaException(String message, Throwable cause) {
        super(message, cause);
        this.sagaId = null;
    }
    
    public SagaException(String message, String sagaId, Throwable cause) {
        super(message, cause);
        this.sagaId = sagaId;
    }
    
    public String getSagaId() {
        return sagaId;
    }
}