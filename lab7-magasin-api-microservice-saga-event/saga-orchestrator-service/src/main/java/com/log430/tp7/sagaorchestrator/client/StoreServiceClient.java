package com.log430.tp7.sagaorchestrator.client;

import com.log430.tp7.sagaorchestrator.dto.OrderRequest;
import com.log430.tp7.sagaorchestrator.dto.OrderResponse;
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

import java.math.BigDecimal;

/**
 * Client for communicating with the store service
 */
@Component
public class StoreServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(StoreServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String storeServiceUrl;
    
    public StoreServiceClient(RestTemplate restTemplate, 
                            @Value("${services.store.url:http://store-service:8083}") String storeServiceUrl) {
        this.restTemplate = restTemplate;
        this.storeServiceUrl = storeServiceUrl;
    }
    
    /**
     * Creates an order in the store service
     */
    public OrderResponse createOrder(String customerId, String productId, Integer quantity, 
                                   BigDecimal amount, String sagaId, String stockReservationId,
                                   String paymentTransactionId, String shippingAddress) {
        try {
            OrderRequest request = new OrderRequest(
                customerId, productId, quantity, amount, sagaId,
                stockReservationId, paymentTransactionId, shippingAddress,
                String.format("Order created via saga %s", sagaId)
            );
            
            logger.info("Creating order for saga {} - customer: {}, product: {}, quantity: {}", 
                       sagaId, customerId, productId, quantity);
            
            ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                storeServiceUrl + "/api/v1/orders/create-order",
                request,
                OrderResponse.class
            );
            
            OrderResponse result = response.getBody();
            if (result != null && result.success()) {
                logger.info("Order created successfully for saga {}: orderId={}, orderNumber={}", 
                           sagaId, result.orderId(), result.orderNumber());
            } else {
                logger.warn("Order creation failed for saga {}: {}", 
                           sagaId, result != null ? result.message() : "No response body");
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during order creation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            
            String errorMessage;
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                errorMessage = "Invalid order data: " + e.getResponseBodyAsString();
            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                errorMessage = "Order conflict: " + e.getResponseBodyAsString();
            } else {
                errorMessage = "Order creation failed: " + e.getResponseBodyAsString();
            }
            
            return OrderResponse.failure(customerId, productId, sagaId, errorMessage);
                
        } catch (HttpServerErrorException e) {
            logger.error("Server error during order creation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return OrderResponse.failure(customerId, productId, sagaId, 
                "Store service error: " + e.getMessage());
                
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during order creation for saga {}: {}", 
                        sagaId, e.getMessage());
            return OrderResponse.failure(customerId, productId, sagaId, 
                "Store service unavailable: " + e.getMessage());
                
        } catch (Exception e) {
            logger.error("Unexpected error during order creation for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return OrderResponse.failure(customerId, productId, sagaId, 
                "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Cancels an order (for compensation scenarios)
     */
    public boolean cancelOrder(String orderId, String sagaId) {
        try {
            logger.info("Cancelling order {} for saga {}", orderId, sagaId);
            
            restTemplate.postForEntity(
                storeServiceUrl + "/api/v1/orders/" + orderId + "/cancel",
                null,
                Void.class
            );
            
            logger.info("Order {} cancelled successfully for saga {}", orderId, sagaId);
            return true;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Order {} not found for saga {} - may already be cancelled", 
                           orderId, sagaId);
                return true; // Consider it successful if already cancelled
            }
            logger.error("Client error during order cancellation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
            
        } catch (HttpServerErrorException e) {
            logger.error("Server error during order cancellation for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
            
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error during order cancellation for saga {}: {}", 
                        sagaId, e.getMessage());
            return false;
            
        } catch (Exception e) {
            logger.error("Unexpected error during order cancellation for saga {}: {}", 
                        sagaId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets order status
     */
    public OrderResponse getOrderStatus(String orderId, String sagaId) {
        try {
            logger.debug("Getting order status for order {} in saga {}", orderId, sagaId);
            
            ResponseEntity<OrderResponse> response = restTemplate.getForEntity(
                storeServiceUrl + "/api/v1/orders/" + orderId,
                OrderResponse.class
            );
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Order {} not found for saga {}", orderId, sagaId);
                return null;
            }
            logger.error("Client error getting order status for saga {}: {} - {}", 
                        sagaId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
            
        } catch (Exception e) {
            logger.error("Error getting order status for saga {}: {}", sagaId, e.getMessage());
            return null;
        }
    }
}