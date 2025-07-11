package com.log430.tp5.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.log430.tp5.dto.TransactionDTO;

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
}
