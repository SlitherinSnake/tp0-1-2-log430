package com.log430.tp7.gateway.filter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.log430.tp7.gateway.model.ApiKeyEntity;
import com.log430.tp7.gateway.repository.ApiKeyRepository;

import reactor.core.publisher.Mono;

/**
 * API Key authentication filter for Spring Cloud Gateway.
 * Validates API keys for incoming requests.
 */
@Component
public class ApiKeyAuthenticationFilter extends AbstractGatewayFilterFactory<ApiKeyAuthenticationFilter.Config> {
    
    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    
    private final ApiKeyRepository apiKeyRepository;
    
    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        super(Config.class);
        this.apiKeyRepository = apiKeyRepository;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            
            // Skip authentication for health check endpoints
            if (path.contains("/actuator/health") || path.contains("/test")) {
                return chain.filter(exchange);
            }
            
            String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
            
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Missing API key for request to: {}", path);
                return handleUnauthorized(exchange, "Missing API key");
            }
            
            Optional<ApiKeyEntity> apiKeyEntity = apiKeyRepository.findByApiKey(apiKey);
            
            if (apiKeyEntity.isEmpty()) {
                log.warn("Invalid API key for request to: {}", path);
                return handleUnauthorized(exchange, "Invalid API key");
            }
            
            ApiKeyEntity entity = apiKeyEntity.get();
            
            if (!entity.isActive()) {
                log.warn("Inactive API key used for request to: {}", path);
                return handleUnauthorized(exchange, "API key is inactive");
            }
            
            // Add API key info to request headers for downstream services
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-API-Key-Name", entity.getClientName())
                            .header("X-API-Key-Id", entity.getId().toString())
                            .build())
                    .build();
            
            log.info("API key validated for request to: {} with key: {}", path, entity.getClientName());
            
            return chain.filter(modifiedExchange);
        };
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"%s\", \"status\": 401}", message);
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
    
    public static class Config {
        // Configuration properties can be added here if needed
    }
}
