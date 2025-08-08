package com.log430.tp7.domain.audit;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Audit log entry entity for tracking all business events in the system.
 * Provides comprehensive audit trail for compliance and forensic analysis.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_event_type", columnList = "eventType"),
    @Index(name = "idx_audit_aggregate_id", columnList = "aggregateId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_correlation_id", columnList = "correlationId"),
    @Index(name = "idx_audit_service_name", columnList = "serviceName")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;
    
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Column(name = "causation_id", length = 36)
    private String causationId;
    
    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
    
    @Column(name = "processed_by", nullable = false, length = 50)
    private String processedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "audit_level", nullable = false)
    private AuditLevel auditLevel;
    
    @Column(name = "business_context", columnDefinition = "TEXT")
    private String businessContext;
    
    @Column(name = "compliance_tags", length = 500)
    private String complianceTags;
    
    // Constructors
    public AuditLog() {
        this.processedAt = Instant.now();
        this.processedBy = "audit-service";
    }
    
    public AuditLog(String eventId, String eventType, String aggregateId, String aggregateType,
                   Instant timestamp, String correlationId, String causationId, String serviceName,
                   String eventData, String metadata, AuditLevel auditLevel) {
        this();
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.serviceName = serviceName;
        this.eventData = eventData;
        this.metadata = metadata;
        this.auditLevel = auditLevel;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    
    public AuditLevel getAuditLevel() { return auditLevel; }
    public void setAuditLevel(AuditLevel auditLevel) { this.auditLevel = auditLevel; }
    
    public String getBusinessContext() { return businessContext; }
    public void setBusinessContext(String businessContext) { this.businessContext = businessContext; }
    
    public String getComplianceTags() { return complianceTags; }
    public void setComplianceTags(String complianceTags) { this.complianceTags = complianceTags; }
    
    // Helper methods
    public void addComplianceTag(String tag) {
        if (this.complianceTags == null || this.complianceTags.trim().isEmpty()) {
            this.complianceTags = tag;
        } else {
            this.complianceTags += "," + tag;
        }
    }
    
    public void setBusinessContextFromEventType() {
        if (eventType != null) {
            if (eventType.contains("Transaction")) {
                this.businessContext = "E-COMMERCE_TRANSACTION";
            } else if (eventType.contains("Payment")) {
                this.businessContext = "PAYMENT_PROCESSING";
            } else if (eventType.contains("Inventory")) {
                this.businessContext = "INVENTORY_MANAGEMENT";
            } else if (eventType.contains("Order") || eventType.contains("Store")) {
                this.businessContext = "ORDER_FULFILLMENT";
            } else {
                this.businessContext = "GENERAL_BUSINESS_EVENT";
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(eventId, auditLog.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
    
    @Override
    public String toString() {
        return String.format("AuditLog{id=%d, eventId='%s', eventType='%s', aggregateId='%s', " +
                           "timestamp=%s, correlationId='%s', serviceName='%s', auditLevel=%s}",
                           id, eventId, eventType, aggregateId, timestamp, correlationId, 
                           serviceName, auditLevel);
    }
}
