package com.log430.tp7.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreatePaymentRequest {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String correlationId;
    
    // Default constructor
    public CreatePaymentRequest() {}
    
    public CreatePaymentRequest(String transactionId, String customerId, 
                              BigDecimal amount, String paymentMethod) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    public CreatePaymentRequest(String transactionId, String customerId, 
                              BigDecimal amount, String paymentMethod, String correlationId) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.correlationId = correlationId;
    }
    
    // Getters and setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}