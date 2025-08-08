package com.log430.tp7.infrastructure.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for all domain events in the system.
 * Provides common event metadata like ID, timestamp, and aggregate information.
 */
public abstract class DomainEvent {
    
    @JsonProperty("eventId")
    private final String eventId;
    
    @JsonProperty("eventType")
    private final String eventType;
    
    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;
    
    @JsonProperty("aggregateId")
    private final String aggregateId;
    
    @JsonProperty("aggregateType")
    private final String aggregateType;
    
    @JsonProperty("version")
    private final int version;
    
    @JsonProperty("correlationId")
    private final String correlationId;
    
    @JsonProperty("causationId")
    private final String causationId;
    
    protected DomainEvent(String aggregateId, String aggregateType, int version) {
        this(null, aggregateId, aggregateType, version, null, null);
    }
    
    protected DomainEvent(String eventType, String aggregateId, String aggregateType, int version, String correlationId) {
        this(eventType, aggregateId, aggregateType, version, correlationId, null);
    }
    
    protected DomainEvent(String eventType, String aggregateId, String aggregateType, int version, String correlationId, String causationId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType != null ? eventType : this.getClass().getSimpleName();
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.correlationId = correlationId;
        this.causationId = causationId;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public int getVersion() {
        return version;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public String getCausationId() {
        return causationId;
    }
}
