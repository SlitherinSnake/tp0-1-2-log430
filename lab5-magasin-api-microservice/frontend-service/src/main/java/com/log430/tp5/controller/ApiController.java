package com.log430.tp5.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    
    private final WebClient webClient;
    
    @Value("${gateway.base-url:http://localhost:8765}")
    private String gatewayBaseUrl;

    public ApiController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        logger.info("API call: health check endpoint");
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "frontend-service",
            "timestamp", System.currentTimeMillis()
        );
        logger.info("Health check response: {}", response);
        return response;
    }

    /**
     * Proxy for inventory stock decrease endpoint
     */
    @PatchMapping("/inventory/{id}/stock/decrease")
    public Mono<ResponseEntity<String>> decreaseStock(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        logger.info("Proxying stock decrease request for product ID: {}", id);
        
        return webClient.patch()
                .uri(gatewayBaseUrl + "/api/inventory/{id}/stock/decrease", id)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Stock decrease successful for product {}", id))
                .doOnError(error -> logger.error("Stock decrease failed for product {}: {}", id, error.getMessage()));
    }

    /**
     * Proxy for transactions endpoint
     */
    @PostMapping("/transactions")
    public Mono<ResponseEntity<String>> createTransaction(@RequestBody Map<String, Object> request) {
        logger.info("Proxying transaction creation request");
        
        return webClient.post()
                .uri(gatewayBaseUrl + "/api/transactions")
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Transaction created successfully"))
                .doOnError(error -> logger.error("Transaction creation failed: {}", error.getMessage()));
    }

    /**
     * Proxy for getting transactions by personnel
     */
    @GetMapping("/transactions/personnel/{personnelId}")
    public Mono<ResponseEntity<String>> getTransactionsByPersonnel(@PathVariable Long personnelId) {
        logger.info("Proxying transactions request for personnel ID: {}", personnelId);
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/transactions/personnel/{personnelId}", personnelId)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Transactions retrieved successfully for personnel {}", personnelId))
                .doOnError(error -> logger.error("Failed to retrieve transactions for personnel {}: {}", personnelId, error.getMessage()));
    }

    /**
     * Test endpoint to debug transaction service connectivity
     */
    @GetMapping("/test/transactions")
    public Mono<ResponseEntity<String>> testTransactions() {
        logger.info("Testing transaction service connectivity");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/transactions")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Test successful: {}", response.getStatusCode()))
                .doOnError(error -> logger.error("Test failed: {}", error.getMessage()));
    }
}
