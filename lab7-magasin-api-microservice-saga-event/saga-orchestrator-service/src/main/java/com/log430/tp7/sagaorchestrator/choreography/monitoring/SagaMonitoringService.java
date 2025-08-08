package com.log430.tp7.sagaorchestrator.choreography.monitoring;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import org.springframework.stereotype.Service;

import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationCoordinator;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;

/**
 * Service for providing choreographed saga monitoring dashboard data.
 * Aggregates saga statistics and health metrics for visualization.
 */
@Service
public class SagaMonitoringService {
    
    private final ChoreographedSagaRepository sagaRepository;
    private final CompensationCoordinator compensationCoordinator;
    
    public SagaMonitoringService(ChoreographedSagaRepository sagaRepository,
                               CompensationCoordinator compensationCoordinator) {
        this.sagaRepository = sagaRepository;
        this.compensationCoordinator = compensationCoordinator;
    }
    
    /**
     * Gets comprehensive saga dashboard data.
     */
    public SagaDashboardData getDashboardData() {
        return new SagaDashboardData(
            getSagaOverview(),
            getSagaStatusDistribution(),
            getRecentSagaActivity(),
            getPerformanceMetrics(),
            getCompensationMetrics(),
            getHealthIndicators()
        );
    }
    
    /**
     * Gets high-level saga overview statistics.
     */
    public SagaOverview getSagaOverview() {
        List<ChoreographedSagaState> allSagas = sagaRepository.findAll();
        
        long totalSagas = allSagas.size();
        long activeSagas = allSagas.stream()
            .filter(saga -> saga.getStatus().isActive())
            .count();
        
        long completedSagas = sagaRepository.countByStatus(ChoreographedSagaStatus.COMPLETED);
        long failedSagas = sagaRepository.countByStatus(ChoreographedSagaStatus.FAILED);
        long compensatedSagas = sagaRepository.countByStatus(ChoreographedSagaStatus.COMPENSATED);
        
        double successRate = totalSagas > 0 ? (double) completedSagas / totalSagas * 100 : 0.0;
        double failureRate = totalSagas > 0 ? (double) failedSagas / totalSagas * 100 : 0.0;
        
        return new SagaOverview(
            totalSagas,
            activeSagas,
            completedSagas,
            failedSagas,
            compensatedSagas,
            successRate,
            failureRate
        );
    }
    
    /**
     * Gets saga status distribution for pie charts.
     */
    public Map<ChoreographedSagaStatus, Long> getSagaStatusDistribution() {
        Map<ChoreographedSagaStatus, Long> distribution = new EnumMap<>(ChoreographedSagaStatus.class);
        
        for (ChoreographedSagaStatus status : ChoreographedSagaStatus.values()) {
            long count = sagaRepository.countByStatus(status);
            if (count > 0) {
                distribution.put(status, count);
            }
        }
        
        return distribution;
    }
    
    /**
     * Gets recent saga activity for timeline visualization.
     */
    public List<SagaActivity> getRecentSagaActivity() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<ChoreographedSagaState> recentSagas = sagaRepository.findByCreatedAtAfter(since);
        
