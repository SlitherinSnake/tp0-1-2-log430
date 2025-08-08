package com.log430.tp7.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.log430.tp7.domain.audit.AuditLevel;
import com.log430.tp7.domain.audit.AuditLog;

/**
 * Repository interface for audit log persistence operations.
 * Provides specialized queries for audit trail analysis and compliance reporting.
 */
@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit log by event ID (unique identifier).
     */
    Optional<AuditLog> findByEventId(String eventId);
    
    /**
     * Find all audit logs for a specific aggregate ID.
     */
    List<AuditLog> findByAggregateIdOrderByTimestampDesc(String aggregateId);
    
    /**
     * Find audit logs by event type.
     */
    Page<AuditLog> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);
    
    /**
     * Find audit logs by correlation ID for tracing.
     */
    List<AuditLog> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * Find audit logs by service name.
     */
    Page<AuditLog> findByServiceNameOrderByTimestampDesc(String serviceName, Pageable pageable);
    
    /**
     * Find audit logs by audit level.
     */
    Page<AuditLog> findByAuditLevelOrderByTimestampDesc(AuditLevel auditLevel, Pageable pageable);
    
    /**
     * Find audit logs within a time range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        @Param("startTime") Instant startTime, 
        @Param("endTime") Instant endTime, 
        Pageable pageable);
    
    /**
     * Find audit logs by multiple criteria.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:serviceName IS NULL OR a.serviceName = :serviceName) AND " +
           "(:auditLevel IS NULL OR a.auditLevel = :auditLevel) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCriteria(
        @Param("eventType") String eventType,
        @Param("serviceName") String serviceName,
        @Param("auditLevel") AuditLevel auditLevel,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable);
    
    /**
     * Count audit logs by event type.
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a GROUP BY a.eventType ORDER BY COUNT(a) DESC")
    List<Object[]> countByEventType();
    
    /**
     * Count audit logs by service name.
     */
    @Query("SELECT a.serviceName, COUNT(a) FROM AuditLog a GROUP BY a.serviceName ORDER BY COUNT(a) DESC")
    List<Object[]> countByServiceName();
    
    /**
     * Count audit logs by audit level.
     */
    @Query("SELECT a.auditLevel, COUNT(a) FROM AuditLog a GROUP BY a.auditLevel ORDER BY COUNT(a) DESC")
    List<Object[]> countByAuditLevel();
    
    /**
     * Find critical audit logs for immediate attention.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.auditLevel = 'CRITICAL' AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findCriticalAuditLogsSince(@Param("since") Instant since);
    
    /**
     * Find audit logs with compliance tracking requirements.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.auditLevel IN ('CRITICAL', 'HIGH') ORDER BY a.timestamp DESC")
    Page<AuditLog> findComplianceTrackingLogs(Pageable pageable);
    
    /**
     * Check if event ID already exists to prevent duplicates.
     */
    boolean existsByEventId(String eventId);
    
    /**
     * Delete old audit logs for data retention policies.
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    void deleteOldAuditLogs(@Param("cutoffDate") Instant cutoffDate);
    
    /**
     * Find recent audit logs for dashboard display.
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentAuditLogs(Pageable pageable);
}
