package com.log430.tp7.presentation.api.dto;

/**
 * Response DTO for stock verification in saga operations.
 */
public record StockVerificationResponse(
    boolean available,
    String productId,
    Integer requestedQuantity,
    Integer availableQuantity,
    String message,
    String sagaId
) {
    public static StockVerificationResponse success(String productId, Integer requestedQuantity, Integer availableQuantity, String sagaId) {
        return new StockVerificationResponse(
            true,
            productId,
            requestedQuantity,
            availableQuantity,
            "Stock verification successful",
            sagaId
        );
    }
    
    public static StockVerificationResponse failure(String productId, Integer requestedQuantity, Integer availableQuantity, String message, String sagaId) {
        return new StockVerificationResponse(
            false,
            productId,
            requestedQuantity,
            availableQuantity,
            message,
            sagaId
        );
    }
}