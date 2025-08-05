package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp6.sagaorchestrator.model.SagaState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for saga status information
 */
public record SagaStatus(
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("currentState")
    SagaState currentState,
    
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("amount")
    BigDecimal amount,
    
    @JsonProperty("stockReservationId")
    String stockReservationId,
    
    @JsonProperty("paymentTransactionId")
    String paymentTransactionId,
    
    @JsonProperty("orderId")
    String orderId,
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt,
    
    @JsonProperty("errorMessage")
    String errorMessage
) {}