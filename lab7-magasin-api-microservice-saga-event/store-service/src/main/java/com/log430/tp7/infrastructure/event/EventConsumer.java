package com.log430.tp7.infrastructure.event;

import com.log430.tp7.event.DomainEvent;

/**
 * Interface for consuming domain events from the message broker.
 * Implementations should handle deserialization, idempotency, and error handling.
 */
public interface EventConsumer {
    
    /**
     * Handles a domain event received from the message broker.
     * Implementations should be idempotent and handle failures gracefully.
     * 
     * @param event The domain event to process
     */
    void handleEvent(DomainEvent event);
    
    /**
     * Determines if this consumer can handle the given event type.
     * 
     * @param eventType The type of event to check
     * @return true if this consumer can handle the event type
     */
    boolean canHandle(String eventType);
    
    /**
     * Returns the event types this consumer is interested in.
     * Used for automatic routing and subscription setup.
     * 
     * @return Array of event types this consumer handles
     */
    String[] getSupportedEventTypes();
}