package com.log430.tp5.presentation.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.log430.tp5.application.service.InventoryService;
import com.log430.tp5.domain.transaction.Transaction;
import com.log430.tp5.domain.transaction.TransactionItem;
import com.log430.tp5.infrastructure.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@ContextConfiguration(classes = {TransactionController.class})
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private InventoryService inventoryService;

    private Transaction transaction1;
    private Transaction transaction2;
    private TransactionItem transactionItem;

    @BeforeEach
    void setUp() {
        // Setup transaction items
        transactionItem = new TransactionItem();
        transactionItem.setId(1L);
        transactionItem.setInventoryItemId(1L);
        transactionItem.setQuantite(2);
        transactionItem.setPrixUnitaire(50.0);
        transactionItem.calculateSubtotal(); // This will set the subtotal

        // Setup transactions
        transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        transaction1.setDateTransaction(LocalDate.now());
        transaction1.setPersonnelId(1L);
        transaction1.setStoreId(1L);
        transaction1.setMontantTotal(100.0);
        transaction1.setStatut(Transaction.StatutTransaction.COMPLETEE);
        transaction1.setItems(Arrays.asList(transactionItem));

        transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        transaction2.setDateTransaction(LocalDate.now());
        transaction2.setPersonnelId(1L);
        transaction2.setStoreId(1L);
        transaction2.setMontantTotal(50.0);
        transaction2.setStatut(Transaction.StatutTransaction.COMPLETEE);
        transaction2.setTransactionOriginaleId(1L);
        transaction2.setMotifRetour("Produit défectueux");
    }

    @Test
    @DisplayName("POST /api/transactions should create new sale transaction")
    void shouldCreateNewSaleTransaction() throws Exception {
        // Given
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(3L);
        savedTransaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        savedTransaction.setDateTransaction(LocalDate.now());
        savedTransaction.setPersonnelId(1L);
        savedTransaction.setStoreId(1L);
        savedTransaction.setMontantTotal(150.0);
        savedTransaction.setStatut(Transaction.StatutTransaction.COMPLETEE);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        String requestBody = """
            {
                "personnelId": 1,
                "storeId": 1,
                "montantTotal": 150.0,
                "items": [
                    {
                        "id": 1,
                        "quantity": 3,
                        "price": 50.0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value(3));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("GET /api/transactions should return all transactions")
    void shouldReturnAllTransactions() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("VENTE"))
                .andExpect(jsonPath("$[0].total").value(100.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("RETOUR"));

        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Should handle invalid transaction creation request")
    void shouldHandleInvalidTransactionCreationRequest() throws Exception {
        // Given - Invalid request body missing required fields
        String invalidRequestBody = """
            {
                "personnelId": 1
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle transaction creation with empty items")
    void shouldHandleTransactionCreationWithEmptyItems() throws Exception {
        // Given
        String requestBodyWithEmptyItems = """
            {
                "personnelId": 1,
                "storeId": 1,
                "montantTotal": 0.0,
                "items": []
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithEmptyItems))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle transaction creation with invalid personnel ID")
    void shouldHandleTransactionCreationWithInvalidPersonnelId() throws Exception {
        // Given
        String requestBodyWithInvalidPersonnelId = """
            {
                "personnelId": "invalid",
                "storeId": 1,
                "montantTotal": 100.0,
                "items": [
                    {
                        "id": 1,
                        "quantity": 2,
                        "price": 50.0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithInvalidPersonnelId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle transaction creation with negative amount")
    void shouldHandleTransactionCreationWithNegativeAmount() throws Exception {
        // Given
        String requestBodyWithNegativeAmount = """
            {
                "personnelId": 1,
                "storeId": 1,
                "montantTotal": -100.0,
                "items": [
                    {
                        "id": 1,
                        "quantity": 2,
                        "price": 50.0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithNegativeAmount))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle repository exceptions during transaction creation")
    void shouldHandleRepositoryExceptionsDuringTransactionCreation() throws Exception {
        // Given
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        String requestBody = """
            {
                "personnelId": 1,
                "storeId": 1,
                "montantTotal": 100.0,
                "items": [
                    {
                        "id": 1,
                        "quantity": 2,
                        "price": 50.0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should handle repository exceptions during transaction retrieval")
    void shouldHandleRepositoryExceptionsDuringTransactionRetrieval() throws Exception {
        // Given
        when(transactionRepository.findAll())
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isInternalServerError());

        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Should create transaction with multiple items")
    void shouldCreateTransactionWithMultipleItems() throws Exception {
        // Given
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(4L);
        savedTransaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        savedTransaction.setMontantTotal(275.0);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        String requestBody = """
            {
                "personnelId": 1,
                "storeId": 1,
                "montantTotal": 275.0,
                "items": [
                    {
                        "id": 1,
                        "quantity": 2,
                        "price": 50.0
                    },
                    {
                        "id": 2,
                        "quantity": 5,
                        "price": 35.0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value(4));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist")
    void shouldReturnEmptyListWhenNoTransactionsExist() throws Exception {
        // Given
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(transactionRepository).findAll();
    }
}
