package com.log430.tp7.domain.payment.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment processing has failed.
 * This event indicates that the payment could not be processed successfully.
 */
public class PaymentFailed extends DomainEvent {
    
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
    
    @JsonProperty("failureReason")
    private final String failureReason;
    
    @JsonProperty("failedAt")
    private final LocalDateTime failedAt;
    
    public PaymentFailed(String paymentId, String transactionId, String customerId,
                        BigDecimal amount, String paymentMethod, String failureReason,
                        LocalDateTime failedAt, String correlationId, String causationId) {
        super("PaymentFailed", paymentId, "Payment", 1, correlationId, causationId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason;
        this.failedAt = failedAt;
    }
    
    public PaymentFailed(String paymentId, String transactionId, String customerId,
                        BigDecimal amount, String paymentMethod, String failureReason,
                        LocalDateTime failedAt, String correlationId) {
        this(paymentId, transactionId, customerId, amount, paymentMethod, 
             failureReason, failedAt, correlationId, null);
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getFailedAt() { return failedAt; }
}