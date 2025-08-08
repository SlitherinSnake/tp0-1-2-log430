package com.log430.tp7.domain.inventory.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.infrastructure.event.DomainEvent;

/**
 * Event published when inventory is successfully reserved for an order.
 * This event indicates that the requested quantity has been allocated
 * and is no longer available for other orders.
 */
public class InventoryReserved extends DomainEvent {
    
    @JsonProperty("inventoryItemId")
    private final Long inventoryItemId;
    
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @JsonProperty("quantity")
    private final Integer quantity;
    
    @JsonProperty("reservationId")
    private final String reservationId;
    
    @JsonProperty("remainingStock")
    private final Integer remainingStock;
    
    public InventoryReserved(Long inventoryItemId, String transactionId, Integer quantity, 
                           String reservationId, Integer remainingStock, String correlationId) {
        super("InventoryReserved", inventoryItemId.toString(), "inventory", 1, correlationId);
        this.inventoryItemId = inventoryItemId;
        this.transactionId = transactionId;
        this.quantity = quantity;
        this.reservationId = reservationId;
        this.remainingStock = remainingStock;
    }
    
    // Getters
    public Long getInventoryItemId() { return inventoryItemId; }
    public String getTransactionId() { return transactionId; }
    public Integer getQuantity() { return quantity; }
    public String getReservationId() { return reservationId; }
    public Integer getRemainingStock() { return remainingStock; }
    
    @Override
    public String toString() {
        return String.format("InventoryReserved{inventoryItemId=%d, transactionId='%s', quantity=%d, reservationId='%s', remainingStock=%d, correlationId='%s'}", 
                           inventoryItemId, transactionId, quantity, reservationId, remainingStock, getCorrelationId());
    }
}