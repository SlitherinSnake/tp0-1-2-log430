package com.log430.tp7.sagaorchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for stock verification
 */
public record StockVerificationRequest(
    @NotBlank(message = "Product ID is required")
    @JsonProperty("productId")
    String productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("sagaId")
    String sagaId
) {}