package com.log430.tp7.presentation.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for stock reservation in saga operations.
 */
public record StockReservationResponse(
    boolean success,
    String reservationId,
    String productId,
    Integer quantity,
    String sagaId,
    LocalDateTime expiresAt,
    String message
) {
    public static StockReservationResponse success(String reservationId, String productId, Integer quantity, String sagaId, LocalDateTime expiresAt) {
        return new StockReservationResponse(
            true,
            reservationId,
            productId,
            quantity,
            sagaId,
            expiresAt,
            "Stock reserved successfully"
        );
    }
    
    public static StockReservationResponse failure(String productId, Integer quantity, String sagaId, String message) {
        return new StockReservationResponse(
            false,
            null,
            productId,
            quantity,
            sagaId,
            null,
            message
        );
    }
}