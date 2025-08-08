package com.log430.tp7.sagaorchestrator.choreography.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationCoordinator;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Metrics collector for choreographed saga monitoring and observability.
 * Provides Prometheus-compatible metrics for saga performance and health tracking.
 */
@Component
public class ChoreographedSagaMetrics {
    
    private final ChoreographedSagaRepository sagaRepository;
    private final CompensationCoordinator compensationCoordinator;
    
    // Counters for saga lifecycle events
    private final Counter sagaInitiatedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaFailedCounter;
    private final Counter sagaTimedOutCounter;
    private final Counter sagaCompensatedCounter;
    
    // Counters for step operations
    private final Counter stepCompletedCounter;
    private final Counter stepFailedCounter;
    private final Counter compensationTriggeredCounter;
    private final Counter compensationSuccessCounter;
    private final Counter compensationFailureCounter;
    
    // Timers for performance tracking
    private final Timer sagaDurationTimer;
    private final Timer stepExecutionTimer;
    private final Timer compensationExecutionTimer;
    
    // Gauges for current state
    private final AtomicLong activeSagasGauge = new AtomicLong(0);
    private final AtomicLong pendingCompensationsGauge = new AtomicLong(0);
    
    public ChoreographedSagaMetrics(MeterRegistry meterRegistry, 
                                  ChoreographedSagaRepository sagaRepository,
                                  CompensationCoordinator compensationCoordinator) {
        this.sagaRepository = sagaRepository;
        this.compensationCoordinator = compensationCoordinator;
        
        // Initialize counters
        this.sagaInitiatedCounter = Counter.builder("choreographed_saga_initiated_total")
            .description("Total number of choreographed sagas initiated")
            .register(meterRegistry);
        
        this.sagaCompletedCounter = Counter.builder("choreographed_saga_completed_total")
            .description("Total number of choreographed sagas completed successfully")
            .register(meterRegistry);
        
        this.sagaFailedCounter = Counter.builder("choreographed_saga_failed_total")
            .description("Total number of choreographed sagas that failed")
            .register(meterRegistry);
        
        this.sagaTimedOutCounter = Counter.builder("choreographed_saga_timeout_total")
            .description("Total number of choreographed sagas that timed out")
            .register(meterRegistry);
        
        this.sagaCompensatedCounter = Counter.builder("choreographed_saga_compensated_total")
            .description("Total number of choreographed sagas that were compensated")
            .register(meterRegistry);
        
        this.stepCompletedCounter = Counter.builder("choreographed_saga_step_completed_total")
            .description("Total number of saga steps completed")
            .register(meterRegistry);
        
        this.stepFailedCounter = Counter.builder("choreographed_saga_step_failed_total")
            .description("Total number of saga steps that failed")
            .register(meterRegistry);
        
        this.compensationTriggeredCounter = Counter.builder("choreographed_saga_compensation_triggered_total")
            .description("Total number of compensations triggered")
            .register(meterRegistry);
        
        this.compensationSuccessCounter = Counter.builder("choreographed_saga_compensation_success_total")
            .description("Total number of successful compensations")
            .register(meterRegistry);
        
        this.compensationFailureCounter = Counter.builder("choreographed_saga_compensation_failure_total")
            .description("Total number of failed compensations")
            .register(meterRegistry);
        
        // Initialize timers
        this.sagaDurationTimer = Timer.builder("choreographed_saga_duration")
            .description("Duration of choreographed saga execution")
            .register(meterRegistry);
        
        this.stepExecutionTimer = Timer.builder("choreographed_saga_step_duration")
            .description("Duration of individual saga step execution")
            .register(meterRegistry);
        
        this.compensationExecutionTimer = Timer.builder("choreographed_saga_compensation_duration")
            .description("Duration of compensation action execution")
            .register(meterRegistry);
        
        // Initialize gauges with proper syntax
        Gauge.builder("choreographed_saga_active_count", this, ChoreographedSagaMetrics::getActiveSagasCount)
            .description("Number of currently active choreographed sagas")
            .register(meterRegistry);
        
        Gauge.builder("choreographed_saga_pending_compensations_count", this, ChoreographedSagaMetrics::getPendingCompensationsCount)
            .description("Number of pending compensation actions")
            .register(meterRegistry);
        
        // Status-specific gauges
        for (ChoreographedSagaStatus status : ChoreographedSagaStatus.values()) {
            Gauge.builder("choreographed_saga_status_count", status, this::getSagaCountByStatus)
                .description("Number of sagas by status")
                .tag("status", status.name())
                .register(meterRegistry);
        }
    }
    
    // Counter increment methods
    
    public void incrementSagaInitiated() {
        sagaInitiatedCounter.increment();
        activeSagasGauge.incrementAndGet();
    }
    
    public void incrementSagaCompleted() {
        sagaCompletedCounter.increment();
        activeSagasGauge.decrementAndGet();
    }
    
    public void incrementSagaFailed() {
        sagaFailedCounter.increment();
        activeSagasGauge.decrementAndGet();
    }
    
    public void incrementSagaTimedOut() {
        sagaTimedOutCounter.increment();
    }
    
    public void incrementSagaCompensated() {
        sagaCompensatedCounter.increment();
        activeSagasGauge.decrementAndGet();
    }
    
    public void incrementStepCompleted() {
        stepCompletedCounter.increment();
    }
    
    public void incrementStepFailed() {
        stepFailedCounter.increment();
    }
    
    public void incrementCompensationTriggered() {
        compensationTriggeredCounter.increment();
    }
    
    public void incrementCompensationSuccess() {
        compensationSuccessCounter.increment();
    }
    
    public void incrementCompensationFailure() {
        compensationFailureCounter.increment();
    }
    
    // Timer recording methods
    
    public Timer.Sample startSagaDurationTimer() {
        return Timer.start();
    }
    
    public void recordSagaDuration(Timer.Sample sample) {
        sample.stop(sagaDurationTimer);
    }
    
    public Timer.Sample startStepExecutionTimer() {
        return Timer.start();
    }
    
    public void recordStepExecutionDuration(Timer.Sample sample) {
        sample.stop(stepExecutionTimer);
    }
    
    public Timer.Sample startCompensationExecutionTimer() {
        return Timer.start();
    }
    
    public void recordCompensationExecutionDuration(Timer.Sample sample) {
        sample.stop(compensationExecutionTimer);
    }
    
    // Gauge value providers
    
    private double getActiveSagasCount() {
        try {
            return sagaRepository.findActiveSagas().size();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private double getPendingCompensationsCount() {
        try {
            return compensationCoordinator.getPendingActionsCount();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private double getSagaCountByStatus(ChoreographedSagaStatus status) {
        try {
            return sagaRepository.countByStatus(status);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    // Manual gauge updates (for cases where automatic updates aren't sufficient)
    
    public void updateActiveSagasGauge(long count) {
        activeSagasGauge.set(count);
    }
    
    public void updatePendingCompensationsGauge(long count) {
        pendingCompensationsGauge.set(count);
    }
    
    /**
     * Manually update all gauge metrics.
     * This can be called periodically or on-demand for monitoring.
     */
    public void updateGauges() {
        // Gauges are automatically updated when accessed
        // But we can trigger updates by reading the values
        activeSagasGauge.get();
        pendingCompensationsGauge.get();
        // Other gauges are function-based and update automatically
    }
}
