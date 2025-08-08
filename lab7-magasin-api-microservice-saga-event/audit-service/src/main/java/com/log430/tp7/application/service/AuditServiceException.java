package com.log430.tp7.application.service;

/**
 * Exception thrown when audit service operations fail.
 */
public class AuditServiceException extends RuntimeException {
    
    public AuditServiceException(String message) {
        super(message);
    }
    
    public AuditServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
