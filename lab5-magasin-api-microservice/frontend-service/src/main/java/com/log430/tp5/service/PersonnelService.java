package com.log430.tp5.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.log430.tp5.dto.PersonnelDTO;

@Service
public class PersonnelService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonnelService.class);
    private static final String API_KEY_HEADER = "X-API-KEY";
    
    private final WebClient webClient;
    
    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;
    
    @Value("${gateway.api-key}")
    private String apiKey;
    
    public PersonnelService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public List<PersonnelDTO> getAllPersonnel() {
        try {
            logger.debug("Fetching all personnel");
            
            return webClient.get()
                    .uri(gatewayBaseUrl + "/personnel-service/api/personnel")
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PersonnelDTO>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching all personnel", e);
            return List.of();
        }
    }
    
    public PersonnelDTO getPersonnelById(Long id) {
        try {
            logger.debug("Fetching personnel with id: {}", id);
            
            return webClient.get()
                    .uri(gatewayBaseUrl + "/personnel-service/api/personnel/" + id)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(PersonnelDTO.class)
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching personnel with id: {}", id, e);
            return null;
        }
    }
}
