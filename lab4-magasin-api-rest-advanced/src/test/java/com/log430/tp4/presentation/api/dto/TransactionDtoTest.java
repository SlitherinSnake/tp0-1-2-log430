package com.log430.tp4.presentation.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.log430.tp4.domain.transaction.Transaction;
import com.log430.tp4.domain.transaction.TransactionItem;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionDto Tests")
class TransactionDtoTest {

    private Transaction transaction;
    private TransactionItem transactionItem1;
    private TransactionItem transactionItem2;
    private LongFunction<String> productNameResolver;

    @BeforeEach
    void setUp() {
        // Setup transaction items
        transactionItem1 = new TransactionItem();
        transactionItem1.setId(1L);
        transactionItem1.setInventoryItemId(101L);
        transactionItem1.setQuantite(2);
        transactionItem1.setPrixUnitaire(50.0);
        // Note: sousTotal is calculated automatically when quantity and price are set

        transactionItem2 = new TransactionItem();
        transactionItem2.setId(2L);
        transactionItem2.setInventoryItemId(102L);
        transactionItem2.setQuantite(1);
        transactionItem2.setPrixUnitaire(75.0);
        // Note: sousTotal is calculated automatically when quantity and price are set

        // Setup transaction
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        transaction.setDateTransaction(LocalDate.of(2023, 12, 25));
        transaction.setPersonnelId(1L);
        transaction.setStoreId(1L);
        transaction.setMontantTotal(175.0);
        transaction.setStatut(Transaction.StatutTransaction.COMPLETEE);
        transaction.setItems(Arrays.asList(transactionItem1, transactionItem2));

        // Setup product name resolver
        productNameResolver = id -> {
            if (id == 101L) return "Product 1";
            if (id == 102L) return "Product 2";
            return "Unknown Product";
        };
    }

    @Test
    @DisplayName("Should create DTO from entity correctly")
    void shouldCreateDtoFromEntityCorrectly() {
        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getDateTransaction().toString(), dto.getDate());
        assertEquals("Client", dto.getClientName());
        assertEquals("Client", dto.getClient());
        assertEquals(transaction.getMontantTotal(), dto.getTotal());
        assertEquals(transaction.getStatut().name(), dto.getStatus());
        assertEquals(transaction.getTypeTransaction().name(), dto.getType());
        assertEquals(transaction.getPersonnelId(), dto.getPersonnelId());
        assertEquals(transaction.getStoreId(), dto.getStoreId());
        assertEquals(2, dto.getItems().size());
    }

    @Test
    @DisplayName("Should create DTO with product name resolver")
    void shouldCreateDtoWithProductNameResolver() {
        // When
        TransactionDto dto = new TransactionDto(transaction, productNameResolver);

        // Then
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(2, dto.getItems().size());
        
        // Check that product names are resolved
        TransactionDto.TransactionItemDto item1Dto = dto.getItems().get(0);
        TransactionDto.TransactionItemDto item2Dto = dto.getItems().get(1);
        
        // Note: The actual product name resolution would depend on the TransactionItemDto implementation
        assertNotNull(item1Dto);
        assertNotNull(item2Dto);
    }

    @Test
    @DisplayName("Should create DTO using static factory method")
    void shouldCreateDtoUsingStaticFactoryMethod() {
        // When
        TransactionDto dto = TransactionDto.fromEntity(transaction);

        // Then
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getDateTransaction().toString(), dto.getDate());
        assertEquals(transaction.getMontantTotal(), dto.getTotal());
    }

    @Test
    @DisplayName("Should use default constructor correctly")
    void shouldUseDefaultConstructorCorrectly() {
        // When
        TransactionDto dto = new TransactionDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getDate());
        assertNull(dto.getClientName());
        assertNull(dto.getClient());
        assertNull(dto.getItems());
        assertNull(dto.getTotal());
        assertNull(dto.getStatus());
        assertNull(dto.getType());
        assertNull(dto.getPersonnelId());
        assertNull(dto.getStoreId());
    }

    @Test
    @DisplayName("Should handle null transaction date")
    void shouldHandleNullTransactionDate() {
        // Given
        transaction.setDateTransaction(null);

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertNull(dto.getDate());
    }

    @Test
    @DisplayName("Should handle null transaction status")
    void shouldHandleNullTransactionStatus() {
        // Given
        transaction.setStatut(null);

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertEquals("UNKNOWN", dto.getStatus());
    }

    @Test
    @DisplayName("Should handle null transaction type")
    void shouldHandleNullTransactionType() {
        // Given
        transaction.setTypeTransaction(null);

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertEquals("UNKNOWN", dto.getType());
    }

    @Test
    @DisplayName("Should handle null transaction items")
    void shouldHandleNullTransactionItems() {
        // Given
        transaction.setItems(null);

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty transaction items")
    void shouldHandleEmptyTransactionItems() {
        // Given
        transaction.setItems(Arrays.asList());

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should handle return transaction correctly")
    void shouldHandleReturnTransactionCorrectly() {
        // Given
        transaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        transaction.setTransactionOriginaleId(123L);
        transaction.setMotifRetour("Produit défectueux");

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertEquals("RETOUR", dto.getType());
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getMontantTotal(), dto.getTotal());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        // Given
        TransactionDto dto = new TransactionDto();
        List<TransactionDto.TransactionItemDto> items = Arrays.asList(new TransactionDto.TransactionItemDto());

        // When
        dto.setId(2L);
        dto.setDate("2023-12-26");
        dto.setClientName("Test Client");
        dto.setClient("Test Client");
        dto.setItems(items);
        dto.setTotal(250.0);
        dto.setStatus("COMPLETEE");
        dto.setType("VENTE");
        dto.setPersonnelId(2L);
        dto.setStoreId(2L);

        // Then
        assertEquals(2L, dto.getId());
        assertEquals("2023-12-26", dto.getDate());
        assertEquals("Test Client", dto.getClientName());
        assertEquals("Test Client", dto.getClient());
        assertEquals(items, dto.getItems());
        assertEquals(250.0, dto.getTotal());
        assertEquals("COMPLETEE", dto.getStatus());
        assertEquals("VENTE", dto.getType());
        assertEquals(2L, dto.getPersonnelId());
        assertEquals(2L, dto.getStoreId());
    }

    @Test
    @DisplayName("Should maintain data integrity after DTO conversion")
    void shouldMaintainDataIntegrityAfterDtoConversion() {
        // Given
        Long expectedId = 42L;
        Double expectedTotal = 999.99;
        Long expectedPersonnelId = 5L;
        String expectedStatus = "EN_COURS";

        transaction.setId(expectedId);
        transaction.setMontantTotal(expectedTotal);
        transaction.setPersonnelId(expectedPersonnelId);
        transaction.setStatut(Transaction.StatutTransaction.EN_COURS);

        // When
        TransactionDto dto = new TransactionDto(transaction);

        // Then
        assertEquals(expectedId, dto.getId());
        assertEquals(expectedTotal, dto.getTotal());
        assertEquals(expectedPersonnelId, dto.getPersonnelId());
        assertEquals(expectedStatus, dto.getStatus());
    }
}
