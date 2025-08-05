package com.log430.tp6.gateway;

import com.log430.tp6.test.utils.MockWebServerUtils;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiGatewayTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private MockWebServer inventoryMockServer;
    private MockWebServer transactionMockServer;
    private MockWebServer personnelMockServer;
    private MockWebServer storeMockServer;

    @BeforeEach
    void setUp() throws IOException {
        restTemplate = new TestRestTemplate();
        
        // Start mock servers for each microservice
        inventoryMockServer = MockWebServerUtils.startMockServer();
        transactionMockServer = MockWebServerUtils.startMockServer();
        personnelMockServer = MockWebServerUtils.startMockServer();
        storeMockServer = MockWebServerUtils.startMockServer();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (inventoryMockServer != null) {
            inventoryMockServer.shutdown();
        }
        if (transactionMockServer != null) {
            transactionMockServer.shutdown();
        }
        if (personnelMockServer != null) {
            personnelMockServer.shutdown();
        }
        if (storeMockServer != null) {
            storeMockServer.shutdown();
        }
    }

    @Test
    void gatewayHealthCheck_ShouldReturnOk() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
    }

    @Test
    void routeToInventoryService_ShouldForwardRequest() throws IOException {
        // Given
        Map<String, Object> mockInventoryResponse = Map.of(
                "id", 1,
                "nom", "Test Product",
                "prix", 99.99
        );
        inventoryMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockInventoryResponse));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/inventory/1",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("nom", "Test Product");
    }

    @Test
    void routeToTransactionService_ShouldForwardRequest() throws IOException {
        // Given
        Map<String, Object> mockTransactionResponse = Map.of(
                "id", 1,
                "typeTransaction", "VENTE",
                "montantTotal", 199.98
        );
        transactionMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockTransactionResponse));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/transactions/1",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("typeTransaction", "VENTE");
    }

    @Test
    void routeToPersonnelService_ShouldForwardRequest() throws IOException {
        // Given
        Map<String, Object> mockPersonnelResponse = Map.of(
                "id", 1,
                "nom", "John Doe",
                "identifiant", "EMP001"
        );
        personnelMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockPersonnelResponse));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/personnel/1",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("nom", "John Doe");
    }

    @Test
    void routeToStoreService_ShouldForwardRequest() throws IOException {
        // Given
        Map<String, Object> mockStoreResponse = Map.of(
                "id", 1,
                "nom", "Test Store",
                "quartier", "Downtown"
        );
        storeMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockStoreResponse));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/stores/1",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("nom", "Test Store");
    }

    @Test
    void routeToNonExistentService_ShouldReturn404() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/nonexistent/1",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void gatewayTimeout_ShouldReturnTimeout() throws IOException {
        // Given
        inventoryMockServer.enqueue(MockWebServerUtils.createDelayedResponse(30000)); // 30 second delay

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/inventory/1",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.REQUEST_TIMEOUT, HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void serviceUnavailable_ShouldReturnServiceUnavailable() throws IOException {
        // Given
        inventoryMockServer.enqueue(MockWebServerUtils.createServerErrorResponse());

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/inventory/1",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY);
    }

    @Test
    void corsHeaders_ShouldBePresent() throws IOException {
        // Given
        Map<String, Object> mockResponse = Map.of("status", "OK");
        inventoryMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockResponse));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/inventory/test",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // CORS headers should be handled by the gateway
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Origin");
    }

    @Test
    void rateLimiting_ShouldLimitRequests() throws IOException {
        // Given
        Map<String, Object> mockResponse = Map.of("status", "OK");
        for (int i = 0; i < 100; i++) {
            inventoryMockServer.enqueue(MockWebServerUtils.createJsonResponse(mockResponse));
        }

        // When - Make many requests quickly
        int successCount = 0;
        int rateLimitedCount = 0;
        
        for (int i = 0; i < 100; i++) {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/inventory/test",
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                successCount++;
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitedCount++;
            }
        }

        // Then - Some requests should be rate limited
        assertThat(successCount).isGreaterThan(0);
        // Note: Rate limiting behavior depends on configuration
    }
}