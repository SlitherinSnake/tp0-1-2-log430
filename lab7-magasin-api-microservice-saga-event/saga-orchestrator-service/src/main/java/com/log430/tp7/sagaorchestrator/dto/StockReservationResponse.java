package com.log430.tp7.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Response DTO for stock reservation
 */
public record StockReservationResponse(
    @JsonProperty("reservationId")
    String reservationId,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("expiresAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expiresAt,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("success")
    boolean success
) {
    
    /**
     * Creates a successful stock reservation response
     */
    public static StockReservationResponse success(String reservationId, String productId, 
                                                 Integer quantity, String sagaId, LocalDateTime expiresAt) {
        return new StockReservationResponse(reservationId, productId, quantity, sagaId, 
                                          "ACTIVE", expiresAt, "Stock reserved successfully", true);
    }
    
    /**
     * Creates a failed stock reservation response
     */
    public static StockReservationResponse failure(String productId, String sagaId, String errorMessage) {
        return new StockReservationResponse(null, productId, null, sagaId, 
                                          "FAILED", null, errorMessage, false);
    }
}