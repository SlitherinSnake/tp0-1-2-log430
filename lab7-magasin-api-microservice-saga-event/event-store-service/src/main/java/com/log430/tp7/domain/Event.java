package com.log430.tp7.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "events", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id", "event_version"}),
       indexes = {
           @Index(name = "idx_events_aggregate", columnList = "aggregate_id"),
           @Index(name = "idx_events_type", columnList = "event_type"),
           @Index(name = "idx_events_timestamp", columnList = "timestamp"),
           @Index(name = "idx_events_correlation", columnList = "correlation_id")
       })
public class Event {
    
    @Id
    @Column(name = "event_id", columnDefinition = "UUID")
    private UUID eventId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;
    
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;
    
    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> eventData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private Instant timestamp;
    
    @Column(name = "correlation_id", columnDefinition = "UUID")
    private UUID correlationId;
    
    @Column(name = "causation_id", columnDefinition = "UUID")
    private UUID causationId;
    
    // Default constructor
    public Event() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
    }
    
    // Constructor with required fields
    public Event(String eventType, String aggregateId, String aggregateType, 
                 Integer eventVersion, Map<String, Object> eventData) {
        this();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventVersion = eventVersion;
        this.eventData = eventData;
    }
    
    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }
    
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }
    
    public Integer getEventVersion() {
        return eventVersion;
    }
    
    public void setEventVersion(Integer eventVersion) {
        this.eventVersion = eventVersion;
    }
    
    public Map<String, Object> getEventData() {
        return eventData;
    }
    
    public void setEventData(Map<String, Object> eventData) {
        this.eventData = eventData;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public UUID getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }
    
    public UUID getCausationId() {
        return causationId;
    }
    
    public void setCausationId(UUID causationId) {
        this.causationId = causationId;
    }
}