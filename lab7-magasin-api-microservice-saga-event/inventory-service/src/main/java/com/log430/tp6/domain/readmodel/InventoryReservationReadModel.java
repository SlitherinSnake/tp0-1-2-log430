package com.log430.tp7.domain.inventory.readmodel;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read model for inventory reservation queries optimized for performance.
 * This model is updated by inventory reservation events.
 */
@Entity
@Table(name = "inventory_reservation_read_model")
public class InventoryReservationReadModel {
    
    @Id
    @Column(name = "reservation_id", length = 36)
    private String reservationId;
    
    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;
    
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Column(name = "reason", length = 200)
    private String reason;
    
    // Constructors
    public InventoryReservationReadModel() {}
    
    public InventoryReservationReadModel(String reservationId, Long inventoryItemId, String transactionId, 
                                       Integer quantity, String correlationId) {
        this.reservationId = reservationId;
        this.inventoryItemId = inventoryItemId;
        this.transactionId = transactionId;
        this.quantity = quantity;
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // 30 minute default expiration
        this.updatedAt = LocalDateTime.now();
        this.correlationId = correlationId;
    }
    
    // Business methods
    public void release(String reason) {
        this.status = ReservationStatus.RELEASED;
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }
    
    // Getters and Setters
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    
    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    @Override
    public String toString() {
        return String.format("InventoryReservationReadModel{reservationId='%s', inventoryItemId=%d, transactionId='%s', quantity=%d, status=%s}", 
                           reservationId, inventoryItemId, transactionId, quantity, status);
    }
    
    /**
     * Enumeration for reservation status.
     */
    public enum ReservationStatus {
        ACTIVE,
        RELEASED,
        CONFIRMED
    }
}