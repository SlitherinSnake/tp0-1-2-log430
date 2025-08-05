package com.log430.tp7.sagaorchestrator.exception;

import com.log430.tp7.sagaorchestrator.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for saga-specific errors and validation failures.
 * Provides consistent error responses across all saga endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error on request to {}: {}", request.getRequestURI(), ex.getMessage());
        
        List<String> validationErrors = new ArrayList<>();
        
        // Collect field validation errors
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        // Collect global validation errors
        ex.getBindingResult().getGlobalErrors().forEach(error -> 
            validationErrors.add(error.getObjectName() + ": " + error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.validationError(validationErrors, request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles constraint violation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationErrors(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation error on request to {}: {}", request.getRequestURI(), ex.getMessage());
        
        List<String> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(validationErrors, request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Malformed request body on request to {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Malformed Request", 
            "Request body is malformed or contains invalid JSON", 
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles method argument type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Method argument type mismatch on request to {}: {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Invalid Parameter", 
            message, 
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles saga not found errors
     */
    @ExceptionHandler(SagaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSagaNotFound(
            SagaNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Saga not found error on request to {}: sagaId={}, message={}", 
                   request.getRequestURI(), ex.getSagaId(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.sagaError(ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handles saga execution errors
     */
    @ExceptionHandler(SagaExecutionException.class)
    public ResponseEntity<ErrorResponse> handleSagaExecutionError(
            SagaExecutionException ex, HttpServletRequest request) {
        
        logger.error("Saga execution error on request to {}: sagaId={}, message={}", 
                    request.getRequestURI(), ex.getSagaId(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.sagaError(ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles general saga errors
     */
    @ExceptionHandler(SagaException.class)
    public ResponseEntity<ErrorResponse> handleSagaError(
            SagaException ex, HttpServletRequest request) {
        
        logger.error("Saga error on request to {}: sagaId={}, message={}", 
                    request.getRequestURI(), ex.getSagaId(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.sagaError(ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handles all other unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error on request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Internal Server Error", 
            "An unexpected error occurred. Please try again later.", 
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}