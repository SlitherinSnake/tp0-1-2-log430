package com.log430.tp7.application;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the reconstructed state of an aggregate
 */
public class AggregateState {
    
    private final String aggregateId;
    private int version;
    private Instant lastUpdated;
    private Instant reconstructionTime;
    private UUID lastEventId;
    private Instant lastEventTimestamp;
    
    // State properties
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    
    // Generic events that don't have specific handlers
    private final Map<String, Object> genericEvents = new HashMap<>();
    
    public AggregateState(String aggregateId) {
        this.aggregateId = aggregateId;
        this.lastUpdated = Instant.now();
    }
    
    // Getters and setters
    public String getAggregateId() {
        return aggregateId;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Instant getReconstructionTime() {
        return reconstructionTime;
    }
    
    public void setReconstructionTime(Instant reconstructionTime) {
        this.reconstructionTime = reconstructionTime;
    }
    
    public UUID getLastEventId() {
        return lastEventId;
    }
    
    public void setLastEventId(UUID lastEventId) {
        this.lastEventId = lastEventId;
    }
    
    public Instant getLastEventTimestamp() {
        return lastEventTimestamp;
    }
    
    public void setLastEventTimestamp(Instant lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }
    
    // Property management
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        return null;
    }
    
    public String getStringProperty(String key) {
        return getProperty(key, String.class);
    }
    
    public Integer getIntegerProperty(String key) {
        return getProperty(key, Integer.class);
    }
    
    public Boolean getBooleanProperty(String key) {
        return getProperty(key, Boolean.class);
    }
    
    public Instant getInstantProperty(String key) {
        return getProperty(key, Instant.class);
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }
    
    // Generic event management
    public void addGenericEvent(String eventType, Map<String, Object> eventData) {
        genericEvents.put(eventType, eventData);
    }
    
    public Map<String, Object> getGenericEvents() {
        return new HashMap<>(genericEvents);
    }
    
    // Convenience methods for common business states
    public String getStatus() {
        return getStringProperty("status");
    }
    
    public String getPaymentStatus() {
        return getStringProperty("paymentStatus");
    }
    
    public String getInventoryStatus() {
        return getStringProperty("inventoryStatus");
    }
    
    public String getFulfillmentStatus() {
        return getStringProperty("fulfillmentStatus");
    }
    
    public String getTransactionId() {
        return getStringProperty("transactionId");
    }
    
    public String getCustomerId() {
        return getStringProperty("customerId");
    }
    
    public Object getAmount() {
        return getProperty("amount");
    }
    
    public Instant getCreatedAt() {
        return getInstantProperty("createdAt");
    }
    
    public Instant getCompletedAt() {
        return getInstantProperty("completedAt");
    }
    
    public Instant getCancelledAt() {
        return getInstantProperty("cancelledAt");
    }
    
    // State validation methods
    public boolean isCompleted() {
        return "COMPLETED".equals(getStatus());
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(getStatus());
    }
    
    public boolean isPaymentProcessed() {
        return "PROCESSED".equals(getPaymentStatus());
    }
    
    public boolean isInventoryReserved() {
        return "RESERVED".equals(getInventoryStatus());
    }
    
    public boolean isFulfilled() {
        return "FULFILLED".equals(getFulfillmentStatus()) || 
               "DELIVERED".equals(getFulfillmentStatus());
    }
    
    // Summary methods
    public StateSummary getSummary() {
        return new StateSummary(
            aggregateId,
            version,
            getStatus(),
            getPaymentStatus(),
            getInventoryStatus(),
            getFulfillmentStatus(),
            lastUpdated,
            properties.size()
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "AggregateState{aggregateId='%s', version=%d, status='%s', properties=%d, lastUpdated=%s}",
            aggregateId, version, getStatus(), properties.size(), lastUpdated
        );
    }
    
    /**
     * Summary representation of aggregate state
     */
    public static class StateSummary {
        private final String aggregateId;
        private final int version;
        private final String status;
        private final String paymentStatus;
        private final String inventoryStatus;
        private final String fulfillmentStatus;
        private final Instant lastUpdated;
        private final int propertyCount;
        
        public StateSummary(String aggregateId, int version, String status, 
                           String paymentStatus, String inventoryStatus, String fulfillmentStatus,
                           Instant lastUpdated, int propertyCount) {
            this.aggregateId = aggregateId;
            this.version = version;
            this.status = status;
            this.paymentStatus = paymentStatus;
            this.inventoryStatus = inventoryStatus;
            this.fulfillmentStatus = fulfillmentStatus;
            this.lastUpdated = lastUpdated;
            this.propertyCount = propertyCount;
        }
        
        // Getters
        public String getAggregateId() { return aggregateId; }
        public int getVersion() { return version; }
        public String getStatus() { return status; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getInventoryStatus() { return inventoryStatus; }
        public String getFulfillmentStatus() { return fulfillmentStatus; }
        public Instant getLastUpdated() { return lastUpdated; }
        public int getPropertyCount() { return propertyCount; }
    }
}