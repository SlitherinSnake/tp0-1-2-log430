package com.log430.tp7.infrastructure.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for store-specific queues and bindings.
 * Infrastructure beans (messageConverter, rabbitTemplate, etc.) are provided
 * by the shared RabbitMQEventConfig from event-infrastructure.
 */
@Configuration
public class RabbitMQConfig {
    
    public static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    public static final String STORE_QUEUE = "store.events.queue";
    public static final String STORE_INVENTORY_QUEUE = "store.inventory.queue";
    public static final String STORE_DLQ = "store.events.dlq";
    
    @Bean
    public Queue storeQueue() {
        return QueueBuilder
                .durable(STORE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", STORE_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue storeInventoryQueue() {
        return QueueBuilder
                .durable(STORE_INVENTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", STORE_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue storeDeadLetterQueue() {
        return QueueBuilder
                .durable(STORE_DLQ)
                .build();
    }
    
    @Bean
    public Binding storeBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(storeQueue())
                .to(businessEventsExchange)
                .with("store.*");
    }
    
    @Bean
    public Binding storeInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(storeInventoryQueue())
                .to(businessEventsExchange)
                .with("InventoryReserved");
    }
    
    @Bean
    public Binding inventoryToStoreBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(storeQueue())
                .to(businessEventsExchange)
                .with("inventory.reserved");
    }
    
    @Bean
    public Binding storeOrderBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(storeQueue())
                .to(businessEventsExchange)
                .with("order.*");
    }
}