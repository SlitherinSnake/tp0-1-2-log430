package com.log430.tp7.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.domain.audit.AuditLevel;
import com.log430.tp7.domain.audit.AuditLog;
import com.log430.tp7.event.DomainEvent;
import com.log430.tp7.infrastructure.repository.AuditRepository;

/**
 * Service class for audit log management and compliance operations.
 * Handles audit log creation, querying, and compliance reporting.
 */
@Service
@Transactional
public class AuditService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final ComplianceService complianceService;
    
    @Autowired
    public AuditService(AuditRepository auditRepository, ObjectMapper objectMapper, 
                       ComplianceService complianceService) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
        this.complianceService = complianceService;
    }
    
    /**
     * Creates an audit log entry from a domain event.
     */
    public AuditLog createAuditLog(DomainEvent event, String serviceName) {
        try {
            // Check for duplicate events
            if (auditRepository.existsByEventId(event.getEventId())) {
                log.warn("Duplicate event detected, skipping audit log creation for event: {}", 
                        event.getEventId());
                return auditRepository.findByEventId(event.getEventId()).orElse(null);
            }
            
            // Serialize event data
            String eventData = objectMapper.writeValueAsString(event);
            String metadata = event.getMetadata() != null ? 
                objectMapper.writeValueAsString(event.getMetadata()) : null;
            
            // Determine audit level based on event type
            AuditLevel auditLevel = AuditLevel.fromEventType(event.getEventType());
            
            // Create audit log entry
            AuditLog auditLog = new AuditLog(
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getTimestamp(),
                event.getCorrelationId(),
                event.getCausationId(),
                serviceName,
                eventData,
                metadata,
                auditLevel
            );
            
            // Set business context and compliance tags
            auditLog.setBusinessContextFromEventType();
            addComplianceTags(auditLog, event);
            
            // Save audit log
            AuditLog savedLog = auditRepository.save(auditLog);
            
            log.info("Created audit log for event: {} with level: {}", 
                    event.getEventType(), auditLevel);
            
            // Handle compliance requirements
            if (auditLevel.requiresComplianceTracking()) {
                complianceService.processComplianceEvent(savedLog);
            }
            
            // Handle critical events
            if (auditLevel.requiresImmediateNotification()) {
                complianceService.handleCriticalEvent(savedLog);
            }
            
            return savedLog;
            
        } catch (Exception e) {
            log.error("Failed to create audit log for event: {}", event.getEventId(), e);
            throw new AuditServiceException("Failed to create audit log", e);
        }
    }
    
    /**
     * Retrieves audit logs by correlation ID for tracing.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditTrail(String correlationId) {
        log.debug("Retrieving audit trail for correlation ID: {}", correlationId);
        return auditRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }
    
    /**
     * Retrieves audit logs for a specific aggregate.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAggregateAuditHistory(String aggregateId) {
        log.debug("Retrieving audit history for aggregate: {}", aggregateId);
        return auditRepository.findByAggregateIdOrderByTimestampDesc(aggregateId);
    }
    
    /**
     * Searches audit logs by criteria.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(String eventType, String serviceName, 
                                         AuditLevel auditLevel, Instant startTime, 
                                         Instant endTime, Pageable pageable) {
        log.debug("Searching audit logs with criteria - eventType: {}, serviceName: {}, " +
                 "auditLevel: {}, startTime: {}, endTime: {}", 
                 eventType, serviceName, auditLevel, startTime, endTime);
        
        return auditRepository.findByCriteria(eventType, serviceName, auditLevel, 
                                            startTime, endTime, pageable);
    }
    
    /**
     * Retrieves recent audit logs for dashboard.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getRecentAuditLogs(Pageable pageable) {
        return auditRepository.findRecentAuditLogs(pageable);
    }
    
    /**
     * Retrieves critical audit logs since a specific time.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getCriticalAuditLogs(Instant since) {
        return auditRepository.findCriticalAuditLogsSince(since);
    }
    
    /**
     * Retrieves compliance tracking logs.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getComplianceLogs(Pageable pageable) {
        return auditRepository.findComplianceTrackingLogs(pageable);
    }
    
    /**
     * Gets audit statistics for reporting.
     */
    @Transactional(readOnly = true)
    public AuditStatistics getAuditStatistics() {
        List<Object[]> eventTypeCounts = auditRepository.countByEventType();
        List<Object[]> serviceNameCounts = auditRepository.countByServiceName();
        List<Object[]> auditLevelCounts = auditRepository.countByAuditLevel();
        
        return new AuditStatistics(eventTypeCounts, serviceNameCounts, auditLevelCounts);
    }
    
    /**
     * Retrieves a specific audit log by event ID.
     */
    @Transactional(readOnly = true)
    public Optional<AuditLog> getAuditLogByEventId(String eventId) {
        return auditRepository.findByEventId(eventId);
    }
    
    /**
     * Cleans up old audit logs based on retention policy.
     */
    public void cleanupOldAuditLogs(Instant cutoffDate) {
        log.info("Cleaning up audit logs older than: {}", cutoffDate);
        auditRepository.deleteOldAuditLogs(cutoffDate);
        log.info("Completed cleanup of old audit logs");
    }
    
    /**
     * Adds compliance tags to audit log based on event type and content.
     */
    private void addComplianceTags(AuditLog auditLog, DomainEvent event) {
        String eventType = event.getEventType().toLowerCase();
        
        if (eventType.contains("payment")) {
            auditLog.addComplianceTag("PCI_DSS");
            auditLog.addComplianceTag("FINANCIAL_TRANSACTION");
        }
        
        if (eventType.contains("transaction")) {
            auditLog.addComplianceTag("TRANSACTION_LOG");
            auditLog.addComplianceTag("BUSINESS_CRITICAL");
        }
        
        if (eventType.contains("inventory")) {
            auditLog.addComplianceTag("INVENTORY_CONTROL");
        }
        
        if (eventType.contains("failed") || eventType.contains("error")) {
            auditLog.addComplianceTag("ERROR_TRACKING");
            auditLog.addComplianceTag("INCIDENT_MANAGEMENT");
        }
        
        if (auditLog.getAuditLevel() == AuditLevel.CRITICAL) {
            auditLog.addComplianceTag("CRITICAL_EVENT");
            auditLog.addComplianceTag("IMMEDIATE_ATTENTION");
        }
    }
}
