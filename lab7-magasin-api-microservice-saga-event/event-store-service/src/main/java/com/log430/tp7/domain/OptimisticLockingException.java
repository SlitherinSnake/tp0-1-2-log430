package com.log430.tp7.domain;

/**
 * Exception thrown when optimistic concurrency control detects a conflict
 */
public class OptimisticLockingException extends RuntimeException {
    
    private final String aggregateId;
    private final int expectedVersion;
    private final int actualVersion;
    
    public OptimisticLockingException(String aggregateId, int expectedVersion, int actualVersion) {
        super(String.format("Optimistic locking failed for aggregate %s. Expected version: %d, Actual version: %d", 
              aggregateId, expectedVersion, actualVersion));
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
    
    public OptimisticLockingException(String message, String aggregateId, int expectedVersion, int actualVersion) {
        super(message);
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public int getExpectedVersion() {
        return expectedVersion;
    }
    
    public int getActualVersion() {
        return actualVersion;
    }
}