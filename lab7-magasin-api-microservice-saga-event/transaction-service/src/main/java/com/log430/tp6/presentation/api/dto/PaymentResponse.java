package com.log430.tp7.presentation.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment processing in saga operations.
 */
public record PaymentResponse(
    boolean success,
    String transactionId,
    String sagaId,
    String customerId,
    BigDecimal amount,
    String paymentMethod,
    LocalDateTime processedAt,
    String message,
    String errorCode
) {
    public static PaymentResponse success(String transactionId, String sagaId, String customerId, 
                                        BigDecimal amount, String paymentMethod) {
        return new PaymentResponse(
            true,
            transactionId,
            sagaId,
            customerId,
            amount,
            paymentMethod,
            LocalDateTime.now(),
            "Payment processed successfully",
            null
        );
    }
    
    public static PaymentResponse failure(String sagaId, String customerId, BigDecimal amount, 
                                        String message, String errorCode) {
        return new PaymentResponse(
            false,
            null,
            sagaId,
            customerId,
            amount,
            null,
            LocalDateTime.now(),
            message,
            errorCode
        );
    }
}