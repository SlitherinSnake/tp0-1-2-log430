package com.log430.tp7.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Provides common metadata required for event-driven architecture including
 * correlation IDs, causation tracking, and event versioning.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    // Event types will be registered by individual services
})
public abstract class DomainEvent {
    
    @JsonProperty("eventId")
    private final String eventId;
    
    @JsonProperty("eventType")
    private final String eventType;
    
    @JsonProperty("aggregateId")
    private final String aggregateId;
    
    @JsonProperty("aggregateType")
    private final String aggregateType;
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;
    
    @JsonProperty("version")
    private final Integer version;
    
    @JsonProperty("correlationId")
    private final String correlationId;
    
    @JsonProperty("causationId")
    private final String causationId;
    
    @JsonProperty("metadata")
    private final EventMetadata metadata;
    
    protected DomainEvent(String eventType, String aggregateId, String aggregateType, 
                         Integer version, String correlationId, String causationId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.timestamp = Instant.now();
        this.version = version;
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.causationId = causationId;
        this.metadata = new EventMetadata();
    }
    
    protected DomainEvent(String eventType, String aggregateId, String aggregateType, 
                         Integer version, String correlationId) {
        this(eventType, aggregateId, aggregateType, version, correlationId, null);
    }
    
    protected DomainEvent(String eventType, String aggregateId, String aggregateType, 
                         Integer version) {
        this(eventType, aggregateId, aggregateType, version, UUID.randomUUID().toString(), null);
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getAggregateType() { return aggregateType; }
    public Instant getTimestamp() { return timestamp; }
    public Integer getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public EventMetadata getMetadata() { return metadata; }
    
    /**
     * Creates a new event that is caused by this event.
     * The new event will have this event's ID as its causation ID
     * and the same correlation ID for tracing.
     */
    public String createCausationId() {
        return this.eventId;
    }
    
    /**
     * Validates that the event has all required fields.
     */
    public void validate() {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalStateException("Event type cannot be null or empty");
        }
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalStateException("Aggregate ID cannot be null or empty");
        }
        if (aggregateType == null || aggregateType.trim().isEmpty()) {
            throw new IllegalStateException("Aggregate type cannot be null or empty");
        }
        if (version == null || version < 0) {
            throw new IllegalStateException("Version must be a non-negative integer");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId='%s', timestamp=%s, correlationId='%s', causationId='%s'}", 
                           eventType, eventId, aggregateId, timestamp, correlationId, causationId);
    }
}