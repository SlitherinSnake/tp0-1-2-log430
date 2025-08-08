package com.log430.tp7.event;

/**
 * Interface for consuming and handling domain events.
 * Implementations should provide idempotent event processing
 * and proper error handling with retry mechanisms.
 */
public interface EventConsumer {
    
    /**
     * Handles a domain event. Implementations should be idempotent
     * to safely handle duplicate events.
     * 
     * @param event The domain event to handle
     * @throws EventProcessingException if event processing fails
     */
    void handleEvent(DomainEvent event);
    
    /**
     * Determines if this consumer can handle the given event type.
     * 
     * @param eventType The event type to check
     * @return true if this consumer can handle the event type
     */
    boolean canHandle(String eventType);
    
    /**
     * Gets the consumer name for logging and metrics.
     * 
     * @return The consumer name
     */
    String getConsumerName();
    
    /**
     * Handles event processing failure. Called when handleEvent throws an exception.
     * Can be used for custom error handling, logging, or compensation logic.
     * 
     * @param event The event that failed to process
     * @param exception The exception that occurred
     * @return true if the error was handled and processing should continue, false to rethrow
     */
    default boolean handleError(DomainEvent event, Exception exception) {
        return false; // Default: rethrow the exception
    }
    
    /**
     * Called before event processing starts. Can be used for setup or validation.
     * 
     * @param event The event about to be processed
     */
    default void beforeEventProcessing(DomainEvent event) {
        // Default: no-op
    }
    
    /**
     * Called after successful event processing. Can be used for cleanup or metrics.
     * 
     * @param event The event that was successfully processed
     */
    default void afterEventProcessing(DomainEvent event) {
        // Default: no-op
    }
}