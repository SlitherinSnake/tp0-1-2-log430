package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.dto.*;
import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp7.sagaorchestrator.model.SagaEvent;
import com.log430.tp7.sagaorchestrator.model.SagaEventType;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.model.SagaState;
import com.log430.tp7.sagaorchestrator.repository.SagaEventRepository;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Core saga orchestrator service that manages distributed transactions
 * across multiple microservices for customer sales operations.
 */
@Service
public class SagaOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final SagaExecutionRepository sagaExecutionRepository;
    private final SagaEventRepository sagaEventRepository;
    private final ServiceClientWrapper serviceClientWrapper;
    private final ConcurrentSagaManager concurrentSagaManager;
    private final InventoryConcurrencyManager inventoryConcurrencyManager;
    private final SagaMetrics sagaMetrics;
    private final SagaEventLogger sagaEventLogger;

    public SagaOrchestrator(
            SagaExecutionRepository sagaExecutionRepository,
            SagaEventRepository sagaEventRepository,
            ServiceClientWrapper serviceClientWrapper,
            ConcurrentSagaManager concurrentSagaManager,
            InventoryConcurrencyManager inventoryConcurrencyManager,
            SagaMetrics sagaMetrics,
            SagaEventLogger sagaEventLogger) {
        this.sagaExecutionRepository = sagaExecutionRepository;
        this.sagaEventRepository = sagaEventRepository;
        this.serviceClientWrapper = serviceClientWrapper;
        this.concurrentSagaManager = concurrentSagaManager;
        this.inventoryConcurrencyManager = inventoryConcurrencyManager;
        this.sagaMetrics = sagaMetrics;
        this.sagaEventLogger = sagaEventLogger;
    }

    /**
     * Main method to execute a sale saga transaction.
     * Coordinates stock verification, reservation, payment, and order confirmation.
     * 
     * @param request The sale request containing customer and product details
     * @return SagaResponse with saga ID and current state
     */
    @Transactional
    public SagaResponse executeSale(SaleRequest request) {
        // Generate correlation ID for tracing
        String sagaId = generateCorrelationId();

        // Start saga duration timer
        Timer.Sample sagaTimer = sagaMetrics.startSagaDurationTimer();
        long startTime = System.currentTimeMillis();

        // Increment saga started counter
        sagaMetrics.incrementSagaStarted();

        // Log saga initiation with structured logging
        sagaEventLogger.logSagaStarted(sagaId, request.customerId(), request.productId(), request.quantity());

        logger.info("Starting saga execution: sagaId={}, customerId={}, productId={}, quantity={}",
                sagaId, request.customerId(), request.productId(), request.quantity());

        try {
            // Create and persist initial saga state
            SagaExecution saga = createInitialSagaExecution(sagaId, request);
            sagaExecutionRepository.save(saga);

            // Log saga initiation event
            logSagaEvent(sagaId, SagaEventType.SAGA_STARTED,
                    "Saga initiated for customer: " + request.customerId());

            // Execute the saga steps
            executeStockVerification(saga);
            executeStockReservation(saga);
            executePaymentProcessing(saga);
            executeOrderConfirmation(saga);

            // If we reach here, saga completed successfully
            saga.transitionTo(SagaState.SALE_CONFIRMED);
            sagaExecutionRepository.save(saga);

            logSagaEvent(sagaId, SagaEventType.SAGA_COMPLETED,
                    "Saga completed successfully");

            // Record successful completion metrics
            sagaMetrics.incrementSagaCompleted();
            sagaMetrics.recordSagaDuration(sagaTimer);

            // Log saga completion with structured logging
            long durationMs = System.currentTimeMillis() - startTime;
            sagaEventLogger.logSagaCompleted(sagaId, request.customerId(), saga.getOrderId(), durationMs);

            logger.info("Saga execution completed successfully: sagaId={}", sagaId);

            return SagaResponse.success(sagaId, SagaState.SALE_CONFIRMED,
                    "Sale completed successfully");

        } catch (Exception e) {
            logger.error("Saga execution failed: sagaId={}, error={}", sagaId, e.getMessage(), e);

            // Record failure metrics
            sagaMetrics.incrementSagaFailed();
            sagaMetrics.recordSagaDuration(sagaTimer);
            sagaMetrics.recordError(e.getClass().getSimpleName(), "saga_execution");

            // Log saga failure with structured logging
            long durationMs = System.currentTimeMillis() - startTime;
            sagaEventLogger.logSagaFailed(sagaId, request.customerId(), e.getMessage(), "saga_execution", durationMs);

            // Execute compensation logic
            executeCompensation(sagaId, e.getMessage());

            return SagaResponse.failure(sagaId, SagaState.SALE_FAILED,
                    "Sale failed: " + e.getMessage());
        }
    }

    /**
     * Generates a unique correlation ID for saga tracing
     * 
     * @return UUID-based correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates the initial saga execution entity with SALE_INITIATED state
     * 
     * @param sagaId  The unique saga identifier
     * @param request The sale request details
     * @return SagaExecution entity ready for persistence
     */
    private SagaExecution createInitialSagaExecution(String sagaId, SaleRequest request) {
        SagaExecution saga = new SagaExecution();
        saga.setSagaId(sagaId);
        saga.setCurrentState(SagaState.SALE_INITIATED);
        saga.setCustomerId(request.customerId());
        saga.setProductId(request.productId());
        saga.setQuantity(request.quantity());
        saga.setAmount(request.amount());
        saga.setCreatedAt(LocalDateTime.now());
        saga.setUpdatedAt(LocalDateTime.now());

        return saga;
    }

    /**
     * Logs saga events for audit trail and observability
     * 
     * @param sagaId    The saga identifier
     * @param eventType The type of event
     * @param eventData Additional event details
     */
    private void logSagaEvent(String sagaId, SagaEventType eventType, String eventData) {
        SagaEvent event = new SagaEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setSagaId(sagaId);
        event.setEventType(eventType);
        event.setEventData(Map.of("message", eventData));
        event.setCreatedAt(LocalDateTime.now());

        sagaEventRepository.save(event);

        logger.info("Saga event logged: sagaId={}, eventType={}, data={}",
                sagaId, eventType, eventData);
    }

    // Placeholder methods for saga steps - to be implemented in subsequent subtasks

    /**
     * Executes stock verification step of the saga.
     * Transitions from SALE_INITIATED to STOCK_VERIFYING state.
     * 
     * @param saga The saga execution context
     * @throws RuntimeException if stock verification fails
     */
    private void executeStockVerification(SagaExecution saga) {
        logger.info("Executing stock verification: sagaId={}, productId={}, quantity={}",
                saga.getSagaId(), saga.getProductId(), saga.getQuantity());

        // Start stock verification timer
        Timer.Sample stockVerificationTimer = sagaMetrics.startStockVerificationTimer();

        try {
            // Transition to STOCK_VERIFYING state
            SagaState previousState = saga.getCurrentState();
            saga.transitionTo(SagaState.STOCK_VERIFYING);
            sagaExecutionRepository.save(saga);

            // Record state transition
            sagaMetrics.recordStateTransition(previousState, SagaState.STOCK_VERIFYING);
            sagaEventLogger.logStateTransition(saga.getSagaId(), previousState, SagaState.STOCK_VERIFYING,
                    "stock_verification");

            logSagaEvent(saga.getSagaId(), SagaEventType.STATE_TRANSITION,
                    "Transitioned to STOCK_VERIFYING");

            // Create stock verification request
            StockVerificationRequest request = new StockVerificationRequest(
                    saga.getProductId(),
                    saga.getQuantity(),
                    saga.getSagaId());

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_STARTED,
                    "Starting stock verification call to inventory service");

            // Log structured service call start
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("productId", request.productId());
            requestData.put("quantity", request.quantity());
            sagaEventLogger.logServiceCallStarted(saga.getSagaId(), "inventory-service", "verifyStock",
                    "stock_verification", requestData);

            // Call inventory service to verify stock with concurrency protection
            long serviceCallStart = System.currentTimeMillis();
            StockVerificationResponse response = inventoryConcurrencyManager.verifyStockWithConcurrencyProtection(
                    request.productId(), request.quantity(), request.sagaId());

            if (!response.success()) {
                logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_FAILED,
                        "Stock verification service call failed: " + response.message());

                // Log structured service call failure
                long serviceCallDuration = System.currentTimeMillis() - serviceCallStart;
                sagaEventLogger.logServiceCallFailed(saga.getSagaId(), "inventory-service", "verifyStock",
                        "stock_verification", response.message(), serviceCallDuration);

                // Record failure metrics
                sagaMetrics.incrementStockVerification(false);
                sagaMetrics.recordStockVerificationDuration(stockVerificationTimer);
                sagaMetrics.recordError("ServiceCallFailed", "stock_verification");

                throw new RuntimeException("Stock verification service call failed: " + response.message());
            }

            if (!response.available()) {
                logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_COMPLETED,
                        "Stock verification completed - insufficient stock available");

                // Log structured service call failure
                long serviceCallDuration = System.currentTimeMillis() - serviceCallStart;
                sagaEventLogger.logServiceCallFailed(saga.getSagaId(), "inventory-service", "verifyStock",
                        "stock_verification", "Insufficient stock available", serviceCallDuration);

                // Record failure metrics
                sagaMetrics.incrementStockVerification(false);
                sagaMetrics.recordStockVerificationDuration(stockVerificationTimer);
                sagaMetrics.recordError("InsufficientStock", "stock_verification");

                throw new RuntimeException("Insufficient stock available. Requested: " +
                        response.requestedQuantity() + ", Available: " +
                        response.availableQuantity());
            }

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_COMPLETED,
                    "Stock verification completed successfully - stock is available");

            // Log structured service call success
            long serviceCallDuration = System.currentTimeMillis() - serviceCallStart;
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("available", response.available());
            responseData.put("availableQuantity", response.availableQuantity());
            sagaEventLogger.logServiceCallCompleted(saga.getSagaId(), "inventory-service", "verifyStock",
                    "stock_verification", responseData, serviceCallDuration);

            // Record success metrics
            sagaMetrics.incrementStockVerification(true);
            sagaMetrics.recordStockVerificationDuration(stockVerificationTimer);

            logger.info("Stock verification completed successfully: sagaId={}, productId={}, available={}",
                    saga.getSagaId(), saga.getProductId(), response.available());

        } catch (Exception e) {
            // Update saga with error information
            saga.setErrorMessage("Stock verification failed: " + e.getMessage());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.ERROR,
                    "Stock verification failed: " + e.getMessage());

            // Record failure metrics if not already recorded
            if (stockVerificationTimer != null) {
                sagaMetrics.incrementStockVerification(false);
                sagaMetrics.recordStockVerificationDuration(stockVerificationTimer);
                sagaMetrics.recordError(e.getClass().getSimpleName(), "stock_verification");
            }

            logger.error("Stock verification failed: sagaId={}, error={}",
                    saga.getSagaId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Executes stock reservation step of the saga.
     * Transitions from STOCK_VERIFYING to STOCK_RESERVING state.
     * Stores reservation ID for later compensation if needed.
     * Implements concurrent handling to prevent race conditions.
     * 
     * @param saga The saga execution context
     * @throws RuntimeException if stock reservation fails
     */
    private void executeStockReservation(SagaExecution saga) {
        logger.info("Executing stock reservation: sagaId={}, productId={}, quantity={}",
                saga.getSagaId(), saga.getProductId(), saga.getQuantity());

        // Start stock reservation timer
        Timer.Sample stockReservationTimer = sagaMetrics.startStockReservationTimer();

        try {
            // Check for race conditions before proceeding with stock reservation
            if (!concurrentSagaManager.handleStockReservationRaceCondition(
                    saga.getCustomerId(), saga.getProductId(), saga.getSagaId())) {

                logSagaEvent(saga.getSagaId(), SagaEventType.ERROR,
                        "Stock reservation blocked due to race condition");

                sagaMetrics.recordError("StockReservationRaceCondition", "stock_reservation");
                throw new RuntimeException("Stock reservation blocked due to concurrent saga conflict");
            }

            // Use optimistic locking with retry for state transition
            SagaExecution updatedSaga = concurrentSagaManager.updateSagaStateWithRetry(
                    saga.getSagaId(), SagaState.STOCK_RESERVING,
                    s -> {
                        // Additional update actions can be performed here
                        logger.debug("Transitioning saga to STOCK_RESERVING: sagaId={}", s.getSagaId());
                    });

            // Update local reference
            saga.setCurrentState(updatedSaga.getCurrentState());
            saga.setVersion(updatedSaga.getVersion());

            // Record state transition
            sagaMetrics.recordStateTransition(SagaState.STOCK_VERIFYING, SagaState.STOCK_RESERVING);

            logSagaEvent(saga.getSagaId(), SagaEventType.STATE_TRANSITION,
                    "Transitioned to STOCK_RESERVING");

            // Create stock reservation request
            StockReservationRequest request = new StockReservationRequest(
                    saga.getProductId(),
                    saga.getQuantity(),
                    saga.getSagaId(),
                    saga.getCustomerId());

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_STARTED,
                    "Starting stock reservation call to inventory service");

            // Call inventory service to reserve stock with concurrency protection
            StockReservationResponse response = inventoryConcurrencyManager.reserveStockWithConcurrencyProtection(
                    request.productId(), request.quantity(), request.sagaId(), request.customerId());

            if (!response.success()) {
                logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_FAILED,
                        "Stock reservation service call failed: " + response.message());

                // Record failure metrics
                sagaMetrics.incrementStockReservation(false);
                sagaMetrics.recordStockReservationDuration(stockReservationTimer);
                sagaMetrics.recordError("ServiceCallFailed", "stock_reservation");

                throw new RuntimeException("Stock reservation service call failed: " + response.message());
            }

            // Store reservation ID for later compensation if needed
            saga.setStockReservationId(response.reservationId());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_COMPLETED,
                    "Stock reservation completed successfully - reservationId: " + response.reservationId());

            // Record success metrics
            sagaMetrics.incrementStockReservation(true);
            sagaMetrics.recordStockReservationDuration(stockReservationTimer);

            logger.info("Stock reservation completed successfully: sagaId={}, productId={}, reservationId={}",
                    saga.getSagaId(), saga.getProductId(), response.reservationId());

        } catch (Exception e) {
            // Update saga with error information
            saga.setErrorMessage("Stock reservation failed: " + e.getMessage());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.ERROR,
                    "Stock reservation failed: " + e.getMessage());

            // Record failure metrics if not already recorded
            if (stockReservationTimer != null) {
                sagaMetrics.incrementStockReservation(false);
                sagaMetrics.recordStockReservationDuration(stockReservationTimer);
                sagaMetrics.recordError(e.getClass().getSimpleName(), "stock_reservation");
            }

            logger.error("Stock reservation failed: sagaId={}, error={}",
                    saga.getSagaId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Executes payment processing step of the saga.
     * Transitions from STOCK_RESERVING to PAYMENT_PROCESSING state.
     * Stores payment transaction ID for audit and potential reversal.
     * 
     * @param saga The saga execution context
     * @throws RuntimeException if payment processing fails
     */
    private void executePaymentProcessing(SagaExecution saga) {
        logger.info("Executing payment processing: sagaId={}, customerId={}, amount={}",
                saga.getSagaId(), saga.getCustomerId(), saga.getAmount());

        // Start payment processing timer
        Timer.Sample paymentProcessingTimer = sagaMetrics.startPaymentProcessingTimer();

        try {
            // Transition to PAYMENT_PROCESSING state
            SagaState previousState = saga.getCurrentState();
            saga.transitionTo(SagaState.PAYMENT_PROCESSING);
            sagaExecutionRepository.save(saga);

            // Record state transition
            sagaMetrics.recordStateTransition(previousState, SagaState.PAYMENT_PROCESSING);

            logSagaEvent(saga.getSagaId(), SagaEventType.STATE_TRANSITION,
                    "Transitioned to PAYMENT_PROCESSING");

            // Get payment details from the original request (stored in saga context)
            // Note: In a real implementation, payment details would be securely stored or
            // retrieved
            PaymentRequest request = new PaymentRequest(
                    saga.getCustomerId(),
                    saga.getAmount(),
                    "CREDIT_CARD", // Default payment method - in real scenario this would come from request
                    null, // Card details would be securely handled
                    null,
                    null,
                    null,
                    null,
                    saga.getSagaId(),
                    saga.getProductId(),
                    saga.getQuantity(),
                    "Purchase for product: " + saga.getProductId());

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_STARTED,
                    "Starting payment processing call to transaction service");

            // Call transaction service to process payment with circuit breaker
            PaymentResponse response = serviceClientWrapper.processPaymentSync(
                    request.customerId(), request.amount(), request.paymentMethod(),
                    request.cardNumber(),
                    request.expiryMonth() != null ? request.expiryMonth().toString() : null,
                    request.expiryYear() != null ? request.expiryYear().toString() : null,
                    request.cvv(), request.billingAddress(), request.sagaId(),
                    request.productId(), request.quantity());

            if (!response.success()) {
                logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_FAILED,
                        "Payment processing service call failed: " + response.message());

                // Record failure metrics
                sagaMetrics.incrementPaymentProcessing(false);
                sagaMetrics.recordPaymentProcessingDuration(paymentProcessingTimer);
                sagaMetrics.recordError("ServiceCallFailed", "payment_processing");

                throw new RuntimeException("Payment processing failed: " + response.message());
            }

            // Store payment transaction ID for audit and potential reversal
            saga.setPaymentTransactionId(response.transactionId());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_COMPLETED,
                    "Payment processing completed successfully - transactionId: " + response.transactionId());

            // Record success metrics
            sagaMetrics.incrementPaymentProcessing(true);
            sagaMetrics.recordPaymentProcessingDuration(paymentProcessingTimer);

            logger.info("Payment processing completed successfully: sagaId={}, customerId={}, transactionId={}",
                    saga.getSagaId(), saga.getCustomerId(), response.transactionId());

        } catch (Exception e) {
            // Update saga with error information
            saga.setErrorMessage("Payment processing failed: " + e.getMessage());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.ERROR,
                    "Payment processing failed: " + e.getMessage());

            // Record failure metrics if not already recorded
            if (paymentProcessingTimer != null) {
                sagaMetrics.incrementPaymentProcessing(false);
                sagaMetrics.recordPaymentProcessingDuration(paymentProcessingTimer);
                sagaMetrics.recordError(e.getClass().getSimpleName(), "payment_processing");
            }

            logger.error("Payment processing failed: sagaId={}, error={}",
                    saga.getSagaId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Executes order confirmation step of the saga.
     * Transitions from PAYMENT_PROCESSING to ORDER_CONFIRMING state.
     * Handles successful completion with SALE_CONFIRMED state.
     * 
     * @param saga The saga execution context
     * @throws RuntimeException if order confirmation fails
     */
    private void executeOrderConfirmation(SagaExecution saga) {
        logger.info("Executing order confirmation: sagaId={}, customerId={}, productId={}",
                saga.getSagaId(), saga.getCustomerId(), saga.getProductId());

        // Start order confirmation timer
        Timer.Sample orderConfirmationTimer = sagaMetrics.startOrderConfirmationTimer();

        try {
            // Transition to ORDER_CONFIRMING state
            SagaState previousState = saga.getCurrentState();
            saga.transitionTo(SagaState.ORDER_CONFIRMING);
            sagaExecutionRepository.save(saga);

            // Record state transition
            sagaMetrics.recordStateTransition(previousState, SagaState.ORDER_CONFIRMING);

            logSagaEvent(saga.getSagaId(), SagaEventType.STATE_TRANSITION,
                    "Transitioned to ORDER_CONFIRMING");

            // Create order request with all saga context
            OrderRequest request = new OrderRequest(
                    saga.getCustomerId(),
                    saga.getProductId(),
                    saga.getQuantity(),
                    saga.getAmount(),
                    saga.getSagaId(),
                    saga.getStockReservationId(),
                    saga.getPaymentTransactionId(),
                    null, // Shipping address would come from customer profile or request
                    "Order created via saga orchestration");

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_STARTED,
                    "Starting order creation call to store service");

            // Call store service to create order with circuit breaker
            OrderResponse response = serviceClientWrapper.createOrderSync(
                    request.customerId(), request.productId(), request.quantity(),
                    request.amount(), request.sagaId(), request.stockReservationId(),
                    request.paymentTransactionId(), request.shippingAddress());

            if (!response.success()) {
                logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_FAILED,
                        "Order creation service call failed: " + response.message());

                // Record failure metrics
                sagaMetrics.incrementOrderConfirmation(false);
                sagaMetrics.recordOrderConfirmationDuration(orderConfirmationTimer);
                sagaMetrics.recordError("ServiceCallFailed", "order_confirmation");

                throw new RuntimeException("Order creation failed: " + response.message());
            }

            // Store order ID for reference
            saga.setOrderId(response.orderId());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.SERVICE_CALL_COMPLETED,
                    "Order confirmation completed successfully - orderId: " + response.orderId());

            // Record success metrics
            sagaMetrics.incrementOrderConfirmation(true);
            sagaMetrics.recordOrderConfirmationDuration(orderConfirmationTimer);

            logger.info("Order confirmation completed successfully: sagaId={}, customerId={}, orderId={}",
                    saga.getSagaId(), saga.getCustomerId(), response.orderId());

        } catch (Exception e) {
            // Update saga with error information
            saga.setErrorMessage("Order confirmation failed: " + e.getMessage());
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            logSagaEvent(saga.getSagaId(), SagaEventType.ERROR,
                    "Order confirmation failed: " + e.getMessage());

            // Record failure metrics if not already recorded
            if (orderConfirmationTimer != null) {
                sagaMetrics.incrementOrderConfirmation(false);
                sagaMetrics.recordOrderConfirmationDuration(orderConfirmationTimer);
                sagaMetrics.recordError(e.getClass().getSimpleName(), "order_confirmation");
            }

            logger.error("Order confirmation failed: sagaId={}, error={}",
                    saga.getSagaId(), e.getMessage());

            throw e;
        }
    }

    /**
     * Executes compensation logic for rollback scenarios.
     * Implements stock release compensation for payment failures.
     * Adds comprehensive error logging and state transitions to SALE_FAILED.
     * 
     * @param sagaId       The saga identifier
     * @param errorMessage The error that triggered compensation
     */
    private void executeCompensation(String sagaId, String errorMessage) {
        logger.warn("Starting compensation for sagaId={}, error={}", sagaId, errorMessage);

        // Increment compensation counter
        sagaMetrics.incrementCompensationExecuted();

        // Log structured compensation start
        sagaEventLogger.logCompensationStarted(sagaId, errorMessage, "compensation", errorMessage);

        try {
            // Retrieve saga execution to determine what needs to be compensated
            SagaExecution saga = sagaExecutionRepository.findById(sagaId)
                    .orElseThrow(() -> new RuntimeException("Saga not found for compensation: " + sagaId));

            logSagaEvent(sagaId, SagaEventType.COMPENSATION_STARTED,
                    "Starting compensation due to: " + errorMessage);

            // Determine compensation actions based on current state
            boolean compensationSuccessful = true;
            StringBuilder compensationLog = new StringBuilder("Compensation actions: ");

            // If we have a stock reservation, release it
            if (saga.getStockReservationId() != null && !saga.getStockReservationId().isEmpty()) {
                try {
                    logger.info("Releasing stock reservation: sagaId={}, reservationId={}",
                            sagaId, saga.getStockReservationId());

                    serviceClientWrapper.releaseStockReservationSync(saga.getStockReservationId(), sagaId);

                    compensationLog.append("Stock released (reservationId: ")
                            .append(saga.getStockReservationId())
                            .append("); ");

                    // Log structured compensation success
                    sagaEventLogger.logCompensationCompleted(sagaId, "stock_release",
                            "Released reservation: " + saga.getStockReservationId(),
                            true);

                    logSagaEvent(sagaId, SagaEventType.SERVICE_CALL_COMPLETED,
                            "Stock reservation released successfully");

                } catch (Exception e) {
                    compensationSuccessful = false;
                    compensationLog.append("Stock release failed: ").append(e.getMessage()).append("; ");

                    // Log structured compensation failure
                    sagaEventLogger.logCompensationCompleted(sagaId, "stock_release",
                            "Failed to release reservation: " + e.getMessage(),
                            false);

                    logSagaEvent(sagaId, SagaEventType.SERVICE_CALL_FAILED,
                            "Stock reservation release failed: " + e.getMessage());

                    logger.error("Failed to release stock reservation: sagaId={}, reservationId={}, error={}",
                            sagaId, saga.getStockReservationId(), e.getMessage());
                }
            }

            // Note: Payment reversal would be implemented here if business rules require it
            // For now, we log that payment was processed but order failed
            if (saga.getPaymentTransactionId() != null && !saga.getPaymentTransactionId().isEmpty()) {
                compensationLog.append("Payment reversal may be required (transactionId: ")
                        .append(saga.getPaymentTransactionId())
                        .append("); ");

                logSagaEvent(sagaId, SagaEventType.CUSTOM,
                        "Payment reversal may be required - manual intervention needed for transactionId: " +
                                saga.getPaymentTransactionId());

                logger.warn(
                        "Payment was processed but saga failed - manual review may be needed: sagaId={}, transactionId={}",
                        sagaId, saga.getPaymentTransactionId());
            }

            // Transition saga to SALE_FAILED state
            saga.transitionTo(SagaState.SALE_FAILED);
            saga.setErrorMessage(errorMessage);
            saga.setUpdatedAt(LocalDateTime.now());
            sagaExecutionRepository.save(saga);

            // Log compensation completion
            String compensationResult = compensationSuccessful ? "successful" : "partial";
            logSagaEvent(sagaId, SagaEventType.COMPENSATION_COMPLETED,
                    "Compensation " + compensationResult + ": " + compensationLog.toString());

            logger.warn("Compensation completed ({}): sagaId={}, actions={}",
                    compensationResult, sagaId, compensationLog.toString());

        } catch (Exception e) {
            logger.error("Compensation execution failed: sagaId={}, compensationError={}",
                    sagaId, e.getMessage(), e);

            logSagaEvent(sagaId, SagaEventType.ERROR,
                    "Compensation execution failed: " + e.getMessage());

            // Even if compensation fails, we still need to mark the saga as failed
            try {
                SagaExecution saga = sagaExecutionRepository.findById(sagaId).orElse(null);
                if (saga != null) {
                    saga.transitionTo(SagaState.SALE_FAILED);
                    saga.setErrorMessage("Original error: " + errorMessage + "; Compensation error: " + e.getMessage());
                    saga.setUpdatedAt(LocalDateTime.now());
                    sagaExecutionRepository.save(saga);
                }
            } catch (Exception saveException) {
                logger.error("Failed to save saga state after compensation failure: sagaId={}",
                        sagaId, saveException);
            }
        }
    }
}