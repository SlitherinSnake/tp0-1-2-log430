package com.log430.tp6.sagaorchestrator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing saga events for audit trail and debugging purposes.
 * Tracks all significant events and state changes during saga execution.
 */
@Entity
@Table(name = "saga_events")
public class SagaEvent {
    
    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;
    
    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SagaEventType eventType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Static ObjectMapper for JSON serialization
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    // Default constructor for JPA
    public SagaEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor for creating new events
    public SagaEvent(String sagaId, SagaEventType eventType, Map<String, Object> eventData) {
        this();
        this.sagaId = sagaId;
        this.eventType = eventType;
        this.eventData = eventData;
    }
    
    // Constructor with string data (will be parsed as JSON)
    public SagaEvent(String sagaId, SagaEventType eventType, String jsonData) {
        this(sagaId, eventType, parseJsonData(jsonData));
    }
    
    /**
     * Creates a saga started event.
     */
    public static SagaEvent sagaStarted(String sagaId, String customerId, String productId, 
                                       Integer quantity, String amount) {
        Map<String, Object> data = Map.of(
            "customerId", customerId,
            "productId", productId,
            "quantity", quantity,
            "amount", amount
        );
        return new SagaEvent(sagaId, SagaEventType.SAGA_STARTED, data);
    }
    
    /**
     * Creates a state transition event.
     */
    public static SagaEvent stateTransition(String sagaId, SagaState fromState, SagaState toState) {
        Map<String, Object> data = Map.of(
            "fromState", fromState.name(),
            "toState", toState.name()
        );
        return new SagaEvent(sagaId, SagaEventType.STATE_TRANSITION, data);
    }
    
    /**
     * Creates a service call started event.
     */
    public static SagaEvent serviceCallStarted(String sagaId, String serviceName, String operation, 
                                             Map<String, Object> requestData) {
        Map<String, Object> data = Map.of(
            "serviceName", serviceName,
            "operation", operation,
            "requestData", requestData != null ? requestData : Map.of()
        );
        return new SagaEvent(sagaId, SagaEventType.SERVICE_CALL_STARTED, data);
    }
    
    /**
     * Creates a service call completed event.
     */
    public static SagaEvent serviceCallCompleted(String sagaId, String serviceName, String operation, 
                                                boolean success, Map<String, Object> responseData) {
        Map<String, Object> data = Map.of(
            "serviceName", serviceName,
            "operation", operation,
            "success", success,
            "responseData", responseData != null ? responseData : Map.of()
        );
        return new SagaEvent(sagaId, SagaEventType.SERVICE_CALL_COMPLETED, data);
    }
    
    /**
     * Creates a service call failed event.
     */
    public static SagaEvent serviceCallFailed(String sagaId, String serviceName, String operation, 
                                            String errorMessage, String errorCode) {
        Map<String, Object> data = Map.of(
            "serviceName", serviceName,
            "operation", operation,
            "errorMessage", errorMessage != null ? errorMessage : "Unknown error",
            "errorCode", errorCode != null ? errorCode : "UNKNOWN"
        );
        return new SagaEvent(sagaId, SagaEventType.SERVICE_CALL_FAILED, data);
    }
    
    /**
     * Creates a compensation started event.
     */
    public static SagaEvent compensationStarted(String sagaId, String compensationAction, String reason) {
        Map<String, Object> data = Map.of(
            "compensationAction", compensationAction,
            "reason", reason != null ? reason : "Unknown reason"
        );
        return new SagaEvent(sagaId, SagaEventType.COMPENSATION_STARTED, data);
    }
    
    /**
     * Creates a compensation completed event.
     */
    public static SagaEvent compensationCompleted(String sagaId, String compensationAction, boolean success) {
        Map<String, Object> data = Map.of(
            "compensationAction", compensationAction,
            "success", success
        );
        return new SagaEvent(sagaId, SagaEventType.COMPENSATION_COMPLETED, data);
    }
    
    /**
     * Creates a saga completed event.
     */
    public static SagaEvent sagaCompleted(String sagaId, SagaState finalState, boolean success) {
        Map<String, Object> data = Map.of(
            "finalState", finalState.name(),
            "success", success
        );
        return new SagaEvent(sagaId, SagaEventType.SAGA_COMPLETED, data);
    }
    
    /**
     * Creates an error event.
     */
    public static SagaEvent error(String sagaId, String errorMessage, String errorCode, 
                                 Map<String, Object> errorContext) {
        Map<String, Object> data = Map.of(
            "errorMessage", errorMessage != null ? errorMessage : "Unknown error",
            "errorCode", errorCode != null ? errorCode : "UNKNOWN",
            "errorContext", errorContext != null ? errorContext : Map.of()
        );
        return new SagaEvent(sagaId, SagaEventType.ERROR, data);
    }
    
    /**
     * Serializes the event data to JSON string.
     */
    public String getEventDataAsJson() {
        if (eventData == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Failed to serialize event data\"}";
        }
    }
    
    /**
     * Gets a specific value from the event data.
     */
    public Object getEventDataValue(String key) {
        return eventData != null ? eventData.get(key) : null;
    }
    
    /**
     * Gets a specific value from the event data as a string.
     */
    public String getEventDataValueAsString(String key) {
        Object value = getEventDataValue(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Checks if the event data contains a specific key.
     */
    public boolean hasEventDataKey(String key) {
        return eventData != null && eventData.containsKey(key);
    }
    
    /**
     * Parses JSON string to Map for event data.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJsonData(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(jsonData, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of("rawData", jsonData, "parseError", e.getMessage());
        }
    }
    
    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Getters and setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getSagaId() {
        return sagaId;
    }
    
    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }
    
    public SagaEventType getEventType() {
        return eventType;
    }
    
    public void setEventType(SagaEventType eventType) {
        this.eventType = eventType;
    }
    
    public Map<String, Object> getEventData() {
        return eventData;
    }
    
    public void setEventData(Map<String, Object> eventData) {
        this.eventData = eventData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "SagaEvent{" +
                "eventId='" + eventId + '\'' +
                ", sagaId='" + sagaId + '\'' +
                ", eventType=" + eventType +
                ", createdAt=" + createdAt +
                '}';
    }
}