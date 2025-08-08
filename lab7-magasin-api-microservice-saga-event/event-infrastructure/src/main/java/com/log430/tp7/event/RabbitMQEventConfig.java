package com.log430.tp7.event;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Enhanced RabbitMQ configuration for event-driven architecture.
 * Sets up exchanges, queues, bindings, and message converters with
 * proper error handling and dead letter queue support.
 */
@Configuration
public class RabbitMQEventConfig {
    
    public static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    public static final String NOTIFICATION_QUEUE = "notification.events.queue";
    public static final String AUDIT_QUEUE = "audit.events.queue";
    public static final String NOTIFICATION_DLQ = "notification.events.dlq";
    public static final String AUDIT_DLQ = "audit.events.dlq";
    
    @Bean
    public TopicExchange businessEventsExchange() {
        return ExchangeBuilder
                .topicExchange(BUSINESS_EVENTS_EXCHANGE)
                .build();
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .withArgument("x-max-retries", 3)
                .build();
    }
    
    @Bean
    public Queue auditQueue() {
        return QueueBuilder
                .durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", AUDIT_DLQ)
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .withArgument("x-max-retries", 5)
                .build();
    }
    
    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_DLQ)
                .build();
    }
    
    @Bean
    public Queue auditDeadLetterQueue() {
        return QueueBuilder
                .durable(AUDIT_DLQ)
                .build();
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(businessEventsExchange())
                .with("*.*"); // All events for notifications
    }
    
    @Bean
    public Binding auditBinding() {
        return BindingBuilder
                .bind(auditQueue())
                .to(businessEventsExchange())
                .with("*.*"); // All events for audit
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper eventObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper eventObjectMapper) {
        return new Jackson2JsonMessageConverter(eventObjectMapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                       Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        
        // Publisher confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message publishing failed: " + cause);
            }
        });
        
        // Publisher returns (when message cannot be routed)
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage() + 
                             ", reply code: " + returned.getReplyCode() + 
                             ", reply text: " + returned.getReplyText());
        });
        
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        
        // Error handler
        factory.setErrorHandler(throwable -> {
            System.err.println("Error in message processing: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        
        return factory;
    }
}