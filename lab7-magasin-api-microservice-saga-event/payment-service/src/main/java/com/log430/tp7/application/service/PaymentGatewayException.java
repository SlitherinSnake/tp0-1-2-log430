package com.log430.tp7.application.service;

public class PaymentGatewayException extends RuntimeException {
    
    public PaymentGatewayException(String message) {
        super(message);
    }
    
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}