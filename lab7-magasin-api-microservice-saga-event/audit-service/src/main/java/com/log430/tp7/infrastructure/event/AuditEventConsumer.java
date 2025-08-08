package com.log430.tp7.infrastructure.event;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.log430.tp7.application.service.AuditService;
import com.log430.tp7.event.AbstractEventConsumer;
import com.log430.tp7.event.DomainEvent;

/**
 * Comprehensive event consumer for audit logging.
 * Consumes all business events and creates audit log entries for compliance tracking.
 */
@Component
public class AuditEventConsumer extends AbstractEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);
    
    private final AuditService auditService;
    
    @Autowired
    public AuditEventConsumer(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @Override
    protected Set<String> getSupportedEventTypes() {
        // This consumer handles ALL event types for comprehensive audit logging
        return Set.of(
            // Transaction events
            "TransactionCreated",
            "TransactionCancelled", 
            "TransactionCompleted",
            
            // Payment events
            "PaymentProcessed",
            "PaymentFailed",
            "PaymentRefunded",
            
            // Inventory events
            "InventoryReserved",
            "InventoryUnavailable",
            "InventoryReleased",
            
            // Store/Order events
            "OrderFulfilled",
            "OrderDelivered",
            "OrderCancelled",
            
            // Saga events (if any)
            "SagaStarted",
            "SagaCompleted",
            "SagaFailed",
            "SagaCompensated",
            
            // Generic wildcard for any other events
            "*"
        );
    }
    
    @Override
    public String getConsumerName() {
        return "AuditEventConsumer";
    }
    
    @Override
    public boolean canHandle(String eventType) {
        // This audit consumer can handle ANY event type for comprehensive logging
        return true;
    }
    
    @RabbitListener(queues = "audit.events.queue")
    public void handleBusinessEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @RabbitListener(queues = "audit.transaction.queue")
    public void handleTransactionEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @RabbitListener(queues = "audit.payment.queue")
    public void handlePaymentEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @RabbitListener(queues = "audit.inventory.queue")
    public void handleInventoryEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @RabbitListener(queues = "audit.store.queue")
    public void handleStoreEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @Override
    protected void processEvent(DomainEvent event) {
        try {
            log.debug("Processing audit event: {} with ID: {}", 
                     event.getEventType(), event.getEventId());
            
            // Determine the source service based on event type or other criteria
            String serviceName = determineServiceName(event);
            
            // Create audit log entry
            auditService.createAuditLog(event, serviceName);
            
            log.debug("Successfully processed audit event: {} for service: {}", 
                     event.getEventType(), serviceName);
            
        } catch (Exception e) {
            log.error("Failed to process audit event: {} with ID: {}", 
                     event.getEventType(), event.getEventId(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
    
    /**
     * Determines the source service name based on event characteristics.
     */
    private String determineServiceName(DomainEvent event) {
        String eventType = event.getEventType().toLowerCase();
        
        if (eventType.contains("transaction")) {
            return "transaction-service";
        } else if (eventType.contains("payment")) {
            return "payment-service";
        } else if (eventType.contains("inventory")) {
            return "inventory-service";
        } else if (eventType.contains("order") || eventType.contains("store") || 
                  eventType.contains("fulfilled") || eventType.contains("delivered")) {
            return "store-service";
        } else if (eventType.contains("saga")) {
            return "saga-orchestrator-service";
        } else {
            // Try to extract from metadata or use aggregate type
            String aggregateType = event.getAggregateType();
            if (aggregateType != null) {
                return aggregateType.toLowerCase() + "-service";
            }
            return "unknown-service";
        }
    }
}
