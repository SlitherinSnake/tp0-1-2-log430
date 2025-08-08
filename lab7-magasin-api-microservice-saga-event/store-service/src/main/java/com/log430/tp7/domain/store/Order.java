package com.log430.tp7.domain.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Domain entity representing an order created through saga operations.
 * Tracks customer orders with their items and status.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    @Column(name = "stock_reservation_id", length = 36)
    private String stockReservationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Constructors
    public Order() {}

    public Order(String orderId, String sagaId, String customerId, Long storeId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.sagaId = sagaId;
        this.customerId = customerId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    /**
     * Add an item to the order.
     */
    public void addItem(String productId, Integer quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem(this, productId, quantity, unitPrice);
        items.add(item);
        updateTimestamp();
    }

    /**
     * Confirm the order.
     */
    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order is already confirmed");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled order");
        }
        this.status = OrderStatus.CONFIRMED;
        updateTimestamp();
    }

    /**
     * Cancel the order.
     */
    public void cancel() {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel a confirmed order");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        updateTimestamp();
    }

    /**
     * Check if the order is confirmed.
     */
    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }

    /**
     * Check if the order is cancelled.
     */
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    /**
     * Update the timestamp.
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public String getStockReservationId() { return stockReservationId; }
    public void setStockReservationId(String stockReservationId) { this.stockReservationId = stockReservationId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", sagaId='" + sagaId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", storeId=" + storeId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Enumeration for order status.
     */
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }
}