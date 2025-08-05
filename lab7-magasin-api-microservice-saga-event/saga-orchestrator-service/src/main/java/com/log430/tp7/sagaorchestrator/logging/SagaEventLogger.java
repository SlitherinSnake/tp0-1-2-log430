package com.log430.tp7.sagaorchestrator.logging;

import com.log430.tp7.sagaorchestrator.model.SagaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Structured logging component for saga orchestrator events.
 * Provides consistent log formatting with correlation ID tracking
 * and appropriate log levels for different saga events and errors.
 */
@Component
public class SagaEventLogger {

    private static final Logger logger = LoggerFactory.getLogger(SagaEventLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // MDC keys for structured logging
    private static final String SAGA_ID_KEY = "sagaId";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String SAGA_STATE_KEY = "sagaState";
    private static final String EVENT_TYPE_KEY = "eventType";
    private static final String STEP_KEY = "step";
    private static final String CUSTOMER_ID_KEY = "customerId";
    private static final String PRODUCT_ID_KEY = "productId";

    /**
     * Logs saga initiation event with structured context
     */
    public void logSagaStarted(String sagaId, String customerId, String productId, Integer quantity) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.SALE_INITIATED, "SAGA_STARTED", "initiation");
            MDC.put(CUSTOMER_ID_KEY, customerId);
            MDC.put(PRODUCT_ID_KEY, productId);

            logger.info("Saga started: customerId={}, productId={}, quantity={}, timestamp={}",
                    customerId, productId, quantity, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs saga completion event with structured context
     */
    public void logSagaCompleted(String sagaId, String customerId, String orderId, long durationMs) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.SALE_CONFIRMED, "SAGA_COMPLETED", "completion");
            MDC.put(CUSTOMER_ID_KEY, customerId);

            logger.info("Saga completed successfully: customerId={}, orderId={}, durationMs={}, timestamp={}",
                    customerId, orderId, durationMs, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs saga failure event with structured context
     */
    public void logSagaFailed(String sagaId, String customerId, String errorMessage, String failureStep,
            long durationMs) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.SALE_FAILED, "SAGA_FAILED", failureStep);
            MDC.put(CUSTOMER_ID_KEY, customerId);

