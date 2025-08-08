package com.log430.tp7.sagaorchestrator.choreography.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * REST controller for choreographed saga monitoring and observability endpoints.
 * Provides dashboard data, health checks, and metrics for saga coordination.
 */
@RestController
@RequestMapping("/api/saga/monitoring")
@CrossOrigin(origins = "*")
public class SagaMonitoringController {
    
    private static final String STATUS_KEY = "status";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String MESSAGE_KEY = "message";
    
    private final SagaMonitoringService monitoringService;
    private final ChoreographedSagaMetrics sagaMetrics;
    private final MeterRegistry meterRegistry;
    
    public SagaMonitoringController(SagaMonitoringService monitoringService,
                                  ChoreographedSagaMetrics sagaMetrics,
                                  MeterRegistry meterRegistry) {
        this.monitoringService = monitoringService;
        this.sagaMetrics = sagaMetrics;
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Get comprehensive dashboard data for saga monitoring.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<SagaMonitoringService.SagaDashboardData> getDashboard() {
        SagaMonitoringService.SagaDashboardData dashboardData = monitoringService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get saga overview statistics.
     */
    @GetMapping("/overview")
    public ResponseEntity<SagaMonitoringService.SagaOverview> getOverview() {
        SagaMonitoringService.SagaOverview overview = monitoringService.getSagaOverview();
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get saga status distribution for charts.
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<Map<ChoreographedSagaStatus, Long>> getStatusDistribution() {
        Map<ChoreographedSagaStatus, Long> distribution = monitoringService.getSagaStatusDistribution();
        return ResponseEntity.ok(distribution);
    }
    
    /**
     * Get recent saga activity timeline.
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<SagaMonitoringService.SagaActivity>> getRecentActivity() {
        List<SagaMonitoringService.SagaActivity> activity = monitoringService.getRecentSagaActivity();
        return ResponseEntity.ok(activity);
    }
    
    /**
     * Get performance metrics for saga execution.
     */
    @GetMapping("/performance")
    public ResponseEntity<SagaMonitoringService.PerformanceMetrics> getPerformanceMetrics() {
        SagaMonitoringService.PerformanceMetrics metrics = monitoringService.getPerformanceMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get compensation-specific metrics.
     */
    @GetMapping("/compensation")
    public ResponseEntity<SagaMonitoringService.CompensationMetrics> getCompensationMetrics() {
        SagaMonitoringService.CompensationMetrics metrics = monitoringService.getCompensationMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get health indicators for system monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<List<SagaMonitoringService.HealthIndicator>> getHealthIndicators() {
        List<SagaMonitoringService.HealthIndicator> indicators = monitoringService.getHealthIndicators();
        return ResponseEntity.ok(indicators);
    }
    
    /**
     * Get health check summary for simple monitoring.
     */
    @GetMapping("/health/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        List<SagaMonitoringService.HealthIndicator> indicators = monitoringService.getHealthIndicators();
        
        boolean isHealthy = indicators.stream()
            .allMatch(indicator -> indicator.status() == SagaMonitoringService.HealthStatus.HEALTHY);
        
        boolean hasWarnings = indicators.stream()
            .anyMatch(indicator -> indicator.status() == SagaMonitoringService.HealthStatus.WARNING);
        
        boolean hasCritical = indicators.stream()
            .anyMatch(indicator -> indicator.status() == SagaMonitoringService.HealthStatus.CRITICAL);
        
        String overallStatus;
        if (hasCritical) {
            overallStatus = "CRITICAL";
        } else if (hasWarnings) {
            overallStatus = "WARNING";
        } else {
            overallStatus = "HEALTHY";
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put(STATUS_KEY, overallStatus);
        summary.put("healthy", isHealthy);
        summary.put("warnings", hasWarnings);
        summary.put("critical", hasCritical);
        summary.put(TIMESTAMP_KEY, System.currentTimeMillis());
        summary.put("indicators", indicators);
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get Prometheus-compatible metrics endpoint.
     */
    @GetMapping(value = "/metrics", produces = "text/plain")
    public ResponseEntity<String> getPrometheusMetrics() {
        // This would typically be handled by the actuator endpoint
        // But we can provide a custom summary here
        StringBuilder metrics = new StringBuilder();
        
        // Add custom saga metrics
        SagaMonitoringService.SagaOverview overview = monitoringService.getSagaOverview();
        
        metrics.append("# HELP saga_total Total number of sagas\n");
        metrics.append("# TYPE saga_total counter\n");
        metrics.append("saga_total ").append(overview.totalSagas()).append("\n");
        
        metrics.append("# HELP saga_active Active sagas count\n");
        metrics.append("# TYPE saga_active gauge\n");
        metrics.append("saga_active ").append(overview.activeSagas()).append("\n");
        
        metrics.append("# HELP saga_success_rate Saga success rate percentage\n");
        metrics.append("# TYPE saga_success_rate gauge\n");
        metrics.append("saga_success_rate ").append(overview.successRate()).append("\n");
        
        metrics.append("# HELP saga_failure_rate Saga failure rate percentage\n");
        metrics.append("# TYPE saga_failure_rate gauge\n");
        metrics.append("saga_failure_rate ").append(overview.failureRate()).append("\n");
        
        // Add compensation metrics
        SagaMonitoringService.CompensationMetrics compensation = monitoringService.getCompensationMetrics();
        
        metrics.append("# HELP compensation_pending Pending compensation actions\n");
        metrics.append("# TYPE compensation_pending gauge\n");
        metrics.append("compensation_pending ").append(compensation.pendingActions()).append("\n");
        
        metrics.append("# HELP compensation_success_rate Compensation success rate percentage\n");
        metrics.append("# TYPE compensation_success_rate gauge\n");
        metrics.append("compensation_success_rate ").append(compensation.compensationSuccessRate()).append("\n");
        
        return ResponseEntity.ok(metrics.toString());
    }
    
    /**
     * Trigger manual metrics collection.
     */
    @PostMapping("/collect-metrics")
    public ResponseEntity<Map<String, String>> collectMetrics() {
        try {
            // Trigger metrics updates
            sagaMetrics.updateGauges();
            
            Map<String, String> response = new HashMap<>();
            response.put(STATUS_KEY, "success");
            response.put(MESSAGE_KEY, "Metrics collection triggered successfully");
            response.put(TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put(STATUS_KEY, "error");
            response.put(MESSAGE_KEY, "Failed to trigger metrics collection: " + e.getMessage());
            response.put(TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get system information for debugging.
     */
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // JVM metrics
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("memory_total", runtime.totalMemory());
        systemInfo.put("memory_free", runtime.freeMemory());
        systemInfo.put("memory_used", runtime.totalMemory() - runtime.freeMemory());
        systemInfo.put("memory_max", runtime.maxMemory());
        
        // Application info
        systemInfo.put("active_threads", Thread.activeCount());
        systemInfo.put(TIMESTAMP_KEY, System.currentTimeMillis());
        
        // Meter registry metrics count
        systemInfo.put("registered_meters", meterRegistry.getMeters().size());
        
        return ResponseEntity.ok(systemInfo);
    }
}
