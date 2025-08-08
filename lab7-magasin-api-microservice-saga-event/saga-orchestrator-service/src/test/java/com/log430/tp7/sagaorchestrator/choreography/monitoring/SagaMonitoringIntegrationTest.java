package com.log430.tp7.sagaorchestrator.choreography.monitoring;

import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaState;
import com.log430.tp7.sagaorchestrator.choreography.model.ChoreographedSagaStatus;
import com.log430.tp7.sagaorchestrator.choreography.repository.ChoreographedSagaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for choreographed saga monitoring functionality.
 * Tests the monitoring REST endpoints and dashboard data aggregation.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SagaMonitoringIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ChoreographedSagaRepository sagaRepository;
    
    @Test
    void shouldReturnDashboardData() throws Exception {
        // Given - mock repository responses
        setupMockRepositoryData();
        
        // When & Then - test dashboard endpoint
        mockMvc.perform(get("/api/saga/monitoring/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overview").exists())
            .andExpect(jsonPath("$.statusDistribution").exists())
            .andExpect(jsonPath("$.recentActivity").exists())
            .andExpect(jsonPath("$.performance").exists())
            .andExpect(jsonPath("$.compensation").exists())
            .andExpect(jsonPath("$.health").exists());
    }
    
    @Test
    void shouldReturnOverviewStatistics() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSagas").exists())
            .andExpect(jsonPath("$.activeSagas").exists())
            .andExpect(jsonPath("$.completedSagas").exists())
            .andExpect(jsonPath("$.failedSagas").exists())
            .andExpect(jsonPath("$.successRate").exists())
            .andExpect(jsonPath("$.failureRate").exists());
    }
    
    @Test
    void shouldReturnStatusDistribution() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/status-distribution"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void shouldReturnRecentActivity() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/recent-activity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void shouldReturnPerformanceMetrics() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/performance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageDurationMinutes").exists())
            .andExpect(jsonPath("$.throughputPerHour").exists())
            .andExpect(jsonPath("$.completedCount").exists())
            .andExpect(jsonPath("$.failedCount").exists());
    }
    
    @Test
    void shouldReturnHealthIndicators() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].status").exists());
    }
    
    @Test
    void shouldReturnHealthSummary() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/health/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.healthy").exists())
            .andExpect(jsonPath("$.warnings").exists())
            .andExpect(jsonPath("$.critical").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void shouldTriggerMetricsCollection() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/saga/monitoring/collect-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void shouldReturnSystemInfo() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/system-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memory_total").exists())
            .andExpect(jsonPath("$.memory_free").exists())
            .andExpect(jsonPath("$.active_threads").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void shouldReturnPrometheusMetrics() throws Exception {
        // Given
        setupMockRepositoryData();
        
        // When & Then
        mockMvc.perform(get("/api/saga/monitoring/metrics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("saga_total")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("saga_active")));
    }
    
    private void setupMockRepositoryData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(1);
        
        // Create mock saga states
        ChoreographedSagaState completedSaga = createMockSaga("saga-1", "order-processing", 
            ChoreographedSagaStatus.COMPLETED, past, now);
        ChoreographedSagaState activeSaga = createMockSaga("saga-2", "payment-processing", 
            ChoreographedSagaStatus.STARTED, past, null);
        ChoreographedSagaState failedSaga = createMockSaga("saga-3", "inventory-processing", 
            ChoreographedSagaStatus.FAILED, past, now);
        
        List<ChoreographedSagaState> allSagas = Arrays.asList(completedSaga, activeSaga, failedSaga);
        List<ChoreographedSagaState> activeSagas = Arrays.asList(activeSaga);
        List<ChoreographedSagaState> completedSagas = Arrays.asList(completedSaga);
        List<ChoreographedSagaState> failedSagas = Arrays.asList(failedSaga);
        
        // Mock repository calls
        when(sagaRepository.findAll()).thenReturn(allSagas);
        when(sagaRepository.findActiveSagas()).thenReturn(activeSagas);
        when(sagaRepository.findByStatus(ChoreographedSagaStatus.COMPLETED)).thenReturn(completedSagas);
        when(sagaRepository.findByStatus(ChoreographedSagaStatus.FAILED)).thenReturn(failedSagas);
        when(sagaRepository.findByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(allSagas);
        when(sagaRepository.findSagasRequiringCompensation()).thenReturn(Arrays.asList());
        
        // Mock count methods
        when(sagaRepository.countByStatus(ChoreographedSagaStatus.COMPLETED)).thenReturn(1L);
        when(sagaRepository.countByStatus(ChoreographedSagaStatus.FAILED)).thenReturn(1L);
        when(sagaRepository.countByStatus(ChoreographedSagaStatus.STARTED)).thenReturn(1L);
        when(sagaRepository.countByStatus(ChoreographedSagaStatus.COMPENSATED)).thenReturn(0L);
    }
    
    private ChoreographedSagaState createMockSaga(String sagaId, String sagaType, 
                                                 ChoreographedSagaStatus status, 
                                                 LocalDateTime createdAt, 
                                                 LocalDateTime completedAt) {
        ChoreographedSagaState saga = new ChoreographedSagaState();
        saga.setSagaId(sagaId);
        saga.setSagaType(sagaType);
        saga.setStatus(status);
        saga.setCreatedAt(createdAt);
        saga.setCompletedAt(completedAt);
        saga.setCorrelationId("corr-" + sagaId);
        return saga;
    }
}
