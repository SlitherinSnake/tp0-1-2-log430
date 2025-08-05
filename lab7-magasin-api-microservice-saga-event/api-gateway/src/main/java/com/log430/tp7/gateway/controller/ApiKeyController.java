package com.log430.tp7.gateway.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp7.gateway.model.ApiKeyEntity;
import com.log430.tp7.gateway.repository.ApiKeyRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API Key management controller for the gateway.
 */
@Tag(name = "API Keys", description = "Gestion des clés API")
@RestController
@RequestMapping("/api/gateway/keys")
public class ApiKeyController {
    
    private static final Logger log = LoggerFactory.getLogger(ApiKeyController.class);
    
    // Constants for repeated string literals
    private static final String CLIENT_ID_KEY = "clientId";
    private static final String CLIENT_NAME_KEY = "clientName";
    private static final String ACTIVE_KEY = "active";
    private static final String CREATED_AT_KEY = "createdAt";
    
    private final ApiKeyRepository apiKeyRepository;
    
    public ApiKeyController(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }
    
    /**
     * Create a new API key.
     */
    @Operation(summary = "Créer une clé API", description = "Crée une nouvelle clé API pour un client.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createApiKey(@RequestBody CreateApiKeyRequest request) {
        log.info("Creating API key for client: {}", request.clientId());
        
        try {
            String apiKey = UUID.randomUUID().toString();
            
            ApiKeyEntity apiKeyEntity = new ApiKeyEntity(apiKey, request.clientId(), request.clientName());
            
            ApiKeyEntity savedEntity = apiKeyRepository.save(apiKeyEntity);
            
            Map<String, Object> response = Map.of(
                "id", savedEntity.getId(),
                "apiKey", savedEntity.getApiKey(),
                CLIENT_ID_KEY, savedEntity.getClientId(),
                CLIENT_NAME_KEY, savedEntity.getClientName(),
                ACTIVE_KEY, savedEntity.isActive(),
                CREATED_AT_KEY, savedEntity.getCreatedAt()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating API key: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all API keys.
     */
    @Operation(summary = "Lister les clés API", description = "Retourne toutes les clés API.")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllApiKeys() {
        log.info("Getting all API keys");
        
        try {
            List<Map<String, Object>> apiKeys = apiKeyRepository.findAll().stream()
                    .map(entity -> {
                        Map<String, Object> entityMap = new java.util.HashMap<>();
                        entityMap.put("id", entity.getId());
                        entityMap.put(CLIENT_ID_KEY, entity.getClientId());
                        entityMap.put(CLIENT_NAME_KEY, entity.getClientName());
                        entityMap.put(ACTIVE_KEY, entity.isActive());
                        entityMap.put(CREATED_AT_KEY, entity.getCreatedAt());
                        return entityMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(apiKeys);
        } catch (Exception e) {
            log.error("Error getting API keys: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get API key by ID.
     */
    @Operation(summary = "Obtenir une clé API", description = "Retourne une clé API par son ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApiKeyById(@PathVariable Long id) {
        log.info("Getting API key by ID: {}", id);
        
        try {
            return apiKeyRepository.findById(id)
                    .map(entity -> {
                        Map<String, Object> response = Map.of(
                            "id", entity.getId(),
                            CLIENT_ID_KEY, entity.getClientId(),
                            CLIENT_NAME_KEY, entity.getClientName(),
                            ACTIVE_KEY, entity.isActive(),
                            CREATED_AT_KEY, entity.getCreatedAt()
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            log.error("Error getting API key by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Test endpoint to verify gateway is working.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("API Gateway test endpoint called");
        Map<String, Object> response = Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "API Gateway is working"
        );
        return ResponseEntity.ok(response);
    }
    
    // Request DTOs
    public static class CreateApiKeyRequest {
        private String clientId;
        private String clientName;
        
        public CreateApiKeyRequest() {}
        
        public CreateApiKeyRequest(String clientId, String clientName) {
            this.clientId = clientId;
            this.clientName = clientName;
        }
        
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        
        // For convenience with the existing code
        public String clientId() { return clientId; }
        public String clientName() { return clientName; }
    }
}
