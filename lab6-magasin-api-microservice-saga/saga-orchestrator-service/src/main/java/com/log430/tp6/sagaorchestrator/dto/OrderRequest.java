package com.log430.tp6.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for order creation
 */
public record OrderRequest(
    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customerId")
    String customerId,
    
    @NotBlank(message = "Product ID is required")
    @JsonProperty("productId")
    String productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    Integer quantity,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    BigDecimal amount,
    
    @NotBlank(message = "Saga ID is required")
    @JsonProperty("sagaId")
    String sagaId,
    
    @JsonProperty("stockReservationId")
    String stockReservationId,
    
    @JsonProperty("paymentTransactionId")
    String paymentTransactionId,
    
    @JsonProperty("shippingAddress")
    String shippingAddress,
    
    @JsonProperty("notes")
    String notes
) {}