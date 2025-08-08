package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.time.LocalDate;

/**
 * Event published when a transaction is cancelled.
 * This is a compensation event in the choreographed saga.
 */
public class TransactionCancelled extends DomainEvent {
    
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
    
    @JsonProperty("cancellationReason")
    private final String cancellationReason;
    
    @JsonProperty("originalTransactionId")
    private final Long originalTransactionId; // For returns
    
    public TransactionCancelled(Long transactionId, String transactionType, Long personnelId, 
                               Long storeId, Double totalAmount, LocalDate transactionDate,
                               String cancellationReason, String correlationId, String causationId) {
        super("TransactionCancelled", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.cancellationReason = cancellationReason;
        this.originalTransactionId = null;
    }
    
    // Constructor for return transaction cancellations
    public TransactionCancelled(Long transactionId, String transactionType, Long personnelId, 
                               Long storeId, Double totalAmount, LocalDate transactionDate,
                               String cancellationReason, Long originalTransactionId,
                               String correlationId, String causationId) {
        super("TransactionCancelled", transactionId.toString(), "Transaction", 1, correlationId, causationId);
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.cancellationReason = cancellationReason;
        this.originalTransactionId = originalTransactionId;
    }
    
    // Getters
    public Long getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public Long getPersonnelId() { return personnelId; }
    public Long getStoreId() { return storeId; }
    public Double getTotalAmount() { return totalAmount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getCancellationReason() { return cancellationReason; }
    public Long getOriginalTransactionId() { return originalTransactionId; }
}