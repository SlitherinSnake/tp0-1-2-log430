package com.log430.tp7.infrastructure.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a distributed trace across multiple services and events.
 * Tracks spans, timing, errors, and service involvement.
 */
public class EventTrace {
    
    private final String correlationId;
    private final String traceId;
    private final long startTime;
    private long endTime;
    private boolean completed;
    private boolean successful = true;
    
    // Span tracking
    private final List<TraceSpan> spans = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, TraceSpan> activeSpans = new ConcurrentHashMap<>();
    
    // Error tracking
    private final List<TraceError> errors = Collections.synchronizedList(new ArrayList<>());
    
    // Service involvement tracking
    private final Set<String> involvedServices = Collections.synchronizedSet(new HashSet<>());
    
    // Metrics
    private final AtomicLong spanCount = new AtomicLong(0);
    
    public EventTrace(String correlationId, String traceId, long startTime) {
        this.correlationId = correlationId;
        this.traceId = traceId;
        this.startTime = startTime;
        this.completed = false;
    }
    
    /**
     * Add a new span to the trace.
     */
    public TraceSpan addSpan(String serviceName, String eventType, long startTime) {
        String spanId = serviceName + ":" + eventType + ":" + System.nanoTime();
        TraceSpan span = new TraceSpan(spanId, serviceName, eventType, startTime);
        
        spans.add(span);
        activeSpans.put(spanId, span);
        involvedServices.add(serviceName);
        spanCount.incrementAndGet();
        
        return span;
    }
    
    /**
     * End the current span for a service and event type.
     */
    public void endCurrentSpan(String serviceName, String eventType, long endTime, boolean success) {
        // Find the most recent active span for this service and event type
        TraceSpan spanToEnd = activeSpans.values().stream()
            .filter(span -> span.getServiceName().equals(serviceName) && 
                          span.getEventType().equals(eventType) && 
                          !span.isCompleted())
            .max(Comparator.comparing(TraceSpan::getStartTime))
            .orElse(null);
        
        if (spanToEnd != null) {
            spanToEnd.complete(endTime, success);
            activeSpans.remove(spanToEnd.getSpanId());
            
            if (!success) {
                this.successful = false;
            }
        }
    }
    
    /**
     * Add an error to the trace.
     */
    public void addError(String serviceName, String eventType, String errorType, 
                        String errorMessage, long timestamp) {
        TraceError error = new TraceError(serviceName, eventType, errorType, errorMessage, timestamp);
        errors.add(error);
        this.successful = false;
    }
    
    /**
     * Complete the entire trace.
     */
    public void complete(long endTime, boolean success) {
        this.endTime = endTime;
        this.completed = true;
        
        if (!success) {
            this.successful = false;
        }
        
        // Mark any remaining active spans as completed
        for (TraceSpan activeSpan : activeSpans.values()) {
            if (!activeSpan.isCompleted()) {
                activeSpan.complete(endTime, success);
            }
        }
        activeSpans.clear();
    }
    
    /**
     * Get total duration of the trace.
     */
    public long getTotalDuration() {
        if (completed) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Get the longest span duration.
     */
    public long getLongestSpanDuration() {
        return spans.stream()
            .filter(TraceSpan::isCompleted)
            .mapToLong(TraceSpan::getDuration)
            .max()
            .orElse(0L);
    }
    
    /**
     * Get spans for a specific service.
     */
    public List<TraceSpan> getSpansForService(String serviceName) {
        return spans.stream()
            .filter(span -> span.getServiceName().equals(serviceName))
            .toList();
    }
    
    /**
     * Get trace summary for logging/monitoring.
     */
    public TraceSummary getSummary() {
        return new TraceSummary(
            correlationId,
            traceId,
            startTime,
            getTotalDuration(),
            spans.size(),
            involvedServices.size(),
            errors.size(),
            successful,
            completed
        );
    }
    
    // Getters
    public String getCorrelationId() { return correlationId; }
    public String getTraceId() { return traceId; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public boolean isCompleted() { return completed; }
    public boolean isSuccessful() { return successful; }
    public List<TraceSpan> getSpans() { return new ArrayList<>(spans); }
    public List<TraceError> getErrors() { return new ArrayList<>(errors); }
    public Set<String> getInvolvedServices() { return new HashSet<>(involvedServices); }
    public long getSpanCount() { return spanCount.get(); }
    
    /**
     * Represents a single span within a trace.
     */
    public static class TraceSpan {
        private final String spanId;
        private final String serviceName;
        private final String eventType;
        private final long startTime;
        private long endTime;
        private boolean completed;
        private boolean successful = true;
        private final Map<String, String> tags = new ConcurrentHashMap<>();
        
        public TraceSpan(String spanId, String serviceName, String eventType, long startTime) {
            this.spanId = spanId;
            this.serviceName = serviceName;
            this.eventType = eventType;
            this.startTime = startTime;
            this.completed = false;
        }
        
        public void complete(long endTime, boolean success) {
            this.endTime = endTime;
            this.completed = true;
            this.successful = success;
        }
        
        public void addTag(String key, String value) {
            tags.put(key, value);
        }
        
        public long getDuration() {
            if (completed) {
                return endTime - startTime;
            }
            return System.currentTimeMillis() - startTime;
        }
        
        // Getters
        public String getSpanId() { return spanId; }
        public String getServiceName() { return serviceName; }
        public String getEventType() { return eventType; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public boolean isCompleted() { return completed; }
        public boolean isSuccessful() { return successful; }
        public Map<String, String> getTags() { return new HashMap<>(tags); }
    }
    
    /**
     * Represents an error within a trace.
     */
    public static class TraceError {
        private final String serviceName;
        private final String eventType;
        private final String errorType;
        private final String errorMessage;
        private final long timestamp;
        
        public TraceError(String serviceName, String eventType, String errorType, 
                         String errorMessage, long timestamp) {
            this.serviceName = serviceName;
            this.eventType = eventType;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getEventType() { return eventType; }
        public String getErrorType() { return errorType; }
        public String getErrorMessage() { return errorMessage; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Summary of trace information for monitoring.
     */
    public static class TraceSummary {
        private final String correlationId;
        private final String traceId;
        private final long startTime;
        private final long duration;
        private final int spanCount;
        private final int serviceCount;
        private final int errorCount;
        private final boolean successful;
        private final boolean completed;
        
        public TraceSummary(String correlationId, String traceId, long startTime, 
                           long duration, int spanCount, int serviceCount, 
                           int errorCount, boolean successful, boolean completed) {
            this.correlationId = correlationId;
            this.traceId = traceId;
            this.startTime = startTime;
            this.duration = duration;
            this.spanCount = spanCount;
            this.serviceCount = serviceCount;
            this.errorCount = errorCount;
            this.successful = successful;
            this.completed = completed;
        }
        
        // Getters
        public String getCorrelationId() { return correlationId; }
        public String getTraceId() { return traceId; }
        public long getStartTime() { return startTime; }
        public long getDuration() { return duration; }
        public int getSpanCount() { return spanCount; }
        public int getServiceCount() { return serviceCount; }
        public int getErrorCount() { return errorCount; }
        public boolean isSuccessful() { return successful; }
        public boolean isCompleted() { return completed; }
    }
}
