package com.log430.tp7.sagaorchestrator.exception;

/**
 * Exception thrown when saga execution fails
 */
public class SagaExecutionException extends SagaException {
    
    public SagaExecutionException(String message, String sagaId) {
        super(message, sagaId);
    }
    
    public SagaExecutionException(String message, String sagaId, Throwable cause) {
        super(message, sagaId, cause);
    }
}