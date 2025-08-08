package com.log430.tp7.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.inventory.events.InventoryUnavailable;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.event.DomainEvent;
import com.log430.tp7.infrastructure.event.InventoryEventConsumer;
import com.log430.tp7.infrastructure.event.TransactionEventConsumer;
import com.log430.tp7.infrastructure.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentEventConsumptionIntegrationTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private TransactionEventConsumer transactionEventConsumer;
    
    @Autowired
    private InventoryEventConsumer inventoryEventConsumer;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }
    
    @Test
    void shouldProcessTransactionCreatedEventAndCreatePayment() {
        // Given
        DomainEvent transactionCreatedEvent = createTransactionCreatedEvent();
        
        // When
        assertDoesNotThrow(() -> transactionEventConsumer.handleEvent(transactionCreatedEvent));
        
        // Then
        Optional<Payment> payment = paymentService.findByTransactionId("tx-123");
        assertTrue(payment.isPresent());
        assertEquals("tx-123", payment.get().getTransactionId());
        assertEquals(PaymentStatus.PROCESSED, payment.get().getStatus());
        assertNotNull(payment.get().getPaymentReference());
    }
    
    @Test
    void shouldProcessInventoryUnavailableEventAndRefundPayment() {
        // Given - First create and process a payment
        Payment payment = paymentService.createPayment(
            "tx-456", "customer-456", new BigDecimal("150.00"), "CREDIT_CARD", "corr-456");
        paymentService.processPayment(payment.getPaymentId());
        
        // Verify payment is processed
        Payment processedPayment = paymentService.findById(payment.getPaymentId()).orElseThrow();
        assertEquals(PaymentStatus.PROCESSED, processedPayment.getStatus());
        
        // Create inventory unavailable event
        InventoryUnavailable inventoryEvent = new InventoryUnavailable(
            "tx-456", 
            List.of(new InventoryUnavailable.UnavailableItemData(1L, 5, 2, "Test Item")),
            "Insufficient stock",
            "corr-456"
        );
        
        // When
        assertDoesNotThrow(() -> inventoryEventConsumer.handleEvent(inventoryEvent));
        
        // Then
        Payment refundedPayment = paymentService.findById(payment.getPaymentId()).orElseThrow();
        assertEquals(PaymentStatus.REFUNDED, refundedPayment.getStatus());
        assertNotNull(refundedPayment.getRefundedAt());
    }
    
    @Test
    void shouldBeIdempotentForDuplicateTransactionCreatedEvents() {
        // Given
        DomainEvent transactionCreatedEvent = createTransactionCreatedEvent();
        
        // When - Process the same event twice
        assertDoesNotThrow(() -> transactionEventConsumer.handleEvent(transactionCreatedEvent));
        assertDoesNotThrow(() -> transactionEventConsumer.handleEvent(transactionCreatedEvent));
        
        // Then - Should only have one payment
        Optional<Payment> payment = paymentRepository.findByTransactionId("tx-123");
        assertTrue(payment.isPresent());
    }
    
    @Test
    void shouldBeIdempotentForDuplicateInventoryUnavailableEvents() {
        // Given - Create and process a payment first
        Payment payment = paymentService.createPayment(
            "tx-789", "customer-789", new BigDecimal("200.00"), "CREDIT_CARD", "corr-789");
        paymentService.processPayment(payment.getPaymentId());
        
        InventoryUnavailable inventoryEvent = new InventoryUnavailable(
            "tx-789", 
            List.of(new InventoryUnavailable.UnavailableItemData(2L, 3, 1, "Another Item")),
            "Out of stock",
            "corr-789"
        );
        
        // When - Process the same event twice
        assertDoesNotThrow(() -> inventoryEventConsumer.handleEvent(inventoryEvent));
        assertDoesNotThrow(() -> inventoryEventConsumer.handleEvent(inventoryEvent));
        
        // Then - Payment should still be refunded only once
        Payment refundedPayment = paymentService.findById(payment.getPaymentId()).orElseThrow();
        assertEquals(PaymentStatus.REFUNDED, refundedPayment.getStatus());
    }
    
    @Test
    void shouldHandleInventoryUnavailableForNonExistentTransaction() {
        // Given
        InventoryUnavailable inventoryEvent = new InventoryUnavailable(
            "non-existent-tx", 
            List.of(new InventoryUnavailable.UnavailableItemData(3L, 1, 0, "Missing Item")),
            "Transaction not found",
            "corr-missing"
        );
        
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> inventoryEventConsumer.handleEvent(inventoryEvent));
    }
    
    @Test
    void shouldNotRefundPendingPayments() {
        // Given - Create a payment but don't process it
        Payment payment = paymentService.createPayment(
            "tx-pending", "customer-pending", new BigDecimal("100.00"), "CREDIT_CARD", "corr-pending");
        
        InventoryUnavailable inventoryEvent = new InventoryUnavailable(
            "tx-pending", 
            List.of(new InventoryUnavailable.UnavailableItemData(4L, 2, 0, "Pending Item")),
            "Stock unavailable",
            "corr-pending"
        );
        
        // When
        assertDoesNotThrow(() -> inventoryEventConsumer.handleEvent(inventoryEvent));
        
        // Then - Payment should still be pending
        Payment unchangedPayment = paymentService.findById(payment.getPaymentId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, unchangedPayment.getStatus());
        assertNull(unchangedPayment.getRefundedAt());
    }
    
    private DomainEvent createTransactionCreatedEvent() {
        return new DomainEvent("TransactionCreated", "tx-123", "Transaction", 1, "corr-123", "cause-123") {
            // Anonymous implementation for testing
        };
    }
}