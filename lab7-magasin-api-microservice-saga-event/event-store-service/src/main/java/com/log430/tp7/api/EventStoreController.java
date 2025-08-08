package com.log430.tp7.api;

import com.log430.tp7.domain.Event;
import com.log430.tp7.domain.EventStore;
import com.log430.tp7.domain.OptimisticLockingException;
import com.log430.tp7.infrastructure.PostgreSQLEventStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Store", description = "Event Store API for managing domain events")
public class EventStoreController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventStoreController.class);
    
    private final EventStore eventStore;
    private final PostgreSQLEventStore postgreSQLEventStore;
    
    @Autowired
    public EventStoreController(EventStore eventStore, PostgreSQLEventStore postgreSQLEventStore) {
        this.eventStore = eventStore;
        this.postgreSQLEventStore = postgreSQLEventStore;
    }
    
    @PostMapping
    @Operation(summary = "Save a single event", description = "Saves a domain event to the event store")
    public ResponseEntity<?> saveEvent(@RequestBody Event event) {
        try {
            eventStore.saveEvent(event);
            logger.info("Event saved via API: {} for aggregate: {}", 
                       event.getEventType(), event.getAggregateId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (OptimisticLockingException e) {
            logger.warn("Optimistic locking conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("OPTIMISTIC_LOCK_CONFLICT", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to save event"));
        }
    }
    
    @PostMapping("/batch")
    @Operation(summary = "Save multiple events", description = "Saves multiple domain events atomically")
    public ResponseEntity<?> saveEvents(@RequestBody List<Event> events) {
        try {
            eventStore.saveEvents(events);
            logger.info("Batch saved {} events via API", events.size());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (OptimisticLockingException e) {
            logger.warn("Optimistic locking conflict in batch: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("OPTIMISTIC_LOCK_CONFLICT", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving events batch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to save events"));
        }
    }
    
    @GetMapping("/aggregate/{aggregateId}")
    @Operation(summary = "Get events for aggregate", description = "Retrieves all events for a specific aggregate")
    public ResponseEntity<List<Event>> getEventsForAggregate(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        List<Event> events = eventStore.getEvents(aggregateId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/aggregate/{aggregateId}/from/{version}")
    @Operation(summary = "Get events from version", description = "Retrieves events for an aggregate starting from a specific version")
    public ResponseEntity<List<Event>> getEventsFromVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Starting version") @PathVariable int version) {
        List<Event> events = eventStore.getEvents(aggregateId, version);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/aggregate/{aggregateId}/to/{version}")
    @Operation(summary = "Get events up to version", description = "Retrieves events for an aggregate up to a specific version")
    public ResponseEntity<List<Event>> getEventsUpToVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId,
            @Parameter(description = "Ending version") @PathVariable int version) {
        List<Event> events = eventStore.getEventsUpTo(aggregateId, version);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/type/{eventType}")
    @Operation(summary = "Get events by type", description = "Retrieves all events of a specific type")
    public ResponseEntity<List<Event>> getEventsByType(
            @Parameter(description = "Event type") @PathVariable String eventType) {
        List<Event> events = eventStore.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get events by correlation ID", description = "Retrieves events with a specific correlation ID")
    public ResponseEntity<List<Event>> getEventsByCorrelationId(
            @Parameter(description = "Correlation ID") @PathVariable UUID correlationId) {
        List<Event> events = eventStore.getEventsByCorrelationId(correlationId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/timerange")
    @Operation(summary = "Get events by time range", description = "Retrieves events within a specific time range")
    public ResponseEntity<List<Event>> getEventsByTimeRange(
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<Event> events = eventStore.getEventsByTimeRange(from, to);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID", description = "Retrieves a specific event by its ID")
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        Optional<Event> event = eventStore.getEventById(eventId);
        return event.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/aggregate/{aggregateId}/version")
    @Operation(summary = "Get latest version", description = "Gets the latest version number for an aggregate")
    public ResponseEntity<VersionResponse> getLatestVersion(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        int version = eventStore.getLatestVersion(aggregateId);
        return ResponseEntity.ok(new VersionResponse(aggregateId, version));
    }
    
    @GetMapping("/aggregate/{aggregateId}/exists")
    @Operation(summary = "Check if aggregate exists", description = "Checks if an aggregate has any events")
    public ResponseEntity<ExistsResponse> aggregateExists(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        boolean exists = eventStore.aggregateExists(aggregateId);
        return ResponseEntity.ok(new ExistsResponse(aggregateId, exists));
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get total event count", description = "Gets the total number of events in the store")
    public ResponseEntity<CountResponse> getTotalEventCount() {
        long count = eventStore.getTotalEventCount();
        return ResponseEntity.ok(new CountResponse(count));
    }
    
    @GetMapping("/aggregate/{aggregateId}/count")
    @Operation(summary = "Get event count for aggregate", description = "Gets the number of events for a specific aggregate")
    public ResponseEntity<CountResponse> getEventCountForAggregate(
            @Parameter(description = "Aggregate ID") @PathVariable String aggregateId) {
        long count = eventStore.getEventCount(aggregateId);
        return ResponseEntity.ok(new CountResponse(count));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search events by criteria", description = "Search events using multiple criteria")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam(required = false) String aggregateId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String aggregateType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toTime) {
        List<Event> events = postgreSQLEventStore.findEventsByCriteria(
            aggregateId, eventType, aggregateType, fromTime, toTime);
        return ResponseEntity.ok(events);
    }
    
    // Response DTOs
    public static class ErrorResponse {
        private String code;
        private String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
    
    public static class VersionResponse {
        private String aggregateId;
        private int version;
        
        public VersionResponse(String aggregateId, int version) {
            this.aggregateId = aggregateId;
            this.version = version;
        }
        
        public String getAggregateId() { return aggregateId; }
        public int getVersion() { return version; }
    }
    
    public static class ExistsResponse {
        private String aggregateId;
        private boolean exists;
        
        public ExistsResponse(String aggregateId, boolean exists) {
            this.aggregateId = aggregateId;
            this.exists = exists;
        }
        
        public String getAggregateId() { return aggregateId; }
        public boolean isExists() { return exists; }
    }
    
    public static class CountResponse {
        private long count;
        
        public CountResponse(long count) {
            this.count = count;
        }
        
        public long getCount() { return count; }
    }
}