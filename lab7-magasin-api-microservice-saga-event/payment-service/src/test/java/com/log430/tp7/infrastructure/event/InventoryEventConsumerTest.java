package com.log430.tp7.infrastructure.event;

import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.event.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class InventoryEventConsumerTest {
    
    @Mock
    private PaymentService paymentService;
    
    private InventoryEventConsumer consumer;
    
    @BeforeEach
    void setUp() {
        consumer = new InventoryEventConsumer(paymentService);
    }
    
    @Test
    void shouldSupportInventoryUnavailableEventType() {
        assertTrue(consumer.canHandle("InventoryUnavailable"));
        assertFalse(consumer.canHandle("OtherEvent"));
    }
    
    @Test
    void shouldReturnCorrectConsumerName() {
        assertEquals("InventoryEventConsumer", consumer.getConsumerName());
    }
    
    @Test
    void shouldProcessInventoryUnavailableEventAndRefundPayment() {
        // Given
        DomainEvent event = createInventoryUnavailableEvent();
        Payment processedPayment = createProcessedPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.of(processedPayment));
        when(paymentService.refundPayment(anyString(), anyString())).thenReturn(processedPayment);
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService).refundPayment(eq("payment-123"), anyString());
    }
    
    @Test
    void shouldSkipProcessingIfNoPaymentFound() {
        // Given
        DomainEvent event = createInventoryUnavailableEvent();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.empty());
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService, never()).refundPayment(anyString(), anyString());
    }
    
    @Test
    void shouldSkipProcessingIfPaymentNotProcessed() {
        // Given
        DomainEvent event = createInventoryUnavailableEvent();
        Payment pendingPayment = createPendingPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.of(pendingPayment));
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService, never()).refundPayment(anyString(), anyString());
    }
    
    @Test
    void shouldSkipProcessingIfPaymentAlreadyRefunded() {
        // Given
        DomainEvent event = createInventoryUnavailableEvent();
        Payment refundedPayment = createRefundedPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.of(refundedPayment));
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService, never()).refundPayment(anyString(), anyString());
    }
    
    @Test
    void shouldHandleNullEvent() {
        // When & Then
        assertThrows(Exception.class, () -> consumer.handleEvent(null));
    }
    
    @Test
    void shouldHandleUnsupportedEventType() {
        // Given
        DomainEvent event = createUnsupportedEvent();
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService, never()).findByTransactionId(anyString());
    }
    
    @Test
    void shouldPropagateExceptionOnPaymentServiceFailure() {
        // Given
        DomainEvent event = createInventoryUnavailableEvent();
        Payment processedPayment = createProcessedPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.of(processedPayment));
        when(paymentService.refundPayment(anyString(), anyString()))
                .thenThrow(new RuntimeException("Refund failed"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> consumer.handleEvent(event));
    }
    
    private DomainEvent createInventoryUnavailableEvent() {
        return new DomainEvent("InventoryUnavailable", "123", "Transaction", 1, "corr-123", "cause-123") {
            // Anonymous implementation for testing
        };
    }
    
    private DomainEvent createUnsupportedEvent() {
        return new DomainEvent("UnsupportedEvent", "123", "Transaction", 1, "corr-123", "cause-123") {
            // Anonymous implementation for testing
        };
    }
    
    private Payment createProcessedPayment() {
        Payment payment = mock(Payment.class);
        lenient().when(payment.getPaymentId()).thenReturn("payment-123");
        lenient().when(payment.getTransactionId()).thenReturn("123");
        lenient().when(payment.getStatus()).thenReturn(PaymentStatus.PROCESSED);
        lenient().when(payment.isProcessed()).thenReturn(true);
        lenient().when(payment.isRefunded()).thenReturn(false);
        return payment;
    }
    
    private Payment createPendingPayment() {
        Payment payment = mock(Payment.class);
        lenient().when(payment.getPaymentId()).thenReturn("payment-123");
        lenient().when(payment.getTransactionId()).thenReturn("123");
        lenient().when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        lenient().when(payment.isProcessed()).thenReturn(false);
        lenient().when(payment.isRefunded()).thenReturn(false);
        return payment;
    }
    
    private Payment createRefundedPayment() {
        Payment payment = mock(Payment.class);
        lenient().when(payment.getPaymentId()).thenReturn("payment-123");
        lenient().when(payment.getTransactionId()).thenReturn("123");
        lenient().when(payment.getStatus()).thenReturn(PaymentStatus.REFUNDED);
        lenient().when(payment.isProcessed()).thenReturn(true);
        lenient().when(payment.isRefunded()).thenReturn(true);
        return payment;
    }
}