package com.log430.tp7.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for order delivery operations.
 */
@Schema(description = "Request for delivering an order")
public record DeliveryRequest(
    
    @Schema(description = "Correlation ID for tracking the request", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "Correlation ID is required")
    String correlationId,
    
    @Schema(description = "Delivery address", example = "123 Main St, Montreal, QC H3A 1A1")
    @NotBlank(message = "Delivery address is required")
    String deliveryAddress,
    
    @Schema(description = "Delivery method", example = "COURIER")
    @NotBlank(message = "Delivery method is required")
    String deliveryMethod,
    
    @Schema(description = "Delivery confirmation code", example = "DEL-123456")
    @NotBlank(message = "Delivery confirmation is required")
    String deliveryConfirmation
) {}