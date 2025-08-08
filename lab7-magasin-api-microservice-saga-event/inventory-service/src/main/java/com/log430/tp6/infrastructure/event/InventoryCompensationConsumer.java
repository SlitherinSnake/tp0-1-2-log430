package com.log430.tp7.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.EventDrivenInventoryService;
import com.log430.tp7.application.service.InventorySagaService;

/**
 * Event consumer for inventory compensation events.
 * Handles events that require inventory reservation rollback or release.
 */
@Component
public class InventoryCompensationConsumer implements EventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryCompensationConsumer.class);
    
    private final EventDrivenInventoryService eventDrivenInventoryService;
    private final InventorySagaService inventorySagaService;
    private final ObjectMapper objectMapper;
    
    public InventoryCompensationConsumer(EventDrivenInventoryService eventDrivenInventoryService,
                                        InventorySagaService inventorySagaService,
                                        ObjectMapper objectMapper) {
        this.eventDrivenInventoryService = eventDrivenInventoryService;
        this.inventorySagaService = inventorySagaService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handleEvent(DomainEvent event) {
        switch (event.getEventType()) {
            case "PaymentFailed":
                handlePaymentFailed(event);
                break;
            case "PaymentRefunded":
                handlePaymentRefunded(event);
                break;
            case "TransactionCancelled":
                handleTransactionCancelled(event);
                break;
            default:
                logger.warn("Received unsupported compensation event type: {}", event.getEventType());
        }
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return "PaymentFailed".equals(eventType) || 
               "PaymentRefunded".equals(eventType) || 
               "TransactionCancelled".equals(eventType);
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"PaymentFailed", "PaymentRefunded", "TransactionCancelled"};
    }
    
    /**
     * Listen for compensation events from RabbitMQ.
     */
    @RabbitListener(queues = "#{rabbitMQConfig.INVENTORY_QUEUE}")
    public void handleCompensationMessage(String message) {
        logger.info("Received compensation message: {}", message);
        
        try {
            // Parse the message as a generic DomainEvent first to get event type
            DomainEvent genericEvent = objectMapper.readValue(message, DomainEvent.class);
            
            if (canHandle(genericEvent.getEventType())) {
                handleEvent(genericEvent);
            } else {
                logger.debug("Ignoring non-compensation event: {}", genericEvent.getEventType());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process compensation event message: {}", message, e);
            // In a production system, we might want to send this to a dead letter queue
            throw new RuntimeException("Compensation event processing failed", e);
        }
    }
    
    /**
     * Handle PaymentFailed event by releasing any inventory reservations.
     */
    private void handlePaymentFailed(DomainEvent event) {
        logger.info("Processing PaymentFailed event: aggregateId={}, correlationId={}", 
                   event.getAggregateId(), event.getCorrelationId());
        
        try {
            // In a real implementation, we would:
            // 1. Look up any inventory reservations for this transaction
            // 2. Release all reservations
            // 3. Publish InventoryReleased events
            
            String transactionId = extractTransactionId(event);
            if (transactionId != null) {
                logger.info("Releasing inventory reservations for failed payment, transaction: {}", transactionId);
                
                // Use the InventorySagaService to release all reservations for this transaction
                inventorySagaService.releaseTransactionReservations(
                    transactionId, 
                    "Payment failed", 
                    event.getCorrelationId()
                );
                
                logger.info("Inventory compensation completed for failed payment, transaction: {}", transactionId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to process PaymentFailed compensation for event: {}", event.getEventId(), e);
            throw new RuntimeException("PaymentFailed compensation processing failed", e);
        }
    }
    
    /**
     * Handle PaymentRefunded event by releasing inventory reservations.
     */
    private void handlePaymentRefunded(DomainEvent event) {
        logger.info("Processing PaymentRefunded event: aggregateId={}, correlationId={}", 
                   event.getAggregateId(), event.getCorrelationId());
        
        try {
            String transactionId = extractTransactionId(event);
            if (transactionId != null) {
                logger.info("Releasing inventory reservations for refunded payment, transaction: {}", transactionId);
                
                // Use the InventorySagaService to release all reservations for this transaction
                inventorySagaService.releaseTransactionReservations(
                    transactionId, 
                    "Payment refunded", 
                    event.getCorrelationId()
                );
                
                logger.info("Inventory compensation completed for refunded payment, transaction: {}", transactionId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to process PaymentRefunded compensation for event: {}", event.getEventId(), e);
            throw new RuntimeException("PaymentRefunded compensation processing failed", e);
        }
    }
    
    /**
     * Handle TransactionCancelled event by releasing inventory reservations.
     */
    private void handleTransactionCancelled(DomainEvent event) {
        logger.info("Processing TransactionCancelled event: aggregateId={}, correlationId={}", 
                   event.getAggregateId(), event.getCorrelationId());
        
        try {
            String transactionId = event.getAggregateId();
            logger.info("Releasing inventory reservations for cancelled transaction: {}", transactionId);
            
            // Use the InventorySagaService to release all reservations for this transaction
            inventorySagaService.releaseTransactionReservations(
                transactionId, 
                "Transaction cancelled", 
                event.getCorrelationId()
            );
            
            logger.info("Inventory compensation completed for cancelled transaction: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Failed to process TransactionCancelled compensation for event: {}", event.getEventId(), e);
            throw new RuntimeException("TransactionCancelled compensation processing failed", e);
        }
    }
    
    /**
     * Extract transaction ID from the event.
     * This might be in the aggregateId or in the event payload depending on the event structure.
     */
    private String extractTransactionId(DomainEvent event) {
        // This is a simplified implementation
        // In a real system, you might need to parse the event payload to get the transaction ID
        return event.getAggregateId();
    }
}