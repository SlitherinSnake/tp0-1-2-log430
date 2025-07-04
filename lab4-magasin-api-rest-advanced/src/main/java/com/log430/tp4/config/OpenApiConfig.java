package com.log430.tp4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuration d’OpenAPI (Swagger) :
 * - Génère automatiquement une page de documentation interactive pour l’API REST.
 * - Décrit la manière de s’authentifier (JWT Bearer token).
 *
 * On peut ensuite visiter
 * http://localhost:8080/swagger-ui.html pour explorer tous les endpoints,
 * lire les descriptions et tester les appels directe-ment depuis le navigateur.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Déclare un bean Spring : l’objet OpenAPI décrit toute la documentation.
     * Le reste de l’application le récupère automatiquement.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"; // Nom arbitraire utilisé partout
        
        // Construction de l’objet avec titre, version, licence, etc.
        return new OpenAPI()
                .info(new Info()
                        .title("Magasin Web API-REST") // Titre lisible
                        .version("v1.0") // Version de l’API
                        .description("API RESTful pour le système multi-magasins")
                        .license(new License() // Mention légale : Apache 2.0
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                // Indique que chaque appel « sécurisé » nécessite un JWT
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Définit le schéma de sécurité (type : HTTP Bearer, format : JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer") // Utilise l’en-tête Authorization: Bearer <token>
                                        .bearerFormat("JWT"))); // Précise le format
    }
} 