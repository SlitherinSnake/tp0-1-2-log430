package com.log430.tp7.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for audit service.
 * Sets up exchanges, queues, and bindings for comprehensive event auditing.
 */
@Configuration
public class RabbitMQConfig {
    
    // Exchange names
    public static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    
    // Queue names for audit service
    public static final String AUDIT_EVENTS_QUEUE = "audit.events.queue";
    public static final String AUDIT_TRANSACTION_QUEUE = "audit.transaction.queue";
    public static final String AUDIT_PAYMENT_QUEUE = "audit.payment.queue";
    public static final String AUDIT_INVENTORY_QUEUE = "audit.inventory.queue";
    public static final String AUDIT_STORE_QUEUE = "audit.store.queue";
    
    // Dead letter queue
    public static final String AUDIT_DLQ = "audit.dlq";
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("audit.dlx", true, false);
    }
    
    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(AUDIT_DLQ).build();
    }
    
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("audit.dlq");
    }
    
    // Audit Queues with DLQ configuration
    @Bean
    public Queue auditEventsQueue() {
        return QueueBuilder.durable(AUDIT_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", "audit.dlx")
                .withArgument("x-dead-letter-routing-key", "audit.dlq")
                .withArgument("x-message-ttl", 3600000) // 1 hour TTL
                .build();
    }
    
    @Bean
    public Queue auditTransactionQueue() {
        return QueueBuilder.durable(AUDIT_TRANSACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", "audit.dlx")
                .withArgument("x-dead-letter-routing-key", "audit.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }
    
    @Bean
    public Queue auditPaymentQueue() {
        return QueueBuilder.durable(AUDIT_PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "audit.dlx")
                .withArgument("x-dead-letter-routing-key", "audit.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }
    
    @Bean
    public Queue auditInventoryQueue() {
        return QueueBuilder.durable(AUDIT_INVENTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", "audit.dlx")
                .withArgument("x-dead-letter-routing-key", "audit.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }
    
    @Bean
    public Queue auditStoreQueue() {
        return QueueBuilder.durable(AUDIT_STORE_QUEUE)
                .withArgument("x-dead-letter-exchange", "audit.dlx")
                .withArgument("x-dead-letter-routing-key", "audit.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }
    
    // Bindings for comprehensive event capture
    @Bean
    public Binding auditEventsBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditEventsQueue())
                .to(businessEventsExchange)
                .with("#"); // Capture ALL events
    }
    
    @Bean
    public Binding auditTransactionBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditTransactionQueue())
                .to(businessEventsExchange)
                .with("transaction.*");
    }
    
    @Bean
    public Binding auditPaymentBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditPaymentQueue())
                .to(businessEventsExchange)
                .with("payment.*");
    }
    
    @Bean
    public Binding auditInventoryBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditInventoryQueue())
                .to(businessEventsExchange)
                .with("inventory.*");
    }
    
    @Bean
    public Binding auditStoreBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditStoreQueue())
                .to(businessEventsExchange)
                .with("store.*");
    }
    
    // Additional bindings for order events
    @Bean
    public Binding auditOrderBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditStoreQueue())
                .to(businessEventsExchange)
                .with("order.*");
    }
    
    // Saga event bindings (if applicable)
    @Bean
    public Binding auditSagaBinding(TopicExchange businessEventsExchange) {
        return BindingBuilder.bind(auditEventsQueue())
                .to(businessEventsExchange)
                .with("saga.*");
    }
}
