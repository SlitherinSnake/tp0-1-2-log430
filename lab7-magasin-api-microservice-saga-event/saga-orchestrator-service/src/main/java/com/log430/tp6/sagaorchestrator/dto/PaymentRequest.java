package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for payment processing
 */
public record PaymentRequest(
    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customerId")
    String customerId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    BigDecimal amount,
    
    @NotBlank(message = "Payment method is required")
    @JsonProperty("paymentMethod")
    String paymentMethod,
    
    @JsonProperty("cardNumber")
    String cardNumber,
    
    @JsonProperty("expiryMonth")
    Integer expiryMonth,
    
    @JsonProperty("expiryYear")
    Integer expiryYear,
    
    @JsonProperty("cvv")
    String cvv,
    
    @JsonProperty("billingAddress")
    String billingAddress,
    
    @NotBlank(message = "Saga ID is required")
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("description")
    String description
) {}