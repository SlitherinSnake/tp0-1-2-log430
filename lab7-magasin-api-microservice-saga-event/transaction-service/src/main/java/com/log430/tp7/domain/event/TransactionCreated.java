package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Event published when a new transaction is created.
 * This event initiates the choreographed saga for transaction processing.
 */
public class TransactionCreated extends DomainEvent {
    
    @JsonProperty("transactionId")
    private final Long transactionId;
    
    @JsonProperty("transactionType")
    private final String transactionType;
    
    @JsonProperty("personnelId")
    private final Long personnelId;
    
    @JsonProperty("storeId")
    private final Long storeId;
    
    @JsonProperty("totalAmount")
    private final Double totalAmount;
    
    @JsonProperty("transactionDate")
    private final LocalDate transactionDate;
    
    @JsonProperty("items")
    private final List<TransactionItemData> items;
    
    @JsonProperty("originalTransactionId")
    private final Long originalTransactionId; // For returns
    
    @JsonProperty("returnReason")
    private final String returnReason; // For returns
    
    public TransactionCreated(Long transactionId, String transactionType, Long personnelId, 
                             Long storeId, Double totalAmount, LocalDate transactionDate,
                             List<TransactionItemData> items, String correlationId, String causationId) {
        super("TransactionCreated", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.items = items;
        this.originalTransactionId = null;
        this.returnReason = null;
    }
    
    // Constructor for return transactions
    public TransactionCreated(Long transactionId, String transactionType, Long personnelId, 
                             Long storeId, Double totalAmount, LocalDate transactionDate,
                             List<TransactionItemData> items, Long originalTransactionId, 
                             String returnReason, String correlationId, String causationId) {
        super("TransactionCreated", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.items = items;
        this.originalTransactionId = originalTransactionId;
        this.returnReason = returnReason;
    }
    
    // Getters
    public Long getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public Long getPersonnelId() { return personnelId; }
    public Long getStoreId() { return storeId; }
    public Double getTotalAmount() { return totalAmount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public List<TransactionItemData> getItems() { return items; }
    public Long getOriginalTransactionId() { return originalTransactionId; }
    public String getReturnReason() { return returnReason; }
    
    /**
     * Data class for transaction item information in events.
     */
    public static class TransactionItemData {
        @JsonProperty("inventoryItemId")
        private final Long inventoryItemId;
        
        @JsonProperty("quantity")
        private final Integer quantity;
        
        @JsonProperty("unitPrice")
        private final Double unitPrice;
        
        @JsonProperty("subtotal")
        private final Double subtotal;
        
        public TransactionItemData(Long inventoryItemId, Integer quantity, Double unitPrice, Double subtotal) {
            this.inventoryItemId = inventoryItemId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }
        
        // Getters
        public Long getInventoryItemId() { return inventoryItemId; }
        public Integer getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public Double getSubtotal() { return subtotal; }
    }
}