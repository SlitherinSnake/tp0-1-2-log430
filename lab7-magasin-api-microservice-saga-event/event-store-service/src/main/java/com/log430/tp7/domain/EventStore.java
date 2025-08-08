package com.log430.tp7.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Event Store interface for storing and retrieving domain events
 * Supports event sourcing patterns with optimistic concurrency control
 */
public interface EventStore {
    
    /**
     * Save a single event to the store
     * @param event The event to save
     * @throws OptimisticLockingException if version conflict occurs
     */
    void saveEvent(Event event);
    
    /**
     * Save multiple events atomically
     * @param events List of events to save
     * @throws OptimisticLockingException if version conflict occurs
     */
    void saveEvents(List<Event> events);
    
    /**
     * Get all events for a specific aggregate
     * @param aggregateId The aggregate identifier
     * @return List of events ordered by version
     */
    List<Event> getEvents(String aggregateId);
    
    /**
     * Get events for a specific aggregate starting from a version
     * @param aggregateId The aggregate identifier
     * @param fromVersion Starting version (inclusive)
     * @return List of events ordered by version
     */
    List<Event> getEvents(String aggregateId, int fromVersion);
    
    /**
     * Get events for a specific aggregate up to a version
     * @param aggregateId The aggregate identifier
     * @param toVersion Ending version (inclusive)
     * @return List of events ordered by version
     */
    List<Event> getEventsUpTo(String aggregateId, int toVersion);
    
    /**
     * Get events by type
     * @param eventType The event type to filter by
     * @return List of events of the specified type
     */
    List<Event> getEventsByType(String eventType);
    
    /**
     * Get events by correlation ID
     * @param correlationId The correlation ID to filter by
     * @return List of events with the specified correlation ID
     */
    List<Event> getEventsByCorrelationId(UUID correlationId);
    
    /**
     * Get events within a time range
     * @param from Start time (inclusive)
     * @param to End time (inclusive)
     * @return List of events within the time range
     */
    List<Event> getEventsByTimeRange(Instant from, Instant to);
    
    /**
     * Get events for multiple aggregates
     * @param aggregateIds List of aggregate identifiers
     * @return List of events for all specified aggregates
     */
    List<Event> getEventsForAggregates(List<String> aggregateIds);
    
    /**
     * Get the latest version for an aggregate
     * @param aggregateId The aggregate identifier
     * @return The latest version number, or 0 if no events exist
     */
    int getLatestVersion(String aggregateId);
    
    /**
     * Check if an aggregate exists
     * @param aggregateId The aggregate identifier
     * @return true if the aggregate has events, false otherwise
     */
    boolean aggregateExists(String aggregateId);
    
    /**
     * Get a specific event by ID
     * @param eventId The event identifier
     * @return Optional containing the event if found
     */
    Optional<Event> getEventById(UUID eventId);
    
    /**
     * Get events with pagination
     * @param offset Number of events to skip
     * @param limit Maximum number of events to return
     * @return List of events with pagination applied
     */
    List<Event> getEventsWithPagination(int offset, int limit);
    
    /**
     * Get total count of events
     * @return Total number of events in the store
     */
    long getTotalEventCount();
    
    /**
     * Get count of events for a specific aggregate
     * @param aggregateId The aggregate identifier
     * @return Number of events for the aggregate
     */
    long getEventCount(String aggregateId);
}