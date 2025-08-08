package com.log430.tp7.infrastructure.monitoring;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Distributed tracing component for tracking events across service boundaries.
 * Provides correlation ID propagation and tracing context management.
 */
@Component
public class DistributedTracing {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedTracing.class);
    
    // MDC Keys for tracing context
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String CAUSATION_ID_KEY = "causationId";
    public static final String SERVICE_NAME_KEY = "serviceName";
    public static final String EVENT_TYPE_KEY = "eventType";
    public static final String SAGA_ID_KEY = "sagaId";
    public static final String TRACE_ID_KEY = "traceId";
    
    // In-memory trace storage for demonstration (in production, use proper tracing infrastructure)
    private final ConcurrentMap<String, EventTrace> activeTraces = new ConcurrentHashMap<>();
    
    /**
     * Start a new trace with correlation ID.
     */
    public String startTrace(String serviceName, String eventType) {
        String correlationId = UUID.randomUUID().toString();
        String traceId = UUID.randomUUID().toString();
        
        return startTrace(correlationId, traceId, serviceName, eventType, null, null);
    }
    
    /**
     * Start a trace with existing correlation ID (for event consumption).
     */
    public String startTrace(String correlationId, String traceId, String serviceName, 
                           String eventType, String causationId, String sagaId) {
        // Set MDC context for logging
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
        MDC.put(EVENT_TYPE_KEY, eventType);
        
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
        
        if (causationId != null) {
            MDC.put(CAUSATION_ID_KEY, causationId);
        }
        
        if (sagaId != null) {
            MDC.put(SAGA_ID_KEY, sagaId);
        }
        
        // Create or update trace
        EventTrace trace = activeTraces.computeIfAbsent(correlationId, k -> 
            new EventTrace(correlationId, traceId, System.currentTimeMillis()));
        
        trace.addSpan(serviceName, eventType, System.currentTimeMillis());
        
        logger.info("Started trace - correlationId: {}, traceId: {}, service: {}, eventType: {}", 
                   correlationId, traceId, serviceName, eventType);
        
        return correlationId;
    }
    
    /**
     * Continue an existing trace (for event processing).
     */
    public void continueTrace(String correlationId, String serviceName, String eventType) {
        if (correlationId == null) {
            logger.warn("No correlation ID provided for trace continuation");
            return;
        }
        
        EventTrace trace = activeTraces.get(correlationId);
        if (trace == null) {
            logger.warn("No active trace found for correlation ID: {}", correlationId);
            // Create a new trace if one doesn't exist
            startTrace(correlationId, UUID.randomUUID().toString(), serviceName, eventType, null, null);
            return;
        }
        
        // Update MDC context
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
        MDC.put(EVENT_TYPE_KEY, eventType);
        if (trace.getTraceId() != null) {
            MDC.put(TRACE_ID_KEY, trace.getTraceId());
        }
        
        trace.addSpan(serviceName, eventType, System.currentTimeMillis());
        
        logger.info("Continued trace - correlationId: {}, service: {}, eventType: {}", 
                   correlationId, serviceName, eventType);
    }
    
    /**
     * End a trace span.
     */
    public void endSpan(String correlationId, String serviceName, String eventType, boolean success) {
        EventTrace trace = activeTraces.get(correlationId);
        if (trace != null) {
            trace.endCurrentSpan(serviceName, eventType, System.currentTimeMillis(), success);
            
            logger.info("Ended span - correlationId: {}, service: {}, eventType: {}, success: {}", 
                       correlationId, serviceName, eventType, success);
        }
    }
    
    /**
     * Complete a trace.
     */
    public void completeTrace(String correlationId, boolean success) {
        EventTrace trace = activeTraces.remove(correlationId);
        if (trace != null) {
            trace.complete(System.currentTimeMillis(), success);
            
            logger.info("Completed trace - correlationId: {}, success: {}, duration: {}ms, spans: {}", 
                       correlationId, success, trace.getTotalDuration(), trace.getSpanCount());
            
            // In production, this would be sent to a tracing system like Jaeger or Zipkin
            logTraceMetrics(trace);
        }
        
        // Clear MDC context
        clearContext();
    }
    
    /**
     * Add event processing error to trace.
     */
    public void recordError(String correlationId, String serviceName, String eventType, 
                          String errorType, String errorMessage) {
        EventTrace trace = activeTraces.get(correlationId);
        if (trace != null) {
            trace.addError(serviceName, eventType, errorType, errorMessage, System.currentTimeMillis());
        }
        
        // Add error to MDC for logging
        MDC.put("errorType", errorType);
        MDC.put("errorMessage", errorMessage);
        
        logger.error("Error in trace - correlationId: {}, service: {}, eventType: {}, error: {} - {}", 
                    correlationId, serviceName, eventType, errorType, errorMessage);
    }
    
    /**
     * Get current correlation ID from MDC.
     */
    public String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Get current trace ID from MDC.
     */
    public String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * Clear tracing context from MDC.
     */
    public void clearContext() {
        MDC.clear();
    }
    
    /**
     * Get trace information for monitoring.
     */
    public EventTrace getTrace(String correlationId) {
        return activeTraces.get(correlationId);
    }
    
    /**
     * Get all active traces.
     */
    public ConcurrentMap<String, EventTrace> getActiveTraces() {
        return new ConcurrentHashMap<>(activeTraces);
    }
    
    /**
     * Log trace metrics for observability.
     */
    private void logTraceMetrics(EventTrace trace) {
        // Create structured log entry for trace completion
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("TRACE_COMPLETED|");
        logEntry.append("correlationId=").append(trace.getCorrelationId()).append("|");
        logEntry.append("traceId=").append(trace.getTraceId()).append("|");
        logEntry.append("duration=").append(trace.getTotalDuration()).append("ms|");
        logEntry.append("spans=").append(trace.getSpanCount()).append("|");
        logEntry.append("success=").append(trace.isSuccessful()).append("|");
        logEntry.append("services=").append(String.join(",", trace.getInvolvedServices()));
        
        if (!trace.getErrors().isEmpty()) {
            logEntry.append("|errors=").append(trace.getErrors().size());
        }
        
        logger.info(logEntry.toString());
    }
    
    /**
     * Create causation ID for event chaining.
     */
    public String createCausationId(String parentEventId) {
        return parentEventId != null ? parentEventId : UUID.randomUUID().toString();
    }
}
