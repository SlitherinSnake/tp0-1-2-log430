package com.log430.tp7.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.transaction.Transaction;
import com.log430.tp7.infrastructure.repository.TransactionRepository;
import com.log430.tp7.presentation.api.dto.PaymentRequest;
import com.log430.tp7.presentation.api.dto.PaymentResponse;

/**
 * Application service for saga-specific payment operations.
 * Handles payment processing for distributed transactions.
 */
@Service
@Transactional
public class SagaPaymentService {
    private static final Logger log = LoggerFactory.getLogger(SagaPaymentService.class);

    private final TransactionRepository transactionRepository;

    public SagaPaymentService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Process payment for a saga transaction.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for saga {} - customer: {}, amount: {}", 
            request.sagaId(), request.customerId(), request.amount());
        
        try {
            // Validate payment details
            if (!isValidPaymentMethod(request.paymentDetails().paymentMethod())) {
                log.warn("Invalid payment method for saga {}: {}", 
                    request.sagaId(), request.paymentDetails().paymentMethod());
                return PaymentResponse.failure(
                    request.sagaId(), request.customerId(), request.amount(),
                    "Invalid payment method", "INVALID_PAYMENT_METHOD"
                );
            }

            // Simulate payment processing logic
            boolean paymentSuccessful = simulatePaymentProcessing(request);
            
            if (paymentSuccessful) {
                // Create transaction record for successful payment
                String transactionId = createPaymentTransaction(request);
                
                log.info("Payment processed successfully for saga {} - transactionId: {}", 
                    request.sagaId(), transactionId);
                
                return PaymentResponse.success(
                    transactionId, request.sagaId(), request.customerId(),
                    request.amount(), request.paymentDetails().paymentMethod()
                );
            } else {
                log.warn("Payment processing failed for saga {}", request.sagaId());
                return PaymentResponse.failure(
                    request.sagaId(), request.customerId(), request.amount(),
                    "Payment processing failed", "PAYMENT_DECLINED"
                );
            }
            
        } catch (Exception e) {
            log.error("Error processing payment for saga {}: {}", request.sagaId(), e.getMessage(), e);
            return PaymentResponse.failure(
                request.sagaId(), request.customerId(), request.amount(),
                "Internal error during payment processing", "INTERNAL_ERROR"
            );
        }
    }

    /**
     * Validate payment method.
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethod != null && 
               (paymentMethod.equalsIgnoreCase("CREDIT_CARD") || 
                paymentMethod.equalsIgnoreCase("DEBIT_CARD") ||
                paymentMethod.equalsIgnoreCase("CASH"));
    }

    /**
     * Simulate payment processing (in real implementation, this would call external payment gateway).
     */
    private boolean simulatePaymentProcessing(PaymentRequest request) {
        // Simulate different scenarios based on amount or customer
        BigDecimal amount = request.amount();
        
        // Simulate failure for amounts over 10000
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return false;
        }
        
        // Simulate failure for specific test customer
        if ("test-fail-customer".equals(request.customerId())) {
            return false;
        }
        
        // Simulate card validation failure
        if (request.paymentDetails().paymentMethod().equalsIgnoreCase("CREDIT_CARD")) {
            String cardNumber = request.paymentDetails().cardNumber();
            if (cardNumber != null && cardNumber.startsWith("4000")) {
                return false; // Simulate declined card
            }
        }
        
        // Default to success
        return true;
    }

    /**
     * Create a transaction record for the payment.
     */
    private String createPaymentTransaction(PaymentRequest request) {
        try {
            // Convert customer ID to personnel ID (for demo purposes)
            Long personnelId = 1L; // Default personnel for saga transactions
            Long storeId = 1L; // Default store for saga transactions
            
            Transaction transaction = new Transaction(personnelId, storeId);
            transaction.setMontantTotal(request.amount().doubleValue());
            transaction.setDateTransaction(LocalDate.now());
            transaction.complete(); // Mark as completed immediately for saga
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            log.info("Created payment transaction record with ID: {} for saga: {}", 
                savedTransaction.getId(), request.sagaId());
            
            return savedTransaction.getId().toString();
            
        } catch (Exception e) {
            log.error("Error creating payment transaction record for saga {}: {}", 
                request.sagaId(), e.getMessage(), e);
            // Return a UUID as fallback transaction ID
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Reverse a payment (compensation action).
     */
    public boolean reversePayment(String transactionId, String sagaId) {
        log.info("Reversing payment for transaction {} in saga {}", transactionId, sagaId);
        
        try {
            // In a real implementation, this would call the payment gateway to reverse the payment
            // For now, we'll just log the reversal
            
            // Try to find and cancel the transaction if it exists
            try {
                Long txnId = Long.parseLong(transactionId);
                transactionRepository.findById(txnId).ifPresent(transaction -> {
                    if (!transaction.isCancelled()) {
                        transaction.cancel();
                        transactionRepository.save(transaction);
                        log.info("Cancelled transaction {} for saga {}", transactionId, sagaId);
                    }
                });
            } catch (NumberFormatException e) {
                log.warn("Transaction ID {} is not a valid number, skipping database update", transactionId);
            }
            
            log.info("Payment reversal completed for transaction {} in saga {}", transactionId, sagaId);
            return true;
            
        } catch (Exception e) {
            log.error("Error reversing payment for transaction {} in saga {}: {}", 
                transactionId, sagaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check payment status.
     */
    public boolean isPaymentSuccessful(String transactionId) {
        try {
            Long txnId = Long.parseLong(transactionId);
            return transactionRepository.findById(txnId)
                .map(Transaction::isCompleted)
                .orElse(false);
        } catch (NumberFormatException e) {
            log.warn("Invalid transaction ID format: {}", transactionId);
            return false;
        }
    }
}