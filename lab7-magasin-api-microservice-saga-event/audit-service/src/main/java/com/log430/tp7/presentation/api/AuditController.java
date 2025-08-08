package com.log430.tp7.presentation.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.application.service.AuditService;
import com.log430.tp7.application.service.AuditStatistics;
import com.log430.tp7.domain.audit.AuditLevel;
import com.log430.tp7.domain.audit.AuditLog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for audit log queries and compliance reporting.
 * Provides endpoints for retrieving audit trails, compliance logs, and statistics.
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Service", description = "Audit log queries and compliance reporting")
public class AuditController {
    
    private final AuditService auditService;
    
    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @GetMapping("/trail/{correlationId}")
    @Operation(summary = "Get audit trail by correlation ID", 
               description = "Retrieves complete audit trail for a specific correlation ID")
    public ResponseEntity<List<AuditLog>> getAuditTrail(
            @Parameter(description = "Correlation ID to trace") 
            @PathVariable String correlationId) {
        List<AuditLog> auditTrail = auditService.getAuditTrail(correlationId);
        return ResponseEntity.ok(auditTrail);
    }
    
    @GetMapping("/aggregate/{aggregateId}")
    @Operation(summary = "Get audit history for aggregate", 
               description = "Retrieves audit history for a specific business aggregate")
    public ResponseEntity<List<AuditLog>> getAggregateHistory(
            @Parameter(description = "Aggregate ID") 
            @PathVariable String aggregateId) {
        List<AuditLog> history = auditService.getAggregateAuditHistory(aggregateId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get audit log by event ID", 
               description = "Retrieves specific audit log entry by event ID")
    public ResponseEntity<AuditLog> getAuditLogByEventId(
            @Parameter(description = "Event ID") 
            @PathVariable String eventId) {
        Optional<AuditLog> auditLog = auditService.getAuditLogByEventId(eventId);
        return auditLog.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search audit logs", 
               description = "Search audit logs with multiple criteria")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @Parameter(description = "Event type filter") 
            @RequestParam(required = false) String eventType,
            @Parameter(description = "Service name filter") 
            @RequestParam(required = false) String serviceName,
            @Parameter(description = "Audit level filter") 
            @RequestParam(required = false) AuditLevel auditLevel,
            @Parameter(description = "Start time (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "End time (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> results = auditService.searchAuditLogs(
                eventType, serviceName, auditLevel, startTime, endTime, pageable);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent audit logs", 
               description = "Retrieves most recent audit log entries")
    public ResponseEntity<Page<AuditLog>> getRecentAuditLogs(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> recentLogs = auditService.getRecentAuditLogs(pageable);
        return ResponseEntity.ok(recentLogs);
    }
    
    @GetMapping("/critical")
    @Operation(summary = "Get critical audit logs", 
               description = "Retrieves critical audit logs since specified time")
    public ResponseEntity<List<AuditLog>> getCriticalAuditLogs(
            @Parameter(description = "Since time (ISO format), defaults to last 24 hours") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        
        if (since == null) {
            since = Instant.now().minus(24, ChronoUnit.HOURS);
        }
        
        List<AuditLog> criticalLogs = auditService.getCriticalAuditLogs(since);
        return ResponseEntity.ok(criticalLogs);
    }
    
    @GetMapping("/compliance")
    @Operation(summary = "Get compliance tracking logs", 
               description = "Retrieves audit logs that require compliance tracking")
    public ResponseEntity<Page<AuditLog>> getComplianceLogs(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> complianceLogs = auditService.getComplianceLogs(pageable);
        return ResponseEntity.ok(complianceLogs);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics", 
               description = "Retrieves audit statistics for reporting and monitoring")
    public ResponseEntity<AuditStatistics> getAuditStatistics() {
        AuditStatistics statistics = auditService.getAuditStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", 
               description = "Returns health status of audit service")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Audit Service is healthy");
    }
}
