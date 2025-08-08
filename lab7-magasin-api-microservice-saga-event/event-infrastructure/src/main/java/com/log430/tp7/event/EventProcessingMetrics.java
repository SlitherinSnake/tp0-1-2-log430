package com.log430.tp7.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics collection for event processing operations.
 * Tracks processing success rates, latencies, and error types by consumer.
 */
public class EventProcessingMetrics {
    
    private static final Logger logger = LoggerFactory.getLogger(EventProcessingMetrics.class);
    
    private final ConcurrentHashMap<String, AtomicLong> processedCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> duplicateCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalLatency = new ConcurrentHashMap<>();
    
    /**
     * Records a successful event processing.
     */
    public void recordEventProcessed(String eventType, String consumerName, long latencyMs) {
        String key = eventType + ":" + consumerName;
        processedCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        totalLatency.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(latencyMs);
        
        logger.debug("Event processed: type={}, consumer={}, latency={}ms", 
                    eventType, consumerName, latencyMs);
    }
    
    /**
     * Records an event processing error.
     */
    public void recordEventProcessingError(String eventType, String consumerName) {
        String key = eventType + ":" + consumerName;
        errorCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.debug("Event processing error: type={}, consumer={}", eventType, consumerName);
    }
    
    /**
     * Records a duplicate event (handled by idempotency).
     */
    public void recordDuplicateEvent(String eventType) {
        duplicateCounts.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.debug("Duplicate event detected: type={}", eventType);
    }
    
    /**
     * Gets the total number of processed events for a given type and consumer.
     */
    public long getProcessedCount(String eventType, String consumerName) {
        String key = eventType + ":" + consumerName;
        return processedCounts.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    /**
     * Gets the total number of errors for a given event type and consumer.
     */
    public long getErrorCount(String eventType, String consumerName) {
        String key = eventType + ":" + consumerName;
        return errorCounts.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    /**
     * Gets the total number of duplicate events for a given type.
     */
    public long getDuplicateCount(String eventType) {
        return duplicateCounts.getOrDefault(eventType, new AtomicLong(0)).get();
    }
    
    /**
     * Gets the average latency for processing events of a given type by a consumer.
     */
    public double getAverageLatency(String eventType, String consumerName) {
        long count = getProcessedCount(eventType, consumerName);
        if (count == 0) {
            return 0.0;
        }
        String key = eventType + ":" + consumerName;
        long total = totalLatency.getOrDefault(key, new AtomicLong(0)).get();
        return (double) total / count;
    }
    
    /**
     * Calculates success rate for a given event type and consumer.
     */
    public double getSuccessRate(String eventType, String consumerName) {
        long processed = getProcessedCount(eventType, consumerName);
        long errors = getErrorCount(eventType, consumerName);
        long total = processed + errors;
        
        if (total == 0) {
            return 0.0;
        }
        
        return (double) processed / total * 100.0;
    }
    
    /**
     * Resets all metrics. Useful for testing.
     */
    public void reset() {
        processedCounts.clear();
        errorCounts.clear();
        duplicateCounts.clear();
        totalLatency.clear();
    }
    
    /**
     * Logs current metrics summary.
     */
    public void logMetricsSummary() {
        logger.info("=== Event Processing Metrics Summary ===");
        
        processedCounts.forEach((key, count) -> {
            String[] parts = key.split(":");
            if (parts.length == 2) {
                String eventType = parts[0];
                String consumerName = parts[1];
                double avgLatency = getAverageLatency(eventType, consumerName);
                double successRate = getSuccessRate(eventType, consumerName);
                
                logger.info("Event: {}, Consumer: {}, Processed: {}, Success Rate: {:.1f}%, Avg Latency: {:.2f}ms", 
                           eventType, consumerName, count.get(), successRate, avgLatency);
            }
        });
        
        duplicateCounts.forEach((eventType, count) -> {
            logger.info("Duplicates - Event: {}, Count: {}", eventType, count.get());
        });
    }
}