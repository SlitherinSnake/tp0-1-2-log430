package com.log430.tp7.domain.payment.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment has been refunded.
 * This event indicates that a previously processed payment has been reversed.
 */
public class PaymentRefunded extends DomainEvent {
    
    @JsonProperty("paymentId")
    private final String paymentId;
    
    @JsonProperty("transactionId")
    private final String transactionId;
    
    @JsonProperty("customerId")
    private final String customerId;
    
    @JsonProperty("amount")
    private final BigDecimal amount;
    
    @JsonProperty("paymentMethod")
    private final String paymentMethod;
    
    @JsonProperty("originalPaymentReference")
    private final String originalPaymentReference;
    
    @JsonProperty("refundReason")
    private final String refundReason;
    
    @JsonProperty("refundedAt")
    private final LocalDateTime refundedAt;
    
    public PaymentRefunded(String paymentId, String transactionId, String customerId,
                          BigDecimal amount, String paymentMethod, String originalPaymentReference,
                          String refundReason, LocalDateTime refundedAt, String correlationId, String causationId) {
        super("PaymentRefunded", paymentId, "Payment", 1, correlationId, causationId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.originalPaymentReference = originalPaymentReference;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
    }
    
    public PaymentRefunded(String paymentId, String transactionId, String customerId,
                          BigDecimal amount, String paymentMethod, String originalPaymentReference,
                          String refundReason, LocalDateTime refundedAt, String correlationId) {
        this(paymentId, transactionId, customerId, amount, paymentMethod, 
             originalPaymentReference, refundReason, refundedAt, correlationId, null);
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOriginalPaymentReference() { return originalPaymentReference; }
    public String getRefundReason() { return refundReason; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
}