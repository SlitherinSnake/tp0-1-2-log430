package com.log430.tp7.event;

/**
 * Exception thrown when event publishing fails.
 * Provides context about the failure for proper error handling and retry logic.
 */
public class EventPublishingException extends RuntimeException {
    
    private final DomainEvent event;
    private final String routingKey;
    private final boolean retryable;
    
    public EventPublishingException(String message, DomainEvent event) {
        super(message);
        this.event = event;
        this.routingKey = null;
        this.retryable = true;
    }
    
    public EventPublishingException(String message, DomainEvent event, String routingKey) {
        super(message);
        this.event = event;
        this.routingKey = routingKey;
        this.retryable = true;
    }
    
    public EventPublishingException(String message, DomainEvent event, Throwable cause) {
        super(message, cause);
        this.event = event;
        this.routingKey = null;
        this.retryable = true;
    }
    
    public EventPublishingException(String message, DomainEvent event, String routingKey, 
                                   Throwable cause, boolean retryable) {
        super(message, cause);
        this.event = event;
        this.routingKey = routingKey;
        this.retryable = retryable;
    }
    
    public DomainEvent getEvent() {
        return event;
    }
    
    public String getRoutingKey() {
        return routingKey;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    @Override
    public String toString() {
        return String.format("EventPublishingException{event=%s, routingKey='%s', retryable=%s, message='%s'}", 
                           event != null ? event.getEventType() : "null", routingKey, retryable, getMessage());
    }
}