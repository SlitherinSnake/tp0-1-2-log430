package com.log430.tp7.application.service;

import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.domain.payment.events.PaymentFailed;
import com.log430.tp7.domain.payment.events.PaymentProcessed;
import com.log430.tp7.domain.payment.events.PaymentRefunded;
import com.log430.tp7.event.EventProducer;
import com.log430.tp7.infrastructure.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentGatewayService paymentGatewayService;
    
    @Mock
    private EventProducer eventProducer;
    
    private PaymentService paymentService;
    
    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentGatewayService, eventProducer);
    }
    
    @Test
    void createPayment_ShouldCreateNewPayment_WhenValidRequest() {
        // Arrange
        String transactionId = "txn-123";
        String customerId = "customer-456";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String paymentMethod = "credit_card";
        
        when(paymentRepository.existsByTransactionId(transactionId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Payment result = paymentService.createPayment(transactionId, customerId, amount, paymentMethod);
        
        // Assert
        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(amount, result.getAmount());
        assertEquals(paymentMethod, result.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        
        verify(paymentRepository).existsByTransactionId(transactionId);
        verify(paymentRepository).save(any(Payment.class));
    }
    
    @Test
    void createPayment_ShouldThrowException_WhenPaymentAlreadyExists() {
        // Arrange
        String transactionId = "txn-123";
        when(paymentRepository.existsByTransactionId(transactionId)).thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.createPayment(transactionId, "customer-456", BigDecimal.valueOf(100.00), "credit_card"));
        
        verify(paymentRepository).existsByTransactionId(transactionId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
    
    @Test
    void processPayment_ShouldProcessSuccessfully_WhenPaymentIsPending() {
        // Arrange
        String paymentId = "payment-123";
        String paymentReference = "PAY_REF_123";
        Payment payment = new Payment("txn-123", "customer-456", BigDecimal.valueOf(100.00), "credit_card");
        
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.processPayment(any(), anyString(), anyString())).thenReturn(paymentReference);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Payment result = paymentService.processPayment(paymentId);
        
        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.PROCESSED, result.getStatus());
        assertEquals(paymentReference, result.getPaymentReference());
        assertNotNull(result.getProcessedAt());
        
        verify(paymentRepository).findById(paymentId);
        verify(paymentGatewayService).processPayment(payment.getAmount(), payment.getPaymentMethod(), payment.getCustomerId());
        verify(paymentRepository).save(payment);
        verify(eventProducer).publishEvent(any(PaymentProcessed.class));
    }
    
    @Test
    void processPayment_ShouldThrowException_WhenPaymentNotFound() {
        // Arrange
        String paymentId = "payment-123";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.processPayment(paymentId));
        
        verify(paymentRepository).findById(paymentId);
        verify(paymentGatewayService, never()).processPayment(any(), anyString(), anyString());
    }
    
    @Test
    void processPayment_ShouldPublishFailedEvent_WhenPaymentProcessingFails() {
        // Arrange
        String paymentId = "payment-123";
        Payment payment = new Payment("txn-123", "customer-456", BigDecimal.valueOf(100.00), "credit_card");
        String errorMessage = "Payment gateway error";
        
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.processPayment(any(), anyString(), anyString()))
            .thenThrow(new RuntimeException(errorMessage));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act & Assert
        assertThrows(PaymentProcessingException.class, () -> 
            paymentService.processPayment(paymentId));
        
        // Verify payment status was updated to FAILED
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(errorMessage, payment.getFailureReason());
        
        verify(paymentRepository).findById(paymentId);
        verify(paymentGatewayService).processPayment(payment.getAmount(), payment.getPaymentMethod(), payment.getCustomerId());
        verify(paymentRepository).save(payment);
        verify(eventProducer).publishEvent(any(PaymentFailed.class));
    }
    
    @Test
    void refundPayment_ShouldRefundSuccessfully_WhenPaymentIsProcessed() {
        // Arrange
        String paymentId = "payment-123";
        Payment payment = new Payment("txn-123", "customer-456", BigDecimal.valueOf(100.00), "credit_card");
        payment.processPayment("PAY_REF_123");
        
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Payment result = paymentService.refundPayment(paymentId);
        
        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        assertNotNull(result.getRefundedAt());
        
        verify(paymentRepository).findById(paymentId);
        verify(paymentGatewayService).refundPayment(payment.getPaymentReference(), payment.getAmount());
        verify(paymentRepository).save(payment);
        verify(eventProducer).publishEvent(any(PaymentRefunded.class));
    }
    
    @Test
    void createPayment_ShouldSetCorrelationId_WhenProvided() {
        // Arrange
        String transactionId = "txn-123";
        String customerId = "customer-456";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String paymentMethod = "credit_card";
        String correlationId = "correlation-123";
        
        when(paymentRepository.existsByTransactionId(transactionId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Payment result = paymentService.createPayment(transactionId, customerId, amount, paymentMethod, correlationId);
        
        // Assert
        assertNotNull(result);
        assertEquals(correlationId, result.getCorrelationId());
        
        verify(paymentRepository).existsByTransactionId(transactionId);
        verify(paymentRepository).save(any(Payment.class));
    }
    
    @Test
    void createPayment_ShouldGenerateCorrelationId_WhenNotProvided() {
        // Arrange
        String transactionId = "txn-123";
        String customerId = "customer-456";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String paymentMethod = "credit_card";
        
        when(paymentRepository.existsByTransactionId(transactionId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Payment result = paymentService.createPayment(transactionId, customerId, amount, paymentMethod, null);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getCorrelationId());
        assertFalse(result.getCorrelationId().isEmpty());
        
        verify(paymentRepository).existsByTransactionId(transactionId);
        verify(paymentRepository).save(any(Payment.class));
    }
}