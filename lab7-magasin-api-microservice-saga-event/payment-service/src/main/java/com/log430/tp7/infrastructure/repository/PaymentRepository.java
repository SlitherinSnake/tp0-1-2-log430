package com.log430.tp7.infrastructure.repository;

import com.log430.tp7.domain.payment.Payment;
import com.log430.tp7.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByCustomerId(String customerId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    List<Payment> findByCustomerIdAndStatus(@Param("customerId") String customerId, 
                                          @Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
    
    boolean existsByTransactionId(String transactionId);
    
    List<Payment> findByCorrelationId(String correlationId);
}