package com.log430.tp7.domain.inventory.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.infrastructure.event.DomainEvent;

/**
 * Event published when previously reserved inventory is released back to available stock.
 * This is typically a compensation event in saga workflows when transactions fail.
 */
public class InventoryReleased extends DomainEvent {
    
    @JsonProperty("inventoryItemId")
    private final Long inventoryItemId;
    
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @JsonProperty("quantity")
    private final Integer quantity;
    
    @JsonProperty("reservationId")
    private final String reservationId;
    
    @JsonProperty("newAvailableStock")
    private final Integer newAvailableStock;
    
    @JsonProperty("reason")
    private final String reason;
    
    public InventoryReleased(Long inventoryItemId, String transactionId, Integer quantity, 
                           String reservationId, Integer newAvailableStock, String reason, String correlationId) {
        super("InventoryReleased", inventoryItemId.toString(), "inventory", 1, correlationId);
        this.inventoryItemId = inventoryItemId;
        this.transactionId = transactionId;
        this.quantity = quantity;
        this.reservationId = reservationId;
        this.newAvailableStock = newAvailableStock;
        this.reason = reason;
    }
    
    // Getters
    public Long getInventoryItemId() { return inventoryItemId; }
    public String getTransactionId() { return transactionId; }
    public Integer getQuantity() { return quantity; }
    public String getReservationId() { return reservationId; }
    public Integer getNewAvailableStock() { return newAvailableStock; }
    public String getReason() { return reason; }
    
    @Override
    public String toString() {
        return String.format("InventoryReleased{inventoryItemId=%d, transactionId='%s', quantity=%d, reservationId='%s', newAvailableStock=%d, reason='%s', correlationId='%s'}", 
                           inventoryItemId, transactionId, quantity, reservationId, newAvailableStock, reason, getCorrelationId());
    }
}