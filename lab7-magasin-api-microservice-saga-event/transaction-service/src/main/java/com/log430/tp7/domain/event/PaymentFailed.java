package com.log430.tp7.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.log430.tp7.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event representing a failed payment processing.
 * This event is consumed by the transaction service to cancel transactions.
 */
public class PaymentFailed extends DomainEvent {
    
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
    
    @JsonProperty("failureReason")
    private final String failureReason;
    
    @JsonProperty("errorCode")
    private final String errorCode;
    
    @JsonProperty("failedAt")
    private final LocalDateTime failedAt;
    
    @JsonProperty("sagaId")
    private final String sagaId;
    
    public PaymentFailed(String paymentId, Long transactionId, String customerId,
                        BigDecimal amount, String currency, String paymentMethod,
                        String failureReason, String errorCode, LocalDateTime failedAt,
                        String sagaId, String correlationId, String causationId) {
        super("PaymentFailed", paymentId, "Payment", 1, correlationId, causationId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
        this.failedAt = failedAt;
        this.sagaId = sagaId;
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public Long getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getFailureReason() { return failureReason; }
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public String getSagaId() { return sagaId; }
}