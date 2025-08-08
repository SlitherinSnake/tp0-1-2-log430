package com.log430.tp7.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for payment-specific queues and bindings.
 * Infrastructure beans (messageConverter, rabbitTemplate, etc.) are provided
 * by the shared RabbitMQEventConfig from event-infrastructure.
 */
@Configuration
public class RabbitMQConfig {
    
    // Exchange for business events
    public static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    
    // Payment service queues
    public static final String PAYMENT_TRANSACTION_QUEUE = "payment.transaction.queue";
    public static final String PAYMENT_INVENTORY_QUEUE = "payment.inventory.queue";
    
    // Dead letter queues
    public static final String PAYMENT_TRANSACTION_DLQ = "payment.transaction.dlq";
    public static final String PAYMENT_INVENTORY_DLQ = "payment.inventory.dlq";
    
    // Routing keys
    public static final String TRANSACTION_CREATED_ROUTING_KEY = "transaction.created";
    public static final String INVENTORY_UNAVAILABLE_ROUTING_KEY = "inventory.unavailable";
    
    // Transaction event queue and bindings
    @Bean
    public Queue paymentTransactionQueue() {
        return QueueBuilder.durable(PAYMENT_TRANSACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PAYMENT_TRANSACTION_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue paymentTransactionDLQ() {
        return QueueBuilder.durable(PAYMENT_TRANSACTION_DLQ).build();
    }
    
    @Bean
    public Binding paymentTransactionBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(paymentTransactionQueue())
                .to(businessEventsExchange)
                .with(TRANSACTION_CREATED_ROUTING_KEY);
    }
    
    // Inventory event queue and bindings
    @Bean
    public Queue paymentInventoryQueue() {
        return QueueBuilder.durable(PAYMENT_INVENTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PAYMENT_INVENTORY_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue paymentInventoryDLQ() {
        return QueueBuilder.durable(PAYMENT_INVENTORY_DLQ).build();
    }
    
    @Bean
    public Binding paymentInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(paymentInventoryQueue())
                .to(businessEventsExchange)
                .with(INVENTORY_UNAVAILABLE_ROUTING_KEY);
    }
}