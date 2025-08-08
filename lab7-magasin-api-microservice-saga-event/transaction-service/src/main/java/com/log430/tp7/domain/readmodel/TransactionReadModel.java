package com.log430.tp7.domain.readmodel;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Read model for transaction queries optimized for read operations.
 * This is part of the CQRS implementation to separate read and write concerns.
 */
@Entity
@Table(name = "transaction_read_model")
public class TransactionReadModel {
    
    @Id
    private Long transactionId;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "personnel_id", nullable = false)
    private Long personnelId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "original_transaction_id")
    private Long originalTransactionId; // For returns
    
    @Column(name = "return_reason")
    private String returnReason; // For returns
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(name = "correlation_id")
    private String correlationId;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @OneToMany(mappedBy = "transactionReadModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionItemReadModel> items;
    
    // Constructors
    public TransactionReadModel() {}
    
    public TransactionReadModel(Long transactionId, String transactionType, LocalDate transactionDate,
                               Double totalAmount, Long personnelId, Long storeId, String status,
                               String correlationId) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.totalAmount = totalAmount;
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.status = status;
        this.correlationId = correlationId;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
    
    // Business methods
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDate.now();
    }
    
    public void updateTotalAmount(Double newAmount) {
        this.totalAmount = newAmount;
        this.updatedAt = LocalDate.now();
    }
    
    public void setReturnInfo(Long originalTransactionId, String returnReason) {
        this.originalTransactionId = originalTransactionId;
        this.returnReason = returnReason;
        this.updatedAt = LocalDate.now();
    }
    
    public void updateItemCount(Integer count) {
        this.itemCount = count;
        this.updatedAt = LocalDate.now();
    }
    
    public boolean isSale() {
        return "VENTE".equals(transactionType);
    }
    
    public boolean isReturn() {
        return "RETOUR".equals(transactionType);
    }
    
    public boolean isCompleted() {
        return "COMPLETEE".equals(status);
    }
    
    public boolean isCancelled() {
        return "ANNULEE".equals(status);
    }
    
    public boolean isInProgress() {
        return "EN_COURS".equals(status);
    }
    
    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Long getPersonnelId() { return personnelId; }
    public void setPersonnelId(Long personnelId) { this.personnelId = personnelId; }
    
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(Long originalTransactionId) { this.originalTransactionId = originalTransactionId; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    
    public List<TransactionItemReadModel> getItems() { return items; }
    public void setItems(List<TransactionItemReadModel> items) { this.items = items; }
    
    @Override
    public String toString() {
        return "TransactionReadModel{" +
                "transactionId=" + transactionId +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", totalAmount=" + totalAmount +
                ", personnelId=" + personnelId +
                ", storeId=" + storeId +
                ", status='" + status + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}