package com.log430.tp6.sagaorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SagaConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConfigurationProperties(prefix = "saga.timeout")
    public SagaTimeoutProperties sagaTimeoutProperties() {
        return new SagaTimeoutProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "saga.retry")
    public SagaRetryProperties sagaRetryProperties() {
        return new SagaRetryProperties();
    }

    public static class SagaTimeoutProperties {
        private long defaultTimeout = 300000; // 5 minutes
        private long stockVerification = 30000; // 30 seconds
        private long stockReservation = 30000; // 30 seconds
        private long paymentProcessing = 60000; // 1 minute
        private long orderConfirmation = 30000; // 30 seconds

        // Getters and setters
        public long getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(long defaultTimeout) { this.defaultTimeout = defaultTimeout; }
        
        public long getStockVerification() { return stockVerification; }
        public void setStockVerification(long stockVerification) { this.stockVerification = stockVerification; }
        
        public long getStockReservation() { return stockReservation; }
        public void setStockReservation(long stockReservation) { this.stockReservation = stockReservation; }
        
        public long getPaymentProcessing() { return paymentProcessing; }
        public void setPaymentProcessing(long paymentProcessing) { this.paymentProcessing = paymentProcessing; }
        
        public long getOrderConfirmation() { return orderConfirmation; }
        public void setOrderConfirmation(long orderConfirmation) { this.orderConfirmation = orderConfirmation; }
    }

    public static class SagaRetryProperties {
        private int maxAttempts = 3;
        private long backoffDelay = 1000; // 1 second

        // Getters and setters
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        
        public long getBackoffDelay() { return backoffDelay; }
        public void setBackoffDelay(long backoffDelay) { this.backoffDelay = backoffDelay; }
    }
}