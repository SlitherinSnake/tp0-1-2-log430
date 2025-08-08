package com.log430.tp7.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.EventDrivenInventoryService;
import com.log430.tp7.application.service.InventorySagaService;
import com.log430.tp7.domain.transaction.events.TransactionCreated;

/**
 * Event consumer for transaction-related events.
 * Handles TransactionCreated events for inventory validation and pre-processing.
 */
@Component
public class TransactionEventConsumer implements EventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionEventConsumer.class);
    
    private final EventDrivenInventoryService eventDrivenInventoryService;
    private final InventorySagaService inventorySagaService;
    private final ObjectMapper objectMapper;
    
    public TransactionEventConsumer(EventDrivenInventoryService eventDrivenInventoryService,
                                   InventorySagaService inventorySagaService,
                                   ObjectMapper objectMapper) {
        this.eventDrivenInventoryService = eventDrivenInventoryService;
        this.inventorySagaService = inventorySagaService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handleEvent(DomainEvent event) {
        if (event instanceof TransactionCreated transactionCreated) {
            handleTransactionCreated(transactionCreated);
        } else {
            logger.warn("Received unsupported event type: {}", event.getEventType());
        }
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return "TransactionCreated".equals(eventType);
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"TransactionCreated"};
    }
    
    /**
     * Listen for TransactionCreated events from RabbitMQ.
     */
    @RabbitListener(queues = "#{rabbitMQConfig.INVENTORY_QUEUE}")
    public void handleTransactionCreatedMessage(String message) {
        logger.info("Received transaction created message: {}", message);
        
        try {
            // Parse the message as a generic DomainEvent first to get event type
            DomainEvent genericEvent = objectMapper.readValue(message, DomainEvent.class);
            
            if ("TransactionCreated".equals(genericEvent.getEventType())) {
                // Parse as TransactionCreated event
                TransactionCreated transactionCreated = objectMapper.readValue(message, TransactionCreated.class);
                handleTransactionCreated(transactionCreated);
            } else {
                logger.debug("Ignoring non-transaction event: {}", genericEvent.getEventType());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process transaction event message: {}", message, e);
            // In a production system, we might want to send this to a dead letter queue
            throw new RuntimeException("Transaction event processing failed", e);
        }
    }
    
    /**
     * Handle TransactionCreated event by validating inventory availability.
     * This provides early feedback on inventory availability before payment processing.
     */
    private void handleTransactionCreated(TransactionCreated event) {
        logger.info("Processing TransactionCreated event: transactionId={}, itemCount={}, correlationId={}", 
                   event.getTransactionId(), event.getItems().size(), event.getCorrelationId());
        
        try {
            // Use the InventorySagaService to validate inventory availability
            boolean inventoryAvailable = inventorySagaService.validateTransactionInventory(event);
            
            if (inventoryAvailable) {
                logger.info("All inventory items are available for transaction: {}", event.getTransactionId());
                // In a full implementation, we might publish an InventoryValidated event here
            } else {
                logger.warn("Some inventory items are not available for transaction: {}", event.getTransactionId());
                // In a full implementation, we might publish an InventoryInsufficient event here
            }
            
            logger.info("Transaction inventory validation completed for transaction: {}, result: {}", 
                       event.getTransactionId(), inventoryAvailable);
            
        } catch (Exception e) {
            logger.error("Failed to process TransactionCreated event for transaction: {}", 
                        event.getTransactionId(), e);
            throw new RuntimeException("TransactionCreated event processing failed", e);
        }
    }
}