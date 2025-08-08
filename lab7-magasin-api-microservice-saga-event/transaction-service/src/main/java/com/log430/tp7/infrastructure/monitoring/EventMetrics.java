package com.log430.tp7.infrastructure.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Event metrics component for Transaction Service observability.
 * Provides Prometheus-compatible metrics for event-driven operations.
 */
@Component
public class EventMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Event Publishing Metrics
    private final Counter eventsPublishedCounter;
    private final Counter eventPublishFailuresCounter;
    private final Timer eventPublishLatencyTimer;
    
    // Event Consumption Metrics
    private final Counter eventsConsumedCounter;
    private final Counter eventConsumptionFailuresCounter;
    
    // Business Event Metrics
    private final Counter transactionCreatedCounter;
    private final Counter transactionCompletedCounter;
    private final Counter transactionCancelledCounter;
    
    // Saga Coordination Metrics
    private final Counter paymentProcessedEventsCounter;
    private final Counter paymentFailedEventsCounter;
    private final Counter paymentRefundedEventsCounter;
    
    // System State Metrics
    private final AtomicLong activeTransactionsGauge = new AtomicLong(0);
    private final AtomicLong pendingTransactionsGauge = new AtomicLong(0);
    private final AtomicLong failedTransactionsGauge = new AtomicLong(0);
    
    // Performance Metrics
    private final Timer transactionProcessingTimer;
    
    public EventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize event publishing metrics
        this.eventsPublishedCounter = Counter.builder("events_published_total")
            .description("Total number of events published by transaction service")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.eventPublishFailuresCounter = Counter.builder("event_publish_failures_total")
            .description("Total number of event publishing failures")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.eventPublishLatencyTimer = Timer.builder("event_publish_latency_seconds")
            .description("Latency of event publishing operations")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        // Initialize event consumption metrics
        this.eventsConsumedCounter = Counter.builder("events_consumed_total")
            .description("Total number of events consumed by transaction service")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.eventConsumptionFailuresCounter = Counter.builder("event_consumption_failures_total")
            .description("Total number of event consumption failures")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        // Initialize business event metrics
        this.transactionCreatedCounter = Counter.builder("transaction_events_total")
            .description("Total number of transaction events")
            .tag("event_type", "TransactionCreated")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.transactionCompletedCounter = Counter.builder("transaction_events_total")
            .description("Total number of transaction events")
            .tag("event_type", "TransactionCompleted")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.transactionCancelledCounter = Counter.builder("transaction_events_total")
            .description("Total number of transaction events")
            .tag("event_type", "TransactionCancelled")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        // Initialize saga coordination metrics
        this.paymentProcessedEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "PaymentProcessed")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.paymentFailedEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "PaymentFailed")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        this.paymentRefundedEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "PaymentRefunded")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        // Initialize performance metrics
        this.transactionProcessingTimer = Timer.builder("transaction_processing_duration_seconds")
            .description("Duration of transaction processing operations")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("active_transactions_count", activeTransactionsGauge, AtomicLong::doubleValue)
            .description("Current number of active transactions")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        Gauge.builder("pending_transactions_count", pendingTransactionsGauge, AtomicLong::doubleValue)
            .description("Current number of pending transactions")
            .tag("service", "transaction-service")
            .register(meterRegistry);
        
        Gauge.builder("failed_transactions_count", failedTransactionsGauge, AtomicLong::doubleValue)
            .description("Current number of failed transactions")
            .tag("service", "transaction-service")
            .register(meterRegistry);
    }
    
    // Event Publishing Metrics Methods
    public void recordEventPublished(String eventType) {
        Counter.builder("events_published_by_type_total")
            .tag("event_type", eventType)
            .tag("service", "transaction-service")
            .register(meterRegistry)
            .increment();
        eventsPublishedCounter.increment();
    }
    
    public void recordEventPublishFailure(String eventType, String errorType) {
        Counter.builder("event_publish_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "transaction-service")
            .register(meterRegistry)
            .increment();
        eventPublishFailuresCounter.increment();
    }
    
    public Timer.Sample startEventPublishTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordEventPublishLatency(Timer.Sample sample) {
        sample.stop(eventPublishLatencyTimer);
    }
    
    // Event Consumption Metrics Methods
    public void recordEventConsumed(String eventType) {
        Counter.builder("events_consumed_by_type_total")
            .tag("event_type", eventType)
            .tag("service", "transaction-service")
            .register(meterRegistry)
            .increment();
        eventsConsumedCounter.increment();
    }
    
    public void recordEventConsumptionFailure(String eventType, String errorType) {
        Counter.builder("event_consumption_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "transaction-service")
            .register(meterRegistry)
            .increment();
        eventConsumptionFailuresCounter.increment();
    }
    
    public Timer.Sample startEventProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordEventProcessingLatency(Timer.Sample sample, String eventType) {
        sample.stop(Timer.builder("event_processing_latency_seconds")
            .tag("event_type", eventType)
            .tag("service", "transaction-service")
            .register(meterRegistry));
    }
    
    // Business Event Metrics Methods
    public void recordTransactionCreated() {
        transactionCreatedCounter.increment();
    }
    
    public void recordTransactionCompleted() {
        transactionCompletedCounter.increment();
    }
    
    public void recordTransactionCancelled() {
        transactionCancelledCounter.increment();
    }
    
    // Saga Coordination Metrics Methods
    public void recordPaymentProcessedEvent() {
        paymentProcessedEventsCounter.increment();
    }
    
    public void recordPaymentFailedEvent() {
        paymentFailedEventsCounter.increment();
    }
    
    public void recordPaymentRefundedEvent() {
        paymentRefundedEventsCounter.increment();
    }
    
    // Performance Metrics Methods
    public Timer.Sample startTransactionProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordTransactionProcessingDuration(Timer.Sample sample) {
        sample.stop(transactionProcessingTimer);
    }
    
    // State Management Methods
    public void incrementActiveTransactions() {
        activeTransactionsGauge.incrementAndGet();
    }
    
    public void decrementActiveTransactions() {
        activeTransactionsGauge.decrementAndGet();
    }
    
    public void incrementPendingTransactions() {
        pendingTransactionsGauge.incrementAndGet();
    }
    
    public void decrementPendingTransactions() {
        pendingTransactionsGauge.decrementAndGet();
    }
    
    public void incrementFailedTransactions() {
        failedTransactionsGauge.incrementAndGet();
    }
    
    // Getter methods for monitoring
    public long getActiveTransactions() {
        return activeTransactionsGauge.get();
    }
    
    public long getPendingTransactions() {
        return pendingTransactionsGauge.get();
    }
    
    public long getFailedTransactions() {
        return failedTransactionsGauge.get();
    }
    
    // Throughput calculation methods
    public double getEventPublishingThroughput() {
        return eventsPublishedCounter.count();
    }
    
    public double getEventConsumptionThroughput() {
        return eventsConsumedCounter.count();
    }
    
    public double getEventPublishingErrorRate() {
        double total = eventsPublishedCounter.count() + eventPublishFailuresCounter.count();
        return total > 0 ? eventPublishFailuresCounter.count() / total : 0.0;
    }
    
    public double getEventConsumptionErrorRate() {
        double total = eventsConsumedCounter.count() + eventConsumptionFailuresCounter.count();
        return total > 0 ? eventConsumptionFailuresCounter.count() / total : 0.0;
    }
}
