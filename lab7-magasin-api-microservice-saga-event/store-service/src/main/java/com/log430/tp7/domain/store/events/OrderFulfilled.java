package com.log430.tp7.domain.store.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

/**
 * Event published when an order has been fulfilled by the store.
 * This event indicates that the order items have been prepared and are ready for delivery.
 */
public class OrderFulfilled extends DomainEvent {
    
    @JsonProperty("orderId")
    private final String orderId;
    
    @JsonProperty("sagaId")
    private final String sagaId;
    
    @JsonProperty("customerId")
    private final String customerId;
    
    @JsonProperty("storeId")
    private final Long storeId;
    
    @JsonProperty("totalAmount")
    private final BigDecimal totalAmount;
    
    @JsonProperty("items")
    private final List<FulfilledItem> items;
    
    @JsonProperty("fulfilledAt")
    private final LocalDateTime fulfilledAt;
    
    @JsonProperty("estimatedDeliveryTime")
    private final LocalDateTime estimatedDeliveryTime;
    
    public OrderFulfilled(String orderId, String sagaId, String customerId, Long storeId,
                         BigDecimal totalAmount, List<FulfilledItem> items,
                         LocalDateTime fulfilledAt, LocalDateTime estimatedDeliveryTime,
                         String correlationId, String causationId) {
        super("OrderFulfilled", orderId, "Order", 1, correlationId, causationId);
        this.orderId = orderId;
        this.sagaId = sagaId;
        this.customerId = customerId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.items = items;
        this.fulfilledAt = fulfilledAt;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
    
    public OrderFulfilled(String orderId, String sagaId, String customerId, Long storeId,
                         BigDecimal totalAmount, List<FulfilledItem> items,
                         LocalDateTime fulfilledAt, LocalDateTime estimatedDeliveryTime,
                         String correlationId) {
        this(orderId, sagaId, customerId, storeId, totalAmount, items, 
             fulfilledAt, estimatedDeliveryTime, correlationId, null);
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getSagaId() { return sagaId; }
    public String getCustomerId() { return customerId; }
    public Long getStoreId() { return storeId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<FulfilledItem> getItems() { return items; }
    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    
    /**
     * Represents an item that has been fulfilled in the order.
     */
    public static class FulfilledItem {
        @JsonProperty("productId")
        private final String productId;
        
        @JsonProperty("quantity")
        private final Integer quantity;
        
        @JsonProperty("unitPrice")
        private final BigDecimal unitPrice;
        
        public FulfilledItem(String productId, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public String getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }
}