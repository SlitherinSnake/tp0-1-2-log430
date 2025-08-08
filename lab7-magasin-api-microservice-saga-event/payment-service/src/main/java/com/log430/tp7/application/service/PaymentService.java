package com.log430.tp7.application.service;

import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import com.log430.tp7.domain.payment.events.PaymentFailed;
import com.log430.tp7.domain.payment.events.PaymentProcessed;
import com.log430.tp7.domain.payment.events.PaymentRefunded;
import com.log430.tp7.event.EventProducer;
import com.log430.tp7.infrastructure.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final EventProducer eventProducer;
    
    @Autowired
    public PaymentService(PaymentRepository paymentRepository, 
                         PaymentGatewayService paymentGatewayService,
                         EventProducer eventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.eventProducer = eventProducer;
    }
    
    public Payment createPayment(String transactionId, String customerId, 
                               BigDecimal amount, String paymentMethod) {
        return createPayment(transactionId, customerId, amount, paymentMethod, null);
    }
    
    public Payment createPayment(String transactionId, String customerId, 
                               BigDecimal amount, String paymentMethod, String correlationId) {
        logger.info("Creating payment for transaction: {}, customer: {}, amount: {}", 
                   transactionId, customerId, amount);
        
        // Check if payment already exists for this transaction
        if (paymentRepository.existsByTransactionId(transactionId)) {
            throw new IllegalArgumentException("Payment already exists for transaction: " + transactionId);
        }
        
        Payment payment = new Payment(transactionId, customerId, amount, paymentMethod, correlationId);
        Payment savedPayment = paymentRepository.save(payment);
        
        logger.info("Payment created with ID: {}, correlationId: {}", 
                   savedPayment.getPaymentId(), savedPayment.getCorrelationId());
        return savedPayment;
    }
    
    public Payment processPayment(String paymentId) {
        logger.info("Processing payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (!payment.isPending()) {
            throw new IllegalStateException("Payment is not in pending status: " + payment.getStatus());
        }
        
        try {
            // Simulate payment processing through external gateway
            String paymentReference = paymentGatewayService.processPayment(
                payment.getAmount(), payment.getPaymentMethod(), payment.getCustomerId());
            
            payment.processPayment(paymentReference);
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish PaymentProcessed event
            PaymentProcessed event = new PaymentProcessed(
                savedPayment.getPaymentId(),
                savedPayment.getTransactionId(),
                savedPayment.getCustomerId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentMethod(),
                savedPayment.getPaymentReference(),
                savedPayment.getProcessedAt(),
                savedPayment.getCorrelationId()
            );
            
            eventProducer.publishEvent(event);
            
            logger.info("Payment processed successfully: {}, reference: {}, event published", 
                       paymentId, paymentReference);
            return savedPayment;
            
        } catch (Exception e) {
            logger.error("Payment processing failed for payment: {}, error: {}", 
                        paymentId, e.getMessage());
            
            payment.failPayment(e.getMessage());
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish PaymentFailed event
            PaymentFailed event = new PaymentFailed(
                savedPayment.getPaymentId(),
                savedPayment.getTransactionId(),
                savedPayment.getCustomerId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentMethod(),
                savedPayment.getFailureReason(),
                savedPayment.getProcessedAt(),
                savedPayment.getCorrelationId()
            );
            
            eventProducer.publishEvent(event);
            
            logger.info("PaymentFailed event published for payment: {}", paymentId);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }
    
    public Payment refundPayment(String paymentId) {
        return refundPayment(paymentId, "Manual refund");
    }
    
    public Payment refundPayment(String paymentId, String refundReason) {
        logger.info("Refunding payment: {}, reason: {}", paymentId, refundReason);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (!payment.isProcessed()) {
            throw new IllegalStateException("Only processed payments can be refunded");
        }
        
        try {
            // Process refund through external gateway
            paymentGatewayService.refundPayment(payment.getPaymentReference(), payment.getAmount());
            
            payment.refundPayment();
            Payment savedPayment = paymentRepository.save(payment);
            
            // Publish PaymentRefunded event
            PaymentRefunded event = new PaymentRefunded(
                savedPayment.getPaymentId(),
                savedPayment.getTransactionId(),
                savedPayment.getCustomerId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentMethod(),
                savedPayment.getPaymentReference(),
                refundReason,
                savedPayment.getRefundedAt(),
                savedPayment.getCorrelationId()
            );
            
            eventProducer.publishEvent(event);
            
            logger.info("Payment refunded successfully: {}, event published", paymentId);
            return savedPayment;
            
        } catch (Exception e) {
            logger.error("Payment refund failed for payment: {}, error: {}", 
                        paymentId, e.getMessage());
            throw new PaymentProcessingException("Payment refund failed: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> findByCustomerId(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> findByCorrelationId(String correlationId) {
        return paymentRepository.findByCorrelationId(correlationId);
    }
}