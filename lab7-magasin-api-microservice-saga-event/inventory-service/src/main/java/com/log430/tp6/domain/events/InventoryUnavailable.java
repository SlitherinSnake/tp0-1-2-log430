package com.log430.tp7.domain.inventory.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.infrastructure.event.DomainEvent;

/**
 * Event published when inventory reservation fails due to insufficient stock.
 * This event triggers compensation logic in the saga workflow.
 */
public class InventoryUnavailable extends DomainEvent {
    
    @JsonProperty("inventoryItemId")
    private final Long inventoryItemId;
    
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @JsonProperty("requestedQuantity")
    private final Integer requestedQuantity;
    
    @JsonProperty("availableStock")
    private final Integer availableStock;
    
    @JsonProperty("reason")
    private final String reason;
    
    public InventoryUnavailable(Long inventoryItemId, String transactionId, Integer requestedQuantity, 
                              Integer availableStock, String reason, String correlationId) {
        super("InventoryUnavailable", inventoryItemId.toString(), "inventory", 1, correlationId);
        this.inventoryItemId = inventoryItemId;
        this.transactionId = transactionId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
        this.reason = reason;
    }
    
    // Getters
    public Long getInventoryItemId() { return inventoryItemId; }
    public String getTransactionId() { return transactionId; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public Integer getAvailableStock() { return availableStock; }
    public String getReason() { return reason; }
    
    @Override
    public String toString() {
        return String.format("InventoryUnavailable{inventoryItemId=%d, transactionId='%s', requestedQuantity=%d, availableStock=%d, reason='%s', correlationId='%s'}", 
                           inventoryItemId, transactionId, requestedQuantity, availableStock, reason, getCorrelationId());
    }
}