package com.log430.tp7.application;

import com.log430.tp7.domain.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of an event replay operation
 */
public class ReplayResult {
    
    private final String identifier;
    private Instant startTime;
    private Instant endTime;
    private int processedCount;
    private boolean success;
    private boolean cancelled;
    private String failureReason;
    private Event lastProcessedEvent;
    
    // Track issues and errors
    private final List<Event> skippedEvents = new ArrayList<>();
    private final List<Event> orderingIssues = new ArrayList<>();
    private final Map<Event, Exception> errors = new HashMap<>();
    
    public ReplayResult(String identifier) {
        this.identifier = identifier;
    }
    
    // Getters and setters
    public String getIdentifier() {
        return identifier;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public int getProcessedCount() {
        return processedCount;
    }
    
    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public Event getLastProcessedEvent() {
        return lastProcessedEvent;
    }
    
    public void setLastProcessedEvent(Event lastProcessedEvent) {
        this.lastProcessedEvent = lastProcessedEvent;
    }
    
    public List<Event> getSkippedEvents() {
        return new ArrayList<>(skippedEvents);
    }
    
    public void addSkippedEvent(Event event) {
        this.skippedEvents.add(event);
    }
    
    public List<Event> getOrderingIssues() {
        return new ArrayList<>(orderingIssues);
    }
    
    public void addOrderingIssue(Event event) {
        this.orderingIssues.add(event);
    }
    
    public Map<Event, Exception> getErrors() {
        return new HashMap<>(errors);
    }
    
    public void addError(Event event, Exception error) {
        this.errors.put(event, error);
    }
    
    // Computed properties
    public long getDurationMillis() {
        if (startTime != null && endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return 0;
    }
    
    public int getSkippedCount() {
        return skippedEvents.size();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getOrderingIssueCount() {
        return orderingIssues.size();
    }
    
    public boolean hasIssues() {
        return !skippedEvents.isEmpty() || !orderingIssues.isEmpty() || !errors.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format(
            "ReplayResult{identifier='%s', success=%s, processed=%d, skipped=%d, errors=%d, duration=%dms}",
            identifier, success, processedCount, getSkippedCount(), getErrorCount(), getDurationMillis()
        );
    }
}