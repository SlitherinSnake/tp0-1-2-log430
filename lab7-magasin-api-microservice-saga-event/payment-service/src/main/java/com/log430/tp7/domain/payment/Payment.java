package com.log430.tp7.domain.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    private String paymentId;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(nullable = false)
    private String paymentMethod;
    
    @Column
    private String paymentReference;
    
    @Column
    private String failureReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime processedAt;
    
    @Column
    private LocalDateTime refundedAt;
    
    @Column
    private String correlationId;
    
    @Version
    private Long version;
    
    // Default constructor for JPA
    protected Payment() {}
    
    public Payment(String transactionId, String customerId, BigDecimal amount, String paymentMethod) {
        this.paymentId = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.correlationId = UUID.randomUUID().toString();
    }
    
    public Payment(String transactionId, String customerId, BigDecimal amount, String paymentMethod, String correlationId) {
        this.paymentId = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
    
    public void processPayment(String paymentReference) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment can only be processed from PENDING status");
        }
        this.status = PaymentStatus.PROCESSED;
        this.paymentReference = paymentReference;
        this.processedAt = LocalDateTime.now();
    }
    
    public void failPayment(String failureReason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment can only be failed from PENDING status");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.processedAt = LocalDateTime.now();
    }
    
    public void refundPayment() {
        if (this.status != PaymentStatus.PROCESSED) {
            throw new IllegalStateException("Only processed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public String getCorrelationId() { return correlationId; }
    public Long getVersion() { return version; }
    
    public boolean isPending() { return status == PaymentStatus.PENDING; }
    public boolean isProcessed() { return status == PaymentStatus.PROCESSED; }
    public boolean isFailed() { return status == PaymentStatus.FAILED; }
    public boolean isRefunded() { return status == PaymentStatus.REFUNDED; }
}