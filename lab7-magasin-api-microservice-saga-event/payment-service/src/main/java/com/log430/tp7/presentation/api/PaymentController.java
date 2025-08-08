package com.log430.tp7.presentation.api;

import com.log430.tp7.application.service.PaymentService;
import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.presentation.dto.CreatePaymentRequest;
import com.log430.tp7.presentation.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Management", description = "APIs for payment processing")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new payment", description = "Creates a new payment for a transaction")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        logger.info("Creating payment for transaction: {}", request.getTransactionId());
        
        try {
            Payment payment = paymentService.createPayment(
                request.getTransactionId(),
                request.getCustomerId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getCorrelationId()
            );
            
            PaymentResponse response = PaymentResponse.fromPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process a payment", description = "Processes a pending payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        logger.info("Processing payment: {}", paymentId);
        
        try {
            Payment payment = paymentService.processPayment(paymentId);
            PaymentResponse response = PaymentResponse.fromPayment(payment);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Invalid payment state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Refunds a processed payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        logger.info("Refunding payment: {}", paymentId);
        
        try {
            Payment payment = paymentService.refundPayment(paymentId);
            PaymentResponse response = PaymentResponse.fromPayment(payment);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Invalid payment state for refund: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error refunding payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        return paymentService.findById(paymentId)
            .map(payment -> ResponseEntity.ok(PaymentResponse.fromPayment(payment)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get payment by transaction ID", description = "Retrieves a payment by transaction ID")
    public ResponseEntity<PaymentResponse> getPaymentByTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        return paymentService.findByTransactionId(transactionId)
            .map(payment -> ResponseEntity.ok(PaymentResponse.fromPayment(payment)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payments by customer", description = "Retrieves all payments for a customer")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        List<Payment> payments = paymentService.findByCustomerId(customerId);
        List<PaymentResponse> responses = payments.stream()
            .map(PaymentResponse::fromPayment)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieves all payments with a specific status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.findByStatus(status);
        List<PaymentResponse> responses = payments.stream()
            .map(PaymentResponse::fromPayment)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get payments by correlation ID", description = "Retrieves all payments with a specific correlation ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCorrelationId(
            @Parameter(description = "Correlation ID") @PathVariable String correlationId) {
        List<Payment> payments = paymentService.findByCorrelationId(correlationId);
        List<PaymentResponse> responses = payments.stream()
            .map(PaymentResponse::fromPayment)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}