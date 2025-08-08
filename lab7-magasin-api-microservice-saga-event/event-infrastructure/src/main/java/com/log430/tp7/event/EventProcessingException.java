package com.log430.tp7.event;

/**
 * Exception thrown when event processing fails.
 * Provides context about the failure for proper error handling and retry logic.
 */
public class EventProcessingException extends RuntimeException {
    
    private final DomainEvent event;
    private final String consumerName;
    private final boolean retryable;
    
    public EventProcessingException(String message, DomainEvent event, String consumerName) {
        super(message);
        this.event = event;
        this.consumerName = consumerName;
        this.retryable = true;
    }
    
    public EventProcessingException(String message, DomainEvent event, String consumerName, 
                                   Throwable cause) {
        super(message, cause);
        this.event = event;
        this.consumerName = consumerName;
        this.retryable = true;
    }
    
    public EventProcessingException(String message, DomainEvent event, String consumerName, 
                                   Throwable cause, boolean retryable) {
        super(message, cause);
        this.event = event;
        this.consumerName = consumerName;
        this.retryable = retryable;
    }
    
    public DomainEvent getEvent() {
        return event;
    }
    
    public String getConsumerName() {
        return consumerName;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    @Override
    public String toString() {
        return String.format("EventProcessingException{event=%s, consumer='%s', retryable=%s, message='%s'}", 
                           event != null ? event.getEventType() : "null", consumerName, retryable, getMessage());
    }
}