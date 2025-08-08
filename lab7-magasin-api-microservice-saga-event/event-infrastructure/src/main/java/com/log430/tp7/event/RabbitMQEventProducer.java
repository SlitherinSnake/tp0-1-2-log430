package com.log430.tp7.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ implementation of EventProducer with enhanced features:
 * - Correlation ID management and propagation
 * - Structured logging with correlation context
 * - Retry mechanism with exponential backoff
 * - Asynchronous publishing support
 * - Batch publishing capabilities
 */
@Component
public class RabbitMQEventProducer implements EventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQEventProducer.class);
    private static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    private static final String CORRELATION_ID_HEADER = "correlationId";
    private static final String CAUSATION_ID_HEADER = "causationId";
    private static final String EVENT_TYPE_HEADER = "eventType";
    private static final String AGGREGATE_TYPE_HEADER = "aggregateType";
    private static final String AGGREGATE_ID_HEADER = "aggregateId";
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final EventPublishingMetrics metrics;
    
    @Autowired
    public RabbitMQEventProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.metrics = new EventPublishingMetrics();
    }
    
    @Override
    public void publishEvent(DomainEvent event) {
        String routingKey = generateRoutingKey(event);
        publishEvent(routingKey, event);
    }
    
    @Override
    public void publishEvent(String routingKey, DomainEvent event) {
        publishEvent(routingKey, event, new HashMap<>());
    }
    
    @Override
    public void publishEvent(String routingKey, DomainEvent event, Map<String, Object> headers) {
        // Set correlation context for logging
        String previousCorrelationId = MDC.get(CORRELATION_ID_HEADER);
        long startTime = System.currentTimeMillis();
        try {
            MDC.put(CORRELATION_ID_HEADER, event.getCorrelationId());
            
            // Validate event before publishing
            event.validate();
            
            // Serialize and publish
            Message message = createMessage(event, headers);
            rabbitTemplate.send(BUSINESS_EVENTS_EXCHANGE, routingKey, message);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Record metrics
            metrics.recordEventPublished(event.getEventType(), duration);
            
            // Use structured logging
            EventLogger.logEventPublished(event, routingKey, duration);
            
        } catch (JsonProcessingException e) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordEventPublishingError(event.getEventType(), "SERIALIZATION_ERROR");
            EventLogger.logEventPublishingFailed(event, routingKey, e, duration);
            throw new EventPublishingException("Event serialization failed", event, routingKey, e, false);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordEventPublishingError(event.getEventType(), "PUBLISHING_ERROR");
            EventLogger.logEventPublishingFailed(event, routingKey, e, duration);
            throw new EventPublishingException("Event publishing failed", event, routingKey, e, true);
        } finally {
            // Restore previous correlation ID
            if (previousCorrelationId != null) {
                MDC.put(CORRELATION_ID_HEADER, previousCorrelationId);
            } else {
                MDC.remove(CORRELATION_ID_HEADER);
            }
        }
    }
    
    @Override
    public CompletableFuture<Void> publishEventAsync(DomainEvent event) {
        String routingKey = generateRoutingKey(event);
        return publishEventAsync(routingKey, event);
    }
    
    @Override
    public CompletableFuture<Void> publishEventAsync(String routingKey, DomainEvent event) {
        return CompletableFuture.runAsync(() -> publishEvent(routingKey, event));
    }
    
    @Override
    public void publishEvents(DomainEvent... events) {
        if (events == null || events.length == 0) {
            return;
        }
        
        logger.info("Publishing batch of {} events", events.length);
        
        for (DomainEvent event : events) {
            publishEvent(event);
        }
        
        logger.info("Successfully published batch of {} events", events.length);
    }
    
    @Override
    public void publishEventWithRetry(DomainEvent event, int maxRetries) {
        String routingKey = generateRoutingKey(event);
        publishEventWithRetry(routingKey, event, maxRetries, 1);
    }
    
    private void publishEventWithRetry(String routingKey, DomainEvent event, int maxRetries, int attempt) {
        try {
            publishEvent(routingKey, event);
        } catch (EventPublishingException e) {
            if (!e.isRetryable() || attempt >= maxRetries) {
                logger.error("Failed to publish event after {} attempts: {}", attempt, event);
                throw e;
            }
            
            // Calculate exponential backoff delay
            long delay = calculateBackoffDelay(attempt);
            
            logger.warn("Failed to publish event (attempt {}/{}), retrying in {}ms: {}", 
                       attempt, maxRetries, delay, event);
            
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new EventPublishingException("Retry interrupted", event, routingKey, ie, false);
            }
            
            publishEventWithRetry(routingKey, event, maxRetries, attempt + 1);
        }
    }
    
    private Message createMessage(DomainEvent event, Map<String, Object> additionalHeaders) 
            throws JsonProcessingException {
        String eventJson = objectMapper.writeValueAsString(event);
        
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setCorrelationId(event.getCorrelationId());
        properties.setMessageId(event.getEventId());
        properties.setTimestamp(java.util.Date.from(event.getTimestamp()));
        
        // Add event metadata as headers
        properties.setHeader(CORRELATION_ID_HEADER, event.getCorrelationId());
        if (event.getCausationId() != null) {
            properties.setHeader(CAUSATION_ID_HEADER, event.getCausationId());
        }
        properties.setHeader(EVENT_TYPE_HEADER, event.getEventType());
        properties.setHeader(AGGREGATE_TYPE_HEADER, event.getAggregateType());
        properties.setHeader(AGGREGATE_ID_HEADER, event.getAggregateId());
        properties.setHeader("version", event.getVersion());
        
        // Add metadata properties
        if (event.getMetadata().getSource() != null) {
            properties.setHeader("source", event.getMetadata().getSource());
        }
        if (event.getMetadata().getUserId() != null) {
            properties.setHeader("userId", event.getMetadata().getUserId());
        }
        
        // Add custom headers
        additionalHeaders.forEach(properties::setHeader);
        
        return new Message(eventJson.getBytes(), properties);
    }
    
    private String generateRoutingKey(DomainEvent event) {
        return event.getAggregateType().toLowerCase() + "." + 
               event.getEventType().toLowerCase().replace("_", ".");
    }
    
    private long calculateBackoffDelay(int attempt) {
        // Exponential backoff: 100ms * 2^(attempt-1), max 30 seconds
        long delay = (long) (100 * Math.pow(2, (double) attempt - 1));
        return Math.min(delay, 30000);
    }
}