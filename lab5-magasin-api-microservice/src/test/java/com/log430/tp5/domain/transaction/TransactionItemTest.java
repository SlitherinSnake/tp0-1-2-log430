package com.log430.tp5.domain.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionItem Domain Tests")
class TransactionItemTest {

    private TransactionItem transactionItem;

    @BeforeEach
    void setUp() {
        transactionItem = new TransactionItem();
        transactionItem.setInventoryItemId(1L);
        transactionItem.setQuantite(5);
        transactionItem.setPrixUnitaire(20.0);
    }

    @Test
    @DisplayName("Should create transaction item with valid data")
    void shouldCreateTransactionItemWithValidData() {
        // Given
        Long expectedInventoryItemId = 1L;
        Integer expectedQuantity = 5;
        Double expectedUnitPrice = 20.0;

        // When & Then
        assertEquals(expectedInventoryItemId, transactionItem.getInventoryItemId());
        assertEquals(expectedQuantity, transactionItem.getQuantite());
        assertEquals(expectedUnitPrice, transactionItem.getPrixUnitaire());
    }

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void shouldCalculateSubtotalCorrectly() {
        // Given
        transactionItem.setQuantite(3);
        transactionItem.setPrixUnitaire(25.0);

        // When
        transactionItem.calculateSubtotal();

        // Then
        assertEquals(75.0, transactionItem.getSousTotal());
    }

    @Test
    @DisplayName("Should validate positive quantity")
    void shouldValidatePositiveQuantity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionItem.setQuantite(-1);
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should validate positive unit price")
    void shouldValidatePositiveUnitPrice() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionItem.setPrixUnitaire(-10.0);
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should validate required inventory item ID")
    void shouldValidateRequiredInventoryItemId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionItem.setInventoryItemId(null);
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidationWithValidData() {
        // Given
        transactionItem.setQuantite(2);
        transactionItem.setPrixUnitaire(15.0);
        transactionItem.setInventoryItemId(1L);

        // When & Then
        assertDoesNotThrow(() -> {
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should update quantity and recalculate subtotal")
    void shouldUpdateQuantityAndRecalculateSubtotal() {
        // Given
        transactionItem.setPrixUnitaire(10.0);
        transactionItem.setQuantite(2);
        transactionItem.calculateSubtotal();
        assertEquals(20.0, transactionItem.getSousTotal());

        // When
        transactionItem.updateQuantity(5);

        // Then
        assertEquals(5, transactionItem.getQuantite());
        assertEquals(50.0, transactionItem.getSousTotal());
    }

    @Test
    @DisplayName("Should not allow zero quantity")
    void shouldNotAllowZeroQuantity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionItem.setQuantite(0);
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should not allow zero unit price")
    void shouldNotAllowZeroUnitPrice() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionItem.setPrixUnitaire(0.0);
            transactionItem.validate();
        });
    }

    @Test
    @DisplayName("Should check if quantity is available")
    void shouldCheckIfQuantityIsAvailable() {
        // Given
        int availableStock = 10;
        transactionItem.setQuantite(5);

        // When
        boolean isAvailable = transactionItem.isQuantityAvailable(availableStock);

        // Then
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("Should check if quantity exceeds available stock")
    void shouldCheckIfQuantityExceedsAvailableStock() {
        // Given
        int availableStock = 3;
        transactionItem.setQuantite(5);

        // When
        boolean isAvailable = transactionItem.isQuantityAvailable(availableStock);

        // Then
        assertFalse(isAvailable);
    }
}
