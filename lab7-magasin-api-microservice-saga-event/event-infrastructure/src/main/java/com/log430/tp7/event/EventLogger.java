package com.log430.tp7.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Structured logging utility for domain events.
 * Provides JSON-formatted logs with correlation IDs and context tracking
 * for comprehensive event tracing and observability.
 */
public class EventLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogger.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // MDC keys for correlation tracking
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String CAUSATION_ID_KEY = "causationId";
    public static final String EVENT_ID_KEY = "eventId";
    public static final String EVENT_TYPE_KEY = "eventType";
    public static final String AGGREGATE_ID_KEY = "aggregateId";
    public static final String AGGREGATE_TYPE_KEY = "aggregateType";
    public static final String SERVICE_NAME_KEY = "serviceName";
    public static final String OPERATION_KEY = "operation";
    
    /**
     * Logs event publishing with structured JSON format.
     */
    public static void logEventPublished(DomainEvent event, String routingKey, long durationMs) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "EVENT_PUBLISHED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("routingKey", routingKey);
        logData.put("durationMs", durationMs);
        logData.put("status", "SUCCESS");
        
        logger.info("Event published: {}", toJson(logData));
        
        clearEventContext();
    }
    
    /**
     * Logs event publishing failure with structured JSON format.
     */
    public static void logEventPublishingFailed(DomainEvent event, String routingKey, 
                                              Exception exception, long durationMs) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "EVENT_PUBLISH_FAILED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("routingKey", routingKey);
        logData.put("durationMs", durationMs);
        logData.put("status", "FAILED");
        logData.put("errorType", exception.getClass().getSimpleName());
        logData.put("errorMessage", exception.getMessage());
        
        logger.error("Event publishing failed: {}", toJson(logData), exception);
        
        clearEventContext();
    }
    
    /**
     * Logs event consumption start with structured JSON format.
     */
    public static void logEventReceived(DomainEvent event, String consumerName, String queueName) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "EVENT_RECEIVED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("consumerName", consumerName);
        logData.put("queueName", queueName);
        logData.put("status", "RECEIVED");
        
        logger.info("Event received: {}", toJson(logData));
    }
    
    /**
     * Logs successful event processing with structured JSON format.
     */
    public static void logEventProcessed(DomainEvent event, String consumerName, long durationMs) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "EVENT_PROCESSED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("consumerName", consumerName);
        logData.put("durationMs", durationMs);
        logData.put("status", "SUCCESS");
        
        logger.info("Event processed: {}", toJson(logData));
        
        clearEventContext();
    }
    
    /**
     * Logs event processing failure with structured JSON format.
     */
    public static void logEventProcessingFailed(DomainEvent event, String consumerName, 
                                              Exception exception, long durationMs, int retryCount) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "EVENT_PROCESSING_FAILED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("consumerName", consumerName);
        logData.put("durationMs", durationMs);
        logData.put("retryCount", retryCount);
        logData.put("status", "FAILED");
        logData.put("errorType", exception.getClass().getSimpleName());
        logData.put("errorMessage", exception.getMessage());
        
        logger.error("Event processing failed: {}", toJson(logData), exception);
        
        clearEventContext();
    }
    
    /**
     * Logs duplicate event detection with structured JSON format.
     */
    public static void logDuplicateEvent(DomainEvent event, String consumerName) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "DUPLICATE_EVENT_DETECTED");
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("consumerName", consumerName);
        logData.put("status", "DUPLICATE");
        
        logger.info("Duplicate event detected: {}", toJson(logData));
        
        clearEventContext();
    }
    
    /**
     * Logs saga step execution with structured JSON format.
     */
    public static void logSagaStep(DomainEvent event, String sagaId, String stepName, 
                                 String stepStatus, long durationMs) {
        setEventContext(event);
        MDC.put(OPERATION_KEY, "SAGA_STEP");
        MDC.put("sagaId", sagaId);
        
        Map<String, Object> logData = createBaseLogData(event);
        logData.put("sagaId", sagaId);
        logData.put("stepName", stepName);
        logData.put("stepStatus", stepStatus);
        logData.put("durationMs", durationMs);
        
        logger.info("Saga step executed: {}", toJson(logData));
        
        MDC.remove("sagaId");
        clearEventContext();
    }
    
    /**
     * Logs event replay operation with structured JSON format.
     */
    public static void logEventReplay(String aggregateId, String aggregateType, 
                                    int eventCount, long durationMs) {
        MDC.put(OPERATION_KEY, "EVENT_REPLAY");
        MDC.put(AGGREGATE_ID_KEY, aggregateId);
        MDC.put(AGGREGATE_TYPE_KEY, aggregateType);
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", "EVENT_REPLAY");
        logData.put("aggregateId", aggregateId);
        logData.put("aggregateType", aggregateType);
        logData.put("eventCount", eventCount);
        logData.put("durationMs", durationMs);
        logData.put("timestamp", Instant.now().toString());
        
        logger.info("Event replay completed: {}", toJson(logData));
        
        MDC.remove(OPERATION_KEY);
        MDC.remove(AGGREGATE_ID_KEY);
        MDC.remove(AGGREGATE_TYPE_KEY);
    }
    
    /**
     * Sets event context in MDC for correlation tracking.
     */
    public static void setEventContext(DomainEvent event) {
        if (event != null) {
            MDC.put(CORRELATION_ID_KEY, event.getCorrelationId());
            if (event.getCausationId() != null) {
                MDC.put(CAUSATION_ID_KEY, event.getCausationId());
            }
            MDC.put(EVENT_ID_KEY, event.getEventId());
            MDC.put(EVENT_TYPE_KEY, event.getEventType());
            MDC.put(AGGREGATE_ID_KEY, event.getAggregateId());
            MDC.put(AGGREGATE_TYPE_KEY, event.getAggregateType());
        }
    }
    
    /**
     * Clears event context from MDC.
     */
    public static void clearEventContext() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(CAUSATION_ID_KEY);
        MDC.remove(EVENT_ID_KEY);
        MDC.remove(EVENT_TYPE_KEY);
        MDC.remove(AGGREGATE_ID_KEY);
        MDC.remove(AGGREGATE_TYPE_KEY);
        MDC.remove(OPERATION_KEY);
    }
    
    /**
     * Sets service context in MDC.
     */
    public static void setServiceContext(String serviceName) {
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }
    
    /**
     * Creates base log data structure with common event fields.
     */
    private static Map<String, Object> createBaseLogData(DomainEvent event) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventId", event.getEventId());
        logData.put("eventType", event.getEventType());
        logData.put("aggregateId", event.getAggregateId());
        logData.put("aggregateType", event.getAggregateType());
        logData.put("correlationId", event.getCorrelationId());
        if (event.getCausationId() != null) {
            logData.put("causationId", event.getCausationId());
        }
        logData.put("eventTimestamp", event.getTimestamp().toString());
        logData.put("logTimestamp", Instant.now().toString());
        logData.put("version", event.getVersion());
        
        // Add service name if available in MDC
        String serviceName = MDC.get(SERVICE_NAME_KEY);
        if (serviceName != null) {
            logData.put("serviceName", serviceName);
        }
        
        return logData;
    }
    
    /**
     * Converts object to JSON string for logging.
     */
    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Failed to serialize log data to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
    
    /**
     * Creates a correlation context that can be used to wrap operations.
     */
    public static CorrelationContext createCorrelationContext(DomainEvent event) {
        return new CorrelationContext(event);
    }
    
    /**
     * Correlation context for automatic MDC management.
     */
    public static class CorrelationContext implements AutoCloseable {
        private final Map<String, String> previousContext;
        
        public CorrelationContext(DomainEvent event) {
            // Save current context
            previousContext = new HashMap<>();
            for (String key : new String[]{CORRELATION_ID_KEY, CAUSATION_ID_KEY, EVENT_ID_KEY, 
                                         EVENT_TYPE_KEY, AGGREGATE_ID_KEY, AGGREGATE_TYPE_KEY}) {
                String value = MDC.get(key);
                if (value != null) {
                    previousContext.put(key, value);
                }
            }
            
            // Set new context
            setEventContext(event);
        }
        
        @Override
        public void close() {
            // Clear current context
            clearEventContext();
            
            // Restore previous context
            previousContext.forEach(MDC::put);
        }
    }
}