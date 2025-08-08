package com.log430.tp7.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for initiating a saga-based sale transaction
 */
public record SaleRequest(
    @NotBlank(message = "Customer ID is required")
    @Size(min = 1, max = 100, message = "Customer ID must be between 1 and 100 characters")
    @JsonProperty("customerId")
    String customerId,
    
    @NotBlank(message = "Product ID is required")
    @Size(min = 1, max = 100, message = "Product ID must be between 1 and 100 characters")
    @JsonProperty("productId")
    String productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    @JsonProperty("quantity")
    Integer quantity,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999,999.99")
    @JsonProperty("amount")
    BigDecimal amount,
    
    @NotNull(message = "Payment details are required")
    @Valid
    @JsonProperty("paymentDetails")
    PaymentDetails paymentDetails
) {
    
    /**
     * Payment details for the transaction
     */
    public record PaymentDetails(
        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|PAYPAL|BANK_TRANSFER)$", 
                message = "Payment method must be one of: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER")
        @JsonProperty("paymentMethod")
        String paymentMethod,
        
        @Size(max = 20, message = "Card number cannot exceed 20 characters")
        @JsonProperty("cardNumber")
        String cardNumber,
        
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        @JsonProperty("expiryMonth")
        Integer expiryMonth,
        
        @Min(value = 2024, message = "Expiry year cannot be in the past")
        @Max(value = 2034, message = "Expiry year cannot be more than 10 years in the future")
        @JsonProperty("expiryYear")
        Integer expiryYear,
        
        @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
        @Pattern(regexp = "^\\d{3,4}$", message = "CVV must contain only digits")
        @JsonProperty("cvv")
        String cvv,
        
        @Size(max = 500, message = "Billing address cannot exceed 500 characters")
        @JsonProperty("billingAddress")
        String billingAddress
    ) {}
}