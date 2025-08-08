package com.log430.tp7.sagaorchestrator.client;

import com.log430.tp7.sagaorchestrator.dto.StockReservationRequest;
import com.log430.tp7.sagaorchestrator.dto.StockReservationResponse;
import com.log430.tp7.sagaorchestrator.dto.StockVerificationRequest;
import com.log430.tp7.sagaorchestrator.dto.StockVerificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with the inventory service
 */
@Component
public class InventoryServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;
    
    public InventoryServiceClient(RestTemplate restTemplate, 
                                @Value("${services.inventory.url:http://inventory-service:8081}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }
    
    /**
     * Verifies if stock is available for the requested product and quantity
     */
    public StockVerificationResponse verifyStock(String productId, Integer quantity, String sagaId) {
        try {
            StockVerificationRequest request = new StockVerificationRequest(productId, quantity, sagaId);
            
            logger.info("Verifying stock for product {} with quantity {} for saga {}", 
                       productId, quantity, sagaId);
            
            ResponseEntity<StockVerificationResponse> response = restTemplate.postForEntity(
                inventoryServiceUrl + "/api/v1/inventory/verify-stock",
                request,
                StockVerificationResponse.class
            );
            
            StockVerificationResponse result = response.getBody();
            if (result != null && result.success()) {
                logger.info("Stock verification successful for saga {}: available={}", 
                           sagaId, result.available());
            } else {
                logger.warn("Stock verification failed for saga {}: {}", 
                           sagaId, result != null ? result.message() : "No response body");
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during stock verification for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return StockVerificationResponse.failure(productId, 
                "Stock verification failed: " + e.getResponseBodyAsString());
                
        } catch (HttpServerErrorException e) {
            logger.error("Server error during stock verification for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return StockVerificationResponse.failure(productId, 
                "Inventory service error: " + e.getMessage());
                
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during stock verification for saga {}: {}", 
                        sagaId, e.getMessage());
            return StockVerificationResponse.failure(productId, 
                "Inventory service unavailable: " + e.getMessage());
                
        } catch (Exception e) {
            logger.error("Unexpected error during stock verification for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return StockVerificationResponse.failure(productId, 
                "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Reserves stock for the specified product and quantity
     */
    public StockReservationResponse reserveStock(String productId, Integer quantity, String sagaId, String customerId) {
        try {
            StockReservationRequest request = new StockReservationRequest(productId, quantity, sagaId, customerId);
            
            logger.info("Reserving stock for product {} with quantity {} for saga {}", 
                       productId, quantity, sagaId);
            
            ResponseEntity<StockReservationResponse> response = restTemplate.postForEntity(
                inventoryServiceUrl + "/api/v1/inventory/reserve-stock",
                request,
                StockReservationResponse.class
            );
            
            StockReservationResponse result = response.getBody();
            if (result != null && result.success()) {
                logger.info("Stock reservation successful for saga {}: reservationId={}", 
                           sagaId, result.reservationId());
            } else {
                logger.warn("Stock reservation failed for saga {}: {}", 
                           sagaId, result != null ? result.message() : "No response body");
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during stock reservation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return StockReservationResponse.failure(productId, sagaId, 
                "Stock reservation failed: " + e.getResponseBodyAsString());
                
        } catch (HttpServerErrorException e) {
            logger.error("Server error during stock reservation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return StockReservationResponse.failure(productId, sagaId, 
                "Inventory service error: " + e.getMessage());
                
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during stock reservation for saga {}: {}", 
                        sagaId, e.getMessage());
            return StockReservationResponse.failure(productId, sagaId, 
                "Inventory service unavailable: " + e.getMessage());
                
        } catch (Exception e) {
            logger.error("Unexpected error during stock reservation for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return StockReservationResponse.failure(productId, sagaId, 
                "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Releases a stock reservation
     */
    public boolean releaseStock(String reservationId, String sagaId) {
        try {
            logger.info("Releasing stock reservation {} for saga {}", reservationId, sagaId);
            
            restTemplate.delete(inventoryServiceUrl + "/api/v1/inventory/reservations/" + reservationId);
            
            logger.info("Stock reservation {} released successfully for saga {}", reservationId, sagaId);
            return true;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Stock reservation {} not found for saga {} - may already be released", 
                           reservationId, sagaId);
                return true; // Consider it successful if already released
            }
            logger.error("Client error during stock release for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
            
        } catch (HttpServerErrorException e) {
            logger.error("Server error during stock release for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
            
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during stock release for saga {}: {}", 
                        sagaId, e.getMessage());
            return false;
            
        } catch (Exception e) {
            logger.error("Unexpected error during stock release for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return false;
        }
    }
}