        return recentSagas.stream()
            .map(saga -> new SagaActivity(
                saga.getSagaId(),
                saga.getCorrelationId(),
                saga.getSagaType(),
                saga.getStatus(),
                saga.getCreatedAt(),
                saga.getCompletedAt(),
                calculateDuration(saga)
            ))
            .sorted(Comparator.comparing(SagaActivity::timestamp).reversed())
            .limit(50)
            .toList();
    }
    
    /**
     * Gets performance metrics for monitoring.
     */
    public PerformanceMetrics getPerformanceMetrics() {
        List<ChoreographedSagaState> completedSagas = sagaRepository.findByStatus(ChoreographedSagaStatus.COMPLETED);
        List<ChoreographedSagaState> failedSagas = sagaRepository.findByStatus(ChoreographedSagaStatus.FAILED);
        
        OptionalDouble avgDuration = completedSagas.stream()
            .filter(saga -> saga.getCompletedAt() != null)
            .mapToLong(this::calculateDuration)
            .average();
        
        OptionalLong maxDuration = completedSagas.stream()
            .filter(saga -> saga.getCompletedAt() != null)
            .mapToLong(this::calculateDuration)
            .max();
        
        OptionalLong minDuration = completedSagas.stream()
            .filter(saga -> saga.getCompletedAt() != null)
            .mapToLong(this::calculateDuration)
            .min();
        
        // Calculate throughput (sagas per hour in last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long sagasLast24Hours = sagaRepository.findByCreatedAtAfter(last24Hours).size();
        double throughputPerHour = sagasLast24Hours / 24.0;
        
        return new PerformanceMetrics(
            avgDuration.orElse(0.0),
            maxDuration.orElse(0L),
            minDuration.orElse(0L),
            throughputPerHour,
            completedSagas.size(),
            failedSagas.size()
        );
    }
    
    /**
     * Gets compensation-specific metrics.
     */
    public CompensationMetrics getCompensationMetrics() {
        int pendingCompensations = compensationCoordinator.getPendingActionsCount();
        long sagasRequiringCompensation = sagaRepository.findSagasRequiringCompensation().size();
        long compensatedSagas = sagaRepository.countByStatus(ChoreographedSagaStatus.COMPENSATED);
        
        // Calculate compensation success rate
        long totalCompensations = compensatedSagas + sagasRequiringCompensation;
        double compensationSuccessRate = totalCompensations > 0 ? 
            (double) compensatedSagas / totalCompensations * 100 : 0.0;
        
        return new CompensationMetrics(
            pendingCompensations,
            sagasRequiringCompensation,
            compensatedSagas,
            compensationSuccessRate
        );
    }
    
    /**
     * Gets health indicators for system monitoring.
     */
    public List<HealthIndicator> getHealthIndicators() {
        List<HealthIndicator> indicators = new ArrayList<>();
        
        // Check for stuck sagas (active for more than 2 hours)
        LocalDateTime stuckThreshold = LocalDateTime.now().minusHours(2);
        List<ChoreographedSagaState> activeSagas = sagaRepository.findActiveSagas();
        long stuckSagas = activeSagas.stream()
            .filter(saga -> saga.getCreatedAt().isBefore(stuckThreshold))
            .count();
        
        HealthStatus stuckSagasStatus;
        if (stuckSagas == 0) {
            stuckSagasStatus = HealthStatus.HEALTHY;
        } else if (stuckSagas < 5) {
            stuckSagasStatus = HealthStatus.WARNING;
        } else {
            stuckSagasStatus = HealthStatus.CRITICAL;
        }
        
        indicators.add(new HealthIndicator(
            "stuck_sagas",
            "Stuck Sagas",
            stuckSagas,
            stuckSagasStatus,
            "Sagas active for more than 2 hours"
        ));
        
        // Check compensation backlog
        int compensationBacklog = compensationCoordinator.getPendingActionsCount();
        HealthStatus compensationBacklogStatus;
        if (compensationBacklog == 0) {
            compensationBacklogStatus = HealthStatus.HEALTHY;
        } else if (compensationBacklog < 10) {
            compensationBacklogStatus = HealthStatus.WARNING;
        } else {
            compensationBacklogStatus = HealthStatus.CRITICAL;
        }
        
        indicators.add(new HealthIndicator(
            "compensation_backlog",
            "Compensation Backlog",
            compensationBacklog,
            compensationBacklogStatus,
            "Pending compensation actions"
        ));
        
        // Check recent failure rate
        LocalDateTime recentThreshold = LocalDateTime.now().minusHours(1);
        List<ChoreographedSagaState> recentSagas = sagaRepository.findByCreatedAtAfter(recentThreshold);
        long recentFailures = recentSagas.stream()
            .filter(saga -> saga.getStatus() == ChoreographedSagaStatus.FAILED)
            .count();
        
        double recentFailureRate = !recentSagas.isEmpty() ? 
            (double) recentFailures / recentSagas.size() * 100 : 0.0;
        
        HealthStatus recentFailureStatus;
        if (recentFailureRate < 5) {
            recentFailureStatus = HealthStatus.HEALTHY;
        } else if (recentFailureRate < 15) {
            recentFailureStatus = HealthStatus.WARNING;
        } else {
            recentFailureStatus = HealthStatus.CRITICAL;
        }
        
        indicators.add(new HealthIndicator(
            "recent_failure_rate",
            "Recent Failure Rate",
            (long) recentFailureRate,
            recentFailureStatus,
            "Failure rate in last hour (%)"
        ));
        
        return indicators;
    }
    
    /**
     * Calculates duration between saga creation and completion.
     */
    private long calculateDuration(ChoreographedSagaState saga) {
        if (saga.getCompletedAt() == null) {
            return ChronoUnit.MINUTES.between(saga.getCreatedAt(), LocalDateTime.now());
        }
        return ChronoUnit.MINUTES.between(saga.getCreatedAt(), saga.getCompletedAt());
    }
    
    // Data classes for monitoring
    
    public record SagaDashboardData(
        SagaOverview overview,
        Map<ChoreographedSagaStatus, Long> statusDistribution,
        List<SagaActivity> recentActivity,
        PerformanceMetrics performance,
        CompensationMetrics compensation,
        List<HealthIndicator> health
    ) {}
    
    public record SagaOverview(
        long totalSagas,
        long activeSagas,
        long completedSagas,
        long failedSagas,
        long compensatedSagas,
        double successRate,
        double failureRate
    ) {}
    
    public record SagaActivity(
        String sagaId,
        String correlationId,
        String sagaType,
        ChoreographedSagaStatus status,
        LocalDateTime timestamp,
        LocalDateTime completedAt,
        long durationMinutes
    ) {}
    
    public record PerformanceMetrics(
        double averageDurationMinutes,
        long maxDurationMinutes,
        long minDurationMinutes,
        double throughputPerHour,
        int completedCount,
        int failedCount
    ) {}
    
    public record CompensationMetrics(
        int pendingActions,
        long sagasRequiringCompensation,
        long compensatedSagas,
        double compensationSuccessRate
    ) {}
    
    public record HealthIndicator(
        String id,
        String name,
        long value,
        HealthStatus status,
        String description
    ) {}
    
    public enum HealthStatus {
        HEALTHY, WARNING, CRITICAL
    }
}
