package com.log430.tp7.sagaorchestrator.service;

import com.log430.tp7.sagaorchestrator.logging.SagaEventLogger;
import com.log430.tp7.sagaorchestrator.metrics.SagaMetrics;
import com.log430.tp7.sagaorchestrator.model.SagaExecution;
import com.log430.tp7.sagaorchestrator.model.SagaState;
import com.log430.tp7.sagaorchestrator.repository.SagaExecutionRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for concurrent saga handling functionality.
 * Tests database-level locking, optimistic locking, and race condition handling.
 */
@ExtendWith(MockitoExtension.class)
class ConcurrentSagaHandlingTest {

    @Mock
    private SagaExecutionRepository sagaExecutionRepository;
    
    @Mock
    private SagaMetrics sagaMetrics;
    
    @Mock
    private SagaEventLogger sagaEventLogger;
    
    @Mock
    private ServiceClientWrapper serviceClientWrapper;
    
    private ConcurrentSagaManager concurrentSagaManager;
    private InventoryConcurrencyManager inventoryConcurrencyManager;
    
    private SagaExecution testSaga;
    
    @BeforeEach
    void setUp() {
        concurrentSagaManager = new ConcurrentSagaManager(
            sagaExecutionRepository, sagaMetrics, sagaEventLogger);
        
        inventoryConcurrencyManager = new InventoryConcurrencyManager(
            sagaExecutionRepository, serviceClientWrapper, sagaMetrics, sagaEventLogger);
        
        // Create test saga
        testSaga = new SagaExecution();
        testSaga.setSagaId("test-saga-1");
        testSaga.setCustomerId("customer-1");
        testSaga.setProductId("product-1");
        testSaga.setQuantity(5);
        testSaga.setAmount(new BigDecimal("100.00"));
        testSaga.setCurrentState(SagaState.SALE_INITIATED);
        testSaga.setCreatedAt(LocalDateTime.now());
        testSaga.setUpdatedAt(LocalDateTime.now());
        testSaga.setVersion(0L);
    }
    
    @Test
    void testOptimisticLockingRetrySuccess() {
        // Given
        when(sagaExecutionRepository.findById("test-saga-1"))
            .thenReturn(Optional.of(testSaga));
        
        // First attempt fails with optimistic lock exception
        when(sagaExecutionRepository.save(any(SagaExecution.class)))
            .thenThrow(new OptimisticLockException("Version mismatch"))
            .thenReturn(testSaga);
        
        // When
        SagaExecution result = concurrentSagaManager.updateSagaStateWithRetry(
            "test-saga-1", SagaState.STOCK_VERIFYING, null);
        
        // Then
        assertNotNull(result);
        verify(sagaExecutionRepository, times(2)).save(any(SagaExecution.class));
        verify(sagaMetrics).recordStateTransition(SagaState.SALE_INITIATED, SagaState.STOCK_VERIFYING);
    }
    
