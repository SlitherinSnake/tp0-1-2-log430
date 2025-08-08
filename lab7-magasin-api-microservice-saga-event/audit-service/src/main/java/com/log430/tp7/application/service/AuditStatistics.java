package com.log430.tp7.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data class containing audit statistics for reporting and monitoring.
 */
public class AuditStatistics {
    
    private final Map<String, Long> eventTypeCounts;
    private final Map<String, Long> serviceNameCounts;
    private final Map<String, Long> auditLevelCounts;
    
    public AuditStatistics(List<Object[]> eventTypeCounts, 
                          List<Object[]> serviceNameCounts, 
                          List<Object[]> auditLevelCounts) {
        this.eventTypeCounts = convertToMap(eventTypeCounts);
        this.serviceNameCounts = convertToMap(serviceNameCounts);
        this.auditLevelCounts = convertToMap(auditLevelCounts);
    }
    
    private Map<String, Long> convertToMap(List<Object[]> counts) {
        return counts.stream()
            .collect(Collectors.toMap(
                arr -> arr[0].toString(),
                arr -> ((Number) arr[1]).longValue()
            ));
    }
    
    public Map<String, Long> getEventTypeCounts() {
        return eventTypeCounts;
    }
    
    public Map<String, Long> getServiceNameCounts() {
        return serviceNameCounts;
    }
    
    public Map<String, Long> getAuditLevelCounts() {
        return auditLevelCounts;
    }
    
    public long getTotalEventCount() {
        return eventTypeCounts.values().stream().mapToLong(Long::longValue).sum();
    }
    
    public long getCriticalEventCount() {
        return auditLevelCounts.getOrDefault("CRITICAL", 0L);
    }
    
    public long getHighPriorityEventCount() {
        return auditLevelCounts.getOrDefault("HIGH", 0L);
    }
}
