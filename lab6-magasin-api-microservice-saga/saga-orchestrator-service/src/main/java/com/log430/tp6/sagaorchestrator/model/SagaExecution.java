package com.log430.tp6.sagaorchestrator.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entity representing a saga execution instance with state tracking and audit fields.
 * Manages the lifecycle of a distributed transaction across multiple microservices.
 */
@Entity
@Table(name = "saga_executions")
public class SagaExecution {
    
    @Id
    @Column(name = "saga_id", length = 36)
    private String sagaId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false, length = 50)
    private SagaState currentState;
    
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;
    
    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "stock_reservation_id", length = 36)
    private String stockReservationId;
    
    @Column(name = "payment_transaction_id", length = 36)
    private String paymentTransactionId;
    
    @Column(name = "order_id", length = 36)
    private String orderId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Valid state transitions mapping
    private static final Set<SagaState> INITIAL_STATES = EnumSet.of(SagaState.SALE_INITIATED);
    private static final Set<SagaState> FINAL_STATES = EnumSet.of(SagaState.SALE_CONFIRMED, SagaState.SALE_FAILED);
    
    // Default constructor for JPA
    public SagaExecution() {}
    
    // Constructor for creating new saga
    public SagaExecution(String sagaId, String customerId, String productId, Integer quantity, BigDecimal amount) {
        this.sagaId = sagaId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.currentState = SagaState.SALE_INITIATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Validates if the saga can transition to the specified new state.
     * 
     * @param newState the target state to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(SagaState newState) {
        if (newState == null) {
            return false;
        }
        
        // Cannot transition from final states
        if (FINAL_STATES.contains(this.currentState)) {
            return false;
        }
        
        // Cannot transition to the same state
        if (this.currentState == newState) {
            return false;
        }
        
        return this.currentState.canTransitionTo(newState);
    }
    
    /**
     * Transitions the saga to a new state if the transition is valid.
     * Updates the updatedAt timestamp automatically.
     * 
     * @param newState the target state to transition to
     * @throws IllegalStateException if the transition is not valid
     */
    public void transitionTo(SagaState newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s for saga %s", 
                    this.currentState, newState, this.sagaId));
        }
        
        this.currentState = newState;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the saga is in a final state (completed or failed).
     * 
     * @return true if the saga is in a final state
     */
    public boolean isInFinalState() {
        return FINAL_STATES.contains(this.currentState);
    }
    
    /**
     * Checks if the saga is in an active (non-final) state.
     * 
     * @return true if the saga is still active
     */
    public boolean isActive() {
        return !isInFinalState();
    }
    
    /**
     * Sets an error message and updates the timestamp.
     * 
     * @param errorMessage the error message to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    // JPA lifecycle callbacks for audit fields
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getSagaId() {
        return sagaId;
    }
    
    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }
    
    public SagaState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(SagaState currentState) {
        this.currentState = currentState;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getStockReservationId() {
        return stockReservationId;
    }
    
    public void setStockReservationId(String stockReservationId) {
        this.stockReservationId = stockReservationId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "SagaExecution{" +
                "sagaId='" + sagaId + '\'' +
                ", currentState=" + currentState +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}