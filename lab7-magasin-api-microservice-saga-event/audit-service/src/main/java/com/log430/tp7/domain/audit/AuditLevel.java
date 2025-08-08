package com.log430.tp7.domain.audit;

/**
 * Enumeration representing different levels of audit importance and compliance requirements.
 * Used to categorize audit logs for filtering and compliance reporting.
 */
public enum AuditLevel {
    
    /**
     * Critical business events that require immediate attention and have
     * significant compliance implications (e.g., payment failures, transaction cancellations)
     */
    CRITICAL("Critical events requiring immediate attention"),
    
    /**
     * High-importance business events that should be monitored closely
     * (e.g., successful payments, inventory reservations)
     */
    HIGH("High-importance business events"),
    
    /**
     * Standard business events that are part of normal operations
     * (e.g., transaction creation, order fulfillment)
     */
    MEDIUM("Standard business events"),
    
    /**
     * Low-priority events primarily for operational visibility
     * (e.g., order status updates, notification events)
     */
    LOW("Low-priority operational events"),
    
    /**
     * Informational events for debugging and system monitoring
     * (e.g., service health checks, configuration changes)
     */
    INFO("Informational events for monitoring");
    
    private final String description;
    
    AuditLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines audit level based on event type.
     * @param eventType the event type to evaluate
     * @return appropriate audit level
     */
    public static AuditLevel fromEventType(String eventType) {
        if (eventType == null) {
            return INFO;
        }
        
        String lowerEventType = eventType.toLowerCase();
        
        // Critical events
        if (lowerEventType.contains("failed") || 
            lowerEventType.contains("cancelled") ||
            lowerEventType.contains("error") ||
            lowerEventType.contains("timeout")) {
            return CRITICAL;
        }
        
        // High priority events
        if (lowerEventType.contains("payment") ||
            lowerEventType.contains("refund") ||
            lowerEventType.contains("inventory") ||
            lowerEventType.contains("transaction")) {
            return HIGH;
        }
        
        // Medium priority events
        if (lowerEventType.contains("order") ||
            lowerEventType.contains("fulfilled") ||
            lowerEventType.contains("delivered") ||
            lowerEventType.contains("reserved")) {
            return MEDIUM;
        }
        
        // Low priority events
        if (lowerEventType.contains("notification") ||
            lowerEventType.contains("status") ||
            lowerEventType.contains("update")) {
            return LOW;
        }
        
        // Default to INFO for unknown event types
        return INFO;
    }
    
    /**
     * Checks if this audit level requires immediate notification.
     * @return true if the level is CRITICAL
     */
    public boolean requiresImmediateNotification() {
        return this == CRITICAL;
    }
    
    /**
     * Checks if this audit level requires compliance tracking.
     * @return true if the level is CRITICAL or HIGH
     */
    public boolean requiresComplianceTracking() {
        return this == CRITICAL || this == HIGH;
    }
}
