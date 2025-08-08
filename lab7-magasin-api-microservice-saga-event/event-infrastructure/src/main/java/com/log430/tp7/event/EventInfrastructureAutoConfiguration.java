package com.log430.tp7.event;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for event infrastructure.
 * Enables event-driven capabilities when included in a Spring Boot application.
 */
@AutoConfiguration
@ConditionalOnProperty(name = "event.infrastructure.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.log430.tp7.event")
@Import({RabbitMQEventConfig.class})
public class EventInfrastructureAutoConfiguration {
    
    // Configuration is handled by component scanning and imports
}