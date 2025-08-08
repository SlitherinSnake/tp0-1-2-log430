package com.log430.tp7.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized audit logger for compliance and regulatory requirements.
 * Creates immutable audit trails for all business events with full context.
 */
public class EventAuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Logs a business event for audit purposes with full context.
     */
    public static void auditBusinessEvent(DomainEvent event, String action, String serviceName, 
                                        String userId, Map<String, Object> additionalContext) {
        // Set audit context
        String previousLogType = MDC.get("logType");
        MDC.put("logType", "AUDIT");
        
        try {
            Map<String, Object> auditData = createAuditRecord(event, action, serviceName, userId);
            
            // Add additional context if provided
            if (additionalContext != null) {
                auditData.put("additionalContext", additionalContext);
            }
            
            auditLogger.info("Business event audit: {}", toJson(auditData));
            
        } finally {
            // Restore previous log type
            if (previousLogType != null) {
                MDC.put("logType", previousLogType);
            } else {
                MDC.remove("logType");
            }
        }
    }
    
    /**
     * Logs a saga execution step for audit purposes.
     */
    public static void auditSagaStep(String sagaId, String sagaType, String stepName, 
                                   String stepStatus, DomainEvent triggerEvent, 
                                   String serviceName, long durationMs) {
        String previousLogType = MDC.get("logType");
        MDC.put("logType", "AUDIT");
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("auditType", "SAGA_STEP");
            auditData.put("timestamp", Instant.now().toString());
            auditData.put("sagaId", sagaId);
            auditData.put("sagaType", sagaType);
            auditData.put("stepName", stepName);
            auditData.put("stepStatus", stepStatus);
            auditData.put("serviceName", serviceName);
            auditData.put("durationMs", durationMs);
            
            if (triggerEvent != null) {
                auditData.put("triggerEvent", createEventSummary(triggerEvent));
            }
            
            auditLogger.info("Saga step audit: {}", toJson(auditData));
            
        } finally {
            if (previousLogType != null) {
                MDC.put("logType", previousLogType);
            } else {
                MDC.remove("logType");
            }
        }
    }
    
    /**
     * Logs event processing failure for audit and compliance.
     */
    public static void auditEventProcessingFailure(DomainEvent event, String consumerName, 
                                                  Exception exception, int retryCount, 
                                                  boolean willRetry) {
        String previousLogType = MDC.get("logType");
        MDC.put("logType", "AUDIT");
        
        try {
            Map<String, Object> auditData = createAuditRecord(event, "EVENT_PROCESSING_FAILED", 
                                                            consumerName, null);
            auditData.put("errorType", exception.getClass().getSimpleName());
            auditData.put("errorMessage", exception.getMessage());
            auditData.put("retryCount", retryCount);
            auditData.put("willRetry", willRetry);
            auditData.put("severity", willRetry ? "WARNING" : "ERROR");
            
            auditLogger.error("Event processing failure audit: {}", toJson(auditData));
            
        } finally {
            if (previousLogType != null) {
                MDC.put("logType", previousLogType);
            } else {
                MDC.remove("logType");
            }
        }
    }
    
    /**
     * Logs data access for audit purposes (GDPR compliance).
     */
    public static void auditDataAccess(String dataType, String dataId, String action, 
                                     String userId, String serviceName, String purpose) {
        String previousLogType = MDC.get("logType");
        MDC.put("logType", "AUDIT");
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("auditType", "DATA_ACCESS");
            auditData.put("timestamp", Instant.now().toString());
            auditData.put("dataType", dataType);
            auditData.put("dataId", dataId);
            auditData.put("action", action);
            auditData.put("userId", userId);
            auditData.put("serviceName", serviceName);
            auditData.put("purpose", purpose);
            
            auditLogger.info("Data access audit: {}", toJson(auditData));
            
        } finally {
            if (previousLogType != null) {
                MDC.put("logType", previousLogType);
            } else {
                MDC.remove("logType");
            }
        }
    }
    
    /**
     * Logs security events for audit purposes.
     */
    public static void auditSecurityEvent(String eventType, String userId, String serviceName, 
                                        String details, String severity) {
        String previousLogType = MDC.get("logType");
        MDC.put("logType", "AUDIT");
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("auditType", "SECURITY_EVENT");
            auditData.put("timestamp", Instant.now().toString());
            auditData.put("eventType", eventType);
            auditData.put("userId", userId);
            auditData.put("serviceName", serviceName);
            auditData.put("details", details);
            auditData.put("severity", severity);
            
            auditLogger.warn("Security event audit: {}", toJson(auditData));
            
        } finally {
            if (previousLogType != null) {
                MDC.put("logType", previousLogType);
            } else {
                MDC.remove("logType");
            }
        }
    }
    
    /**
     * Creates a complete audit record for a business event.
     */
    private static Map<String, Object> createAuditRecord(DomainEvent event, String action, 
                                                        String serviceName, String userId) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("auditType", "BUSINESS_EVENT");
        auditData.put("timestamp", Instant.now().toString());
        auditData.put("action", action);
        auditData.put("serviceName", serviceName);
        
        if (userId != null) {
            auditData.put("userId", userId);
        }
        
        // Event details
        auditData.put("event", createEventSummary(event));
        
        // Correlation tracking
        auditData.put("correlationId", event.getCorrelationId());
        if (event.getCausationId() != null) {
            auditData.put("causationId", event.getCausationId());
        }
        
        return auditData;
    }
    
    /**
     * Creates a summary of event data for audit logging.
     */
    private static Map<String, Object> createEventSummary(DomainEvent event) {
        Map<String, Object> eventSummary = new HashMap<>();
        eventSummary.put("eventId", event.getEventId());
        eventSummary.put("eventType", event.getEventType());
        eventSummary.put("aggregateId", event.getAggregateId());
        eventSummary.put("aggregateType", event.getAggregateType());
        eventSummary.put("version", event.getVersion());
        eventSummary.put("timestamp", event.getTimestamp().toString());
        
        // Include metadata if available
        if (event.getMetadata() != null) {
            if (event.getMetadata().getSource() != null) {
                eventSummary.put("source", event.getMetadata().getSource());
            }
            if (event.getMetadata().getUserId() != null) {
                eventSummary.put("userId", event.getMetadata().getUserId());
            }
        }
        
        return eventSummary;
    }
    
    /**
     * Converts object to JSON string for audit logging.
     */
    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // Fallback to string representation if JSON serialization fails
            return obj.toString();
        }
    }
}