package com.log430.tp6.presentation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for stock verification in saga operations.
 */
public record StockVerificationRequest(
    @NotBlank(message = "Product ID cannot be blank")
    String productId,
    
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    @NotBlank(message = "Saga ID cannot be blank")
    String sagaId
) {}