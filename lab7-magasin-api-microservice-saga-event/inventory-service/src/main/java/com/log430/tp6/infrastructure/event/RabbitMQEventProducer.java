package com.log430.tp7.infrastructure.event;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ implementation of EventProducer for Inventory Service.
 * Handles serialization and publishing of domain events to RabbitMQ.
 */
@Component("inventoryEventProducer")
public class RabbitMQEventProducer implements EventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQEventProducer.class);
    private static final String BUSINESS_EVENTS_EXCHANGE = "business.events";
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public RabbitMQEventProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void publishEvent(DomainEvent event) {
        String routingKey = generateRoutingKey(event);
        publishEvent(routingKey, event);
    }
    
    @Override
    public void publishEvent(String routingKey, DomainEvent event) {
        publishEvent(routingKey, event, new HashMap<>());
    }
    
    @Override
    public void publishEvent(String routingKey, DomainEvent event, Map<String, Object> headers) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            messageProperties.setHeader("eventType", event.getEventType());
            messageProperties.setHeader("aggregateId", event.getAggregateId());
            messageProperties.setHeader("aggregateType", event.getAggregateType());
            messageProperties.setHeader("eventId", event.getEventId());
            
            // Add custom headers
            headers.forEach(messageProperties::setHeader);
            
            Message message = new Message(eventJson.getBytes(), messageProperties);
            
            rabbitTemplate.send(BUSINESS_EVENTS_EXCHANGE, routingKey, message);
            
            logger.info("Published event: {} with routing key: {} for aggregate: {}", 
                       event.getEventType(), routingKey, event.getAggregateId());
                       
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new RuntimeException("Event serialization failed", e);
        } catch (Exception e) {
            logger.error("Failed to publish event: {} with routing key: {}", 
                        event.getEventType(), routingKey, e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
    
    private String generateRoutingKey(DomainEvent event) {
        String eventType = event.getEventType();
        // Convert CamelCase to dot notation: InventoryReserved -> inventory.reserved
        return eventType.replaceAll("([a-z])([A-Z])", "$1.$2").toLowerCase();
    }
}
