package com.log430.tp7.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Abstract base class for event consumers that provides common functionality
 * including event type filtering, error handling, and logging.
 */
public abstract class AbstractEventConsumer implements EventConsumer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Returns the set of event types this consumer can handle.
     * Subclasses should override this method.
     */
    protected abstract Set<String> getSupportedEventTypes();
    
    /**
     * Processes the event. Subclasses should implement their specific logic here.
     */
    protected abstract void processEvent(DomainEvent event);
    
    @Override
    public final void handleEvent(DomainEvent event) {
        if (event == null) {
            throw new EventProcessingException("Event cannot be null", null, getConsumerName());
        }
        
        if (!canHandle(event.getEventType())) {
            logger.warn("Consumer {} cannot handle event type: {}", getConsumerName(), event.getEventType());
            return;
        }
        
        try {
            logger.debug("Processing event: {} with consumer: {}", event.getEventType(), getConsumerName());
            processEvent(event);
            logger.debug("Successfully processed event: {} with consumer: {}", 
                        event.getEventType(), getConsumerName());
        } catch (Exception e) {
            logger.error("Failed to process event: {} with consumer: {}", 
                        event.getEventType(), getConsumerName(), e);
            throw new EventProcessingException(
                "Event processing failed in " + getConsumerName(), event, getConsumerName(), e);
        }
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return getSupportedEventTypes().contains(eventType);
    }
    
    @Override
    public void beforeEventProcessing(DomainEvent event) {
        logger.debug("Starting event processing: {} with consumer: {}", 
                    event.getEventType(), getConsumerName());
    }
    
    @Override
    public void afterEventProcessing(DomainEvent event) {
        logger.debug("Completed event processing: {} with consumer: {}", 
                    event.getEventType(), getConsumerName());
    }
    
    @Override
    public boolean handleError(DomainEvent event, Exception exception) {
        logger.error("Error in consumer {}: {}", getConsumerName(), exception.getMessage(), exception);
        
        // Check if this is a non-retryable error
        if (isNonRetryableError(exception)) {
            logger.error("Non-retryable error detected, marking event as failed: {}", event.getEventId());
            return true; // Handle the error (don't retry)
        }
        
        return false; // Let the framework handle retries
    }
    
    /**
     * Determines if an exception represents a non-retryable error.
     * Subclasses can override this to define their own non-retryable error conditions.
     */
    protected boolean isNonRetryableError(Exception exception) {
        // Common non-retryable errors
        return exception instanceof IllegalArgumentException ||
               exception instanceof IllegalStateException ||
               (exception instanceof EventProcessingException && 
                !((EventProcessingException) exception).isRetryable());
    }
    
    /**
     * Validates that the event has the required fields for this consumer.
     * Subclasses can override this to add specific validation logic.
     */
    protected void validateEvent(DomainEvent event) {
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (event.getAggregateId() == null || event.getAggregateId().trim().isEmpty()) {
            throw new IllegalArgumentException("Aggregate ID cannot be null or empty");
        }
        if (event.getCorrelationId() == null || event.getCorrelationId().trim().isEmpty()) {
            throw new IllegalArgumentException("Correlation ID cannot be null or empty");
        }
    }
}