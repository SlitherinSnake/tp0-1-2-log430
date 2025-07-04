package com.log430.tp4.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration CORS centralisée pour améliorer la sécurité de l'API.
 * 
 * Cette configuration remplace les annotations @CrossOrigin dispersées
 * dans les contrôleurs et offre un contrôle plus fin et sécurisé.
 */
@Configuration
public class CorsConfig {

    // Origines autorisées configurables via application.properties
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:8081}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers:Authorization}")
    private String exposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * Configuration CORS globale et sécurisée pour l'API REST.
     * 
     * Sécurité renforcée :
     * - Origines spécifiques (pas de wildcard)
     * - Méthodes limitées aux besoins
     * - Headers contrôlés
     * - Support des credentials
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées (remplace l'insécure "*")
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // Méthodes HTTP autorisées
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Headers autorisés
        if ("*".equals(allowedHeaders)) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        // Headers exposés au client
        if (!exposedHeaders.isEmpty()) {
            List<String> exposed = Arrays.asList(exposedHeaders.split(","));
            configuration.setExposedHeaders(exposed);
        }
        
        // Autoriser l'envoi de cookies/credentials
        configuration.setAllowCredentials(allowCredentials);
        
        // Durée de cache pour les requêtes preflight
        configuration.setMaxAge(maxAge);

        // Application de la configuration à toutes les routes /api/**
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
