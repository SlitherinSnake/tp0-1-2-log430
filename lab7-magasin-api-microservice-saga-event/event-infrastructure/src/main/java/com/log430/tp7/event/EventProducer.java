package com.log430.tp7.event;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for publishing domain events to the message broker.
 * Implementations should handle serialization, routing, correlation ID management,
 * and error handling with retry mechanisms.
 */
public interface EventProducer {
    
    /**
     * Publishes an event to the default exchange with routing based on event type.
     * Uses synchronous publishing with confirmation.
     * 
     * @param event The domain event to publish
     * @throws EventPublishingException if publishing fails
     */
    void publishEvent(DomainEvent event);
    
    /**
     * Publishes an event to a specific topic/routing key.
     * Uses synchronous publishing with confirmation.
     * 
     * @param routingKey The routing key for message routing
     * @param event The domain event to publish
     * @throws EventPublishingException if publishing fails
     */
    void publishEvent(String routingKey, DomainEvent event);
    
    /**
     * Publishes an event with additional headers.
     * Uses synchronous publishing with confirmation.
     * 
     * @param routingKey The routing key for message routing
     * @param event The domain event to publish
     * @param headers Additional message headers
     * @throws EventPublishingException if publishing fails
     */
    void publishEvent(String routingKey, DomainEvent event, Map<String, Object> headers);
    
    /**
     * Publishes an event asynchronously.
     * Returns a CompletableFuture that completes when the event is confirmed.
     * 
     * @param event The domain event to publish
     * @return CompletableFuture that completes when publishing is confirmed
     */
    CompletableFuture<Void> publishEventAsync(DomainEvent event);
    
    /**
     * Publishes an event asynchronously with custom routing key.
     * Returns a CompletableFuture that completes when the event is confirmed.
     * 
     * @param routingKey The routing key for message routing
     * @param event The domain event to publish
     * @return CompletableFuture that completes when publishing is confirmed
     */
    CompletableFuture<Void> publishEventAsync(String routingKey, DomainEvent event);
    
    /**
     * Publishes multiple events in a batch for better performance.
     * All events will be published atomically or none at all.
     * 
     * @param events The domain events to publish
     * @throws EventPublishingException if any event publishing fails
     */
    void publishEvents(DomainEvent... events);
    
    /**
     * Publishes an event with retry logic.
     * Will retry failed publications according to configured retry policy.
     * 
     * @param event The domain event to publish
     * @param maxRetries Maximum number of retry attempts
     * @throws EventPublishingException if publishing fails after all retries
     */
    void publishEventWithRetry(DomainEvent event, int maxRetries);
}