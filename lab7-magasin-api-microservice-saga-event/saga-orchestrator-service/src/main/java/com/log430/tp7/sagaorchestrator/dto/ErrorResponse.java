package com.log430.tp7.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response DTO for API error handling
 */
public record ErrorResponse(
    @JsonProperty("error")
    String error,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("details")
    List<String> details,
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    
    @JsonProperty("path")
    String path
) {
    
    /**
     * Creates a simple error response with a single message
     */
    public static ErrorResponse of(String error, String message, String path) {
        return new ErrorResponse(error, message, null, LocalDateTime.now(), path);
    }
    
    /**
     * Creates a detailed error response with multiple validation errors
     */
    public static ErrorResponse of(String error, String message, List<String> details, String path) {
        return new ErrorResponse(error, message, details, LocalDateTime.now(), path);
    }
    
    /**
     * Creates a validation error response
     */
    public static ErrorResponse validationError(List<String> validationErrors, String path) {
        return new ErrorResponse(
            "Validation Failed", 
            "Request validation failed", 
            validationErrors, 
            LocalDateTime.now(), 
            path
        );
    }
    
    /**
     * Creates a saga-specific error response
     */
    public static ErrorResponse sagaError(String message, String path) {
        return new ErrorResponse(
            "Saga Error", 
            message, 
            null, 
            LocalDateTime.now(), 
            path
        );
    }
}