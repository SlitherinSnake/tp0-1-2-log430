package com.log430.tp7.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.sagaorchestrator.model.SagaState;

import java.time.LocalDateTime;

/**
 * Response DTO for saga operations
 */
public record SagaResponse(
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("state")
    SagaState state,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp
) {
    
    /**
     * Creates a successful saga response
     */
    public static SagaResponse success(String sagaId, SagaState state, String message) {
        return new SagaResponse(sagaId, state, message, LocalDateTime.now());
    }
    
    /**
     * Creates a failed saga response
     */
    public static SagaResponse failure(String sagaId, SagaState state, String errorMessage) {
        return new SagaResponse(sagaId, state, errorMessage, LocalDateTime.now());
    }
}