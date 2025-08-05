package com.log430.tp6.sagaorchestrator.client;

import com.log430.tp6.sagaorchestrator.dto.PaymentRequest;
import com.log430.tp6.sagaorchestrator.dto.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Client for communicating with the transaction service
 */
@Component
public class TransactionServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String transactionServiceUrl;
    
    public TransactionServiceClient(RestTemplate restTemplate, 
                                  @Value("${services.transaction.url:http://transaction-service:8082}") String transactionServiceUrl) {
        this.restTemplate = restTemplate;
        this.transactionServiceUrl = transactionServiceUrl;
    }
    
    /**
     * Processes payment with retry logic and circuit breaker
     */
    @Retry(name = "transaction-service", fallbackMethod = "processPaymentFallback")
    @CircuitBreaker(name = "transaction-service", fallbackMethod = "processPaymentFallback")
    public PaymentResponse processPayment(String customerId, BigDecimal amount, String paymentMethod,
                                        String cardNumber, Integer expiryMonth, Integer expiryYear,
                                        String cvv, String billingAddress, String sagaId,
                                        String productId, Integer quantity) {
        try {
            PaymentRequest request = new PaymentRequest(
                customerId, amount, paymentMethod, cardNumber, expiryMonth, expiryYear,
                cvv, billingAddress, sagaId, productId, quantity,
                String.format("Payment for %d x %s", quantity, productId)
            );
            
            logger.info("Processing payment for saga {} - customer: {}, amount: {}", 
                       sagaId, customerId, amount);
            
            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                transactionServiceUrl + "/api/v1/transactions/process-payment",
                request,
                PaymentResponse.class
            );
            
            PaymentResponse result = response.getBody();
            if (result != null && result.success()) {
                logger.info("Payment processed successfully for saga {}: transactionId={}", 
                           sagaId, result.transactionId());
            } else {
                logger.warn("Payment processing failed for saga {}: {}", 
                           sagaId, result != null ? result.message() : "No response body");
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during payment processing for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return PaymentResponse.failure(customerId, amount, sagaId, 
                "Payment failed: " + e.getResponseBodyAsString());
                
        } catch (HttpServerErrorException e) {
            logger.error("Server error during payment processing for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            throw e; // Let retry/circuit breaker handle server errors
                
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during payment processing for saga {}: {}", 
                        sagaId, e.getMessage());
            throw e; // Let retry/circuit breaker handle connection errors
                
        } catch (Exception e) {
            logger.error("Unexpected error during payment processing for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return PaymentResponse.failure(customerId, amount, sagaId, 
                "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Fallback method for payment processing failures
     */
    public PaymentResponse processPaymentFallback(String customerId, BigDecimal amount, String paymentMethod,
                                                String cardNumber, Integer expiryMonth, Integer expiryYear,
                                                String cvv, String billingAddress, String sagaId,
                                                String productId, Integer quantity, Exception ex) {
        logger.error("Payment processing fallback triggered for saga {}: {}", sagaId, ex.getMessage());
        
        String errorMessage;
        if (ex instanceof ResourceAccessException) {
            errorMessage = "Transaction service is currently unavailable. Please try again later.";
        } else if (ex instanceof HttpServerErrorException) {
            errorMessage = "Transaction service is experiencing technical difficulties.";
        } else {
            errorMessage = "Payment processing failed due to system error.";
        }
        
        return PaymentResponse.failure(customerId, amount, sagaId, errorMessage);
    }
    
    /**
     * Reverses a payment transaction (for compensation scenarios)
     */
    @Retry(name = "transaction-service", fallbackMethod = "reversePaymentFallback")
    @CircuitBreaker(name = "transaction-service", fallbackMethod = "reversePaymentFallback")
    public boolean reversePayment(String transactionId, String sagaId) {
        try {
            logger.info("Reversing payment transaction {} for saga {}", transactionId, sagaId);
            
            restTemplate.postForEntity(
                transactionServiceUrl + "/api/v1/transactions/" + transactionId + "/reverse",
                null,
                Void.class
            );
            
            logger.info("Payment transaction {} reversed successfully for saga {}", transactionId, sagaId);
            return true;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during payment reversal for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
            
        } catch (HttpServerErrorException e) {
            logger.error("Server error during payment reversal for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            throw e; // Let retry/circuit breaker handle server errors
                
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during payment reversal for saga {}: {}", 
                        sagaId, e.getMessage());
            throw e; // Let retry/circuit breaker handle connection errors
                
        } catch (Exception e) {
            logger.error("Unexpected error during payment reversal for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Fallback method for payment reversal failures
     */
    public boolean reversePaymentFallback(String transactionId, String sagaId, Exception ex) {
        logger.error("Payment reversal fallback triggered for saga {}: {}", sagaId, ex.getMessage());
        // In a real system, this might trigger manual intervention or queue for later retry
        return false;
    }
}