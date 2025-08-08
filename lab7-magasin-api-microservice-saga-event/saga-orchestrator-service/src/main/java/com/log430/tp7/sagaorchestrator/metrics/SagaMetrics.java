package com.log430.tp7.sagaorchestrator.metrics;

import com.log430.tp7.sagaorchestrator.model.SagaState;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom metrics component for saga orchestrator monitoring and observability
 */
@Component
public class SagaMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Counters for saga lifecycle events
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaFailedCounter;
    private final Counter compensationExecutedCounter;
    private final Counter compensationSuccessCounter;
    private final Counter compensationFailureCounter;
    
    // Counters for service interactions (success/failure tracked separately)
    private final Counter stockVerificationSuccessCounter;
    private final Counter stockVerificationFailureCounter;
    private final Counter stockReservationSuccessCounter;
    private final Counter stockReservationFailureCounter;
    private final Counter paymentProcessingSuccessCounter;
    private final Counter paymentProcessingFailureCounter;
    private final Counter orderConfirmationSuccessCounter;
    private final Counter orderConfirmationFailureCounter;
    
    // Timers for performance monitoring
    private final Timer sagaDurationTimer;
    private final Timer stockVerificationTimer;
    private final Timer stockReservationTimer;
    private final Timer paymentProcessingTimer;
    private final Timer orderConfirmationTimer;
    
    // Gauges for current state monitoring
    private final AtomicLong activeSagasGauge = new AtomicLong(0);
    private final AtomicLong pendingStockVerificationGauge = new AtomicLong(0);
    private final AtomicLong pendingPaymentGauge = new AtomicLong(0);
    
    public SagaMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.sagaStartedCounter = Counter.builder("saga_started_total")
            .description("Total number of sagas started")
            .register(meterRegistry);
        
        this.sagaCompletedCounter = Counter.builder("saga_completed_total")
            .description("Total number of sagas completed successfully")
            .register(meterRegistry);
        
        this.sagaFailedCounter = Counter.builder("saga_failed_total")
            .description("Total number of sagas that failed")
            .register(meterRegistry);
        
        this.compensationExecutedCounter = Counter.builder("saga_compensation_executed_total")
            .description("Total number of compensation actions executed")
            .register(meterRegistry);
        
        this.compensationSuccessCounter = Counter.builder("saga_compensation_total")
            .description("Total number of compensation actions")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.compensationFailureCounter = Counter.builder("saga_compensation_total")
            .description("Total number of compensation actions")
            .tag("result", "failure")
            .register(meterRegistry);
        
        // Service interaction counters
        this.stockVerificationSuccessCounter = Counter.builder("saga_stock_verification_total")
            .description("Total number of stock verification calls")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.stockVerificationFailureCounter = Counter.builder("saga_stock_verification_total")
            .description("Total number of stock verification calls")
            .tag("result", "failure")
            .register(meterRegistry);
        
        this.stockReservationSuccessCounter = Counter.builder("saga_stock_reservation_total")
            .description("Total number of stock reservation calls")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.stockReservationFailureCounter = Counter.builder("saga_stock_reservation_total")
            .description("Total number of stock reservation calls")
            .tag("result", "failure")
            .register(meterRegistry);
        
        this.paymentProcessingSuccessCounter = Counter.builder("saga_payment_processing_total")
            .description("Total number of payment processing calls")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.paymentProcessingFailureCounter = Counter.builder("saga_payment_processing_total")
            .description("Total number of payment processing calls")
            .tag("result", "failure")
            .register(meterRegistry);
        
        this.orderConfirmationSuccessCounter = Counter.builder("saga_order_confirmation_total")
            .description("Total number of order confirmation calls")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.orderConfirmationFailureCounter = Counter.builder("saga_order_confirmation_total")
            .description("Total number of order confirmation calls")
            .tag("result", "failure")
            .register(meterRegistry);
        
        // Initialize timers
        this.sagaDurationTimer = Timer.builder("saga_duration_seconds")
            .description("Duration of saga execution from start to completion")
            .register(meterRegistry);
        
        this.stockVerificationTimer = Timer.builder("saga_stock_verification_duration_seconds")
            .description("Duration of stock verification calls")
            .register(meterRegistry);
        
        this.stockReservationTimer = Timer.builder("saga_stock_reservation_duration_seconds")
            .description("Duration of stock reservation calls")
            .register(meterRegistry);
        
        this.paymentProcessingTimer = Timer.builder("saga_payment_processing_duration_seconds")
            .description("Duration of payment processing calls")
            .register(meterRegistry);
        
        this.orderConfirmationTimer = Timer.builder("saga_order_confirmation_duration_seconds")
            .description("Duration of order confirmation calls")
            .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("saga_active_count", activeSagasGauge, AtomicLong::doubleValue)
            .description("Number of currently active sagas")
            .register(meterRegistry);
        
        Gauge.builder("saga_pending_stock_verification_count", pendingStockVerificationGauge, AtomicLong::doubleValue)
            .description("Number of sagas pending stock verification")
            .register(meterRegistry);
        
        Gauge.builder("saga_pending_payment_count", pendingPaymentGauge, AtomicLong::doubleValue)
            .description("Number of sagas pending payment processing")
            .register(meterRegistry);
    }
    
    // Counter increment methods
    
    public void incrementSagaStarted() {
        sagaStartedCounter.increment();
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
    
    public void incrementCompensationExecuted() {
        compensationExecutedCounter.increment();
    }
    
    public void incrementCompensationSuccess() {
        compensationSuccessCounter.increment();
    }
    
    public void incrementCompensationFailure() {
        compensationFailureCounter.increment();
    }
    
    public void incrementStockVerification(boolean success) {
        if (success) {
            stockVerificationSuccessCounter.increment();
        } else {
            stockVerificationFailureCounter.increment();
        }
    }
    
    public void incrementStockReservation(boolean success) {
        if (success) {
            stockReservationSuccessCounter.increment();
        } else {
            stockReservationFailureCounter.increment();
        }
    }
    
    public void incrementPaymentProcessing(boolean success) {
        if (success) {
            paymentProcessingSuccessCounter.increment();
        } else {
            paymentProcessingFailureCounter.increment();
        }
    }
    
    public void incrementOrderConfirmation(boolean success) {
        if (success) {
            orderConfirmationSuccessCounter.increment();
        } else {
            orderConfirmationFailureCounter.increment();
        }
    }
    
    // Timer methods
    
    public Timer.Sample startSagaDurationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordSagaDuration(Timer.Sample sample) {
        sample.stop(sagaDurationTimer);
    }
    
    public Timer.Sample startStockVerificationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordStockVerificationDuration(Timer.Sample sample) {
        sample.stop(stockVerificationTimer);
    }
    
    public Timer.Sample startStockReservationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordStockReservationDuration(Timer.Sample sample) {
        sample.stop(stockReservationTimer);
    }
    
    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPaymentProcessingDuration(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }
    
    public Timer.Sample startOrderConfirmationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordOrderConfirmationDuration(Timer.Sample sample) {
        sample.stop(orderConfirmationTimer);
    }
    
    // Gauge update methods
    
    public void updateActiveSagasCount(long count) {
        activeSagasGauge.set(count);
    }
    
    public void updatePendingStockVerificationCount(long count) {
        pendingStockVerificationGauge.set(count);
    }
    
    public void updatePendingPaymentCount(long count) {
        pendingPaymentGauge.set(count);
    }
    
    public void incrementPendingStockVerification() {
        pendingStockVerificationGauge.incrementAndGet();
    }
    
    public void decrementPendingStockVerification() {
        pendingStockVerificationGauge.decrementAndGet();
    }
    
    public void incrementPendingPayment() {
        pendingPaymentGauge.incrementAndGet();
    }
    
    public void decrementPendingPayment() {
        pendingPaymentGauge.decrementAndGet();
    }
    
    // State transition tracking
    
    public void recordStateTransition(SagaState fromState, SagaState toState) {
        Counter.builder("saga_state_transitions_total")
            .description("Total number of saga state transitions")
            .tag("from_state", fromState != null ? fromState.name() : "INITIAL")
            .tag("to_state", toState.name())
            .register(meterRegistry)
            .increment();
    }
    
    // Error tracking
    
    public void recordError(String errorType, String step) {
        Counter.builder("saga_errors_total")
            .description("Total number of saga errors by type and step")
            .tag("error_type", errorType)
            .tag("step", step)
            .register(meterRegistry)
            .increment();
    }
    
    // Timeout tracking
    
    public void incrementSagaTimeout(String state) {
        Counter.builder("saga_timeout_total")
            .description("Total number of saga timeouts by state")
            .tag("state", state)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordTimeoutCleanup(int sagaCount) {
        Counter.builder("saga_timeout_cleanup_total")
            .description("Total number of timeout cleanup operations")
            .register(meterRegistry)
            .increment(sagaCount);
    }
}