package com.log430.tp7.integration;

import com.log430.tp7.domain.payment.events.PaymentProcessed;
import com.log430.tp7.event.EventProducer;
import com.log430.tp7.payment.PaymentServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify event publishing functionality in the Payment Service.
 * This test verifies that events are properly structured and can be created correctly.
 */
public class PaymentEventPublishingIntegrationTest {
    
    @Test
    void paymentProcessedEvent_ShouldHaveCorrectStructure() {
        // Arrange
        String paymentId = "payment-123";
        String transactionId = "txn-456";
        String customerId = "customer-789";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        String paymentMethod = "credit_card";
        String paymentReference = "PAY_REF_123";
        String correlationId = "correlation-123";
        
        // Act
        PaymentProcessed event = new PaymentProcessed(
            paymentId, transactionId, customerId, amount, 
            paymentMethod, paymentReference, 
            java.time.LocalDateTime.now(), correlationId
        );
        
        // Assert
        assertNotNull(event);
        assertEquals("PaymentProcessed", event.getEventType());
        assertEquals(paymentId, event.getAggregateId());
        assertEquals("Payment", event.getAggregateType());
        assertEquals(correlationId, event.getCorrelationId());
        assertEquals(paymentId, event.getPaymentId());
        assertEquals(transactionId, event.getTransactionId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(amount, event.getAmount());
        assertEquals(paymentMethod, event.getPaymentMethod());
        assertEquals(paymentReference, event.getPaymentReference());
        assertNotNull(event.getProcessedAt());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }
}