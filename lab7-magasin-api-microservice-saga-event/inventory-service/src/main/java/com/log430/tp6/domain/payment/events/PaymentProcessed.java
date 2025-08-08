package com.log430.tp7.domain.payment.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.infrastructure.event.DomainEvent;

/**
 * Event consumed when a payment has been successfully processed.
 * This event triggers inventory reservation in the choreographed saga.
 */
public class PaymentProcessed extends DomainEvent {
    
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
    
    @JsonProperty("paymentReference")
    private final String paymentReference;
    
    @JsonProperty("processedAt")
    private final LocalDateTime processedAt;
    
    // For deserialization
    public PaymentProcessed() {
        super("PaymentProcessed", "", "Payment", 1, "");
        this.paymentId = "";
        this.transactionId = "";
        this.customerId = "";
        this.amount = BigDecimal.ZERO;
        this.paymentMethod = "";
        this.paymentReference = "";
        this.processedAt = LocalDateTime.now();
    }
    
    public PaymentProcessed(String paymentId, String transactionId, String customerId,
                           BigDecimal amount, String paymentMethod, String paymentReference,
                           LocalDateTime processedAt, String correlationId, String causationId) {
        super("PaymentProcessed", paymentId, "Payment", 1, correlationId, causationId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.processedAt = processedAt;
    }
    
    public PaymentProcessed(String paymentId, String transactionId, String customerId,
                           BigDecimal amount, String paymentMethod, String paymentReference,
                           LocalDateTime processedAt, String correlationId) {
        this(paymentId, transactionId, customerId, amount, paymentMethod, 
             paymentReference, processedAt, correlationId, null);
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    
    @Override
    public String toString() {
        return String.format("PaymentProcessed{paymentId='%s', transactionId='%s', customerId='%s', amount=%s, correlationId='%s'}", 
                           paymentId, transactionId, customerId, amount, getCorrelationId());
    }
}