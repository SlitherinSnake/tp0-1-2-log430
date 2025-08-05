package com.log430.tp7.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Utility class for setting up mock web servers for testing microservice interactions.
 */
public class MockWebServerUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create a successful JSON response.
     */
    public static MockResponse createJsonResponse(Object body) throws IOException {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(body));
    }
    
    /**
     * Create a successful JSON response with custom status code.
     */
    public static MockResponse createJsonResponse(int statusCode, Object body) throws IOException {
        return new MockResponse()
                .setResponseCode(statusCode)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(body));
    }
    
    /**
     * Create an error response.
     */
    public static MockResponse createErrorResponse(int statusCode, String message) {
        return new MockResponse()
                .setResponseCode(statusCode)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"" + message + "\"}");
    }
    
    /**
     * Create a 404 Not Found response.
     */
    public static MockResponse createNotFoundResponse() {
        return createErrorResponse(404, "Not Found");
    }
    
    /**
     * Create a 500 Internal Server Error response.
     */
    public static MockResponse createServerErrorResponse() {
        return createErrorResponse(500, "Internal Server Error");
    }
    
    /**
     * Create a delayed response for testing timeouts.
     */
    public static MockResponse createDelayedResponse(int delayMs) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"status\":\"ok\"}")
                .setBodyDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    /**
     * Start a mock web server on a random port.
     */
    public static MockWebServer startMockServer() throws IOException {
        MockWebServer server = new MockWebServer();
        server.start();
        return server;
    }
    
    /**
     * Get the base URL for a mock server.
     */
    public static String getBaseUrl(MockWebServer server) {
        return server.url("/").toString();
    }
}