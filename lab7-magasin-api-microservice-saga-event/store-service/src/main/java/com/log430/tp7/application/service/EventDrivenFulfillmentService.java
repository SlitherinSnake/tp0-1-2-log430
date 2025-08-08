package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.store.Order;
import com.log430.tp7.domain.store.events.OrderDelivered;
import com.log430.tp7.domain.store.events.OrderFulfilled;
import com.log430.tp7.event.EventProducer;
import com.log430.tp7.infrastructure.repository.OrderRepository;

/**
 * Application service for event-driven order fulfillment operations.
 * Handles order fulfillment and delivery with event publishing.
 */
@Service
@Transactional
public class EventDrivenFulfillmentService {
    
    private static final Logger log = LoggerFactory.getLogger(EventDrivenFulfillmentService.class);
    
    private final OrderRepository orderRepository;
    private final EventProducer eventProducer;
    
    public EventDrivenFulfillmentService(OrderRepository orderRepository, EventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }
    
    /**
     * Fulfill an order and publish OrderFulfilled event.
     */
    public boolean fulfillOrder(String orderId, String correlationId, LocalDateTime estimatedDeliveryTime) {
        log.info("Fulfilling order {} with correlation ID: {}", orderId, correlationId);
        
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                log.warn("Order not found for fulfillment: {}", orderId);
                return false;
            }
            
            if (!order.isConfirmed()) {
                log.warn("Cannot fulfill unconfirmed order: {}", orderId);
                return false;
            }
            
            if (order.isFulfilled()) {
                log.info("Order {} is already fulfilled", orderId);
                return true;
            }
            
            if (order.isCancelled()) {
                log.warn("Cannot fulfill cancelled order: {}", orderId);
                return false;
            }
            
            // Calculate estimated delivery time if not provided
            LocalDateTime deliveryTime = estimatedDeliveryTime != null ? 
                estimatedDeliveryTime : LocalDateTime.now().plusHours(2);
            
            // Fulfill the order
            order.fulfill(deliveryTime);
            orderRepository.save(order);
            
            // Create fulfilled items list
            List<OrderFulfilled.FulfilledItem> fulfilledItems = order.getItems().stream()
                .map(item -> new OrderFulfilled.FulfilledItem(
                    item.getProductId(), 
                    item.getQuantity(), 
                    item.getUnitPrice()))
                .collect(Collectors.toList());
            
            // Publish OrderFulfilled event
            OrderFulfilled event = new OrderFulfilled(
                order.getOrderId(),
                order.getSagaId(),
                order.getCustomerId(),
                order.getStoreId(),
                order.getTotalAmount(),
                fulfilledItems,
                order.getFulfilledAt(),
                order.getEstimatedDeliveryTime(),
                correlationId
            );
            
            eventProducer.publishEvent(event);
            
            log.info("Order {} fulfilled successfully and event published", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Error fulfilling order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Mark an order as delivered and publish OrderDelivered event.
     */
    public boolean deliverOrder(String orderId, String deliveryAddress, String deliveryMethod, 
                               String deliveryConfirmation, String correlationId) {
        log.info("Delivering order {} with correlation ID: {}", orderId, correlationId);
        
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                log.warn("Order not found for delivery: {}", orderId);
                return false;
            }
            
            if (!order.isFulfilled()) {
                log.warn("Cannot deliver unfulfilled order: {}", orderId);
                return false;
            }
            
            if (order.isDelivered()) {
                log.info("Order {} is already delivered", orderId);
                return true;
            }
            
            // Deliver the order
            order.deliver(deliveryAddress, deliveryMethod, deliveryConfirmation);
            orderRepository.save(order);
            
            // Publish OrderDelivered event
            OrderDelivered event = new OrderDelivered(
                order.getOrderId(),
                order.getSagaId(),
                order.getCustomerId(),
                order.getStoreId(),
                order.getTotalAmount(),
                order.getDeliveredAt(),
                order.getDeliveryAddress(),
                order.getDeliveryMethod(),
                order.getDeliveryConfirmation(),
                correlationId
            );
            
            eventProducer.publishEvent(event);
            
            log.info("Order {} delivered successfully and event published", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Error delivering order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get fulfillment status of an order.
     */
    @Transactional(readOnly = true)
    public String getFulfillmentStatus(String orderId) {
        try {
            return orderRepository.findById(orderId)
                .map(order -> order.getFulfillmentStatus().name())
                .orElse("NOT_FOUND");
        } catch (Exception e) {
            log.error("Error getting fulfillment status for order {}: {}", orderId, e.getMessage(), e);
            return "ERROR";
        }
    }
    
    /**
     * Check if an order is ready for fulfillment.
     */
    @Transactional(readOnly = true)
    public boolean isOrderReadyForFulfillment(String orderId) {
        try {
            return orderRepository.findById(orderId)
                .map(order -> order.isConfirmed() && !order.isFulfilled() && !order.isCancelled())
                .orElse(false);
        } catch (Exception e) {
            log.error("Error checking if order {} is ready for fulfillment: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if an order is ready for delivery.
     */
    @Transactional(readOnly = true)
    public boolean isOrderReadyForDelivery(String orderId) {
        try {
            return orderRepository.findById(orderId)
                .map(order -> order.isFulfilled() && !order.isDelivered())
                .orElse(false);
        } catch (Exception e) {
            log.error("Error checking if order {} is ready for delivery: {}", orderId, e.getMessage(), e);
            return false;
        }
    }
}