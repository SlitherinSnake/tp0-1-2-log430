package com.log430.tp7.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp7.domain.store.Order;

/**
 * Repository interface for Order entities.
 * Provides data access operations for order management.
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Find orders by saga ID.
     */
    List<Order> findBySagaId(String sagaId);

    /**
     * Find orders by customer ID.
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Find orders by store ID.
     */
    List<Order> findByStoreId(Long storeId);

    /**
     * Find orders by status.
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * Find order by saga ID (expecting single result).
     */
    Optional<Order> findBySagaIdAndCustomerId(String sagaId, String customerId);

    /**
     * Count orders by status.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Order.OrderStatus status);

    /**
     * Find orders by store and status.
     */
    List<Order> findByStoreIdAndStatus(Long storeId, Order.OrderStatus status);
}