package com.log430.tp7.infrastructure.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Event metrics component for Payment Service observability.
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
    private final Counter paymentProcessedCounter;
    private final Counter paymentFailedCounter;
    private final Counter paymentRefundedCounter;
    
    // Saga Coordination Metrics
    private final Counter transactionCreatedEventsCounter;
    private final Counter inventoryUnavailableEventsCounter;
    
    // System State Metrics
    private final AtomicLong activePaymentsGauge = new AtomicLong(0);
    private final AtomicLong pendingPaymentsGauge = new AtomicLong(0);
    private final AtomicLong failedPaymentsGauge = new AtomicLong(0);
    
    // Performance Metrics
    private final Timer paymentProcessingTimer;
    
    public EventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize event publishing metrics
        this.eventsPublishedCounter = Counter.builder("events_published_total")
            .description("Total number of events published by payment service")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.eventPublishFailuresCounter = Counter.builder("event_publish_failures_total")
            .description("Total number of event publishing failures")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.eventPublishLatencyTimer = Timer.builder("event_publish_latency_seconds")
            .description("Latency of event publishing operations")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        // Initialize event consumption metrics
        this.eventsConsumedCounter = Counter.builder("events_consumed_total")
            .description("Total number of events consumed by payment service")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.eventConsumptionFailuresCounter = Counter.builder("event_consumption_failures_total")
            .description("Total number of event consumption failures")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        // Initialize business event metrics
        this.paymentProcessedCounter = Counter.builder("payment_events_total")
            .description("Total number of payment events")
            .tag("event_type", "PaymentProcessed")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.paymentFailedCounter = Counter.builder("payment_events_total")
            .description("Total number of payment events")
            .tag("event_type", "PaymentFailed")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.paymentRefundedCounter = Counter.builder("payment_events_total")
            .description("Total number of payment events")
            .tag("event_type", "PaymentRefunded")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        // Initialize saga coordination metrics
        this.transactionCreatedEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "TransactionCreated")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        this.inventoryUnavailableEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "InventoryUnavailable")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        // Initialize performance metrics
        this.paymentProcessingTimer = Timer.builder("payment_processing_duration_seconds")
            .description("Duration of payment processing operations")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("active_payments_count", activePaymentsGauge, AtomicLong::doubleValue)
            .description("Current number of active payments")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        Gauge.builder("pending_payments_count", pendingPaymentsGauge, AtomicLong::doubleValue)
            .description("Current number of pending payments")
            .tag("service", "payment-service")
            .register(meterRegistry);
        
        Gauge.builder("failed_payments_count", failedPaymentsGauge, AtomicLong::doubleValue)
            .description("Current number of failed payments")
            .tag("service", "payment-service")
            .register(meterRegistry);
    }
    
    // Event Publishing Metrics Methods
    public void recordEventPublished(String eventType) {
        Counter.builder("events_published_by_type_total")
            .tag("event_type", eventType)
            .tag("service", "payment-service")
            .register(meterRegistry)
            .increment();
        eventsPublishedCounter.increment();
    }
    
    public void recordEventPublishFailure(String eventType, String errorType) {
        Counter.builder("event_publish_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "payment-service")
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
            .tag("service", "payment-service")
            .register(meterRegistry)
            .increment();
        eventsConsumedCounter.increment();
    }
    
    public void recordEventConsumptionFailure(String eventType, String errorType) {
        Counter.builder("event_consumption_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "payment-service")
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
            .tag("service", "payment-service")
            .register(meterRegistry));
    }
    
    // Business Event Metrics Methods
    public void recordPaymentProcessed() {
        paymentProcessedCounter.increment();
    }
    
    public void recordPaymentFailed() {
        paymentFailedCounter.increment();
    }
    
    public void recordPaymentRefunded() {
        paymentRefundedCounter.increment();
    }
    
    // Saga Coordination Metrics Methods
    public void recordTransactionCreatedEvent() {
        transactionCreatedEventsCounter.increment();
    }
    
    public void recordInventoryUnavailableEvent() {
        inventoryUnavailableEventsCounter.increment();
    }
    
    // Performance Metrics Methods
    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPaymentProcessingDuration(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }
    
    // State Management Methods
    public void incrementActivePayments() {
        activePaymentsGauge.incrementAndGet();
    }
    
    public void decrementActivePayments() {
        activePaymentsGauge.decrementAndGet();
    }
    
    public void incrementPendingPayments() {
        pendingPaymentsGauge.incrementAndGet();
    }
    
    public void decrementPendingPayments() {
        pendingPaymentsGauge.decrementAndGet();
    }
    
    public void incrementFailedPayments() {
        failedPaymentsGauge.incrementAndGet();
    }
    
    // Getter methods for monitoring
    public long getActivePayments() {
        return activePaymentsGauge.get();
    }
    
    public long getPendingPayments() {
        return pendingPaymentsGauge.get();
    }
    
    public long getFailedPayments() {
        return failedPaymentsGauge.get();
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
