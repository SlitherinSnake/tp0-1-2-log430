package com.log430.tp7.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.event.AbstractEventConsumer;
import com.log430.tp7.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Event consumer for transaction-related events.
 * Handles TransactionCreated events to initiate payment processing.
 */
@Component
public class TransactionEventConsumer extends AbstractEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);
    
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public TransactionEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected Set<String> getSupportedEventTypes() {
        return Set.of("TransactionCreated");
    }
    
    @Override
    public String getConsumerName() {
        return "TransactionEventConsumer";
    }
    
    @RabbitListener(queues = "payment.transaction.queue")
    public void handleTransactionEvent(DomainEvent event) {
        handleEvent(event);
    }
    
    @Override
    protected void processEvent(DomainEvent event) {
        validateEvent(event);
        
        if ("TransactionCreated".equals(event.getEventType())) {
            handleTransactionCreated(event);
        }
    }
    
    private void handleTransactionCreated(DomainEvent event) {
        try {
            // Extract transaction data from the event
            String transactionId = event.getAggregateId();
            String correlationId = event.getCorrelationId();
            
            log.info("Processing TransactionCreated event for transaction: {}, correlationId: {}", 
                    transactionId, correlationId);
            
            // Check if payment already exists for this transaction (idempotency)
            if (paymentService.findByTransactionId(transactionId).isPresent()) {
                log.info("Payment already exists for transaction: {}, skipping processing", transactionId);
                return;
            }
            
            // Extract payment details from event metadata or use defaults
            // In a real implementation, these would come from the event payload
            String customerId = extractCustomerId(event);
            BigDecimal amount = extractAmount(event);
            String paymentMethod = extractPaymentMethod(event);
            
            // Create payment for the transaction
            Payment payment = paymentService.createPayment(
                transactionId, customerId, amount, paymentMethod, correlationId);
            
            log.info("Payment created for transaction: {}, paymentId: {}", 
                    transactionId, payment.getPaymentId());
            
            // Automatically process the payment (in choreographed saga)
            paymentService.processPayment(payment.getPaymentId());
            
            log.info("Payment processed successfully for transaction: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to process TransactionCreated event for transaction: {}, error: {}", 
                     event.getAggregateId(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
    
    private String extractCustomerId(DomainEvent event) {
        try {
            // Convert event to JSON and extract personnelId as customer identifier
            JsonNode eventJson = objectMapper.valueToTree(event);
            JsonNode personnelId = eventJson.get("personnelId");
            if (personnelId != null && !personnelId.isNull()) {
                return "customer-" + personnelId.asText();
            }
        } catch (Exception e) {
            log.warn("Failed to extract personnelId from event, using default: {}", e.getMessage());
        }
        // Fallback to transaction ID based customer
        return "customer-" + event.getAggregateId();
    }
    
    private BigDecimal extractAmount(DomainEvent event) {
        try {
            // Convert event to JSON and extract totalAmount
            JsonNode eventJson = objectMapper.valueToTree(event);
            JsonNode totalAmount = eventJson.get("totalAmount");
            if (totalAmount != null && !totalAmount.isNull()) {
                return new BigDecimal(totalAmount.asText());
            }
        } catch (Exception e) {
            log.warn("Failed to extract totalAmount from event, using default: {}", e.getMessage());
        }
        // Fallback to default amount
        return new BigDecimal("100.00");
    }
    
    private String extractPaymentMethod(DomainEvent event) {
        try {
            // Convert event to JSON and check transaction type for payment method hint
            JsonNode eventJson = objectMapper.valueToTree(event);
            JsonNode transactionType = eventJson.get("transactionType");
            if (transactionType != null && !transactionType.isNull()) {
                String type = transactionType.asText();
                // Map transaction types to payment methods
                if ("RETURN".equalsIgnoreCase(type)) {
                    return "REFUND";
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract transactionType from event, using default: {}", e.getMessage());
        }
        // Default payment method
        return "CREDIT_CARD";
    }
    
    @Override
    protected boolean isNonRetryableError(Exception exception) {
        // Consider IllegalArgumentException as non-retryable (e.g., invalid transaction ID)
        return exception instanceof IllegalArgumentException ||
               super.isNonRetryableError(exception);
    }
}