package com.log430.tp7.gateway.filter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import reactor.core.publisher.Mono;

/**
 * Rate limiting filter for Spring Cloud Gateway.
 * Limits requests per client/API key.
 */
@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public RateLimitingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            
            if (apiKey == null) {
                // No API key, apply default rate limiting
                apiKey = "anonymous";
            }
            
            Bucket bucket = buckets.computeIfAbsent(apiKey, this::createBucket);
            
            if (bucket.tryConsume(1)) {
                return chain.filter(exchange);
            } else {
                log.warn("Rate limit exceeded for API key: {}", apiKey);
                return handleRateLimitExceeded(exchange);
            }
        };
    }
    
    private Bucket createBucket(String apiKey) {
        // Default: 100 requests per minute
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
    
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\": \"Rate limit exceeded\", \"status\": 429}";
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
    
    public static class Config {
        private int requestsPerMinute = 100;
        
        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }
        
        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}
