package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for stock verification
 */
public record StockVerificationResponse(
    @JsonProperty("available")
    boolean available,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("requestedQuantity")
    Integer requestedQuantity,
    
    @JsonProperty("availableQuantity")
    Integer availableQuantity,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("success")
    boolean success
) {
    
    /**
     * Creates a successful stock verification response
     */
    public static StockVerificationResponse success(String productId, Integer requestedQuantity, 
                                                   Integer availableQuantity, boolean available) {
        String message = available ? "Stock is available" : "Insufficient stock";
        return new StockVerificationResponse(available, productId, requestedQuantity, 
                                           availableQuantity, message, true);
    }
    
    /**
     * Creates a failed stock verification response
     */
    public static StockVerificationResponse failure(String productId, String errorMessage) {
        return new StockVerificationResponse(false, productId, null, null, errorMessage, false);
    }
}