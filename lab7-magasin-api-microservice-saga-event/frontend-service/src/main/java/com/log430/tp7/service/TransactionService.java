package com.log430.tp7.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.log430.tp7.dto.TransactionDTO;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final String API_KEY_HEADER = "X-API-KEY";
    
    private final WebClient webClient;
    
    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;
    
    @Value("${gateway.api-key}")
    private String apiKey;
    
    public TransactionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public List<TransactionDTO> getTransactionsByPersonnelId(Long personnelId) {
        try {
            logger.info("Fetching transactions for personnel id: {}", personnelId);
            logger.info("Gateway URL: {}", gatewayBaseUrl);
            logger.info("API Key: {}", apiKey != null ? "SET" : "NOT SET");
            
            String url = gatewayBaseUrl + "/api/transactions/personnel/" + personnelId;
            logger.info("Making request to: {}", url);
            
            List<TransactionDTO> result = webClient.get()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionDTO>>() {})
                    .block();
            
            logger.info("Received {} transactions from API", result != null ? result.size() : 0);
            return result != null ? result : List.of();
        } catch (Exception e) {
            logger.error("Error fetching transactions for personnel id: {}", personnelId, e);
            return List.of();
        }
    }
    
    public List<TransactionDTO> getAllTransactions() {
        try {
            logger.info("Fetching all transactions");
            logger.info("Gateway URL: {}", gatewayBaseUrl);
            
            String url = gatewayBaseUrl + "/api/transactions";
            logger.info("Making request to: {}", url);
            
            List<TransactionDTO> result = webClient.get()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionDTO>>() {})
                    .block();
            
            logger.info("Received {} transactions from API", result != null ? result.size() : 0);
            if (result != null && !result.isEmpty()) {
                TransactionDTO first = result.get(0);
                logger.info("First transaction: ID={}, Type={}, Status={}, PersonnelId={}", 
                    first.getId(), first.getType(), first.getStatus(), first.getPersonnelId());
            }
            
            return result != null ? result : List.of();
        } catch (Exception e) {
            logger.error("Error fetching all transactions", e);
            return List.of();
        }
    }
    
    public TransactionDTO createTransaction(TransactionDTO transaction) {
        try {
            logger.debug("Creating new transaction");
            
            return webClient.post()
                    .uri(gatewayBaseUrl + "/api/transactions")
                    .header(API_KEY_HEADER, apiKey)
                    .bodyValue(transaction)
                    .retrieve()
                    .bodyToMono(TransactionDTO.class)
                    .block();
        } catch (Exception e) {
            logger.error("Error creating transaction", e);
            return null;
        }
    }

    public List<TransactionDTO> getReturnableTransactions() {
        try {
            logger.info("Fetching returnable transactions");
            
            String url = gatewayBaseUrl + "/api/transactions/returnable";
            logger.info("Making request to: {}", url);
            
            List<TransactionDTO> result = webClient.get()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionDTO>>() {})
                    .block();
            
            logger.info("Received {} returnable transactions from API", result != null ? result.size() : 0);
            return result != null ? result : List.of();
        } catch (Exception e) {
            logger.error("Error fetching returnable transactions", e);
            return List.of();
        }
    }

    public Map<String, Object> createReturn(Map<String, Object> returnRequest) {
        try {
            logger.info("Creating return transaction");
            
            String url = gatewayBaseUrl + "/api/transactions/returns";
            logger.info("Making request to: {}", url);
            
            Map<String, Object> result = webClient.post()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .bodyValue(returnRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            logger.info("Return transaction created with response: {}", result);
            return result != null ? result : Map.of("success", false, "error", "No response from API");
        } catch (Exception e) {
            logger.error("Error creating return transaction", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public List<TransactionDTO> getReturnsByOriginalTransaction(Long originalTransactionId) {
        try {
            logger.info("Fetching returns for original transaction: {}", originalTransactionId);
            
            String url = gatewayBaseUrl + "/api/transactions/returns/" + originalTransactionId;
            logger.info("Making request to: {}", url);
            
            List<TransactionDTO> result = webClient.get()
                    .uri(url)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionDTO>>() {})
                    .block();
            
            logger.info("Received {} returns from API", result != null ? result.size() : 0);
            return result != null ? result : List.of();
        } catch (Exception e) {
            logger.error("Error fetching returns for original transaction: {}", originalTransactionId, e);
            return List.of();
        }
    }
}
