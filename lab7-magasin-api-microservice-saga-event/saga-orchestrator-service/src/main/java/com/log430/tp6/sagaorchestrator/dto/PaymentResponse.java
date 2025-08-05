package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment processing
 */
public record PaymentResponse(
    @JsonProperty("transactionId")
    String transactionId,
    
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("amount")
    BigDecimal amount,
    
    @JsonProperty("paymentMethod")
    String paymentMethod,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("processedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime processedAt,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("success")
    boolean success,
    
    @JsonProperty("authorizationCode")
    String authorizationCode,
    
    @JsonProperty("gatewayReference")
    String gatewayReference
) {
    
    /**
     * Creates a successful payment response
     */
    public static PaymentResponse success(String transactionId, String customerId, BigDecimal amount, 
                                        String paymentMethod, String sagaId, String authorizationCode) {
        return new PaymentResponse(transactionId, customerId, amount, paymentMethod, "COMPLETED", 
                                 sagaId, LocalDateTime.now(), "Payment processed successfully", 
                                 true, authorizationCode, null);
    }
    
    /**
     * Creates a failed payment response
     */
    public static PaymentResponse failure(String customerId, BigDecimal amount, String sagaId, 
                                        String errorMessage) {
        return new PaymentResponse(null, customerId, amount, null, "FAILED", 
                                 sagaId, LocalDateTime.now(), errorMessage, 
                                 false, null, null);
    }
}