    @Test
    void testOptimisticLockingRetryExhaustion() {
        // Given
        when(sagaExecutionRepository.findById("test-saga-1"))
            .thenReturn(Optional.of(testSaga));
        
        // All attempts fail with optimistic lock exception
        when(sagaExecutionRepository.save(any(SagaExecution.class)))
            .thenThrow(new OptimisticLockingFailureException("Version mismatch"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            concurrentSagaManager.updateSagaStateWithRetry("test-saga-1", SagaState.STOCK_VERIFYING, null));
        
        assertTrue(exception.getMessage().contains("Failed to update saga state after 3 attempts"));
        verify(sagaExecutionRepository, times(3)).save(any(SagaExecution.class));
        verify(sagaMetrics).recordError("OptimisticLockFailure", "concurrent_update");
    }
    
    @Test
    void testPessimisticLockingUpdate() {
        // Given
        when(sagaExecutionRepository.findByIdWithLock("test-saga-1"))
            .thenReturn(Optional.of(testSaga));
        when(sagaExecutionRepository.save(any(SagaExecution.class)))
            .thenReturn(testSaga);
        
        // When
        SagaExecution result = concurrentSagaManager.updateSagaStateWithPessimisticLock(
            "test-saga-1", SagaState.STOCK_VERIFYING, null);
        
        // Then
        assertNotNull(result);
        verify(sagaExecutionRepository).findByIdWithLock("test-saga-1");
        verify(sagaExecutionRepository).save(any(SagaExecution.class));
        verify(sagaMetrics).recordStateTransition(SagaState.SALE_INITIATED, SagaState.STOCK_VERIFYING);
    }
    
    @Test
    void testConcurrentSagaDetection() {
        // Given
        SagaExecution concurrentSaga = new SagaExecution();
        concurrentSaga.setSagaId("test-saga-2");
        concurrentSaga.setCustomerId("customer-1");
        concurrentSaga.setProductId("product-1");
        concurrentSaga.setCurrentState(SagaState.STOCK_VERIFYING);
        concurrentSaga.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        
        when(sagaExecutionRepository.findActiveSagas())
            .thenReturn(Arrays.asList(testSaga, concurrentSaga));
        
        // When
        boolean hasConcurrent = concurrentSagaManager.hasConcurrentSagas(
            "customer-1", "product-1", "test-saga-1");
        
        // Then
        assertTrue(hasConcurrent);
        verify(sagaMetrics).recordError("ConcurrentSagaDetected", "race_condition_check");
    }
    
    @Test
    void testStockReservationRaceConditionHandling() {
        // Given
        SagaExecution earlierSaga = new SagaExecution();
        earlierSaga.setSagaId("earlier-saga");
        earlierSaga.setCustomerId("customer-1");
        earlierSaga.setProductId("product-1");
        earlierSaga.setCurrentState(SagaState.STOCK_RESERVING);
        earlierSaga.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        
        testSaga.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        
        when(sagaExecutionRepository.findActiveByCustomerAndProductWithLock("customer-1", "product-1"))
            .thenReturn(Arrays.asList(earlierSaga, testSaga));
        
        // When
        boolean canProceed = concurrentSagaManager.handleStockReservationRaceCondition(
            "customer-1", "product-1", "test-saga-1");
        
        // Then
        assertFalse(canProceed); // Current saga should not proceed as it's newer
        verify(sagaMetrics).recordError("StockReservationRaceCondition", "race_condition_handling");
    }
    
    @Test
    void testCustomerProductLockingMechanism() {
        // Given
        String customerId = "customer-1";
        String productId = "product-1";
        String sagaId = "test-saga-1";
        
        // When
        String lockKey = concurrentSagaManager.acquireCustomerProductLock(customerId, productId, sagaId);
        
        // Then
        assertNotNull(lockKey);
        assertEquals(customerId + ":" + productId, lockKey);
        
        // Cleanup
        concurrentSagaManager.releaseCustomerProductLock(lockKey, sagaId);
    }
    
    @Test
    void testConcurrentLockAcquisition() throws InterruptedException, java.util.concurrent.ExecutionException {
        // Given
        String customerId = "customer-1";
        String productId = "product-1";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // When
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                startLatch.await();
                String lockKey = concurrentSagaManager.acquireCustomerProductLock(customerId, productId, "saga-1");
                Thread.sleep(100); // Hold lock briefly
                concurrentSagaManager.releaseCustomerProductLock(lockKey, "saga-1");
                completeLatch.countDown();
                return "saga-1-completed";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "saga-1-interrupted";
            }
        }, executor);
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                startLatch.await();
                String lockKey = concurrentSagaManager.acquireCustomerProductLock(customerId, productId, "saga-2");
                Thread.sleep(100); // Hold lock briefly
                concurrentSagaManager.releaseCustomerProductLock(lockKey, "saga-2");
                completeLatch.countDown();
                return "saga-2-completed";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "saga-2-interrupted";
            }
        }, executor);
        
        startLatch.countDown(); // Start both tasks
        
        // Then
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS));
        assertEquals("saga-1-completed", future1.get());
        assertEquals("saga-2-completed", future2.get());
        
        executor.shutdown();
    }
    
    @Test
    void testConcurrencyValidationResult() {
        // Given
        when(sagaExecutionRepository.countConcurrentActiveSagas("customer-1", "product-1", "test-saga-1"))
            .thenReturn(0L);
        
        // When
        ConcurrentSagaManager.ConcurrencyValidationResult result = 
            concurrentSagaManager.validateConcurrentInventoryAccess(testSaga);
        
        // Then
        assertTrue(result.canProceed());
        assertEquals("No concurrent sagas detected", result.message());
        assertNull(result.recommendations());
    }
    
    @Test
    void testConcurrencyMonitoringReport() {
        // Given
        when(sagaExecutionRepository.findActiveSagas())
            .thenReturn(Arrays.asList(testSaga));
        
        // When
        ConcurrentSagaManager.ConcurrencyMonitoringReport report = 
            concurrentSagaManager.generateConcurrencyReport();
        
        // Then
        assertEquals(1, report.totalActiveSagas());
        assertEquals(0, report.concurrentCombinations());
        assertEquals(0, report.activeLocks());
        assertTrue(report.highConcurrencyCombinations().isEmpty());
        assertNotNull(report.timestamp());
    }
    
    @Test
    void testInventoryConcurrencyStatistics() {
        // When
        java.util.Map<String, Object> stats = inventoryConcurrencyManager.getInventoryConcurrencyStatistics();
        
        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("activeProductLocks"));
        assertTrue(stats.containsKey("activeReadLocks"));
        assertTrue(stats.containsKey("activeWriteLocks"));
        assertTrue(stats.containsKey("timestamp"));
        
        assertEquals(0, stats.get("activeProductLocks"));
        assertEquals(0, stats.get("activeReadLocks"));
        assertEquals(0, stats.get("activeWriteLocks"));
    }
    
    @Test
    void testSagaValidationCanProceed() {
        // Given
        when(sagaExecutionRepository.countConcurrentActiveSagas("customer-1", "product-1", "test-saga-1"))
            .thenReturn(0L);
        
        // When
        boolean canProceed = concurrentSagaManager.validateSagaCanProceed(testSaga);
        
        // Then
        assertTrue(canProceed);
    }
    
    @Test
    void testSagaValidationCannotProceedDueToFinalState() {
        // Given
        testSaga.setCurrentState(SagaState.SALE_CONFIRMED);
        
        // When
        boolean canProceed = concurrentSagaManager.validateSagaCanProceed(testSaga);
        
        // Then
        assertFalse(canProceed);
    }
    
    @Test
    void testRaceConditionDetection() {
        // Given
        SagaExecution conflictingSaga = new SagaExecution();
        conflictingSaga.setSagaId("conflicting-saga");
        conflictingSaga.setCustomerId("customer-1");
        conflictingSaga.setProductId("product-1");
        
        when(sagaExecutionRepository.findPotentialRaceConditions())
            .thenReturn(Arrays.asList(testSaga, conflictingSaga));
        
        // When
        int conflicts = concurrentSagaManager.detectAndLogRaceConditions();
        
        // Then
        assertEquals(2, conflicts);
        verify(sagaMetrics).recordError("RaceConditionDetected", "race_condition_detection");
    }
}