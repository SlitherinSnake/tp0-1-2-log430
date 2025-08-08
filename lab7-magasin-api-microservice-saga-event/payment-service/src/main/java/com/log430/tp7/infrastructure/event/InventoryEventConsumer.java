package com.log430.tp7.infrastructure.event;

import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.event.AbstractEventConsumer;
import com.log430.tp7.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Event consumer for inventory-related events.
 * Handles InventoryUnavailable events to trigger payment refunds (compensation logic).
 */
@Component
public class InventoryEventConsumer extends AbstractEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);
    
    private final PaymentService paymentService;
    
    @Autowired
    public InventoryEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    protected Set<String> getSupportedEventTypes() {
        return Set.of("InventoryUnavailable");
    }
    
    @Override
    public String getConsumerName() {
        return "InventoryEventConsumer";
    }
    
    @RabbitListener(queues = "payment.inventory.queue")
    public void handleInventoryEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @Override
    protected void processEvent(DomainEvent event) {
        validateEvent(event);
        
        if ("InventoryUnavailable".equals(event.getEventType())) {
            handleInventoryUnavailable(event);
        }
    }
    
    private void handleInventoryUnavailable(DomainEvent event) {
        try {
            String transactionId = event.getAggregateId();
            String correlationId = event.getCorrelationId();
            
            log.info("Processing InventoryUnavailable event for transaction: {}, correlationId: {}", 
                    transactionId, correlationId);
            
            // Find the payment associated with this transaction
            Optional<Payment> paymentOpt = paymentService.findByTransactionId(transactionId);
            
            if (paymentOpt.isEmpty()) {
                log.warn("No payment found for transaction: {}, skipping compensation", transactionId);
                return;
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if payment is in a state that can be refunded
            if (!payment.isProcessed()) {
                log.info("Payment for transaction: {} is not processed (status: {}), no refund needed", 
                        transactionId, payment.getStatus());
                return;
            }
            
            // Check if payment is already refunded (idempotency)
            if (payment.isRefunded()) {
                log.info("Payment for transaction: {} is already refunded, skipping", transactionId);
                return;
            }
            
            // Extract reason from event
            String refundReason = extractRefundReason(event);
            
            log.info("Initiating payment refund for transaction: {}, paymentId: {}, reason: {}", 
                    transactionId, payment.getPaymentId(), refundReason);
            
            // Refund the payment (compensation logic)
            paymentService.refundPayment(payment.getPaymentId(), refundReason);
            
            log.info("Payment refund completed for transaction: {}, paymentId: {}", 
                    transactionId, payment.getPaymentId());
            
        } catch (Exception e) {
            log.error("Failed to process InventoryUnavailable event for transaction: {}, error: {}", 
                     event.getAggregateId(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
    
    private String extractRefundReason(DomainEvent event) {
        // In a real implementation, this would extract the reason from event payload
        // For now, use a default reason
        return "Inventory unavailable - automatic refund";
    }
    
    @Override
    protected boolean isNonRetryableError(Exception exception) {
        // Consider IllegalArgumentException and IllegalStateException as non-retryable
        return exception instanceof IllegalArgumentException ||
               exception instanceof IllegalStateException ||
               super.isNonRetryableError(exception);
    }
}