            logger.error("Saga failed: customerId={}, failureStep={}, errorMessage={}, durationMs={}, timestamp={}",
                    customerId, failureStep, errorMessage, durationMs, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs saga state transition with structured context
     */
    public void logStateTransition(String sagaId, SagaState fromState, SagaState toState, String step) {
        try {
            setMDCContext(sagaId, sagaId, toState, "STATE_TRANSITION", step);

            logger.info("Saga state transition: fromState={}, toState={}, step={}, timestamp={}",
                    fromState != null ? fromState.name() : "INITIAL", toState.name(), step, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs service call initiation with structured context
     */
    public void logServiceCallStarted(String sagaId, String serviceName, String operation, String step,
            Map<String, Object> requestData) {
        try {
            setMDCContext(sagaId, sagaId, null, "SERVICE_CALL_STARTED", step);

            logger.info("Service call started: serviceName={}, operation={}, step={}, requestData={}, timestamp={}",
                    serviceName, operation, step, requestData, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs successful service call completion with structured context
     */
    public void logServiceCallCompleted(String sagaId, String serviceName, String operation, String step,
            Map<String, Object> responseData, long durationMs) {
        try {
            setMDCContext(sagaId, sagaId, null, "SERVICE_CALL_COMPLETED", step);

            logger.info(
                    "Service call completed: serviceName={}, operation={}, step={}, responseData={}, durationMs={}, timestamp={}",
                    serviceName, operation, step, responseData, durationMs, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs failed service call with structured context
     */
    public void logServiceCallFailed(String sagaId, String serviceName, String operation, String step,
            String errorMessage, long durationMs) {
        try {
            setMDCContext(sagaId, sagaId, null, "SERVICE_CALL_FAILED", step);

            logger.error(
                    "Service call failed: serviceName={}, operation={}, step={}, errorMessage={}, durationMs={}, timestamp={}",
                    serviceName, operation, step, errorMessage, durationMs, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs compensation event initiation with structured context
     */
    public void logCompensationStarted(String sagaId, String customerId, String reason, String step) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.STOCK_RELEASING, "COMPENSATION_STARTED", step);
            MDC.put(CUSTOMER_ID_KEY, customerId);

            logger.warn("Compensation started: customerId={}, reason={}, step={}, timestamp={}",
                    customerId, reason, step, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs compensation action completion with structured context
     */
    public void logCompensationCompleted(String sagaId, String customerId, String reason, boolean success) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.SALE_FAILED, "COMPENSATION_COMPLETED", "compensation");
            MDC.put(CUSTOMER_ID_KEY, customerId);

            if (success) {
                logger.info("Compensation completed successfully: customerId={}, reason={}, timestamp={}",
                        customerId, reason, getCurrentTimestamp());
            } else {
                logger.error("Compensation completed with failures: customerId={}, reason={}, timestamp={}",
                        customerId, reason, getCurrentTimestamp());
            }
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs individual compensation action with structured context
     */
    public void logCompensationAction(String sagaId, String action, String details, boolean success) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.STOCK_RELEASING, "COMPENSATION_ACTION", "compensation");

            if (success) {
                logger.info("Compensation action completed: action={}, details={}, timestamp={}",
                        action, details, getCurrentTimestamp());
            } else {
                logger.error("Compensation action failed: action={}, details={}, timestamp={}",
                        action, details, getCurrentTimestamp());
            }
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs saga timeout event with structured context
     */
    public void logSagaTimeout(String sagaId, String customerId, String currentState, String timeoutMessage,
            long timeoutMinutes) {
        try {
            setMDCContext(sagaId, sagaId, SagaState.valueOf(currentState), "SAGA_TIMEOUT", "timeout_management");
            MDC.put(CUSTOMER_ID_KEY, customerId);

            logger.warn(
                    "Saga timeout detected: customerId={}, currentState={}, timeoutMinutes={}, message={}, timestamp={}",
                    customerId, currentState, timeoutMinutes, timeoutMessage, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs general saga error with structured context
     */
    public void logSagaError(String sagaId, String errorType, String step, String errorMessage, Exception exception) {
        try {
            setMDCContext(sagaId, sagaId, null, "SAGA_ERROR", step);

            if (exception != null) {
                logger.error("Saga error: errorType={}, step={}, errorMessage={}, exceptionClass={}, timestamp={}",
                        errorType, step, errorMessage, exception.getClass().getSimpleName(), getCurrentTimestamp(),
                        exception);
            } else {
                logger.error("Saga error: errorType={}, step={}, errorMessage={}, timestamp={}",
                        errorType, step, errorMessage, getCurrentTimestamp());
            }
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs performance metrics with structured context
     */
    public void logPerformanceMetrics(String sagaId, String step, long durationMs, String additionalMetrics) {
        try {
            setMDCContext(sagaId, sagaId, null, "PERFORMANCE_METRICS", step);

            logger.debug("Performance metrics: step={}, durationMs={}, additionalMetrics={}, timestamp={}",
                    step, durationMs, additionalMetrics, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Logs debug information with structured context
     */
    public void logDebugInfo(String sagaId, String step, String message, Map<String, Object> debugData) {
        try {
            setMDCContext(sagaId, sagaId, null, "DEBUG_INFO", step);

            logger.debug("Debug info: step={}, message={}, debugData={}, timestamp={}",
                    step, message, debugData, getCurrentTimestamp());
        } finally {
            clearMDC();
        }
    }

    /**
     * Sets up MDC context for structured logging
     */
    private void setMDCContext(String sagaId, String correlationId, SagaState sagaState, String eventType,
            String step) {
        MDC.put(SAGA_ID_KEY, sagaId);
        MDC.put(CORRELATION_ID_KEY, correlationId);
        if (sagaState != null) {
            MDC.put(SAGA_STATE_KEY, sagaState.name());
        }
        MDC.put(EVENT_TYPE_KEY, eventType);
        MDC.put(STEP_KEY, step);
    }

    /**
     * Clears MDC context to prevent memory leaks
     */
    private void clearMDC() {
        MDC.clear();
    }

    /**
     * Gets current timestamp in consistent format
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}