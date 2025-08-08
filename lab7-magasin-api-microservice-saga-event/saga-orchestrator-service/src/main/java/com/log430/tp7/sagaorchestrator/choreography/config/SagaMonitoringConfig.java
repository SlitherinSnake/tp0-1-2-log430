package com.log430.tp7.sagaorchestrator.choreography.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.log430.tp7.sagaorchestrator.choreography.compensation.CompensationCoordinator;
import com.log430.tp7.sagaorchestrator.choreography.monitoring.ChoreographedSagaMetrics;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Configuration for choreographed saga monitoring and observability.
 * Sets up Micrometer metrics, Prometheus endpoint, and monitoring components.
 */
@Configuration
@EnableScheduling
public class SagaMonitoringConfig {
    
    /**
     * Configure saga metrics component.
     */
    @Bean
    public ChoreographedSagaMetrics choreographedSagaMetrics(
            MeterRegistry meterRegistry,
            ChoreographedSagaRepository sagaRepository,
            CompensationCoordinator compensationCoordinator) {
        return new ChoreographedSagaMetrics(meterRegistry, sagaRepository, compensationCoordinator);
    }
}
