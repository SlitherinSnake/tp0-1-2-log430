package com.log430.tp7.domain.inventory.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.util.List;

/**
 * Event published when inventory is unavailable for a transaction.
 * This event triggers compensation logic in the payment service.
 */
public class InventoryUnavailable extends DomainEvent {
    
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @JsonProperty("unavailableItems")
    private final List<UnavailableItemData> unavailableItems;
    
    @JsonProperty("reason")
    private final String reason;
    
    public InventoryUnavailable(String transactionId, List<UnavailableItemData> unavailableItems, 
                               String reason, String correlationId, String causationId) {
        super("InventoryUnavailable", transactionId, "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.unavailableItems = unavailableItems;
        this.reason = reason;
    }
    
    public InventoryUnavailable(String transactionId, List<UnavailableItemData> unavailableItems, 
                               String reason, String correlationId) {
        this(transactionId, unavailableItems, reason, correlationId, null);
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public List<UnavailableItemData> getUnavailableItems() { return unavailableItems; }
    public String getReason() { return reason; }
    
    /**
     * Data class for unavailable inventory item information.
     */
    public static class UnavailableItemData {
        @JsonProperty("inventoryItemId")
        private final Long inventoryItemId;
        
        @JsonProperty("requestedQuantity")
        private final Integer requestedQuantity;
        
        @JsonProperty("availableQuantity")
        private final Integer availableQuantity;
        
        @JsonProperty("itemName")
        private final String itemName;
        
        public UnavailableItemData(Long inventoryItemId, Integer requestedQuantity, 
                                  Integer availableQuantity, String itemName) {
            this.inventoryItemId = inventoryItemId;
            this.requestedQuantity = requestedQuantity;
            this.availableQuantity = availableQuantity;
            this.itemName = itemName;
        }
        
        // Getters
        public Long getInventoryItemId() { return inventoryItemId; }
        public Integer getRequestedQuantity() { return requestedQuantity; }
        public Integer getAvailableQuantity() { return availableQuantity; }
        public String getItemName() { return itemName; }
    }
}