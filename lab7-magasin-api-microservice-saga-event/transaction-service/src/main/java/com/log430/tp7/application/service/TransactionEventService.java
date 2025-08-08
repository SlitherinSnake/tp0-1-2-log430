package com.log430.tp7.application.service;

import com.log430.tp7.domain.event.TransactionCancelled;
import com.log430.tp7.domain.event.TransactionCompleted;
import com.log430.tp7.domain.event.TransactionCreated;
import com.log430.tp7.domain.transaction.Transaction;
import com.log430.tp7.domain.transaction.TransactionItem;
import com.log430.tp7.event.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for publishing transaction-related domain events.
 * Handles event creation and publishing for transaction lifecycle events.
 */
@Service
public class TransactionEventService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionEventService.class);
    
    private final EventProducer eventProducer;
    
    public TransactionEventService(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }
    
    /**
     * Publishes a TransactionCreated event when a new transaction is created.
     */
    public void publishTransactionCreated(Transaction transaction, String correlationId) {
        try {
            log.info("Publishing TransactionCreated event for transaction {}", transaction.getId());
            
            List<TransactionCreated.TransactionItemData> itemsData = convertTransactionItems(transaction.getItems());
            
            TransactionCreated event;
            if (transaction.isReturn()) {
                event = new TransactionCreated(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    itemsData,
                    transaction.getTransactionOriginaleId(),
                    transaction.getMotifRetour(),
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    null
                );
            } else {
                event = new TransactionCreated(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    itemsData,
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    null
                );
            }
            
            eventProducer.publishEvent("transaction.created", event);
            log.info("Successfully published TransactionCreated event for transaction {}", transaction.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish TransactionCreated event for transaction {}: {}", 
                     transaction.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish TransactionCreated event", e);
        }
    }
    
    /**
     * Publishes a TransactionCompleted event when a transaction is completed.
     */
    public void publishTransactionCompleted(Transaction transaction, String correlationId, String causationId) {
        try {
            log.info("Publishing TransactionCompleted event for transaction {}", transaction.getId());
            
            List<TransactionCreated.TransactionItemData> itemsData = convertTransactionItems(transaction.getItems());
            
            TransactionCompleted event;
            if (transaction.isReturn()) {
                event = new TransactionCompleted(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    LocalDate.now(), // completion date
                    itemsData,
                    transaction.getTransactionOriginaleId(),
                    transaction.getMotifRetour(),
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    causationId
                );
            } else {
                event = new TransactionCompleted(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    LocalDate.now(), // completion date
                    itemsData,
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    causationId
                );
            }
            
            eventProducer.publishEvent("transaction.completed", event);
            log.info("Successfully published TransactionCompleted event for transaction {}", transaction.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish TransactionCompleted event for transaction {}: {}", 
                     transaction.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish TransactionCompleted event", e);
        }
    }
    
    /**
     * Publishes a TransactionCancelled event when a transaction is cancelled.
     */
    public void publishTransactionCancelled(Transaction transaction, String cancellationReason, 
                                           String correlationId, String causationId) {
        try {
            log.info("Publishing TransactionCancelled event for transaction {}", transaction.getId());
            
            TransactionCancelled event;
            if (transaction.isReturn()) {
                event = new TransactionCancelled(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    cancellationReason,
                    transaction.getTransactionOriginaleId(),
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    causationId
                );
            } else {
                event = new TransactionCancelled(
                    transaction.getId(),
                    transaction.getTypeTransaction().name(),
                    transaction.getPersonnelId(),
                    transaction.getStoreId(),
                    transaction.getMontantTotal(),
                    transaction.getDateTransaction(),
                    cancellationReason,
                    correlationId != null ? correlationId : UUID.randomUUID().toString(),
                    causationId
                );
            }
            
            eventProducer.publishEvent("transaction.cancelled", event);
            log.info("Successfully published TransactionCancelled event for transaction {}", transaction.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish TransactionCancelled event for transaction {}: {}", 
                     transaction.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish TransactionCancelled event", e);
        }
    }
    
    /**
     * Converts transaction items to event data format.
     */
    private List<TransactionCreated.TransactionItemData> convertTransactionItems(List<TransactionItem> items) {
        if (items == null) {
            return List.of();
        }
        
        return items.stream()
                .map(item -> new TransactionCreated.TransactionItemData(
                    item.getInventoryItemId(),
                    item.getQuantite(),
                    item.getPrixUnitaire(),
                    item.getSousTotal()
                ))
                .collect(Collectors.toList());
    }
}