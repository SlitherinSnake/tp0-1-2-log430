package com.log430.tp7.presentation.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for order fulfillment operations.
 */
@Schema(description = "Request for fulfilling an order")
public record FulfillmentRequest(
    
    @Schema(description = "Correlation ID for tracking the request", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "Correlation ID is required")
    String correlationId,
    
    @Schema(description = "Estimated delivery time", example = "2024-12-07T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime estimatedDeliveryTime
) {}