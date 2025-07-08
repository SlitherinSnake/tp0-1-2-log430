package com.log430.tp4.domain.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Domain Tests")
class TransactionTest {

    private Transaction transaction;
    private TransactionItem transactionItem;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        transaction.setDateTransaction(LocalDate.now());
        transaction.setPersonnelId(1L);
        transaction.setStoreId(1L);
        transaction.setStatut(Transaction.StatutTransaction.EN_COURS);

        transactionItem = new TransactionItem();
        transactionItem.setInventoryItemId(1L);
        transactionItem.setQuantite(2);
        transactionItem.setPrixUnitaire(50.0);
        // Note: sousTotal is calculated automatically
    }

    @Test
    @DisplayName("Should create transaction with valid data")
    void shouldCreateTransactionWithValidData() {
        // Given
        Transaction.TypeTransaction expectedType = Transaction.TypeTransaction.VENTE;
        LocalDate expectedDate = LocalDate.now();
        Long expectedPersonnelId = 1L;
        Long expectedStoreId = 1L;

        // When & Then
        assertEquals(expectedType, transaction.getTypeTransaction());
        assertEquals(expectedDate, transaction.getDateTransaction());
        assertEquals(expectedPersonnelId, transaction.getPersonnelId());
        assertEquals(expectedStoreId, transaction.getStoreId());
    }

    @Test
    @DisplayName("Should add transaction items correctly")
    void shouldAddTransactionItemsCorrectly() {
        // Given
        List<TransactionItem> items = new ArrayList<>();
        items.add(transactionItem);

        // When
        transaction.setItems(items);

        // Then
        assertEquals(1, transaction.getItems().size());
        assertEquals(transactionItem, transaction.getItems().get(0));
    }

    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        TransactionItem item1 = new TransactionItem();
        item1.setQuantite(2);
        item1.setPrixUnitaire(25.0);
        // sousTotal will be 50.0
        
        TransactionItem item2 = new TransactionItem();
        item2.setQuantite(3);
        item2.setPrixUnitaire(25.0);
        // sousTotal will be 75.0

        List<TransactionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        // When
        transaction.setItems(items);
        transaction.calculateTotal();

        // Then
        assertEquals(125.0, transaction.getMontantTotal());
    }

    @Test
    @DisplayName("Should complete transaction successfully")
    void shouldCompleteTransactionSuccessfully() {
        // Given
        transaction.setStatut(Transaction.StatutTransaction.EN_COURS);

        // When
        transaction.complete();

        // Then
        assertEquals(Transaction.StatutTransaction.COMPLETEE, transaction.getStatut());
    }

    @Test
    @DisplayName("Should cancel transaction successfully")
    void shouldCancelTransactionSuccessfully() {
        // Given
        transaction.setStatut(Transaction.StatutTransaction.EN_COURS);

        // When
        transaction.cancel();

        // Then
        assertEquals(Transaction.StatutTransaction.ANNULEE, transaction.getStatut());
    }

    @Test
    @DisplayName("Should not complete already completed transaction")
    void shouldNotCompleteAlreadyCompletedTransaction() {
        // Given
        transaction.setStatut(Transaction.StatutTransaction.COMPLETEE);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transaction.complete();
        });
    }

    @Test
    @DisplayName("Should not cancel completed transaction")
    void shouldNotCancelCompletedTransaction() {
        // Given
        transaction.setStatut(Transaction.StatutTransaction.COMPLETEE);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transaction.cancel();
        });
    }

    @Test
    @DisplayName("Should create return transaction with original reference")
    void shouldCreateReturnTransactionWithOriginalReference() {
        // Given
        Long originalTransactionId = 123L;

        // When
        Transaction returnTransaction = new Transaction();
        returnTransaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        returnTransaction.setTransactionOriginaleId(originalTransactionId);
        returnTransaction.setMotifRetour("Produit défectueux");

        // Then
        assertEquals(Transaction.TypeTransaction.RETOUR, returnTransaction.getTypeTransaction());
        assertEquals(originalTransactionId, returnTransaction.getTransactionOriginaleId());
        assertEquals("Produit défectueux", returnTransaction.getMotifRetour());
    }

    @Test
    @DisplayName("Should validate transaction before completion")
    void shouldValidateTransactionBeforeCompletion() {
        // Given
        transaction.setItems(new ArrayList<>()); // Empty items list

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transaction.validate();
        });
    }

    @Test
    @DisplayName("Should pass validation with valid transaction")
    void shouldPassValidationWithValidTransaction() {
        // Given
        List<TransactionItem> items = new ArrayList<>();
        items.add(transactionItem);
        transaction.setItems(items);
        transaction.setMontantTotal(100.0);

        // When & Then
        assertDoesNotThrow(() -> {
            transaction.validate();
        });
    }

    @Test
    @DisplayName("Should check if transaction is sale")
    void shouldCheckIfTransactionIsSale() {
        // Given
        transaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);

        // When
        boolean isSale = transaction.isSale();

        // Then
        assertTrue(isSale);
    }

    @Test
    @DisplayName("Should check if transaction is return")
    void shouldCheckIfTransactionIsReturn() {
        // Given
        transaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);

        // When
        boolean isReturn = transaction.isReturn();

        // Then
        assertTrue(isReturn);
    }
}
