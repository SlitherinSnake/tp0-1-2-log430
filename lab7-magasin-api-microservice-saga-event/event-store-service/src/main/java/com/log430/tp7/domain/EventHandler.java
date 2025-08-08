package com.log430.tp7.domain;

/**
 * Interface for handling events during replay operations
 */
public interface EventHandler {
    
    /**
     * Handle a single event during replay
     * @param event The event to handle
     */
    void handle(Event event);
    
    /**
     * Check if this handler can process the given event type
     * @param eventType The event type to check
     * @return true if the handler can process this event type
     */
    boolean canHandle(String eventType);
    
    /**
     * Called before replay starts
     * @param aggregateId The aggregate being replayed
     */
    default void onReplayStart(String aggregateId) {
        // Default implementation does nothing
    }
    
    /**
     * Called after replay completes
     * @param aggregateId The aggregate that was replayed
     * @param eventCount Number of events processed
     */
    default void onReplayComplete(String aggregateId, int eventCount) {
        // Default implementation does nothing
    }
    
    /**
     * Called when replay encounters an error
     * @param aggregateId The aggregate being replayed
     * @param event The event that caused the error
     * @param error The error that occurred
     */
    default void onReplayError(String aggregateId, Event event, Exception error) {
        // Default implementation does nothing
    }
}