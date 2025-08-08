package com.log430.tp7.presentation.dto;

import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private String paymentId;
    private String transactionId;
    private String customerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String paymentReference;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;
    private String correlationId;
    
    // Default constructor
    public PaymentResponse() {}
    
    public static PaymentResponse fromPayment(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.paymentId = payment.getPaymentId();
        response.transactionId = payment.getTransactionId();
        response.customerId = payment.getCustomerId();
        response.amount = payment.getAmount();
        response.status = payment.getStatus();
        response.paymentMethod = payment.getPaymentMethod();
        response.paymentReference = payment.getPaymentReference();
        response.failureReason = payment.getFailureReason();
        response.createdAt = payment.getCreatedAt();
        response.processedAt = payment.getProcessedAt();
        response.refundedAt = payment.getRefundedAt();
        response.correlationId = payment.getCorrelationId();
        return response;
    }
    
    // Getters and setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}