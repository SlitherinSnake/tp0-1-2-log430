package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.client.InventoryServiceClient;
import com.log430.tp7.sagaorchestrator.client.StoreServiceClient;
import com.log430.tp7.sagaorchestrator.client.TransactionServiceClient;
import com.log430.tp7.sagaorchestrator.dto.*;
import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service wrapper that implements circuit breaker patterns, retry logic, and
 * fallback methods
 * for all external service calls. Provides resilience mechanisms for service
 * unavailability
 * and transient failures.
 */
@Service
public class ServiceClientWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceClientWrapper.class);

    private final InventoryServiceClient inventoryServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final StoreServiceClient storeServiceClient;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;

    public ServiceClientWrapper(
            InventoryServiceClient inventoryServiceClient,
            TransactionServiceClient transactionServiceClient,
            StoreServiceClient storeServiceClient,
            SagaMetrics sagaMetrics,
            SagaEventLogger sagaEventLogger) {
        this.inventoryServiceClient = inventoryServiceClient;
        this.transactionServiceClient = transactionServiceClient;
        this.storeServiceClient = storeServiceClient;
        this.sagaMetrics = sagaMetrics;
        this.sagaEventLogger = sagaEventLogger;
    }

    // Inventory Service Calls with Circuit Breaker

    /**
     * Verifies stock availability with circuit breaker and retry patterns.
     * 
     * @param productId the product identifier
     * @param quantity  the requested quantity
     * @param sagaId    the saga correlation ID
     * @return stock verification response
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "verifyStockFallback")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public CompletableFuture<StockVerificationResponse> verifyStock(String productId, Integer quantity, String sagaId) {
        logger.debug("Calling inventory service to verify stock: productId={}, quantity={}, sagaId={}",
                productId, quantity, sagaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                StockVerificationResponse response = inventoryServiceClient.verifyStock(productId, quantity, sagaId);

                // Record successful call metrics
                sagaMetrics.incrementStockVerification(true);

                return response;
            } catch (Exception e) {
                logger.error("Error calling inventory service for stock verification: sagaId={}, error={}",
                        sagaId, e.getMessage());

                // Record failure metrics
                sagaMetrics.incrementStockVerification(false);
                sagaMetrics.recordError("ServiceCallException", "stock_verification");

                throw new RuntimeException("Inventory service call failed", e);
            }
        });
    }

    /**
     * Fallback method for stock verification when circuit breaker is open or
     * service fails.
     */
    public CompletableFuture<StockVerificationResponse> verifyStockFallback(String productId, Integer quantity,
            String sagaId, Exception ex) {
        logger.warn("Stock verification fallback triggered: productId={}, quantity={}, sagaId={}, error={}",
                productId, quantity, sagaId, ex.getMessage());

        // Log fallback event
        sagaEventLogger.logSagaError(sagaId, "CircuitBreakerFallback", "stock_verification",
                "Inventory service unavailable - fallback triggered", ex);

        // Record circuit breaker fallback metrics
        sagaMetrics.incrementStockVerification(false);
        sagaMetrics.recordError("CircuitBreakerFallback", "stock_verification");

        // Return failure response to trigger saga compensation
        return CompletableFuture.completedFuture(
                StockVerificationResponse.failure(productId,
                        "Inventory service unavailable - circuit breaker open"));
    }

    /**
     * Reserves stock with circuit breaker and retry patterns.
     * 
     * @param productId  the product identifier
     * @param quantity   the quantity to reserve
     * @param sagaId     the saga correlation ID
     * @param customerId the customer identifier
     * @return stock reservation response
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "reserveStockFallback")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public CompletableFuture<StockReservationResponse> reserveStock(String productId, Integer quantity, String sagaId,
            String customerId) {
        logger.debug("Calling inventory service to reserve stock: productId={}, quantity={}, sagaId={}",
                productId, quantity, sagaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                StockReservationResponse response = inventoryServiceClient.reserveStock(productId, quantity, sagaId,
                        customerId);

                // Record successful call metrics
                sagaMetrics.incrementStockReservation(true);

                return response;
            } catch (Exception e) {
                logger.error("Error calling inventory service for stock reservation: sagaId={}, error={}",
                        sagaId, e.getMessage());

                // Record failure metrics
                sagaMetrics.incrementStockReservation(false);
                sagaMetrics.recordError("ServiceCallException", "stock_reservation");

                throw new RuntimeException("Inventory service call failed", e);
            }
        });
    }

    /**
     * Fallback method for stock reservation when circuit breaker is open or service
     * fails.
     */
    public CompletableFuture<StockReservationResponse> reserveStockFallback(String productId, Integer quantity,
            String sagaId, String customerId, Exception ex) {
        logger.warn("Stock reservation fallback triggered: productId={}, quantity={}, sagaId={}, error={}",
                productId, quantity, sagaId, ex.getMessage());

        // Log fallback event
        sagaEventLogger.logSagaError(sagaId, "CircuitBreakerFallback", "stock_reservation",
                "Inventory service unavailable - fallback triggered", ex);

        // Record circuit breaker fallback metrics
        sagaMetrics.incrementStockReservation(false);
        sagaMetrics.recordError("CircuitBreakerFallback", "stock_reservation");

        // Return failure response to trigger saga compensation
        return CompletableFuture.completedFuture(
                StockReservationResponse.failure(productId, sagaId,
                        "Inventory service unavailable - circuit breaker open"));
    }

    /**
     * Releases stock reservation with circuit breaker and retry patterns.
     * 
     * @param reservationId the reservation identifier to release
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "releaseStockFallback")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public CompletableFuture<Void> releaseStock(String reservationId) {
        logger.debug("Calling inventory service to release stock: reservationId={}", reservationId);

        return CompletableFuture.runAsync(() -> {
            try {
                inventoryServiceClient.releaseStock(reservationId, "saga-compensation");

                // Record successful call metrics - stock release doesn't have specific counter,
                // use generic success tracking
                sagaMetrics.recordError("CircuitBreakerSuccess", "stock_release");

            } catch (Exception e) {
                logger.error("Error calling inventory service for stock release: reservationId={}, error={}",
                        reservationId, e.getMessage());

                // Record failure metrics
                sagaMetrics.recordError("ServiceCallException", "stock_release");

                throw new RuntimeException("Inventory service call failed", e);
            }
        });
    }

    /**
     * Fallback method for stock release when circuit breaker is open or service
     * fails.
     */
    public CompletableFuture<Void> releaseStockFallback(String reservationId, Exception ex) {
        logger.warn("Stock release fallback triggered: reservationId={}, error={}", reservationId, ex.getMessage());

        // Record circuit breaker fallback metrics
        sagaMetrics.recordError("CircuitBreakerFallback", "stock_release");

        // For stock release, we log the failure but don't throw exception
        // as this is typically called during compensation
        return CompletableFuture.completedFuture(null);
    }

    // Transaction Service Calls with Circuit Breaker

    /**
     * Processes payment with circuit breaker and retry patterns.
     * 
     * @param customerId     the customer identifier
     * @param amount         the payment amount
     * @param paymentMethod  the payment method
     * @param cardNumber     the card number (masked)
     * @param expiryMonth    the card expiry month
     * @param expiryYear     the card expiry year
     * @param cvv            the card CVV
     * @param billingAddress the billing address
     * @param sagaId         the saga correlation ID
     * @param productId      the product identifier
     * @param quantity       the product quantity
     * @return payment response
     */
    @CircuitBreaker(name = "transaction-service", fallbackMethod = "processPaymentFallback")
    @Retry(name = "transaction-service")
    @TimeLimiter(name = "transaction-service")
    public CompletableFuture<PaymentResponse> processPayment(String customerId, BigDecimal amount, String paymentMethod,
            String cardNumber, String expiryMonth, String expiryYear,
            String cvv, String billingAddress, String sagaId,
            String productId, Integer quantity) {
        logger.debug("Calling transaction service to process payment: customerId={}, amount={}, sagaId={}",
                customerId, amount, sagaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                PaymentResponse response = transactionServiceClient.processPayment(
                        customerId, amount, paymentMethod, cardNumber, Integer.parseInt(expiryMonth), Integer.parseInt(expiryYear),
                        cvv, billingAddress, sagaId, productId, quantity);

                // Record successful call metrics
                sagaMetrics.incrementPaymentProcessing(true);

                return response;
            } catch (Exception e) {
                logger.error("Error calling transaction service for payment processing: sagaId={}, error={}",
                        sagaId, e.getMessage());

                // Record failure metrics
                sagaMetrics.incrementPaymentProcessing(false);
                sagaMetrics.recordError("ServiceCallException", "payment_processing");

                throw new RuntimeException("Transaction service call failed", e);
            }
        });
    }

    /**
     * Fallback method for payment processing when circuit breaker is open or
     * service fails.
     */
    public CompletableFuture<PaymentResponse> processPaymentFallback(String customerId, BigDecimal amount,
            String paymentMethod,
            String cardNumber, String expiryMonth, String expiryYear,
            String cvv, String billingAddress, String sagaId,
            String productId, Integer quantity, Exception ex) {
        logger.warn("Payment processing fallback triggered: customerId={}, amount={}, sagaId={}, error={}",
                customerId, amount, sagaId, ex.getMessage());

        // Log fallback event
        sagaEventLogger.logSagaError(sagaId, "CircuitBreakerFallback", "payment_processing",
                "Transaction service unavailable - fallback triggered", ex);

        // Record circuit breaker fallback metrics
        sagaMetrics.incrementPaymentProcessing(false);
        sagaMetrics.recordError("CircuitBreakerFallback", "payment_processing");

        // Return failure response to trigger saga compensation
        return CompletableFuture.completedFuture(
                PaymentResponse.failure(customerId, amount, sagaId,
                        "Transaction service unavailable - circuit breaker open"));
    }

    // Store Service Calls with Circuit Breaker

    /**
     * Creates order with circuit breaker and retry patterns.
     * 
     * @param customerId           the customer identifier
     * @param productId            the product identifier
     * @param quantity             the product quantity
     * @param amount               the order amount
     * @param sagaId               the saga correlation ID
     * @param stockReservationId   the stock reservation ID
     * @param paymentTransactionId the payment transaction ID
     * @param shippingAddress      the shipping address
     * @return order response
     */
    @CircuitBreaker(name = "store-service", fallbackMethod = "createOrderFallback")
    @Retry(name = "store-service")
    @TimeLimiter(name = "store-service")
    public CompletableFuture<OrderResponse> createOrder(String customerId, String productId, Integer quantity,
            BigDecimal amount, String sagaId, String stockReservationId,
            String paymentTransactionId, String shippingAddress) {
        logger.debug("Calling store service to create order: customerId={}, productId={}, sagaId={}",
                customerId, productId, sagaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                OrderResponse response = storeServiceClient.createOrder(
                        customerId, productId, quantity, amount, sagaId,
                        stockReservationId, paymentTransactionId, shippingAddress);

                // Record successful call metrics
                sagaMetrics.incrementOrderConfirmation(true);

                return response;
            } catch (Exception e) {
                logger.error("Error calling store service for order creation: sagaId={}, error={}",
                        sagaId, e.getMessage());

                // Record failure metrics
                sagaMetrics.incrementOrderConfirmation(false);
                sagaMetrics.recordError("ServiceCallException", "order_confirmation");

                throw new RuntimeException("Store service call failed", e);
            }
        });
    }

    /**
     * Fallback method for order creation when circuit breaker is open or service
     * fails.
     */
    public CompletableFuture<OrderResponse> createOrderFallback(String customerId, String productId, Integer quantity,
            BigDecimal amount, String sagaId, String stockReservationId,
            String paymentTransactionId, String shippingAddress, Exception ex) {
        logger.warn("Order creation fallback triggered: customerId={}, productId={}, sagaId={}, error={}",
                customerId, productId, sagaId, ex.getMessage());

        // Log fallback event
        sagaEventLogger.logSagaError(sagaId, "CircuitBreakerFallback", "order_confirmation",
                "Store service unavailable - fallback triggered", ex);

        // Record circuit breaker fallback metrics
        sagaMetrics.incrementOrderConfirmation(false);
        sagaMetrics.recordError("CircuitBreakerFallback", "order_confirmation");

        // Return failure response to trigger saga compensation
        return CompletableFuture.completedFuture(
                OrderResponse.failure(customerId, productId, sagaId,
                        "Store service unavailable - circuit breaker open"));
    }

    // Synchronous wrapper methods for backward compatibility

    /**
     * Synchronous wrapper for stock verification with circuit breaker.
     */
    public StockVerificationResponse verifyStockSync(String productId, Integer quantity, String sagaId) {
        try {
            return verifyStock(productId, quantity, sagaId).get();
        } catch (Exception e) {
            logger.error("Error in synchronous stock verification: sagaId={}, error={}", sagaId, e.getMessage());
            throw new RuntimeException("Stock verification failed", e);
        }
    }

    /**
     * Synchronous wrapper for stock reservation with circuit breaker.
     */
    public StockReservationResponse reserveStockSync(String productId, Integer quantity, String sagaId,
            String customerId) {
        try {
            return reserveStock(productId, quantity, sagaId, customerId).get();
        } catch (Exception e) {
            logger.error("Error in synchronous stock reservation: sagaId={}, error={}", sagaId, e.getMessage());
            throw new RuntimeException("Stock reservation failed", e);
        }
    }

    /**
     * Synchronous wrapper for stock release with circuit breaker.
     */
    public void releaseStockSync(String reservationId) {
        try {
            releaseStock(reservationId).get();
        } catch (Exception e) {
            logger.error("Error in synchronous stock release: reservationId={}, error={}", reservationId,
                    e.getMessage());
            throw new RuntimeException("Stock release failed", e);
        }
    }

    /**
     * Synchronous wrapper for payment processing with circuit breaker.
     */
    public PaymentResponse processPaymentSync(String customerId, BigDecimal amount, String paymentMethod,
            String cardNumber, String expiryMonth, String expiryYear,
            String cvv, String billingAddress, String sagaId,
            String productId, Integer quantity) {
        try {
            return processPayment(customerId, amount, paymentMethod, cardNumber, expiryMonth, expiryYear,
                    cvv, billingAddress, sagaId, productId, quantity).get();
        } catch (Exception e) {
            logger.error("Error in synchronous payment processing: sagaId={}, error={}", sagaId, e.getMessage());
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    /**
     * Synchronous wrapper for order creation with circuit breaker.
     */
    public OrderResponse createOrderSync(String customerId, String productId, Integer quantity,
            BigDecimal amount, String sagaId, String stockReservationId,
            String paymentTransactionId, String shippingAddress) {
        try {
            return createOrder(customerId, productId, quantity, amount, sagaId,
                    stockReservationId, paymentTransactionId, shippingAddress).get();
        } catch (Exception e) {
            logger.error("Error in synchronous order creation: sagaId={}, error={}", sagaId, e.getMessage());
            throw new RuntimeException("Order creation failed", e);
        }
    }

    /**
     * Synchronous wrapper for stock reservation release with circuit breaker.
     */
    public void releaseStockReservationSync(String reservationId, String sagaId) {
        try {
            releaseStock(reservationId).get();
            logger.debug("Stock reservation released successfully: reservationId={}, sagaId={}", reservationId, sagaId);
        } catch (Exception e) {
            logger.error("Error in synchronous stock reservation release: reservationId={}, sagaId={}, error={}",
                    reservationId, sagaId, e.getMessage());
            throw new RuntimeException("Stock reservation release failed", e);
        }
    }
}