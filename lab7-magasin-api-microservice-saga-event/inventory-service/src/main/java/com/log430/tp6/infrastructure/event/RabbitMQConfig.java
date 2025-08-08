package com.log430.tp7.infrastructure.event;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ configuration for event-driven architecture.
 * Sets up exchanges, queues, and bindings for business events.
 */
@Configuration
public class RabbitMQConfig {
    
    public static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    public static final String INVENTORY_QUEUE = "inventory.events.queue";
    public static final String INVENTORY_DLQ = "inventory.events.dlq";
    
    @Bean
    public TopicExchange businessEventsExchange() {
        return new TopicExchange(BUSINESS_EVENTS_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder
                .durable(INVENTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", INVENTORY_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue inventoryDeadLetterQueue() {
        return QueueBuilder
                .durable(INVENTORY_DLQ)
                .build();
    }
    
    @Bean
    public Binding inventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("inventory.*");
    }
    
    @Bean
    public Binding paymentToInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("payment.processed");
    }
    
    @Bean
    public Binding paymentFailedToInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("payment.failed");
    }
    
    @Bean
    public Binding paymentRefundedToInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("payment.refunded");
    }
    
    @Bean
    public Binding transactionToInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("transaction.created");
    }
    
    @Bean
    public Binding transactionCancelledToInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(businessEventsExchange)
                .with("transaction.cancelled");
    }
    
    @Bean
    public ObjectMapper eventObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(eventObjectMapper());
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // Log failed message publishing
                System.err.println("Message publishing failed: " + cause);
            }
        });
        template.setReturnsCallback(returned -> {
            // Log returned messages
            System.err.println("Message returned: " + returned.getMessage());
        });
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
}