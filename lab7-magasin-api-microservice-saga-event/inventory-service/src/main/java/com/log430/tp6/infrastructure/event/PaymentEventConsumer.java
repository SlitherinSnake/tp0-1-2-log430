package com.log430.tp7.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.EventDrivenInventoryService;
import com.log430.tp7.application.service.InventorySagaService;
import com.log430.tp7.domain.payment.events.PaymentProcessed;

/**
 * Event consumer for payment-related events.
 * Handles PaymentProcessed events to trigger inventory reservation.
 */
@Component
public class PaymentEventConsumer implements EventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);
    
    private final EventDrivenInventoryService eventDrivenInventoryService;
    private final InventorySagaService inventorySagaService;
    private final ObjectMapper objectMapper;
    
    public PaymentEventConsumer(EventDrivenInventoryService eventDrivenInventoryService,
                               InventorySagaService inventorySagaService,
                               ObjectMapper objectMapper) {
        this.eventDrivenInventoryService = eventDrivenInventoryService;
        this.inventorySagaService = inventorySagaService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handleEvent(DomainEvent event) {
        if (event instanceof PaymentProcessed paymentProcessed) {
            handlePaymentProcessed(paymentProcessed);
        } else {
            logger.warn("Received unsupported event type: {}", event.getEventType());
        }
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return "PaymentProcessed".equals(eventType);
    }
    
    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{"PaymentProcessed"};
    }
    
    /**
     * Listen for PaymentProcessed events from RabbitMQ.
     */
    @RabbitListener(queues = "#{rabbitMQConfig.INVENTORY_QUEUE}")
    public void handlePaymentProcessedMessage(String message) {
        logger.info("Received payment processed message: {}", message);
        
        try {
            // Parse the message as a generic DomainEvent first to get event type
            DomainEvent genericEvent = objectMapper.readValue(message, DomainEvent.class);
            
            if ("PaymentProcessed".equals(genericEvent.getEventType())) {
                // Parse as PaymentProcessed event
                PaymentProcessed paymentProcessed = objectMapper.readValue(message, PaymentProcessed.class);
                handlePaymentProcessed(paymentProcessed);
            } else {
                logger.debug("Ignoring non-payment event: {}", genericEvent.getEventType());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process payment event message: {}", message, e);
            // In a production system, we might want to send this to a dead letter queue
            throw new RuntimeException("Payment event processing failed", e);
        }
    }
    
    /**
     * Handle PaymentProcessed event by triggering inventory reservation.
     * In a choreographed saga, this would typically look up the transaction details
     * and reserve inventory for each item in the transaction.
     */
    private void handlePaymentProcessed(PaymentProcessed event) {
        logger.info("Processing PaymentProcessed event: transactionId={}, paymentId={}, correlationId={}", 
                   event.getTransactionId(), event.getPaymentId(), event.getCorrelationId());
        
        try {
            // In a real implementation, we would:
            // 1. Look up the transaction details to get inventory items and quantities
            // 2. Reserve inventory for each item
            // 3. Publish InventoryReserved or InventoryUnavailable events
            
            // For now, we'll log the event and demonstrate the pattern
            // This would typically involve calling a service to get transaction details
            // and then calling eventDrivenInventoryService.reserveInventory for each item
            
            logger.info("Payment processed successfully for transaction: {}. Inventory reservation would be triggered here.", 
                       event.getTransactionId());
            
            // Example of how this might work:
            // TransactionDetails transaction = transactionService.getTransactionDetails(event.getTransactionId());
            // for (TransactionItem item : transaction.getItems()) {
            //     eventDrivenInventoryService.reserveInventory(
            //         item.getInventoryItemId(), 
            //         event.getTransactionId(), 
            //         item.getQuantity(), 
            //         event.getCorrelationId()
            //     );
            // }
            
        } catch (Exception e) {
            logger.error("Failed to process PaymentProcessed event for transaction: {}", 
                        event.getTransactionId(), e);
            throw new RuntimeException("PaymentProcessed event processing failed", e);
        }
    }
}