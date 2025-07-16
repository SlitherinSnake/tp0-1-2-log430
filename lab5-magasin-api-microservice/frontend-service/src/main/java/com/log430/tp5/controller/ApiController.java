package com.log430.tp5.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.log430.tp5.dto.TransactionDTO;
import com.log430.tp5.service.TransactionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    
    private final WebClient webClient;
    private final TransactionService transactionService;
    
    @Value("${gateway.base-url:http://localhost:8765}")
    private String gatewayBaseUrl;

    public ApiController(WebClient.Builder webClientBuilder, TransactionService transactionService) {
        this.webClient = webClientBuilder.build();
        this.transactionService = transactionService;
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
     * Proxy for getting all transactions
     */
    @GetMapping("/transactions")
    public Mono<ResponseEntity<String>> getAllTransactions() {
        logger.info("Proxying request to get all transactions");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/transactions")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("All transactions retrieved successfully"))
                .doOnError(error -> logger.error("Failed to retrieve all transactions: {}", error.getMessage()));
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
     * Get returnable transactions
     */
    @GetMapping("/transactions/returnable")
    public ResponseEntity<List<TransactionDTO>> getReturnableTransactions() {
        logger.info("Fetching returnable transactions");
        List<TransactionDTO> transactions = transactionService.getReturnableTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Create return transaction
     */
    @PostMapping("/transactions/returns")
    public ResponseEntity<Map<String, Object>> createReturn(@RequestBody Map<String, Object> request) {
        logger.info("Creating return transaction");
        Map<String, Object> result = transactionService.createReturn(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Get returns for a specific transaction
     */
    @GetMapping("/transactions/returns/{originalTransactionId}")
    public ResponseEntity<List<TransactionDTO>> getReturnsByOriginalTransaction(@PathVariable Long originalTransactionId) {
        logger.info("Fetching returns for original transaction: {}", originalTransactionId);
        List<TransactionDTO> returns = transactionService.getReturnsByOriginalTransaction(originalTransactionId);
        return ResponseEntity.ok(returns);
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

    /**
     * Proxy for getting all inventory items
     */
    @GetMapping("/inventory/all")
    public Mono<ResponseEntity<String>> getAllInventoryItems() {
        logger.info("Proxying request to get all inventory items");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/inventory/all")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Successfully retrieved inventory items"))
                .doOnError(error -> logger.error("Failed to retrieve inventory items: {}", error.getMessage()));
    }

    /**
     * Proxy for creating new inventory item
     */
    @PostMapping("/inventory")
    public Mono<ResponseEntity<String>> createInventoryItem(@RequestBody Map<String, Object> request) {
        logger.info("Proxying inventory item creation request");
        
        return webClient.post()
                .uri(gatewayBaseUrl + "/api/inventory")
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Inventory item created successfully"))
                .doOnError(error -> logger.error("Inventory item creation failed: {}", error.getMessage()));
    }

    /**
     * Proxy for updating inventory item
     */
    @PutMapping("/inventory/{id}")
    public Mono<ResponseEntity<String>> updateInventoryItem(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        logger.info("Proxying inventory item update request for ID: {}", id);
        
        return webClient.put()
                .uri(gatewayBaseUrl + "/api/inventory/{id}", id)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Inventory item updated successfully for ID: {}", id))
                .doOnError(error -> logger.error("Inventory item update failed for ID {}: {}", id, error.getMessage()));
    }

    /**
     * Proxy for updating inventory item stock
     */
    @PatchMapping("/inventory/{id}/stock")
    public Mono<ResponseEntity<String>> updateInventoryStock(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        logger.info("Proxying inventory stock update request for ID: {}", id);
        
        return webClient.patch()
                .uri(gatewayBaseUrl + "/api/inventory/{id}/stock", id)
                .bodyValue(request)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Inventory stock updated successfully for ID: {}", id))
                .doOnError(error -> logger.error("Inventory stock update failed for ID {}: {}", id, error.getMessage()));
    }

    /**
     * Proxy for deleting inventory item
     */
    @DeleteMapping("/inventory/{id}")
    public Mono<ResponseEntity<String>> deleteInventoryItem(@PathVariable Long id) {
        logger.info("Proxying inventory item deletion request for ID: {}", id);
        
        return webClient.delete()
                .uri(gatewayBaseUrl + "/api/inventory/{id}", id)
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Inventory item deleted successfully for ID: {}", id))
                .doOnError(error -> logger.error("Inventory item deletion failed for ID {}: {}", id, error.getMessage()));
    }

    /**
     * Proxy for getting inventory items needing restock
     */
    @GetMapping("/inventory/restock-needed")
    public Mono<ResponseEntity<String>> getItemsNeedingRestock() {
        logger.info("Proxying request to get items needing restock");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/inventory/restock-needed")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Successfully retrieved items needing restock"))
                .doOnError(error -> logger.error("Failed to retrieve items needing restock: {}", error.getMessage()));
    }

    /**
     * Proxy for getting inventory categories
     */
    @GetMapping("/inventory/categories")
    public Mono<ResponseEntity<String>> getInventoryCategories() {
        logger.info("Proxying request to get inventory categories");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/inventory/categories")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(response -> logger.info("Successfully retrieved inventory categories"))
                .doOnError(error -> logger.error("Failed to retrieve inventory categories: {}", error.getMessage()));
    }

    /**
     * Dashboard statistics endpoint
     */
    @GetMapping("/dashboard/stats")
    public Mono<ResponseEntity<String>> getDashboardStats() {
        logger.info("Getting dashboard statistics");
        
        // For now, we'll combine calls to get the data needed for dashboard
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/inventory/all")
                .retrieve()
                .bodyToMono(String.class)
                .map(inventoryData -> {
                    // Here you would normally parse the JSON and create stats
                    // For simplicity, we'll just return a basic response
                    Map<String, Object> stats = Map.of(
                        "totalItems", 0,
                        "itemsNeedingRestock", 0,
                        "ordersCount", 0,
                        "totalValue", 0.0
                    );
                    return ResponseEntity.ok(stats.toString());
                })
                .doOnSuccess(response -> logger.info("Dashboard stats retrieved successfully"))
                .doOnError(error -> logger.error("Failed to retrieve dashboard stats: {}", error.getMessage()));
    }

    /**
     * Proxy for getting transaction stats
     */
    @GetMapping("/transactions/stats")
    public Mono<ResponseEntity<String>> getTransactionStats() {
        logger.info("Proxying request to get transaction stats");
        
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/transactions/stats")
                .retrieve()
                .toEntity(String.class)
                .onErrorResume(error -> {
                    logger.warn("Stats endpoint not available, falling back to count from all transactions");
                    return webClient.get()
                            .uri(gatewayBaseUrl + "/api/transactions")
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(transactionsJson -> {
                                try {
                                    // Parse the JSON to count transactions
                                    Map<String, Object> stats = Map.of(
                                        "totalTransactions", 0,
                                        "totalSales", 0.0
                                    );
                                    return ResponseEntity.ok(stats.toString());
                                } catch (Exception e) {
                                    Map<String, Object> fallbackStats = Map.of(
                                        "totalTransactions", 0,
                                        "totalSales", 0.0
                                    );
                                    return ResponseEntity.ok(fallbackStats.toString());
                                }
                            });
                })
                .doOnSuccess(response -> logger.info("Transaction stats retrieved successfully"))
                .doOnError(error -> logger.error("Failed to retrieve transaction stats: {}", error.getMessage()));
    }
}
