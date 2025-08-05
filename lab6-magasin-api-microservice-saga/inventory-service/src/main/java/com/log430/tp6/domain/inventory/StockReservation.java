package com.log430.tp6.domain.inventory;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Domain entity representing a stock reservation for saga operations.
 * Tracks temporary stock allocations during distributed transactions.
 */
@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @Column(name = "reservation_id", length = 36)
    private String reservationId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public StockReservation() {}

    public StockReservation(String reservationId, String productId, Integer quantity, String sagaId) {
        this.reservationId = reservationId;
        this.productId = productId;
        this.quantity = quantity;
        this.sagaId = sagaId;
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // 30 minute default expiration
    }

    // Business methods
    /**
     * Check if the reservation has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Release the reservation.
     */
    public void release() {
        this.status = ReservationStatus.RELEASED;
    }

    /**
     * Confirm the reservation (convert to actual stock reduction).
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * Check if the reservation is active.
     */
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }

    // Getters and Setters
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public String toString() {
        return "StockReservation{" +
                "reservationId='" + reservationId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", sagaId='" + sagaId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
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