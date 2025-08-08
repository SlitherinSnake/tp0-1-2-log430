package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Event published when a transaction is successfully completed.
 * This event indicates the successful end of the choreographed saga.
 */
public class TransactionCompleted extends DomainEvent {
    
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
    
    @JsonProperty("completionDate")
    private final LocalDate completionDate;
    
    @JsonProperty("items")
    private final List<TransactionCreated.TransactionItemData> items;
    
    @JsonProperty("originalTransactionId")
    private final Long originalTransactionId; // For returns
    
    @JsonProperty("returnReason")
    private final String returnReason; // For returns
    
    public TransactionCompleted(Long transactionId, String transactionType, Long personnelId, 
                               Long storeId, Double totalAmount, LocalDate transactionDate,
                               LocalDate completionDate, List<TransactionCreated.TransactionItemData> items,
                               String correlationId, String causationId) {
        super("TransactionCompleted", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.completionDate = completionDate;
        this.items = items;
        this.originalTransactionId = null;
        this.returnReason = null;
    }
    
    // Constructor for return transactions
    public TransactionCompleted(Long transactionId, String transactionType, Long personnelId, 
                               Long storeId, Double totalAmount, LocalDate transactionDate,
                               LocalDate completionDate, List<TransactionCreated.TransactionItemData> items,
                               Long originalTransactionId, String returnReason,
                               String correlationId, String causationId) {
        super("TransactionCompleted", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.completionDate = completionDate;
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
    public LocalDate getCompletionDate() { return completionDate; }
    public List<TransactionCreated.TransactionItemData> getItems() { return items; }
    public Long getOriginalTransactionId() { return originalTransactionId; }
    public String getReturnReason() { return returnReason; }
}