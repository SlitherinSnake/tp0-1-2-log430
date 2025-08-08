package com.log430.tp7.application;

import com.log430.tp7.domain.Event;
import com.log430.tp7.domain.EventHandler;
import com.log430.tp7.domain.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for reconstructing aggregate state from events
 */
@Service
public class StateReconstructionService {
    
    private static final Logger logger = LoggerFactory.getLogger(StateReconstructionService.class);
    
    private final EventStore eventStore;
    private final EventReplayService replayService;
    
    // Cache for reconstructed states
    private final Map<String, AggregateState> stateCache = new ConcurrentHashMap<>();
    
    @Autowired
    public StateReconstructionService(EventStore eventStore, EventReplayService replayService) {
        this.eventStore = eventStore;
        this.replayService = replayService;
    }
    
    /**
     * Reconstruct the current state of an aggregate
     */
    public AggregateState reconstructCurrentState(String aggregateId) {
        logger.info("Reconstructing current state for aggregate: {}", aggregateId);
        
        // Check cache first
        AggregateState cachedState = stateCache.get(aggregateId);
        if (cachedState != null && isCacheValid(cachedState, aggregateId)) {
            logger.debug("Returning cached state for aggregate: {}", aggregateId);
            return cachedState;
        }
        
        // Reconstruct from events
        AggregateState state = new AggregateState(aggregateId);
        StateReconstructionHandler handler = new StateReconstructionHandler(state);
        
        ReplayResult result = replayService.replayAggregate(aggregateId, handler);
        
        if (result.isSuccess()) {
            state.setLastUpdated(Instant.now());
            state.setVersion(eventStore.getLatestVersion(aggregateId));
            stateCache.put(aggregateId, state);
            logger.info("Successfully reconstructed state for aggregate: {} with version: {}", 
                       aggregateId, state.getVersion());
        } else {
            logger.error("Failed to reconstruct state for aggregate: {}", aggregateId);
            throw new StateReconstructionException("Failed to reconstruct state for aggregate: " + aggregateId);
        }
        
        return state;
    }
    
    /**
     * Reconstruct state at a specific point in time
     */
    public AggregateState reconstructStateAtTime(String aggregateId, Instant pointInTime) {
        logger.info("Reconstructing state for aggregate: {} at time: {}", aggregateId, pointInTime);
        
        AggregateState state = new AggregateState(aggregateId);
        StateReconstructionHandler handler = new StateReconstructionHandler(state);
        
        // Get events up to the specified time
        List<Event> events = eventStore.getEventsByTimeRange(Instant.EPOCH, pointInTime);
        events = events.stream()
                      .filter(event -> event.getAggregateId().equals(aggregateId))
                      .toList();
        
        // Process events manually since we need time-based filtering
        handler.onReplayStart(aggregateId);
        for (Event event : events) {
            if (handler.canHandle(event.getEventType())) {
                handler.handle(event);
            }
        }
        handler.onReplayComplete(aggregateId, events.size());
        
        state.setLastUpdated(Instant.now());
        state.setReconstructionTime(pointInTime);
        
        logger.info("Successfully reconstructed historical state for aggregate: {}", aggregateId);
        return state;
    }
    
    /**
     * Reconstruct state at a specific version
     */
    public AggregateState reconstructStateAtVersion(String aggregateId, int version) {
        logger.info("Reconstructing state for aggregate: {} at version: {}", aggregateId, version);
        
        AggregateState state = new AggregateState(aggregateId);
        StateReconstructionHandler handler = new StateReconstructionHandler(state);
        
        ReplayResult result = replayService.replayAggregateUpToVersion(aggregateId, version, handler);
        
        if (result.isSuccess()) {
            state.setLastUpdated(Instant.now());
            state.setVersion(version);
            logger.info("Successfully reconstructed state for aggregate: {} at version: {}", 
                       aggregateId, version);
        } else {
            logger.error("Failed to reconstruct state for aggregate: {} at version: {}", 
                        aggregateId, version);
            throw new StateReconstructionException(
                "Failed to reconstruct state for aggregate: " + aggregateId + " at version: " + version);
        }
        
        return state;
    }
    
    /**
     * Get multiple aggregate states
     */
    public Map<String, AggregateState> reconstructMultipleStates(List<String> aggregateIds) {
        logger.info("Reconstructing states for {} aggregates", aggregateIds.size());
        
        Map<String, AggregateState> states = new HashMap<>();
        for (String aggregateId : aggregateIds) {
            try {
                states.put(aggregateId, reconstructCurrentState(aggregateId));
            } catch (Exception e) {
                logger.error("Failed to reconstruct state for aggregate: {}", aggregateId, e);
                // Continue with other aggregates
            }
        }
        
        return states;
    }
    
    /**
     * Clear cached state for an aggregate
     */
    public void invalidateCache(String aggregateId) {
        stateCache.remove(aggregateId);
        logger.debug("Invalidated cache for aggregate: {}", aggregateId);
    }
    
