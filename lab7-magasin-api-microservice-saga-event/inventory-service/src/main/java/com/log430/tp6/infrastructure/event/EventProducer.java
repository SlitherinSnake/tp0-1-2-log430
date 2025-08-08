package com.log430.tp7.infrastructure.event;

import java.util.Map;

/**
 * Interface for publishing domain events to the message broker.
 * Implementations should handle serialization, routing, and error handling.
 */
public interface EventProducer {
    
    /**
     * Publishes an event to the default exchange with routing based on event type.
     * 
     * @param event The domain event to publish
     */
    void publishEvent(DomainEvent event);
    
    /**
     * Publishes an event to a specific topic/routing key.
     * 
     * @param routingKey The routing key for message routing
     * @param event The domain event to publish
     */
    void publishEvent(String routingKey, DomainEvent event);
    
    /**
     * Publishes an event with additional headers.
     * 
     * @param routingKey The routing key for message routing
     * @param event The domain event to publish
     * @param headers Additional message headers
     */
    void publishEvent(String routingKey, DomainEvent event, Map<String, Object> headers);
}
