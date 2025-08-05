package com.log430.tp6.sagaorchestrator.exception;

/**
 * Exception thrown when a saga is not found
 */
public class SagaNotFoundException extends SagaException {
    
    public SagaNotFoundException(String sagaId) {
        super("Saga not found: " + sagaId, sagaId);
    }
    
    public SagaNotFoundException(String message, String sagaId) {
        super(message, sagaId);
    }
}