package com.log430.tp7.api;

import com.log430.tp7.application.AggregateState;
import com.log430.tp7.application.EventReplayService;
import com.log430.tp7.application.ReplayResult;
import com.log430.tp7.application.StateReconstructionService;
import com.log430.tp7.domain.Event;
import com.log430.tp7.domain.EventHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/replay")
@Tag(name = "Event Replay", description = "Event replay and state reconstruction API")
public class EventReplayController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventReplayController.class);
    
    private final EventReplayService replayService;
    private final StateReconstructionService stateReconstructionService;
    
    @Autowired
    public EventReplayController(EventReplayService replayService, 
                                StateReconstructionService stateReconstructionService) {
        this.replayService = replayService;
        this.stateReconstructionService = stateReconstructionService;
    }
    
    @PostMapping("/aggregate/{aggregateId}")
    @Operation(summary = "Replay events for aggregate", description = "Replays all events for a specific aggregate")
    public ResponseEntity<ReplayResult> replayAggregate(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            ReplayResult result = replayService.replayAggregate(aggregateId, handler);
            
            logger.info("Replay completed for aggregate {}: {}", aggregateId, result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during replay for aggregate {}: {}", aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/aggregate/{aggregateId}/from/{version}")
    @Operation(summary = "Replay events from version", description = "Replays events for an aggregate starting from a specific version")
    public ResponseEntity<ReplayResult> replayAggregateFromVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Starting version") @PathVariable int version) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            ReplayResult result = replayService.replayAggregateFromVersion(aggregateId, version, handler);
            
            logger.info("Replay from version {} completed for aggregate {}: {}", version, aggregateId, result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during replay from version {} for aggregate {}: {}", 
                        version, aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/aggregate/{aggregateId}/to/{version}")
    @Operation(summary = "Replay events up to version", description = "Replays events for an aggregate up to a specific version")
    public ResponseEntity<ReplayResult> replayAggregateUpToVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Ending version") @PathVariable int version) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            ReplayResult result = replayService.replayAggregateUpToVersion(aggregateId, version, handler);
            
            logger.info("Replay up to version {} completed for aggregate {}: {}", version, aggregateId, result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during replay up to version {} for aggregate {}: {}", 
                        version, aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/type/{eventType}")
    @Operation(summary = "Replay events by type", description = "Replays all events of a specific type")
    public ResponseEntity<ReplayResult> replayEventsByType(
            @Parameter(description = "Event type") @PathVariable String eventType) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            ReplayResult result = replayService.replayEventsByType(eventType, handler);
            
            logger.info("Replay by type {} completed: {}", eventType, result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during replay by type {}: {}", eventType, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/timerange")
    @Operation(summary = "Replay events by time range", description = "Replays events within a specific time range")
    public ResponseEntity<ReplayResult> replayEventsByTimeRange(
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            ReplayResult result = replayService.replayEventsByTimeRange(from, to, handler);
            
            logger.info("Replay by time range {} to {} completed: {}", from, to, result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during replay by time range {} to {}: {}", from, to, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/aggregate/{aggregateId}/async")
    @Operation(summary = "Replay events asynchronously", description = "Starts an asynchronous replay for an aggregate")
    public ResponseEntity<String> replayAggregateAsync(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        try {
            LoggingEventHandler handler = new LoggingEventHandler();
            CompletableFuture<ReplayResult> future = replayService.replayAggregateAsync(aggregateId, handler);
            
            logger.info("Async replay started for aggregate: {}", aggregateId);
            return ResponseEntity.accepted().body("Replay started for aggregate: " + aggregateId);
            
        } catch (Exception e) {
            logger.error("Error starting async replay for aggregate {}: {}", aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/status/{aggregateId}")
    @Operation(summary = "Get replay status", description = "Gets the status of an ongoing replay operation")
    public ResponseEntity<EventReplayService.ReplayStatus> getReplayStatus(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        EventReplayService.ReplayStatus status = replayService.getReplayStatus(aggregateId);
        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get all active replays", description = "Gets the status of all ongoing replay operations")
    public ResponseEntity<Map<String, EventReplayService.ReplayStatus>> getAllActiveReplays() {
        Map<String, EventReplayService.ReplayStatus> activeReplays = replayService.getActiveReplays();
        return ResponseEntity.ok(activeReplays);
    }
    
    @DeleteMapping("/cancel/{aggregateId}")
    @Operation(summary = "Cancel replay", description = "Cancels an ongoing replay operation")
    public ResponseEntity<String> cancelReplay(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        boolean cancelled = replayService.cancelReplay(aggregateId);
        if (cancelled) {
            logger.info("Replay cancellation requested for aggregate: {}", aggregateId);
            return ResponseEntity.ok("Replay cancellation requested for aggregate: " + aggregateId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // State reconstruction endpoints
    @GetMapping("/state/{aggregateId}")
    @Operation(summary = "Reconstruct current state", description = "Reconstructs the current state of an aggregate")
    public ResponseEntity<AggregateState> reconstructCurrentState(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        try {
            AggregateState state = stateReconstructionService.reconstructCurrentState(aggregateId);
            return ResponseEntity.ok(state);
            
        } catch (Exception e) {
            logger.error("Error reconstructing current state for aggregate {}: {}", 
                        aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/state/{aggregateId}/at-time")
    @Operation(summary = "Reconstruct state at time", description = "Reconstructs the state of an aggregate at a specific point in time")
    public ResponseEntity<AggregateState> reconstructStateAtTime(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Point in time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant pointInTime) {
        
        try {
            AggregateState state = stateReconstructionService.reconstructStateAtTime(aggregateId, pointInTime);
            return ResponseEntity.ok(state);
            
        } catch (Exception e) {
            logger.error("Error reconstructing state at time {} for aggregate {}: {}", 
                        pointInTime, aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/state/{aggregateId}/at-version/{version}")
    @Operation(summary = "Reconstruct state at version", description = "Reconstructs the state of an aggregate at a specific version")
    public ResponseEntity<AggregateState> reconstructStateAtVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Version") @PathVariable int version) {
        
        try {
            AggregateState state = stateReconstructionService.reconstructStateAtVersion(aggregateId, version);
            return ResponseEntity.ok(state);
            
        } catch (Exception e) {
            logger.error("Error reconstructing state at version {} for aggregate {}: {}", 
                        version, aggregateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/state/multiple")
    @Operation(summary = "Reconstruct multiple states", description = "Reconstructs the current state of multiple aggregates")
    public ResponseEntity<Map<String, AggregateState>> reconstructMultipleStates(
            @RequestBody List<String> aggregateIds) {
        
        try {
            Map<String, AggregateState> states = stateReconstructionService.reconstructMultipleStates(aggregateIds);
            return ResponseEntity.ok(states);
            
        } catch (Exception e) {
            logger.error("Error reconstructing multiple states: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/state/cache/{aggregateId}")
    @Operation(summary = "Invalidate state cache", description = "Invalidates the cached state for an aggregate")
    public ResponseEntity<String> invalidateStateCache(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        
        stateReconstructionService.invalidateCache(aggregateId);
        return ResponseEntity.ok("Cache invalidated for aggregate: " + aggregateId);
    }
    
    @DeleteMapping("/state/cache")
    @Operation(summary = "Clear state cache", description = "Clears all cached states")
    public ResponseEntity<String> clearStateCache() {
        stateReconstructionService.clearCache();
        return ResponseEntity.ok("All cached states cleared");
    }
    
    @GetMapping("/state/cache/statistics")
    @Operation(summary = "Get cache statistics", description = "Gets statistics about the state cache")
    public ResponseEntity<StateReconstructionService.CacheStatistics> getCacheStatistics() {
        StateReconstructionService.CacheStatistics stats = stateReconstructionService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Simple event handler that logs processed events
     */
    private static class LoggingEventHandler implements EventHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandler.class);
        
        @Override
        public void handle(Event event) {
            logger.info("Processing event: {} for aggregate: {} at version: {}", 
                       event.getEventType(), event.getAggregateId(), event.getEventVersion());
        }
        
        @Override
        public boolean canHandle(String eventType) {
            return true; // Can handle all event types
        }
        
        @Override
        public void onReplayStart(String aggregateId) {
            logger.info("Starting replay for: {}", aggregateId);
        }
        
        @Override
        public void onReplayComplete(String aggregateId, int eventCount) {
            logger.info("Completed replay for: {} with {} events", aggregateId, eventCount);
        }
        
        @Override
        public void onReplayError(String aggregateId, Event event, Exception error) {
            logger.error("Error during replay for: {} at event: {} - {}", 
                        aggregateId, event.getEventId(), error.getMessage());
        }
    }
}