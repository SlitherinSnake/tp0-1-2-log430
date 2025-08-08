package com.log430.tp7.event;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata container for domain events.
 * Provides extensible metadata storage for events.
 */
public class EventMetadata {
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("customProperties")
    private Map<String, Object> customProperties;
    
    public EventMetadata() {
        this.customProperties = new HashMap<>();
    }
    
    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public Map<String, Object> getCustomProperties() { return customProperties; }
    public void setCustomProperties(Map<String, Object> customProperties) { 
        this.customProperties = customProperties != null ? customProperties : new HashMap<>(); 
    }
    
    // Convenience methods for custom properties
    public void addProperty(String key, Object value) {
        this.customProperties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return this.customProperties.get(key);
    }
    
    public boolean hasProperty(String key) {
        return this.customProperties.containsKey(key);
    }
}