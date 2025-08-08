package com.log430.tp7.domain.store.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

/**
 * Event published when an order has been delivered to the customer.
 * This event indicates the final completion of the order fulfillment process.
 */
public class OrderDelivered extends DomainEvent {
    
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
    
    @JsonProperty("deliveredAt")
    private final LocalDateTime deliveredAt;
    
    @JsonProperty("deliveryAddress")
    private final String deliveryAddress;
    
    @JsonProperty("deliveryMethod")
    private final String deliveryMethod;
    
    @JsonProperty("deliveryConfirmation")
    private final String deliveryConfirmation;
    
    public OrderDelivered(String orderId, String sagaId, String customerId, Long storeId,
                         BigDecimal totalAmount, LocalDateTime deliveredAt, String deliveryAddress,
                         String deliveryMethod, String deliveryConfirmation,
                         String correlationId, String causationId) {
        super("OrderDelivered", orderId, "Order", 1, correlationId, causationId);
        this.orderId = orderId;
        this.sagaId = sagaId;
        this.customerId = customerId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.deliveredAt = deliveredAt;
        this.deliveryAddress = deliveryAddress;
        this.deliveryMethod = deliveryMethod;
        this.deliveryConfirmation = deliveryConfirmation;
    }
    
    public OrderDelivered(String orderId, String sagaId, String customerId, Long storeId,
                         BigDecimal totalAmount, LocalDateTime deliveredAt, String deliveryAddress,
                         String deliveryMethod, String deliveryConfirmation,
                         String correlationId) {
        this(orderId, sagaId, customerId, storeId, totalAmount, deliveredAt, 
             deliveryAddress, deliveryMethod, deliveryConfirmation, correlationId, null);
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getSagaId() { return sagaId; }
    public String getCustomerId() { return customerId; }
    public Long getStoreId() { return storeId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public String getDeliveryConfirmation() { return deliveryConfirmation; }
}