package com.log430.tp7.presentation.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order creation in saga operations.
 */
public record OrderResponse(
    boolean success,
    String orderId,
    String sagaId,
    String customerId,
    Long storeId,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt,
    String message
) {
    public static OrderResponse success(String orderId, String sagaId, String customerId, 
                                      Long storeId, BigDecimal totalAmount) {
        return new OrderResponse(
            true,
            orderId,
            sagaId,
            customerId,
            storeId,
            totalAmount,
            "CONFIRMED",
            LocalDateTime.now(),
            "Order created successfully"
        );
    }
    
    public static OrderResponse failure(String sagaId, String customerId, Long storeId, 
                                      BigDecimal totalAmount, String message) {
        return new OrderResponse(
            false,
            null,
            sagaId,
            customerId,
            storeId,
            totalAmount,
            "FAILED",
            LocalDateTime.now(),
            message
        );
    }
}