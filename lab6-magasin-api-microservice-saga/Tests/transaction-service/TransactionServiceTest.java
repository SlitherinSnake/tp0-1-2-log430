package com.log430.tp6.transaction.service;

import com.log430.tp6.application.service.TransactionService;
import com.log430.tp6.domain.transaction.Transaction;
import com.log430.tp6.infrastructure.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
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
    void getTransactionById_WhenTransactionExists_ShouldReturnTransaction() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        Optional<Transaction> result = transactionService.getTransactionById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTypeTransaction()).isEqualTo(Transaction.TypeTransaction.VENTE);
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransactionById_WhenTransactionNotExists_ShouldReturnEmpty() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Transaction> result = transactionService.getTransactionById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(transactionRepository).findById(999L);
    }

    @Test
    void getTransactionsByPersonnel_ShouldReturnPersonnelTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByPersonnelId(1L)).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getTransactionsByPersonnel(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPersonnelId()).isEqualTo(1L);
        verify(transactionRepository).findByPersonnelId(1L);
    }

    @Test
    void getTransactionsByStore_ShouldReturnStoreTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByStoreId(1L)).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getTransactionsByStore(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo(1L);
        verify(transactionRepository).findByStoreId(1L);
    }

    @Test
    void getTransactionCount_ShouldReturnTotalCount() {
        // Given
        when(transactionRepository.count()).thenReturn(10L);

        // When
        long result = transactionService.getTransactionCount();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(transactionRepository).count();
    }

    @Test
    void getTotalSalesAmount_ShouldReturnTotalAmount() {
        // Given
        Transaction sale1 = new Transaction();
        sale1.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        sale1.setMontantTotal(100.0);
        
        Transaction sale2 = new Transaction();
        sale2.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        sale2.setMontantTotal(200.0);
        
        Transaction returnTx = new Transaction();
        returnTx.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        returnTx.setMontantTotal(50.0);
        
        List<Transaction> transactions = Arrays.asList(sale1, sale2, returnTx);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When
        double result = transactionService.getTotalSalesAmount();

        // Then
        assertThat(result).isEqualTo(300.0); // Only sales counted
        verify(transactionRepository).findAll();
    }

    @Test
    void createReturnTransaction_WithValidData_ShouldCreateReturnTransaction() {
        // Given
        Transaction returnTransaction = new Transaction();
        returnTransaction.setTypeTransaction(Transaction.TypeTransaction.RETOUR);
        returnTransaction.setPersonnelId(1L);
        returnTransaction.setStoreId(1L);
        returnTransaction.setTransactionOriginaleId(1L);
        returnTransaction.setMotifRetour("Defective product");
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(returnTransaction);

        // When
        Transaction result = transactionService.createReturnTransaction(1L, 1L, 1L, "Defective product");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTypeTransaction()).isEqualTo(Transaction.TypeTransaction.RETOUR);
        assertThat(result.getPersonnelId()).isEqualTo(1L);
        assertThat(result.getStoreId()).isEqualTo(1L);
        assertThat(result.getTransactionOriginaleId()).isEqualTo(1L);
        assertThat(result.getMotifRetour()).isEqualTo("Defective product");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createReturnTransaction_WithNullPersonnelId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionService.createReturnTransaction(null, 1L, 1L, "Defective"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Personnel ID");
    }

    @Test
    void createReturnTransaction_WithNullStoreId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionService.createReturnTransaction(1L, null, 1L, "Defective"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Store ID");
    }

    @Test
    void createReturnTransaction_WithNullOriginalTransactionId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionService.createReturnTransaction(1L, 1L, null, "Defective"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Original transaction ID");
    }

    @Test
    void createReturnTransaction_WithEmptyMotif_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionService.createReturnTransaction(1L, 1L, 1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return reason");
    }

    @Test
    void createReturnTransaction_WithNullMotif_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionService.createReturnTransaction(1L, 1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return reason");
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnTransactionsInRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByDateTransactionBetween(startDate, endDate)).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        verify(transactionRepository).findByDateTransactionBetween(startDate, endDate);
    }

    @Test
    void getTransactionsByType_ShouldReturnTransactionsOfType() {
        // Given
        List<Transaction> sales = Arrays.asList(testTransaction);
        when(transactionRepository.findByTypeTransaction(Transaction.TypeTransaction.VENTE)).thenReturn(sales);

        // When
        List<Transaction> result = transactionService.getTransactionsByType(Transaction.TypeTransaction.VENTE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTypeTransaction()).isEqualTo(Transaction.TypeTransaction.VENTE);
        verify(transactionRepository).findByTypeTransaction(Transaction.TypeTransaction.VENTE);
    }

    @Test
    void getCompletedTransactions_ShouldReturnOnlyCompletedTransactions() {
        // Given
        List<Transaction> completedTransactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByStatut(Transaction.StatutTransaction.COMPLETEE)).thenReturn(completedTransactions);

        // When
        List<Transaction> result = transactionService.getCompletedTransactions();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatut()).isEqualTo(Transaction.StatutTransaction.COMPLETEE);
        verify(transactionRepository).findByStatut(Transaction.StatutTransaction.COMPLETEE);
    }

    @Test
    void getPendingTransactions_ShouldReturnOnlyPendingTransactions() {
        // Given
        Transaction pendingTransaction = new Transaction();
        pendingTransaction.setStatut(Transaction.StatutTransaction.EN_ATTENTE);
        List<Transaction> pendingTransactions = Arrays.asList(pendingTransaction);
        when(transactionRepository.findByStatut(Transaction.StatutTransaction.EN_ATTENTE)).thenReturn(pendingTransactions);

        // When
        List<Transaction> result = transactionService.getPendingTransactions();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatut()).isEqualTo(Transaction.StatutTransaction.EN_ATTENTE);
        verify(transactionRepository).findByStatut(Transaction.StatutTransaction.EN_ATTENTE);
    }

    @Test
    void getSalesAmountByStore_ShouldReturnStoreSpecificSalesAmount() {
        // Given
        Transaction storeSale1 = new Transaction();
        storeSale1.setStoreId(1L);
        storeSale1.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        storeSale1.setMontantTotal(100.0);
        
        Transaction storeSale2 = new Transaction();
        storeSale2.setStoreId(1L);
        storeSale2.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        storeSale2.setMontantTotal(150.0);
        
        List<Transaction> storeTransactions = Arrays.asList(storeSale1, storeSale2);
        when(transactionRepository.findByStoreIdAndTypeTransaction(1L, Transaction.TypeTransaction.VENTE))
                .thenReturn(storeTransactions);

        // When
        double result = transactionService.getSalesAmountByStore(1L);

        // Then
        assertThat(result).isEqualTo(250.0);
        verify(transactionRepository).findByStoreIdAndTypeTransaction(1L, Transaction.TypeTransaction.VENTE);
    }

    @Test
    void getSalesAmountByPersonnel_ShouldReturnPersonnelSpecificSalesAmount() {
        // Given
        Transaction personnelSale1 = new Transaction();
        personnelSale1.setPersonnelId(1L);
        personnelSale1.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        personnelSale1.setMontantTotal(200.0);
        
        Transaction personnelSale2 = new Transaction();
        personnelSale2.setPersonnelId(1L);
        personnelSale2.setTypeTransaction(Transaction.TypeTransaction.VENTE);
        personnelSale2.setMontantTotal(300.0);
        
        List<Transaction> personnelTransactions = Arrays.asList(personnelSale1, personnelSale2);
        when(transactionRepository.findByPersonnelIdAndTypeTransaction(1L, Transaction.TypeTransaction.VENTE))
                .thenReturn(personnelTransactions);

        // When
        double result = transactionService.getSalesAmountByPersonnel(1L);

        // Then
        assertThat(result).isEqualTo(500.0);
        verify(transactionRepository).findByPersonnelIdAndTypeTransaction(1L, Transaction.TypeTransaction.VENTE);
    }
}