    /**
     * Clear all cached states
     */
    public void clearCache() {
        stateCache.clear();
        logger.info("Cleared all cached states");
    }
    
    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics(stateCache.size(), stateCache.keySet());
    }
    
    /**
     * Check if cached state is still valid
     */
    private boolean isCacheValid(AggregateState cachedState, String aggregateId) {
        int currentVersion = eventStore.getLatestVersion(aggregateId);
        return cachedState.getVersion() == currentVersion;
    }
    
    /**
     * Event handler for state reconstruction
     */
    private static class StateReconstructionHandler implements EventHandler {
        
        private final AggregateState state;
        
        public StateReconstructionHandler(AggregateState state) {
            this.state = state;
        }
        
        @Override
        public void handle(Event event) {
            // Apply event to state based on event type
            Map<String, Object> eventData = event.getEventData();
            
            switch (event.getEventType()) {
                case "TransactionCreated":
                    handleTransactionCreated(event, eventData);
                    break;
                case "TransactionCompleted":
                    handleTransactionCompleted(event, eventData);
                    break;
                case "TransactionCancelled":
                    handleTransactionCancelled(event, eventData);
                    break;
                case "PaymentProcessed":
                    handlePaymentProcessed(event, eventData);
                    break;
                case "PaymentFailed":
                    handlePaymentFailed(event, eventData);
                    break;
                case "PaymentRefunded":
                    handlePaymentRefunded(event, eventData);
                    break;
                case "InventoryReserved":
                    handleInventoryReserved(event, eventData);
                    break;
                case "InventoryUnavailable":
                    handleInventoryUnavailable(event, eventData);
                    break;
                case "InventoryReleased":
                    handleInventoryReleased(event, eventData);
                    break;
                case "OrderFulfilled":
                    handleOrderFulfilled(event, eventData);
                    break;
                case "OrderDelivered":
                    handleOrderDelivered(event, eventData);
                    break;
                default:
                    // Handle unknown event types generically
                    state.addGenericEvent(event.getEventType(), eventData);
                    break;
            }
            
            state.setLastEventId(event.getEventId());
            state.setLastEventTimestamp(event.getTimestamp());
        }
        
        @Override
        public boolean canHandle(String eventType) {
            // This handler can process all event types
            return true;
        }
        
        // Event-specific handlers
        private void handleTransactionCreated(Event event, Map<String, Object> eventData) {
            state.setProperty("status", "CREATED");
            state.setProperty("transactionId", eventData.get("transactionId"));
            state.setProperty("customerId", eventData.get("customerId"));
            state.setProperty("amount", eventData.get("amount"));
            state.setProperty("createdAt", event.getTimestamp());
        }
        
        private void handleTransactionCompleted(Event event, Map<String, Object> eventData) {
            state.setProperty("status", "COMPLETED");
            state.setProperty("completedAt", event.getTimestamp());
        }
        
        private void handleTransactionCancelled(Event event, Map<String, Object> eventData) {
            state.setProperty("status", "CANCELLED");
            state.setProperty("cancelledAt", event.getTimestamp());
            state.setProperty("cancellationReason", eventData.get("reason"));
        }
        
        private void handlePaymentProcessed(Event event, Map<String, Object> eventData) {
            state.setProperty("paymentStatus", "PROCESSED");
            state.setProperty("paymentId", eventData.get("paymentId"));
            state.setProperty("paymentProcessedAt", event.getTimestamp());
        }
        
        private void handlePaymentFailed(Event event, Map<String, Object> eventData) {
            state.setProperty("paymentStatus", "FAILED");
            state.setProperty("paymentFailureReason", eventData.get("reason"));
            state.setProperty("paymentFailedAt", event.getTimestamp());
        }
        
        private void handlePaymentRefunded(Event event, Map<String, Object> eventData) {
            state.setProperty("paymentStatus", "REFUNDED");
            state.setProperty("refundId", eventData.get("refundId"));
            state.setProperty("refundedAt", event.getTimestamp());
        }
        
        private void handleInventoryReserved(Event event, Map<String, Object> eventData) {
            state.setProperty("inventoryStatus", "RESERVED");
            state.setProperty("reservationId", eventData.get("reservationId"));
            state.setProperty("reservedAt", event.getTimestamp());
        }
        
        private void handleInventoryUnavailable(Event event, Map<String, Object> eventData) {
            state.setProperty("inventoryStatus", "UNAVAILABLE");
            state.setProperty("unavailableReason", eventData.get("reason"));
            state.setProperty("checkedAt", event.getTimestamp());
        }
        
        private void handleInventoryReleased(Event event, Map<String, Object> eventData) {
            state.setProperty("inventoryStatus", "RELEASED");
            state.setProperty("releasedAt", event.getTimestamp());
        }
        
        private void handleOrderFulfilled(Event event, Map<String, Object> eventData) {
            state.setProperty("fulfillmentStatus", "FULFILLED");
            state.setProperty("fulfillmentId", eventData.get("fulfillmentId"));
            state.setProperty("fulfilledAt", event.getTimestamp());
        }
        
        private void handleOrderDelivered(Event event, Map<String, Object> eventData) {
            state.setProperty("fulfillmentStatus", "DELIVERED");
            state.setProperty("deliveryId", eventData.get("deliveryId"));
            state.setProperty("deliveredAt", event.getTimestamp());
        }
    }
    
    /**
     * Exception for state reconstruction failures
     */
    public static class StateReconstructionException extends RuntimeException {
        public StateReconstructionException(String message) {
            super(message);
        }
        
        public StateReconstructionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Cache statistics
     */
    public static class CacheStatistics {
        private final int size;
        private final java.util.Set<String> aggregateIds;
        
        public CacheStatistics(int size, java.util.Set<String> aggregateIds) {
            this.size = size;
            this.aggregateIds = aggregateIds;
        }
        
        public int getSize() { return size; }
        public java.util.Set<String> getAggregateIds() { return aggregateIds; }
    }
}