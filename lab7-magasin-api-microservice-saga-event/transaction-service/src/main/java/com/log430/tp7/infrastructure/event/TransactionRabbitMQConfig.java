package com.log430.tp7.infrastructure.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Transaction-specific RabbitMQ configuration for event-driven architecture.
 * Sets up transaction-specific queues and bindings for business events.
 * Uses the shared businessEventsExchange from event-infrastructure.
 */
@Configuration
public class TransactionRabbitMQConfig {
    
    public static final String TRANSACTION_QUEUE = "transaction.events.queue";
    public static final String TRANSACTION_DLQ = "transaction.events.dlq";
    
    private final TopicExchange businessEventsExchange;
    
    public TransactionRabbitMQConfig(TopicExchange businessEventsExchange) {
        this.businessEventsExchange = businessEventsExchange;
    }
    
    @Bean
    public Queue transactionQueue() {
        return QueueBuilder
                .durable(TRANSACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TRANSACTION_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue transactionDeadLetterQueue() {
        return QueueBuilder
                .durable(TRANSACTION_DLQ)
                .build();
    }
    
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(businessEventsExchange)
                .with("transaction.*");
    }
    
    @Bean
    public Binding paymentToTransactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(businessEventsExchange)
                .with("payment.*");
    }
}
