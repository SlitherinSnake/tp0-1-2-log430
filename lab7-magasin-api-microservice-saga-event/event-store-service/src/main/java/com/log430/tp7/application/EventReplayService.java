package com.log430.tp7.application;

import com.log430.tp7.domain.Event;
import com.log430.tp7.domain.EventHandler;
import com.log430.tp7.domain.EventStore;
import com.log430.tp7.infrastructure.PostgreSQLEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventReplayService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventReplayService.class);
    
    private final EventStore eventStore;
    private final PostgreSQLEventStore postgreSQLEventStore;
    
    // Track ongoing replay operations
    private final Map<String, ReplayStatus> activeReplays = new ConcurrentHashMap<>();
    
    @Autowired
    public EventReplayService(EventStore eventStore, PostgreSQLEventStore postgreSQLEventStore) {
        this.eventStore = eventStore;
        this.postgreSQLEventStore = postgreSQLEventStore;
    }
    
    /**
     * Replay all events for a specific aggregate
     */
    public ReplayResult replayAggregate(String aggregateId, EventHandler handler) {
        logger.info("Starting replay for aggregate: {}", aggregateId);
        
        if (activeReplays.containsKey(aggregateId)) {
            throw new IllegalStateException("Replay already in progress for aggregate: " + aggregateId);
        }
        
        ReplayStatus status = new ReplayStatus(aggregateId, Instant.now());
        activeReplays.put(aggregateId, status);
        
        try {
            List<Event> events = postgreSQLEventStore.getEventsForReplay(aggregateId);
            return executeReplay(aggregateId, events, handler, status);
        } finally {
            activeReplays.remove(aggregateId);
        }
    }
    
    /**
     * Replay events for an aggregate from a specific version
     */
    public ReplayResult replayAggregateFromVersion(String aggregateId, int fromVersion, EventHandler handler) {
        logger.info("Starting replay for aggregate: {} from version: {}", aggregateId, fromVersion);
        
        String replayKey = aggregateId + "_" + fromVersion;
        if (activeReplays.containsKey(replayKey)) {
            throw new IllegalStateException("Replay already in progress for aggregate: " + aggregateId);
        }
        
        ReplayStatus status = new ReplayStatus(aggregateId, Instant.now());
        activeReplays.put(replayKey, status);
        
        try {
            List<Event> events = eventStore.getEvents(aggregateId, fromVersion);
            return executeReplay(aggregateId, events, handler, status);
        } finally {
            activeReplays.remove(replayKey);
        }
    }
    
    /**
     * Replay events for an aggregate up to a specific version
     */
    public ReplayResult replayAggregateUpToVersion(String aggregateId, int toVersion, EventHandler handler) {
        logger.info("Starting replay for aggregate: {} up to version: {}", aggregateId, toVersion);
        
        String replayKey = aggregateId + "_to_" + toVersion;
        if (activeReplays.containsKey(replayKey)) {
            throw new IllegalStateException("Replay already in progress for aggregate: " + aggregateId);
        }
        
        ReplayStatus status = new ReplayStatus(aggregateId, Instant.now());
        activeReplays.put(replayKey, status);
        
        try {
            List<Event> events = eventStore.getEventsUpTo(aggregateId, toVersion);
            return executeReplay(aggregateId, events, handler, status);
        } finally {
            activeReplays.remove(replayKey);
        }
    }
    
    /**
     * Replay events by type
     */
    public ReplayResult replayEventsByType(String eventType, EventHandler handler) {
        logger.info("Starting replay for event type: {}", eventType);
        
        String replayKey = "type_" + eventType;
        if (activeReplays.containsKey(replayKey)) {
            throw new IllegalStateException("Replay already in progress for event type: " + eventType);
        }
        
        ReplayStatus status = new ReplayStatus("type:" + eventType, Instant.now());
        activeReplays.put(replayKey, status);
        
        try {
            List<Event> events = eventStore.getEventsByType(eventType);
            return executeReplay("type:" + eventType, events, handler, status);
        } finally {
            activeReplays.remove(replayKey);
        }
    }
    
    /**
     * Replay events within a time range
     */
    public ReplayResult replayEventsByTimeRange(Instant from, Instant to, EventHandler handler) {
        logger.info("Starting replay for time range: {} to {}", from, to);
        
        String replayKey = "timerange_" + from.toEpochMilli() + "_" + to.toEpochMilli();
        if (activeReplays.containsKey(replayKey)) {
            throw new IllegalStateException("Replay already in progress for this time range");
        }
        
        ReplayStatus status = new ReplayStatus("timerange:" + from + "-" + to, Instant.now());
        activeReplays.put(replayKey, status);
        
        try {
            List<Event> events = eventStore.getEventsByTimeRange(from, to);
            return executeReplay("timerange", events, handler, status);
        } finally {
            activeReplays.remove(replayKey);
        }
    }
    
    /**
     * Replay events asynchronously
     */
    public CompletableFuture<ReplayResult> replayAggregateAsync(String aggregateId, EventHandler handler) {
        return CompletableFuture.supplyAsync(() -> replayAggregate(aggregateId, handler));
    }
    
    /**
     * Get the status of an ongoing replay
     */
    public ReplayStatus getReplayStatus(String aggregateId) {
        return activeReplays.get(aggregateId);
    }
    
    /**
     * Get all active replay operations
     */
    public Map<String, ReplayStatus> getActiveReplays() {
        return new ConcurrentHashMap<>(activeReplays);
    }
    
    /**
     * Cancel an ongoing replay (if possible)
     */
    public boolean cancelReplay(String aggregateId) {
        ReplayStatus status = activeReplays.get(aggregateId);
        if (status != null) {
            status.setCancelled(true);
            logger.info("Replay cancellation requested for aggregate: {}", aggregateId);
            return true;
        }
        return false;
    }
    
    /**
     * Execute the actual replay logic
     */
    private ReplayResult executeReplay(String identifier, List<Event> events, EventHandler handler, ReplayStatus status) {
        ReplayResult result = new ReplayResult(identifier);
        result.setStartTime(Instant.now());
        
        try {
            handler.onReplayStart(identifier);
            status.setTotalEvents(events.size());
            
            int processedCount = 0;
            for (Event event : events) {
                // Check for cancellation
                if (status.isCancelled()) {
                    result.setCancelled(true);
                    logger.info("Replay cancelled for {}", identifier);
                    break;
                }
                
                try {
                    if (handler.canHandle(event.getEventType())) {
                        handler.handle(event);
                        processedCount++;
                        status.setProcessedEvents(processedCount);
                        
                        // Validate event ordering
                        if (!isEventOrderValid(event, result.getLastProcessedEvent())) {
                            result.addOrderingIssue(event);
                            logger.warn("Event ordering issue detected for event: {}", event.getEventId());
                        }
                        
                        result.setLastProcessedEvent(event);
                    } else {
                        result.addSkippedEvent(event);
                        logger.debug("Skipped event {} - handler cannot process type {}", 
                                   event.getEventId(), event.getEventType());
                    }
                } catch (Exception e) {
                    result.addError(event, e);
                    handler.onReplayError(identifier, event, e);
                    logger.error("Error processing event {} during replay: {}", 
                               event.getEventId(), e.getMessage(), e);
                }
            }
            
            result.setProcessedCount(processedCount);
            result.setEndTime(Instant.now());
            result.setSuccess(!result.isCancelled() && result.getErrors().isEmpty());
            
            handler.onReplayComplete(identifier, processedCount);
            
            logger.info("Replay completed for {}: processed {}/{} events", 
                       identifier, processedCount, events.size());
            
        } catch (Exception e) {
            result.setEndTime(Instant.now());
            result.setSuccess(false);
            result.setFailureReason(e.getMessage());
            logger.error("Replay failed for {}: {}", identifier, e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * Validate event ordering for consistency checks
     */
    private boolean isEventOrderValid(Event currentEvent, Event previousEvent) {
        if (previousEvent == null) {
            return true;
        }
        
        // Check if events are from the same aggregate
        if (currentEvent.getAggregateId().equals(previousEvent.getAggregateId())) {
            // Version should be sequential
            return currentEvent.getEventVersion() > previousEvent.getEventVersion();
        }
        
        // For different aggregates, timestamp ordering is sufficient
        return !currentEvent.getTimestamp().isBefore(previousEvent.getTimestamp());
    }
    
    /**
     * Replay status tracking
     */
    public static class ReplayStatus {
        private final String identifier;
        private final Instant startTime;
        private int totalEvents;
        private int processedEvents;
        private boolean cancelled;
        
        public ReplayStatus(String identifier, Instant startTime) {
            this.identifier = identifier;
            this.startTime = startTime;
        }
        
        // Getters and setters
        public String getIdentifier() { return identifier; }
        public Instant getStartTime() { return startTime; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public int getProcessedEvents() { return processedEvents; }
        public void setProcessedEvents(int processedEvents) { this.processedEvents = processedEvents; }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        
        public double getProgress() {
            return totalEvents > 0 ? (double) processedEvents / totalEvents : 0.0;
        }
    }
}