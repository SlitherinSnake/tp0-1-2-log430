package com.log430.tp7.infrastructure.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.log430.tp7.application.service.EventDrivenFulfillmentService;
import com.log430.tp7.event.DomainEvent;

@ExtendWith(MockitoExtension.class)
class InventoryEventConsumerTest {

    @Mock
    private EventDrivenFulfillmentService fulfillmentService;

    @InjectMocks
    private InventoryEventConsumer consumer;

    private DomainEvent inventoryReservedEvent;

    @BeforeEach
    void setUp() {
        inventoryReservedEvent = createInventoryReservedEvent();
    }

    @Test
    void shouldSupportInventoryReservedEventType() {
        assertTrue(consumer.canHandle("InventoryReserved"));
    }

    @Test
    void shouldNotSupportOtherEventTypes() {
        assertFalse(consumer.canHandle("PaymentProcessed"));
        assertFalse(consumer.canHandle("TransactionCreated"));
    }

    @Test
    void shouldReturnCorrectSupportedEventTypes() {
        String[] supportedTypes = consumer.getSupportedEventTypes();
        assertEquals(1, supportedTypes.length);
        assertEquals("InventoryReserved", supportedTypes[0]);
    }

    @Test
    void shouldProcessInventoryReservedEventAndTriggerFulfillment() {
        // Arrange
        when(fulfillmentService.fulfillOrder(eq("123"), eq("corr-123"), isNull()))
                .thenReturn(true);

        // Act
        consumer.handleEvent(inventoryReservedEvent);

        // Assert
        verify(fulfillmentService).fulfillOrder("123", "corr-123", null);
    }

    @Test
    void shouldHandleFulfillmentFailureGracefully() {
        // Arrange
        when(fulfillmentService.fulfillOrder(eq("123"), eq("corr-123"), isNull()))
                .thenReturn(false);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> consumer.handleEvent(inventoryReservedEvent));
        verify(fulfillmentService).fulfillOrder("123", "corr-123", null);
    }

    @Test
    void shouldHandleFulfillmentServiceExceptionAndRethrow() {
        // Arrange
        when(fulfillmentService.fulfillOrder(eq("123"), eq("corr-123"), isNull()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> consumer.handleEvent(inventoryReservedEvent));
        
        assertEquals("Failed to handle InventoryReserved event", exception.getMessage());
        verify(fulfillmentService).fulfillOrder("123", "corr-123", null);
    }

    @Test
    void shouldIgnoreUnsupportedEventTypes() {
        // Arrange
        DomainEvent unsupportedEvent = new DomainEvent("PaymentProcessed", "456", "Payment", 1, "corr-456", "cause-456") {
            // No abstract methods to implement for this DomainEvent class
        };

        // Act
        consumer.handleEvent(unsupportedEvent);

        // Assert
        verifyNoInteractions(fulfillmentService);
    }

    @Test
    void shouldHandleNullCorrelationIdGracefully() {
        // Arrange
        DomainEvent eventWithoutCorrelation = new DomainEvent("InventoryReserved", "123", "Inventory", 1, null, null) {
            // No abstract methods to implement for this DomainEvent class
        };

        when(fulfillmentService.fulfillOrder(eq("123"), isNull(), isNull()))
                .thenReturn(true);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> consumer.handleEvent(eventWithoutCorrelation));
        verify(fulfillmentService).fulfillOrder("123", null, null);
    }

    private DomainEvent createInventoryReservedEvent() {
        return new DomainEvent("InventoryReserved", "123", "Inventory", 1, "corr-123", "cause-123") {
            // No abstract methods to implement for this DomainEvent class
        };
    }
}
