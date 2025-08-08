package com.log430.tp7.presentation.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for payment processing in saga operations.
 */
public record PaymentRequest(
    @NotBlank(message = "Saga ID cannot be blank")
    String sagaId,
    
    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,
    
    @NotNull(message = "Payment details cannot be null")
    PaymentDetails paymentDetails
) {
    public record PaymentDetails(
        @NotBlank(message = "Payment method cannot be blank")
        String paymentMethod,
        
        String cardNumber,
        String cardHolderName,
        String expiryDate,
        String cvv
    ) {}
}