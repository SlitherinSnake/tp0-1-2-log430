package com.log430.tp7.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for fulfillment operations.
 */
@Schema(description = "Response for fulfillment operations")
public record FulfillmentResponse(
    
    @Schema(description = "Order ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String orderId,
    
    @Schema(description = "Operation success status", example = "true")
    boolean success,
    
    @Schema(description = "Response message", example = "Order fulfilled successfully")
    String message,
    
    @Schema(description = "Current fulfillment status", example = "FULFILLED")
    String status
) {
    
    public static FulfillmentResponse success(String orderId, String message, String status) {
        return new FulfillmentResponse(orderId, true, message, status);
    }
    
    public static FulfillmentResponse failure(String orderId, String message, String status) {
        return new FulfillmentResponse(orderId, false, message, status);
    }
}