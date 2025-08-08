package com.log430.tp7.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulates an external payment gateway service.
 * In a real implementation, this would integrate with actual payment providers
 * like Stripe, PayPal, or other payment processors.
 */
@Service
public class PaymentGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);
    
    /**
     * Simulates processing a payment through an external gateway.
     * 
     * @param amount Payment amount
     * @param paymentMethod Payment method (credit_card, debit_card, etc.)
     * @param customerId Customer identifier
     * @return Payment reference from the gateway
     * @throws PaymentGatewayException if payment processing fails
     */
    public String processPayment(BigDecimal amount, String paymentMethod, String customerId) {
        logger.info("Processing payment through gateway: amount={}, method={}, customer={}", 
                   amount, paymentMethod, customerId);
        
        // Simulate processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate payment failure for testing (10% failure rate)
        if (Math.random() < 0.1) {
            throw new PaymentGatewayException("Payment declined by gateway");
        }
        
        // Simulate specific failure scenarios for testing
        if ("FAIL_CARD".equals(paymentMethod)) {
            throw new PaymentGatewayException("Invalid payment method");
        }
        
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new PaymentGatewayException("Amount exceeds limit");
        }
        
        // Generate mock payment reference
        String paymentReference = "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        logger.info("Payment processed successfully: reference={}", paymentReference);
        return paymentReference;
    }
    
    /**
     * Simulates refunding a payment through an external gateway.
     * 
     * @param paymentReference Original payment reference
     * @param amount Refund amount
     * @throws PaymentGatewayException if refund processing fails
     */
    public void refundPayment(String paymentReference, BigDecimal amount) {
        logger.info("Processing refund through gateway: reference={}, amount={}", 
                   paymentReference, amount);
        
        // Simulate processing time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate refund failure for testing (5% failure rate)
        if (Math.random() < 0.05) {
            throw new PaymentGatewayException("Refund failed at gateway");
        }
        
        logger.info("Refund processed successfully: reference={}", paymentReference);
    }
}