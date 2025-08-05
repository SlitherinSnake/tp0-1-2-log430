package com.log430.tp6.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.log430.tp6.dto.InventoryApiResponseDTO;
import com.log430.tp6.dto.ProductDTO;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private static final String API_KEY_HEADER = "X-API-KEY";
    
    private final WebClient webClient;
    
    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;
    
    @Value("${gateway.api-key}")
    private String apiKey;
    
    public InventoryService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public List<ProductDTO> getAllActiveItems() {
        try {
            logger.debug("Fetching all active products from inventory service");
            
            // Fetch the API response with the correct structure
            List<InventoryApiResponseDTO> apiResponse = webClient.get()
                    .uri(gatewayBaseUrl + "/inventory-service/api/inventory/items")
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InventoryApiResponseDTO>>() {})
                    .block();
            
            // Convert API response to ProductDTO format expected by frontend
            if (apiResponse == null) {
                logger.warn("Received null response from inventory service");
                return List.of();
            }
            
            List<ProductDTO> products = apiResponse.stream()
                    .map(InventoryApiResponseDTO::toProductDTO)
                    .toList();
            
            logger.info("Successfully fetched {} products from inventory service", products.size());
            return products;
            
        } catch (Exception e) {
            logger.error("Error fetching active products", e);
            return List.of();
        }
    }
    
    public ProductDTO getItemById(Long id) {
        try {
            logger.debug("Fetching product with id: {}", id);
            
            InventoryApiResponseDTO apiResponse = webClient.get()
                    .uri(gatewayBaseUrl + "/inventory-service/api/inventory/" + id)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(InventoryApiResponseDTO.class)
                    .block();
            
            if (apiResponse == null) {
                logger.warn("No product found with id: {}", id);
                return null;
            }
            
            return apiResponse.toProductDTO();
        } catch (Exception e) {
            logger.error("Error fetching product with id: {}", id, e);
            return null;
        }
    }
    
    public Set<String> getDistinctCategories() {
        try {
            logger.debug("Fetching distinct categories from inventory service");
            
            List<ProductDTO> products = getAllActiveItems();
            return products.stream()
                    .map(ProductDTO::getCategorie)
                    .filter(category -> category != null && !category.isEmpty())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error fetching distinct categories", e);
            return Set.of();
        }
    }
    
    public List<ProductDTO> getItemsNeedingRestock() {
        try {
            logger.debug("Fetching items needing restock from inventory service");
            
            List<InventoryApiResponseDTO> apiResponse = webClient.get()
                    .uri(gatewayBaseUrl + "/inventory-service/api/inventory/restock-needed")
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InventoryApiResponseDTO>>() {})
                    .block();
            
            if (apiResponse == null) {
                logger.warn("Received null response for items needing restock");
                return List.of();
            }
            
            return apiResponse.stream()
                    .map(InventoryApiResponseDTO::toProductDTO)
                    .toList();
        } catch (Exception e) {
            logger.error("Error fetching items needing restock", e);
            return List.of();
        }
    }
    
    public double calculateTotalInventoryValue() {
        try {
            logger.debug("Calculating total inventory value");
            
            List<ProductDTO> products = getAllActiveItems();
            return products.stream()
                    .filter(p -> p.getPrix() != null && p.getStockCentral() != null)
                    .mapToDouble(p -> p.getPrix() * p.getStockCentral())
                    .sum();
        } catch (Exception e) {
            logger.error("Error calculating total inventory value", e);
            return 0.0;
        }
    }
}
