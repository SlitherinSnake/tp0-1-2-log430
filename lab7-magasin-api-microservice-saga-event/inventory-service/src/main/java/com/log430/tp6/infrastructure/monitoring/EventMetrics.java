package com.log430.tp7.infrastructure.monitoring;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Event metrics component for Inventory Service observability.
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
    private final Counter inventoryReservedCounter;
    private final Counter inventoryUnavailableCounter;
    private final Counter inventoryReleasedCounter;
    
    // Saga Coordination Metrics
    private final Counter paymentProcessedEventsCounter;
    
    // System State Metrics
    private final AtomicLong activeReservationsGauge = new AtomicLong(0);
    private final AtomicLong pendingReservationsGauge = new AtomicLong(0);
    private final AtomicLong unavailableItemsGauge = new AtomicLong(0);
    
    // Performance Metrics
    private final Timer inventoryProcessingTimer;
    
    public EventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize event publishing metrics
        this.eventsPublishedCounter = Counter.builder("events_published_total")
            .description("Total number of events published by inventory service")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        this.eventPublishFailuresCounter = Counter.builder("event_publish_failures_total")
            .description("Total number of event publishing failures")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        this.eventPublishLatencyTimer = Timer.builder("event_publish_latency_seconds")
            .description("Latency of event publishing operations")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        // Initialize event consumption metrics
        this.eventsConsumedCounter = Counter.builder("events_consumed_total")
            .description("Total number of events consumed by inventory service")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        this.eventConsumptionFailuresCounter = Counter.builder("event_consumption_failures_total")
            .description("Total number of event consumption failures")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        // Initialize business event metrics
        this.inventoryReservedCounter = Counter.builder("inventory_events_total")
            .description("Total number of inventory events")
            .tag("event_type", "InventoryReserved")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        this.inventoryUnavailableCounter = Counter.builder("inventory_events_total")
            .description("Total number of inventory events")
            .tag("event_type", "InventoryUnavailable")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        this.inventoryReleasedCounter = Counter.builder("inventory_events_total")
            .description("Total number of inventory events")
            .tag("event_type", "InventoryReleased")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        // Initialize saga coordination metrics
        this.paymentProcessedEventsCounter = Counter.builder("saga_coordination_events_total")
            .description("Total number of saga coordination events processed")
            .tag("event_type", "PaymentProcessed")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        // Initialize performance metrics
        this.inventoryProcessingTimer = Timer.builder("inventory_processing_duration_seconds")
            .description("Duration of inventory processing operations")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("active_reservations_count", activeReservationsGauge, AtomicLong::doubleValue)
            .description("Current number of active inventory reservations")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        Gauge.builder("pending_reservations_count", pendingReservationsGauge, AtomicLong::doubleValue)
            .description("Current number of pending inventory reservations")
            .tag("service", "inventory-service")
            .register(meterRegistry);
        
        Gauge.builder("unavailable_items_count", unavailableItemsGauge, AtomicLong::doubleValue)
            .description("Current number of unavailable inventory items")
            .tag("service", "inventory-service")
            .register(meterRegistry);
    }
    
    // Event Publishing Metrics Methods
    public void recordEventPublished(String eventType) {
        Counter.builder("events_published_by_type_total")
            .tag("event_type", eventType)
            .tag("service", "inventory-service")
            .register(meterRegistry)
            .increment();
        eventsPublishedCounter.increment();
    }
    
    public void recordEventPublishFailure(String eventType, String errorType) {
        Counter.builder("event_publish_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "inventory-service")
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
            .tag("service", "inventory-service")
            .register(meterRegistry)
            .increment();
        eventsConsumedCounter.increment();
    }
    
    public void recordEventConsumptionFailure(String eventType, String errorType) {
        Counter.builder("event_consumption_failures_by_type_total")
            .tag("event_type", eventType)
            .tag("error_type", errorType)
            .tag("service", "inventory-service")
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
            .tag("service", "inventory-service")
            .register(meterRegistry));
    }
    
    // Business Event Metrics Methods
    public void recordInventoryReserved() {
        inventoryReservedCounter.increment();
    }
    
    public void recordInventoryUnavailable() {
        inventoryUnavailableCounter.increment();
    }
    
    public void recordInventoryReleased() {
        inventoryReleasedCounter.increment();
    }
    
    // Saga Coordination Metrics Methods
    public void recordPaymentProcessedEvent() {
        paymentProcessedEventsCounter.increment();
    }
    
    // Performance Metrics Methods
    public Timer.Sample startInventoryProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordInventoryProcessingDuration(Timer.Sample sample) {
        sample.stop(inventoryProcessingTimer);
    }
    
    // State Management Methods
    public void incrementActiveReservations() {
        activeReservationsGauge.incrementAndGet();
    }
    
    public void decrementActiveReservations() {
        activeReservationsGauge.decrementAndGet();
    }
    
    public void incrementPendingReservations() {
        pendingReservationsGauge.incrementAndGet();
    }
    
    public void decrementPendingReservations() {
        pendingReservationsGauge.decrementAndGet();
    }
    
    public void incrementUnavailableItems() {
        unavailableItemsGauge.incrementAndGet();
    }
    
    public void decrementUnavailableItems() {
        unavailableItemsGauge.decrementAndGet();
    }
    
    // Getter methods for monitoring
    public long getActiveReservations() {
        return activeReservationsGauge.get();
    }
    
    public long getPendingReservations() {
        return pendingReservationsGauge.get();
    }
    
    public long getUnavailableItems() {
        return unavailableItemsGauge.get();
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
