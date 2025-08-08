package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event representing a successful payment processing.
 * This event is consumed by the transaction service to complete transactions.
 */
public class PaymentProcessed extends DomainEvent {
    
    @JsonProperty("paymentId")
    private final String paymentId;
    
    @JsonProperty("transactionId")
    private final Long transactionId;
    
    @JsonProperty("customerId")
    private final String customerId;
    
    @JsonProperty("amount")
    private final BigDecimal amount;
    
    @JsonProperty("currency")
    private final String currency;
    
    @JsonProperty("paymentMethod")
    private final String paymentMethod;
    
    @JsonProperty("processedAt")
    private final LocalDateTime processedAt;
    
    @JsonProperty("sagaId")
    private final String sagaId;
    
    public PaymentProcessed(String paymentId, Long transactionId, String customerId,
                           BigDecimal amount, String currency, String paymentMethod,
                           LocalDateTime processedAt, String sagaId,
                           String correlationId, String causationId) {
        super("PaymentProcessed", paymentId, "Payment", 1, correlationId, causationId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.processedAt = processedAt;
        this.sagaId = sagaId;
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public Long getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getSagaId() { return sagaId; }
}