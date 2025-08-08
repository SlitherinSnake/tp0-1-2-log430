package com.log430.tp7.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.log430.tp7.application.service.EventDrivenFulfillmentService;
import com.log430.tp7.event.DomainEvent;

/**
 * Event consumer for inventory-related events in the Store Service.
 * Handles InventoryReserved events to trigger order fulfillment.
 */
@Component
public class InventoryEventConsumer implements EventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryEventConsumer.class);
    private static final String INVENTORY_RESERVED_EVENT = "InventoryReserved";
    
    private final EventDrivenFulfillmentService fulfillmentService;
    
    public InventoryEventConsumer(EventDrivenFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }
    
    @Override
    @RabbitListener(queues = "store.inventory.queue")
    public void handleEvent(DomainEvent event) {
        logger.info("Store Service received event: {} with correlation ID: {}", 
                   event.getEventType(), event.getCorrelationId());
        
        try {
            if (canHandle(event.getEventType())) {
                processEvent(event);
            } else {
                logger.debug("Store Service ignoring event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing event {} in Store Service: {}", 
                        event.getEventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to process event in Store Service", e);
        }
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return INVENTORY_RESERVED_EVENT.equals(eventType);
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{INVENTORY_RESERVED_EVENT};
    }
    
    /**
     * Process the supported events.
     */
    private void processEvent(DomainEvent event) {
        if (INVENTORY_RESERVED_EVENT.equals(event.getEventType())) {
            handleInventoryReserved(event);
        }
    }
    
    /**
     * Handles InventoryReserved events to trigger order fulfillment.
     */
    private void handleInventoryReserved(DomainEvent event) {
        try {
            String correlationId = event.getCorrelationId();
            String transactionId = event.getAggregateId();
            
            logger.info("Processing InventoryReserved event for transaction: {}, correlationId: {}", 
                       transactionId, correlationId);
            
            // In this choreographed saga, we need to map the transaction to an order
            // For now, we'll use the transactionId as orderId
            String orderId = transactionId;
            
            logger.info("Inventory reserved for transaction {}, triggering fulfillment process", transactionId);
            
            // Trigger order fulfillment
            boolean fulfillmentSuccess = fulfillmentService.fulfillOrder(orderId, correlationId, null);
            
            if (fulfillmentSuccess) {
                logger.info("Order fulfillment triggered successfully for transaction: {}", transactionId);
            } else {
                logger.warn("Order fulfillment failed for transaction: {}", transactionId);
                // In a choreographed saga, this service might publish a compensation event
                // For now, we'll just log the failure
            }
            
        } catch (Exception e) {
            logger.error("Error handling InventoryReserved event for transaction: {}, error: {}", 
                        event.getAggregateId(), e.getMessage(), e);
            throw new RuntimeException("Failed to handle InventoryReserved event", e);
        }
    }
}
