package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order creation
 */
public record OrderResponse(
    @JsonProperty("orderId")
    String orderId,
    
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("amount")
    BigDecimal amount,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("stockReservationId")
    String stockReservationId,
    
    @JsonProperty("paymentTransactionId")
    String paymentTransactionId,
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("success")
    boolean success,
    
    @JsonProperty("orderNumber")
    String orderNumber,
    
    @JsonProperty("estimatedDelivery")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime estimatedDelivery
) {
    
    /**
     * Creates a successful order response
     */
    public static OrderResponse success(String orderId, String customerId, String productId, 
                                      Integer quantity, BigDecimal amount, String sagaId,
                                      String stockReservationId, String paymentTransactionId,
                                      String orderNumber, LocalDateTime estimatedDelivery) {
        return new OrderResponse(orderId, customerId, productId, quantity, amount, "CONFIRMED", 
                               sagaId, stockReservationId, paymentTransactionId, LocalDateTime.now(),
                               "Order created successfully", true, orderNumber, estimatedDelivery);
    }
    
    /**
     * Creates a failed order response
     */
    public static OrderResponse failure(String customerId, String productId, String sagaId, 
                                      String errorMessage) {
        return new OrderResponse(null, customerId, productId, null, null, "FAILED", 
                               sagaId, null, null, LocalDateTime.now(),
                               errorMessage, false, null, null);
    }
}