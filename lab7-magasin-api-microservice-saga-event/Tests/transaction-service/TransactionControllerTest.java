package com.log430.tp7.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.TransactionService;
import com.log430.tp7.domain.transaction.Transaction;
import com.log430.tp7.infrastructure.repository.TransactionRepository;
import com.log430.tp7.presentation.api.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        testTransaction.setDateTransaction(LocalDate.now());
        testTransaction.setPersonnelId(1L);
        testTransaction.setStoreId(1L);
        testTransaction.setMontantTotal(199.98);
        testTransaction.setStatut(Transaction.StatutTransaction.COMPLETEE);
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].typeTransaction").value("VENTE"));

        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionById_WhenTransactionExists_ShouldReturnTransaction() throws Exception {
        // Given
        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(testTransaction));

        // When & Then
        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeTransaction").value("VENTE"));

        verify(transactionService).getTransactionById(1L);
    }

    @Test
    void getTransactionById_WhenTransactionNotExists_ShouldReturn404() throws Exception {
        // Given
        when(transactionService.getTransactionById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());

        verify(transactionService).getTransactionById(999L);
    }

    @Test
    void createSale_WithValidData_ShouldCreateTransaction() throws Exception {
        // Given
        List<TransactionController.SaleItem> items = Arrays.asList(
                new TransactionController.SaleItem(1L, 2, 99.99)
        );
        TransactionController.CreateSaleRequest request = new TransactionController.CreateSaleRequest(
                1L, 1L, items, 199.98
        );
        
        testTransaction.setId(1L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value(1));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createSale_WithEmptyItems_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionController.CreateSaleRequest request = new TransactionController.CreateSaleRequest(
                1L, 1L, Arrays.asList(), 0.0
        );

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Items list cannot be empty"));
    }

    @Test
    void createSale_WithNullItems_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionController.CreateSaleRequest request = new TransactionController.CreateSaleRequest(
                1L, 1L, null, 0.0
        );

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Items list cannot be empty"));
    }

    @Test
    void getTransactionsByPersonnel_ShouldReturnPersonnelTransactions() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionService.getTransactionsByPersonnel(1L)).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/personnel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].personnelId").value(1));

        verify(transactionService).getTransactionsByPersonnel(1L);
    }

    @Test
    void getTransactionsByStore_ShouldReturnStoreTransactions() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionService.getTransactionsByStore(1L)).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/store/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].storeId").value(1));

        verify(transactionService).getTransactionsByStore(1L);
    }

    @Test
    void getTransactionStats_ShouldReturnStatistics() throws Exception {
        // Given
        when(transactionService.getTransactionCount()).thenReturn(10L);
        when(transactionService.getTotalSalesAmount()).thenReturn(1999.80);

        // When & Then
        mockMvc.perform(get("/api/transactions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(10))
                .andExpect(jsonPath("$.totalSales").value(1999.80));

        verify(transactionService).getTransactionCount();
        verify(transactionService).getTotalSalesAmount();
    }

    @Test
    void createReturn_WithValidData_ShouldCreateReturnTransaction() throws Exception {
        // Given
        Transaction originalTransaction = new Transaction();
        originalTransaction.setId(1L);
        originalTransaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        originalTransaction.setStatut(Transaction.StatutTransaction.COMPLETEE);
        
        Transaction returnTransaction = new Transaction();
        returnTransaction.setId(2L);
        returnTransaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        
        List<TransactionController.ReturnItem> items = Arrays.asList(
                new TransactionController.ReturnItem(1L, 1, 99.99)
        );
        TransactionController.CreateReturnRequest request = new TransactionController.CreateReturnRequest(
                1L, 1L, 1L, "Defective product", items
        );

        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(originalTransaction));
        when(transactionService.createReturnTransaction(1L, 1L, 1L, "Defective product"))
                .thenReturn(returnTransaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(returnTransaction);

        // When & Then
        mockMvc.perform(post("/api/transactions/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value(2));

        verify(transactionService).getTransactionById(1L);
        verify(transactionService).createReturnTransaction(1L, 1L, 1L, "Defective product");
    }

    @Test
    void createReturn_WithInvalidOriginalTransaction_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionController.CreateReturnRequest request = new TransactionController.CreateReturnRequest(
                1L, 1L, 999L, "Defective product", Arrays.asList()
        );

        when(transactionService.getTransactionById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/transactions/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(transactionService).getTransactionById(999L);
    }

    @Test
    void getReturnableTransactions_ShouldReturnCompletedSales() throws Exception {
        // Given
        testTransaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        testTransaction.setStatut(Transaction.StatutTransaction.COMPLETEE);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/transactions/returnable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transactionRepository).findAll();
    }

    @Test
    void getReturnsByOriginalTransaction_ShouldReturnReturns() throws Exception {
        // Given
        Transaction returnTransaction = new Transaction();
        returnTransaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        returnTransaction.setTransactionOriginaleId(1L);
        
        List<Transaction> returns = Arrays.asList(returnTransaction);
        when(transactionRepository.findByTransactionOriginaleId(1L)).thenReturn(returns);

        // When & Then
        mockMvc.perform(get("/api/transactions/returns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transactionRepository).findByTransactionOriginaleId(1L);
    }

    @Test
    void testEndpoint_ShouldReturnOkStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/transactions/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Transaction API is working"));
    }

    @Test
    void createSale_WhenRepositorySaveFails_ShouldReturnError() throws Exception {
        // Given
        List<TransactionController.SaleItem> items = Arrays.asList(
                new TransactionController.SaleItem(1L, 2, 99.99)
        );
        TransactionController.CreateSaleRequest request = new TransactionController.CreateSaleRequest(
                1L, 1L, items, 199.98
        );
        
        Transaction transactionWithoutId = new Transaction();
        transactionWithoutId.setId(null); // Simulate save failure
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transactionWithoutId);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Failed to save transaction"));
    }

    @Test
    void createSale_WhenExceptionThrown_ShouldReturnError() throws Exception {
        // Given
        List<TransactionController.SaleItem> items = Arrays.asList(
                new TransactionController.SaleItem(1L, 2, 99.99)
        );
        TransactionController.CreateSaleRequest request = new TransactionController.CreateSaleRequest(
                1L, 1L, items, 199.98
        );
        
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Database error"));
    }
}