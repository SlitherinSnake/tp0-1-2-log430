package com.log430.tp7.sagaorchestrator.choreography.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for REST client components used in choreographed saga compensation.
 */
@Configuration
public class CompensationRestConfig {
    
    /**
     * RestTemplate bean for making HTTP calls to services for compensation actions.
     */
    @Bean
    public RestTemplate compensationRestTemplate() {
        return new RestTemplate();
    }
}
