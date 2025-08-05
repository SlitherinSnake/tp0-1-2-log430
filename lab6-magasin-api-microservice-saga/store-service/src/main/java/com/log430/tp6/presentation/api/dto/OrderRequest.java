package com.log430.tp6.presentation.api.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for order creation in saga operations.
 */
public record OrderRequest(
    @NotBlank(message = "Saga ID cannot be blank")
    String sagaId,
    
    @NotBlank(message = "Customer ID cannot be blank")
    String customerId,
    
    @NotNull(message = "Store ID cannot be null")
    @Positive(message = "Store ID must be positive")
    Long storeId,
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    List<OrderItem> items,
    
    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    BigDecimal totalAmount,
    
    String paymentTransactionId,
    String stockReservationId
) {
    public record OrderItem(
        @NotBlank(message = "Product ID cannot be blank")
        String productId,
        
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        Integer quantity,
        
        @NotNull(message = "Unit price cannot be null")
        @Positive(message = "Unit price must be positive")
        BigDecimal unitPrice
    ) {}
}