package com.log430.tp7.application.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.store.Order;
import com.log430.tp7.domain.store.Store;
import com.log430.tp7.infrastructure.repository.OrderRepository;
import com.log430.tp7.infrastructure.repository.StoreRepository;
import com.log430.tp7.presentation.api.dto.OrderRequest;
import com.log430.tp7.presentation.api.dto.OrderResponse;

/**
 * Application service for saga-specific order operations.
 * Handles order creation and management for distributed transactions.
 */
@Service
@Transactional
public class SagaOrderService {
    private static final Logger log = LoggerFactory.getLogger(SagaOrderService.class);

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    public SagaOrderService(OrderRepository orderRepository, StoreRepository storeRepository) {
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Create an order for a saga transaction.
     */
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for saga {} - customer: {}, store: {}, amount: {}", 
            request.sagaId(), request.customerId(), request.storeId(), request.totalAmount());
        
        try {
            // Validate store exists and is active
            Store store = storeRepository.findById(request.storeId())
                .orElse(null);
            
            if (store == null) {
                log.warn("Store not found for saga {}: {}", request.sagaId(), request.storeId());
                return OrderResponse.failure(
                    request.sagaId(), request.customerId(), request.storeId(),
                    request.totalAmount(), "Store not found"
                );
            }
            
            if (!store.isActive()) {
                log.warn("Store is inactive for saga {}: {}", request.sagaId(), request.storeId());
                return OrderResponse.failure(
                    request.sagaId(), request.customerId(), request.storeId(),
                    request.totalAmount(), "Store is not active"
                );
            }

            // Validate order items
            if (request.items() == null || request.items().isEmpty()) {
                log.warn("No items provided for order in saga {}", request.sagaId());
                return OrderResponse.failure(
                    request.sagaId(), request.customerId(), request.storeId(),
                    request.totalAmount(), "Order must have at least one item"
                );
            }

            // Create order
            String orderId = UUID.randomUUID().toString();
            Order order = new Order(orderId, request.sagaId(), request.customerId(), 
                                  request.storeId(), request.totalAmount());
            
            // Set additional saga context
            order.setPaymentTransactionId(request.paymentTransactionId());
            order.setStockReservationId(request.stockReservationId());
            
            // Add items to order
            for (OrderRequest.OrderItem item : request.items()) {
                order.addItem(item.productId(), item.quantity(), item.unitPrice());
            }
            
            // Confirm the order immediately for saga (since payment and stock are already handled)
            order.confirm();
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            
            log.info("Order created successfully for saga {} - orderId: {}", 
                request.sagaId(), savedOrder.getOrderId());
            
            return OrderResponse.success(
                savedOrder.getOrderId(), request.sagaId(), request.customerId(),
                request.storeId(), request.totalAmount()
            );
            
        } catch (Exception e) {
            log.error("Error creating order for saga {}: {}", request.sagaId(), e.getMessage(), e);
            return OrderResponse.failure(
                request.sagaId(), request.customerId(), request.storeId(),
                request.totalAmount(), "Internal error during order creation"
            );
        }
    }

    /**
     * Cancel an order (compensation action).
     */
    public boolean cancelOrder(String orderId, String sagaId) {
        log.info("Cancelling order {} for saga {}", orderId, sagaId);
        
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                log.warn("Order not found for cancellation: {} in saga {}", orderId, sagaId);
                return false;
            }
            
            // Verify the order belongs to the saga
            if (!sagaId.equals(order.getSagaId())) {
                log.warn("Order {} does not belong to saga {}", orderId, sagaId);
                return false;
            }
            
            if (order.isCancelled()) {
                log.info("Order {} is already cancelled", orderId);
                return true;
            }
            
            if (order.isConfirmed()) {
                log.warn("Cannot cancel confirmed order {} in saga {}", orderId, sagaId);
                return false;
            }
            
            order.cancel();
            orderRepository.save(order);
            
            log.info("Order {} cancelled successfully for saga {}", orderId, sagaId);
            return true;
            
        } catch (Exception e) {
            log.error("Error cancelling order {} for saga {}: {}", orderId, sagaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get order status.
     */
    public String getOrderStatus(String orderId) {
        try {
            return orderRepository.findById(orderId)
                .map(order -> order.getStatus().name())
                .orElse("NOT_FOUND");
        } catch (Exception e) {
            log.error("Error getting order status for {}: {}", orderId, e.getMessage(), e);
            return "ERROR";
        }
    }

    /**
     * Check if order is confirmed.
     */
    public boolean isOrderConfirmed(String orderId) {
        try {
            return orderRepository.findById(orderId)
                .map(Order::isConfirmed)
                .orElse(false);
        } catch (Exception e) {
            log.error("Error checking order confirmation for {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get order by saga ID.
     */
    public Order getOrderBySagaId(String sagaId) {
        try {
            return orderRepository.findBySagaId(sagaId)
                .stream()
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            log.error("Error getting order by saga ID {}: {}", sagaId, e.getMessage(), e);
            return null;
        }
    }
}