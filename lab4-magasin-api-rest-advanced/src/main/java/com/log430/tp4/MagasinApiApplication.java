package com.log430.tp4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for the Magasin API.
 * Follows DDD architecture with domain-driven design principles.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.log430.tp4.infrastructure.repository")
@EnableCaching
public class MagasinApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagasinApiApplication.class, args);
    }
}
