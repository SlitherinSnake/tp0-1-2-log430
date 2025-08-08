package com.log430.tp7.infrastructure.event;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.TransactionSagaService;
import com.log430.tp7.domain.event.PaymentFailed;
import com.log430.tp7.domain.event.PaymentProcessed;
import com.log430.tp7.domain.event.PaymentRefunded;
import com.log430.tp7.event.AbstractEventConsumer;
import com.log430.tp7.event.DomainEvent;
import com.log430.tp7.infrastructure.monitoring.DistributedTracing;
import com.log430.tp7.infrastructure.monitoring.EventMetrics;

import io.micrometer.core.instrument.Timer;

/**
 * Dedicated event consumer for payment-related events.
 * Handles saga coordination for transaction state management with comprehensive metrics tracking.
 */
@Component
public class PaymentEventConsumer extends AbstractEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final TransactionSagaService sagaService;
    private final ObjectMapper objectMapper;
    private final EventMetrics eventMetrics;
    private final DistributedTracing distributedTracing;

    public PaymentEventConsumer(TransactionSagaService sagaService, ObjectMapper objectMapper, 
                               EventMetrics eventMetrics, DistributedTracing distributedTracing) {
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
        this.eventMetrics = eventMetrics;
        this.distributedTracing = distributedTracing;
    }

    /**
     * Listen for payment events on the transaction queue.
     */
    @RabbitListener(queues = "transaction.events.queue")
    public void handlePaymentEvent(org.springframework.amqp.core.Message message) {
        String eventJson = new String(message.getBody());
        Timer.Sample sample = eventMetrics.startEventProcessingTimer();
        String eventType = "Unknown";
        String correlationId = null;
        
        try {
            log.info("Received payment event: {}", eventJson);

            // Extract tracing information from message headers
            correlationId = (String) message.getMessageProperties().getHeaders().get("correlationId");
            
            // Parse the event to determine its type
            var eventNode = objectMapper.readTree(eventJson);
            eventType = eventNode.get("eventType").asText();
            
            // Start distributed tracing for event consumption
            if (correlationId != null) {
                distributedTracing.continueTrace(correlationId, "transaction-service", "EVENT_CONSUME_" + eventType);
            } else {
                correlationId = distributedTracing.startTrace("transaction-service", "EVENT_CONSUME_" + eventType);
            }

            switch (eventType) {
                case "PaymentProcessed" -> {
                    handlePaymentProcessed(eventJson, correlationId);
                    eventMetrics.recordPaymentProcessedEvent();
                }
                case "PaymentFailed" -> {
                    handlePaymentFailed(eventJson, correlationId);
                    eventMetrics.recordPaymentFailedEvent();
                }
                case "PaymentRefunded" -> {
                    handlePaymentRefunded(eventJson, correlationId);
                    eventMetrics.recordPaymentRefundedEvent();
                }
                default -> {
                    log.debug("Payment event type {} not handled by this consumer", eventType);
                    distributedTracing.endSpan(correlationId, "transaction-service", "EVENT_CONSUME_" + eventType, true);
                    return;
                }
            }

            // Record successful event consumption
            eventMetrics.recordEventConsumed(eventType);
            eventMetrics.recordEventProcessingLatency(sample, eventType);
            distributedTracing.endSpan(correlationId, "transaction-service", "EVENT_CONSUME_" + eventType, true);

        } catch (Exception e) {
            eventMetrics.recordEventConsumptionFailure(eventType, "PROCESSING_ERROR");
            if (correlationId != null) {
                distributedTracing.recordError(correlationId, "transaction-service", "EVENT_CONSUME_" + eventType, 
                                             "PROCESSING_ERROR", e.getMessage());
                distributedTracing.endSpan(correlationId, "transaction-service", "EVENT_CONSUME_" + eventType, false);
            }
            log.error("Failed to process payment event: {}", eventJson, e);
            handleEventProcessingError(eventJson, e);
        }
    }

    /**
     * Handle PaymentProcessed events.
     */
    private void handlePaymentProcessed(String eventJson, String correlationId) {
        try {
            PaymentProcessed event = objectMapper.readValue(eventJson, PaymentProcessed.class);
            log.info("Processing PaymentProcessed event for transaction: {} with saga: {} and correlation: {}",
                    event.getTransactionId(), event.getSagaId(), correlationId);

            sagaService.handlePaymentProcessed(event);

            log.info("Successfully processed PaymentProcessed event for transaction: {}",
                    event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to handle PaymentProcessed event: {}", eventJson, e);
            throw new RuntimeException("PaymentProcessed event handling failed", e);
        }
    }

    /**
     * Handle PaymentFailed events.
     */
    private void handlePaymentFailed(String eventJson, String correlationId) {
        try {
            PaymentFailed event = objectMapper.readValue(eventJson, PaymentFailed.class);
            log.info("Processing PaymentFailed event for transaction: {} with saga: {} and correlation: {}",
                    event.getTransactionId(), event.getSagaId(), correlationId);

            sagaService.handlePaymentFailed(event);

            log.info("Successfully processed PaymentFailed event for transaction: {}",
                    event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to handle PaymentFailed event: {}", eventJson, e);
            throw new RuntimeException("PaymentFailed event handling failed", e);
        }
    }

    /**
     * Handle PaymentRefunded events.
     */
    private void handlePaymentRefunded(String eventJson, String correlationId) {
        try {
            PaymentRefunded event = objectMapper.readValue(eventJson, PaymentRefunded.class);
            log.info("Processing PaymentRefunded event for transaction: {} with saga: {} and correlation: {}",
                    event.getTransactionId(), event.getSagaId(), correlationId);

            sagaService.handlePaymentRefunded(event);

            log.info("Successfully processed PaymentRefunded event for transaction: {}",
                    event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to handle PaymentRefunded event: {}", eventJson, e);
            throw new RuntimeException("PaymentRefunded event handling failed", e);
        }
    }

    @Override
    protected Set<String> getSupportedEventTypes() {
        return Set.of("PaymentProcessed", "PaymentFailed", "PaymentRefunded");
    }

    @Override
    protected void processEvent(DomainEvent event) {
        // Implementation of the abstract method from AbstractEventConsumer
        log.debug("processEvent called with: {}", event);
        // Process the domain event based on its type
        if (event instanceof PaymentProcessed) {
            // Handle payment processed logic
            log.info("Processing PaymentProcessed event: {}", event.getEventId());
        } else if (event instanceof PaymentFailed) {
            // Handle payment failed logic
            log.info("Processing PaymentFailed event: {}", event.getEventId());
        } else if (event instanceof PaymentRefunded) {
            // Handle payment refunded logic
            log.info("Processing PaymentRefunded event: {}", event.getEventId());
        }
    }

    @Override
    public boolean canHandle(String eventType) {
        return eventType.startsWith("Payment");
    }

    @Override
    public String getConsumerName() {
        return "PaymentEventConsumer";
    }

    /**
     * Handle event processing errors with retry logic.
     */
    private void handleEventProcessingError(String eventJson, Exception error) {
        log.error("Event processing failed, implementing error handling strategy", error);

        // In a production system, you might:
        // 1. Send to dead letter queue
        // 2. Implement retry with exponential backoff
        // 3. Alert monitoring systems
        // 4. Store failed events for manual processing

        // For now, we'll just log and re-throw to trigger RabbitMQ retry mechanisms
        throw new RuntimeException("Event processing failed", error);
    }
}