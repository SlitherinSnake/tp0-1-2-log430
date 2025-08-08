package com.log430.tp7.sagaorchestrator.choreography.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data transfer object for saga statistics and monitoring information.
 */
public class SagaStatistics {
    
    private final Map<String, Long> statusCounts;
    private final Map<String, Long> typeCounts;
    private final SagaPerformanceMetrics performanceMetrics;
    
    public SagaStatistics(List<Object[]> statusCounts, List<Object[]> typeCounts, 
                         List<Object[]> performanceMetrics) {
        this.statusCounts = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        
        this.typeCounts = typeCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        
        this.performanceMetrics = new SagaPerformanceMetrics(performanceMetrics);
    }
    
    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
    
    public Map<String, Long> getTypeCounts() {
        return typeCounts;
    }
    
    public SagaPerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    /**
     * Inner class for performance metrics.
     */
    public static class SagaPerformanceMetrics {
        private final double averageDurationMinutes;
        private final long totalSagas;
        private final long activeSagas;
        private final long completedSagas;
        private final long failedSagas;
        
        public SagaPerformanceMetrics(List<Object[]> metricsData) {
            // Extract metrics from query results
            if (!metricsData.isEmpty()) {
                Object[] row = metricsData.get(0);
                this.averageDurationMinutes = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                this.totalSagas = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                this.activeSagas = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                this.completedSagas = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                this.failedSagas = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            } else {
                this.averageDurationMinutes = 0.0;
                this.totalSagas = 0L;
                this.activeSagas = 0L;
                this.completedSagas = 0L;
                this.failedSagas = 0L;
            }
        }
        
        public double getAverageDurationMinutes() {
            return averageDurationMinutes;
        }
        
        public long getTotalSagas() {
            return totalSagas;
        }
        
        public long getActiveSagas() {
            return activeSagas;
        }
        
        public long getCompletedSagas() {
            return completedSagas;
        }
        
        public long getFailedSagas() {
            return failedSagas;
        }
        
        public double getSuccessRate() {
            if (totalSagas == 0) return 0.0;
            return (double) completedSagas / totalSagas * 100.0;
        }
        
        public double getFailureRate() {
            if (totalSagas == 0) return 0.0;
            return (double) failedSagas / totalSagas * 100.0;
        }
    }
}
