package com.log430.tp7.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.event.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransactionEventConsumerTest {
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private TransactionEventConsumer consumer;
    
    @BeforeEach
    void setUp() {
        consumer = new TransactionEventConsumer(paymentService, objectMapper);
    }
    
    @Test
    void shouldSupportTransactionCreatedEventType() {
        assertTrue(consumer.canHandle("TransactionCreated"));
        assertFalse(consumer.canHandle("OtherEvent"));
    }
    
    @Test
    void shouldReturnCorrectConsumerName() {
        assertEquals("TransactionEventConsumer", consumer.getConsumerName());
    }
    
    @Test
    void shouldProcessTransactionCreatedEvent() {
        // Given
        DomainEvent event = createTransactionCreatedEvent();
        Payment mockPayment = createMockPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.empty());
        when(paymentService.createPayment(anyString(), anyString(), any(BigDecimal.class), 
                                        anyString(), anyString())).thenReturn(mockPayment);
        when(paymentService.processPayment(anyString())).thenReturn(mockPayment);
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService).createPayment(eq("123"), anyString(), any(BigDecimal.class), 
                                           anyString(), eq("corr-123"));
        verify(paymentService).processPayment(mockPayment.getPaymentId());
    }
    
    @Test
    void shouldSkipProcessingIfPaymentAlreadyExists() {
        // Given
        DomainEvent event = createTransactionCreatedEvent();
        Payment existingPayment = createMockPayment();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.of(existingPayment));
        
        // When
        assertDoesNotThrow(() -> consumer.handleEvent(event));
        
        // Then
        verify(paymentService).findByTransactionId("123");
        verify(paymentService, never()).createPayment(anyString(), anyString(), 
                                                    any(BigDecimal.class), anyString(), anyString());
        verify(paymentService, never()).processPayment(anyString());
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
        DomainEvent event = createTransactionCreatedEvent();
        
        when(paymentService.findByTransactionId("123")).thenReturn(Optional.empty());
        when(paymentService.createPayment(anyString(), anyString(), any(BigDecimal.class), 
                                        anyString(), anyString()))
                .thenThrow(new RuntimeException("Payment service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> consumer.handleEvent(event));
    }
    
    private DomainEvent createTransactionCreatedEvent() {
        return new DomainEvent("TransactionCreated", "123", "Transaction", 1, "corr-123", "cause-123") {
            // Anonymous implementation for testing
        };
    }
    
    private DomainEvent createUnsupportedEvent() {
        return new DomainEvent("UnsupportedEvent", "123", "Transaction", 1, "corr-123", "cause-123") {
            // Anonymous implementation for testing
        };
    }
    
    private Payment createMockPayment() {
        Payment payment = mock(Payment.class);
        lenient().when(payment.getPaymentId()).thenReturn("payment-123");
        lenient().when(payment.getTransactionId()).thenReturn("123");
        lenient().when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        return payment;
    }
}