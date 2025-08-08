package com.log430.tp7.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics collection for event publishing operations.
 * Tracks publishing success rates, latencies, and error types.
 */
public class EventPublishingMetrics {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublishingMetrics.class);
    
    private final ConcurrentHashMap<String, AtomicLong> publishedCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalLatency = new ConcurrentHashMap<>();
    
    /**
     * Records a successful event publication.
     */
    public void recordEventPublished(String eventType, long latencyMs) {
        publishedCounts.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        totalLatency.computeIfAbsent(eventType, k -> new AtomicLong(0)).addAndGet(latencyMs);
        
        logger.debug("Event published: type={}, latency={}ms", eventType, latencyMs);
    }
    
    /**
     * Records an event publishing error.
     */
    public void recordEventPublishingError(String eventType, String errorType) {
        String key = eventType + ":" + errorType;
        errorCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.debug("Event publishing error: type={}, error={}", eventType, errorType);
    }
    
    /**
     * Gets the total number of published events for a given type.
     */
    public long getPublishedCount(String eventType) {
        return publishedCounts.getOrDefault(eventType, new AtomicLong(0)).get();
    }
    
    /**
     * Gets the total number of errors for a given event type and error type.
     */
    public long getErrorCount(String eventType, String errorType) {
        String key = eventType + ":" + errorType;
        return errorCounts.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    /**
     * Gets the average latency for publishing events of a given type.
     */
    public double getAverageLatency(String eventType) {
        long count = getPublishedCount(eventType);
        if (count == 0) {
            return 0.0;
        }
        long total = totalLatency.getOrDefault(eventType, new AtomicLong(0)).get();
        return (double) total / count;
    }
    
    /**
     * Resets all metrics. Useful for testing.
     */
    public void reset() {
        publishedCounts.clear();
        errorCounts.clear();
        totalLatency.clear();
    }
    
    /**
     * Logs current metrics summary.
     */
    public void logMetricsSummary() {
        logger.info("=== Event Publishing Metrics Summary ===");
        
        publishedCounts.forEach((eventType, count) -> {
            double avgLatency = getAverageLatency(eventType);
            logger.info("Event Type: {}, Published: {}, Avg Latency: {:.2f}ms", 
                       eventType, count.get(), avgLatency);
        });
        
        errorCounts.forEach((key, count) -> {
            logger.info("Error: {}, Count: {}", key, count.get());
        });
    }
}