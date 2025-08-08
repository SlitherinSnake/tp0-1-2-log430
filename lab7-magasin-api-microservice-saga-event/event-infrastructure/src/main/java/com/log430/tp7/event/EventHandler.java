package com.log430.tp7.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central event handler that routes events to appropriate consumers
 * with retry mechanism, idempotency handling, and structured logging.
 */
@Component
public class EventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private static final String CORRELATION_ID_HEADER = "correlationId";
    private static final String RETRY_COUNT_HEADER = "x-retry-count";
    private static final int MAX_RETRIES = 3;
    
    private final List<EventConsumer> eventConsumers;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;
    private final EventProcessingMetrics metrics;
    
    @Autowired
    public EventHandler(List<EventConsumer> eventConsumers, ObjectMapper objectMapper) {
        this.eventConsumers = eventConsumers;
        this.objectMapper = objectMapper;
        this.idempotencyService = new IdempotencyService();
        this.metrics = new EventProcessingMetrics();
    }
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleNotificationEvent(@Payload String eventJson, 
                                      @Header Map<String, Object> headers) {
        handleEvent(eventJson, headers, "notification");
    }
    
    @RabbitListener(queues = "#{auditQueue.name}")
    public void handleAuditEvent(@Payload String eventJson, 
                               @Header Map<String, Object> headers) {
        handleEvent(eventJson, headers, "audit");
    }
    
    private void handleEvent(String eventJson, Map<String, Object> headers, String queueType) {
        String correlationId = (String) headers.get(CORRELATION_ID_HEADER);
        String eventType = (String) headers.get("eventType");
        String eventId = (String) headers.get(AmqpHeaders.MESSAGE_ID);
        
        // Set correlation context for logging
        String previousCorrelationId = MDC.get(CORRELATION_ID_HEADER);
        try {
            if (correlationId != null) {
                MDC.put(CORRELATION_ID_HEADER, correlationId);
            }
            
            // Deserialize event first for structured logging
            DomainEvent event = deserializeEvent(eventJson, eventType);
            if (event != null) {
                EventLogger.logEventReceived(event, "EventHandler", queueType);
            } else {
                logger.info("Received event: type={}, id={}, queue={}", eventType, eventId, queueType);
            }
            
            // Check idempotency
            if (idempotencyService.isProcessed(eventId)) {
                if (event != null) {
                    EventLogger.logDuplicateEvent(event, "EventHandler");
                } else {
                    logger.info("Event already processed, skipping: id={}", eventId);
                }
                metrics.recordDuplicateEvent(eventType);
                return;
            }
            
            if (event == null) {
                logger.error("Failed to deserialize event: {}", eventJson);
                metrics.recordEventProcessingError(eventType, "DESERIALIZATION_ERROR");
                return;
            }
            
            // Process event with retry logic
            processEventWithRetry(event, headers);
            
            // Mark as processed for idempotency
            idempotencyService.markProcessed(eventId);
            
            logger.info("Successfully processed event: type={}, id={}", eventType, eventId);
            
        } catch (Exception e) {
            logger.error("Failed to process event: type={}, id={}", eventType, eventId, e);
            metrics.recordEventProcessingError(eventType, "PROCESSING_ERROR");
            throw e; // Rethrow to trigger message requeue or DLQ
        } finally {
            // Restore previous correlation ID
            if (previousCorrelationId != null) {
                MDC.put(CORRELATION_ID_HEADER, previousCorrelationId);
            } else {
                MDC.remove(CORRELATION_ID_HEADER);
            }
        }
    }
    
    private void processEventWithRetry(DomainEvent event, Map<String, Object> headers) {
        int retryCount = getRetryCount(headers);
        
        for (EventConsumer consumer : eventConsumers) {
            if (consumer.canHandle(event.getEventType())) {
                processWithConsumer(consumer, event, retryCount);
            }
        }
    }
    
    private void processWithConsumer(EventConsumer consumer, DomainEvent event, int retryCount) {
        long startTime = System.currentTimeMillis();
        
        try {
            consumer.beforeEventProcessing(event);
            consumer.handleEvent(event);
            consumer.afterEventProcessing(event);
            
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordEventProcessed(event.getEventType(), consumer.getConsumerName(), duration);
            
            EventLogger.logEventProcessed(event, consumer.getConsumerName(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordEventProcessingError(event.getEventType(), consumer.getConsumerName());
            
            EventLogger.logEventProcessingFailed(event, consumer.getConsumerName(), e, duration, retryCount);
            
            // Let consumer handle the error
            if (!consumer.handleError(event, e)) {
                // Consumer didn't handle the error, apply retry logic
                if (retryCount < MAX_RETRIES) {
                    throw new EventProcessingException(
                        "Event processing failed, will retry", event, consumer.getConsumerName(), e, true);
                } else {
                    throw new EventProcessingException(
                        "Event processing failed after max retries", event, consumer.getConsumerName(), e, false);
                }
            }
        }
    }
    
    private DomainEvent deserializeEvent(String eventJson, String eventType) {
        try {
            // For now, deserialize as generic DomainEvent
            // In a real implementation, you'd have a registry of event types
            return objectMapper.readValue(eventJson, DomainEvent.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize event of type {}: {}", eventType, eventJson, e);
            return null;
        }
    }
    
    private int getRetryCount(Map<String, Object> headers) {
        Object retryCountObj = headers.get(RETRY_COUNT_HEADER);
        if (retryCountObj instanceof Integer) {
            return (Integer) retryCountObj;
        }
        return 0;
    }
    
    /**
     * Simple in-memory idempotency service.
     * In production, this should be backed by a persistent store like Redis.
     */
    private static class IdempotencyService {
        private final Map<String, Long> processedEvents = new ConcurrentHashMap<>();
        private static final long CLEANUP_INTERVAL = 3600000; // 1 hour
        
        public boolean isProcessed(String eventId) {
            cleanupOldEntries();
            return processedEvents.containsKey(eventId);
        }
        
        public void markProcessed(String eventId) {
            processedEvents.put(eventId, System.currentTimeMillis());
        }
        
        private void cleanupOldEntries() {
            long cutoff = System.currentTimeMillis() - CLEANUP_INTERVAL;
            processedEvents.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }
    }
}