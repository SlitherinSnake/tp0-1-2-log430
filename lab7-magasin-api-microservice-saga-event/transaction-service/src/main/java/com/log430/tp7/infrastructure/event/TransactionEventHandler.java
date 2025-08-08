package com.log430.tp7.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.TransactionProjectionService;
import com.log430.tp7.application.service.TransactionSagaService;
import com.log430.tp7.domain.event.PaymentFailed;
import com.log430.tp7.domain.event.PaymentProcessed;
import com.log430.tp7.domain.event.PaymentRefunded;
import com.log430.tp7.domain.event.TransactionCancelled;
import com.log430.tp7.domain.event.TransactionCompleted;
import com.log430.tp7.domain.event.TransactionCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Event handler for transaction-related events.
 * Consumes events from RabbitMQ and updates read model projections.
 */
@Component
public class TransactionEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionEventHandler.class);
    
    private final TransactionProjectionService projectionService;
    private final TransactionSagaService sagaService;
    private final ObjectMapper objectMapper;
    
    public TransactionEventHandler(TransactionProjectionService projectionService,
                                 TransactionSagaService sagaService,
                                 ObjectMapper objectMapper) {
        this.projectionService = projectionService;
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Handle TransactionCreated events for projection updates.
     */
    @RabbitListener(queues = "transaction.events.queue")
    public void handleTransactionCreated(String eventJson) {
        try {
            log.info("Received transaction event: {}", eventJson);
            
            // Parse the event to determine its type
            var eventNode = objectMapper.readTree(eventJson);
            String eventType = eventNode.get("eventType").asText();
            
            switch (eventType) {
                case "TransactionCreated":
                    TransactionCreated createdEvent = objectMapper.readValue(eventJson, TransactionCreated.class);
                    log.info("Processing TransactionCreated event for transaction: {}", createdEvent.getTransactionId());
                    projectionService.handleTransactionCreated(createdEvent);
                    break;
                    
                case "TransactionCompleted":
                    TransactionCompleted completedEvent = objectMapper.readValue(eventJson, TransactionCompleted.class);
                    log.info("Processing TransactionCompleted event for transaction: {}", completedEvent.getTransactionId());
                    projectionService.handleTransactionCompleted(completedEvent);
                    break;
                    
                case "TransactionCancelled":
                    TransactionCancelled cancelledEvent = objectMapper.readValue(eventJson, TransactionCancelled.class);
                    log.info("Processing TransactionCancelled event for transaction: {}", cancelledEvent.getTransactionId());
                    projectionService.handleTransactionCancelled(cancelledEvent);
                    break;
                    
                default:
                    log.warn("Unknown event type received: {}", eventType);
                    break;
            }
            
        } catch (Exception e) {
            log.error("Failed to process transaction event: {}", eventJson, e);
            // In a production system, you might want to send to a dead letter queue
            // or implement retry logic here
            throw new RuntimeException("Event processing failed", e);
        }
    }
    
    /**
     * Handle payment-related events that affect transactions.
     */
    @RabbitListener(queues = "transaction.events.queue")
    public void handlePaymentEvents(String eventJson) {
        try {
            log.info("Received payment event for transaction processing: {}", eventJson);
            
            var eventNode = objectMapper.readTree(eventJson);
            String eventType = eventNode.get("eventType").asText();
            
            switch (eventType) {
                case "PaymentProcessed":
                    handlePaymentProcessed(eventJson);
                    break;
                    
                case "PaymentFailed":
                    handlePaymentFailed(eventJson);
                    break;
                    
                case "PaymentRefunded":
                    handlePaymentRefunded(eventJson);
                    break;
                    
                default:
                    log.debug("Payment event type {} not handled by transaction service", eventType);
                    break;
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", eventJson, e);
            throw new RuntimeException("Payment event processing failed", e);
        }
    }
    
    /**
     * Handle PaymentProcessed events.
     */
    private void handlePaymentProcessed(String eventJson) {
        try {
            log.info("Processing PaymentProcessed event: {}", eventJson);
            PaymentProcessed event = objectMapper.readValue(eventJson, PaymentProcessed.class);
            sagaService.handlePaymentProcessed(event);
        } catch (Exception e) {
            log.error("Failed to process PaymentProcessed event: {}", eventJson, e);
            throw new RuntimeException("PaymentProcessed event processing failed", e);
        }
    }
    
    /**
     * Handle PaymentFailed events.
     */
    private void handlePaymentFailed(String eventJson) {
        try {
            log.info("Processing PaymentFailed event: {}", eventJson);
            PaymentFailed event = objectMapper.readValue(eventJson, PaymentFailed.class);
            sagaService.handlePaymentFailed(event);
        } catch (Exception e) {
            log.error("Failed to process PaymentFailed event: {}", eventJson, e);
            throw new RuntimeException("PaymentFailed event processing failed", e);
        }
    }
    
    /**
     * Handle PaymentRefunded events.
     */
    private void handlePaymentRefunded(String eventJson) {
        try {
            log.info("Processing PaymentRefunded event: {}", eventJson);
            PaymentRefunded event = objectMapper.readValue(eventJson, PaymentRefunded.class);
            sagaService.handlePaymentRefunded(event);
        } catch (Exception e) {
            log.error("Failed to process PaymentRefunded event: {}", eventJson, e);
            throw new RuntimeException("PaymentRefunded event processing failed", e);
        }
    }
}