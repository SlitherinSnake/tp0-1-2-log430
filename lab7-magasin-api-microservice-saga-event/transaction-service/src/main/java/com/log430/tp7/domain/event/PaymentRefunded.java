package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event representing a payment refund.
 * This event is consumed by the transaction service for compensation logic.
 */
public class PaymentRefunded extends DomainEvent {
    
    @JsonProperty("refundId")
    private final String refundId;
    
    @JsonProperty("originalPaymentId")
    private final String originalPaymentId;
    
    @JsonProperty("transactionId")
    private final Long transactionId;
    
    @JsonProperty("customerId")
    private final String customerId;
    
    @JsonProperty("refundAmount")
    private final BigDecimal refundAmount;
    
    @JsonProperty("currency")
    private final String currency;
    
    @JsonProperty("refundReason")
    private final String refundReason;
    
    @JsonProperty("refundedAt")
    private final LocalDateTime refundedAt;
    
    @JsonProperty("sagaId")
    private final String sagaId;
    
    public PaymentRefunded(String refundId, String originalPaymentId, Long transactionId,
                          String customerId, BigDecimal refundAmount, String currency,
                          String refundReason, LocalDateTime refundedAt, String sagaId,
                          String correlationId, String causationId) {
        super("PaymentRefunded", refundId, "Payment", 1, correlationId, causationId);
        this.refundId = refundId;
        this.originalPaymentId = originalPaymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
        this.sagaId = sagaId;
    }
    
    // Getters
    public String getRefundId() { return refundId; }
    public String getOriginalPaymentId() { return originalPaymentId; }
    public Long getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getCurrency() { return currency; }
    public String getRefundReason() { return refundReason; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public String getSagaId() { return sagaId; }
}