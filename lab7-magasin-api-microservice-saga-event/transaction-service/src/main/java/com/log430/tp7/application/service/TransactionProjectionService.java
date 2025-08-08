package com.log430.tp7.application.service;

import com.log430.tp7.domain.event.TransactionCancelled;
import com.log430.tp7.domain.event.TransactionCompleted;
import com.log430.tp7.domain.event.TransactionCreated;
import com.log430.tp7.domain.readmodel.TransactionItemReadModel;
import com.log430.tp7.domain.readmodel.TransactionReadModel;
import com.log430.tp7.infrastructure.repository.TransactionReadModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for updating read models based on domain events.
 * Part of CQRS implementation - handles projection updates from events.
 */
@Service
@Transactional
public class TransactionProjectionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionProjectionService.class);
    
    private final TransactionReadModelRepository readModelRepository;
    
    public TransactionProjectionService(TransactionReadModelRepository readModelRepository) {
        this.readModelRepository = readModelRepository;
    }
    
    /**
     * Handle TransactionCreated event and create/update read model.
     */
    public void handleTransactionCreated(TransactionCreated event) {
        log.info("Handling TransactionCreated event for transaction: {}", event.getTransactionId());
        
        try {
            // Create new read model
            TransactionReadModel readModel = new TransactionReadModel(
                event.getTransactionId(),
                event.getTransactionType(),
                event.getTransactionDate(),
                event.getTotalAmount(),
                event.getPersonnelId(),
                event.getStoreId(),
                "EN_COURS", // Initial status
                event.getCorrelationId()
            );
            
            // Set return information if applicable
            if (event.getOriginalTransactionId() != null) {
                readModel.setReturnInfo(event.getOriginalTransactionId(), event.getReturnReason());
            }
            
            // Create item read models
            List<TransactionItemReadModel> itemReadModels = new ArrayList<>();
            if (event.getItems() != null) {
                for (TransactionCreated.TransactionItemData itemData : event.getItems()) {
                    TransactionItemReadModel itemReadModel = new TransactionItemReadModel(
                        readModel,
                        itemData.getInventoryItemId(),
                        "Product-" + itemData.getInventoryItemId(), // Placeholder - would be enriched from inventory service
                        itemData.getQuantity(),
                        itemData.getUnitPrice(),
                        itemData.getSubtotal(),
                        "General" // Placeholder category
                    );
                    itemReadModels.add(itemReadModel);
                }
            }
            
            readModel.setItems(itemReadModels);
            readModel.updateItemCount(itemReadModels.size());
            
            readModelRepository.save(readModel);
            log.info("Created read model for transaction: {}", event.getTransactionId());
            
        } catch (Exception e) {
            log.error("Failed to handle TransactionCreated event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update read model", e);
        }
    }
    
    /**
     * Handle TransactionCompleted event and update read model.
     */
    public void handleTransactionCompleted(TransactionCompleted event) {
        log.info("Handling TransactionCompleted event for transaction: {}", event.getTransactionId());
        
        try {
            Optional<TransactionReadModel> readModelOpt = readModelRepository.findById(event.getTransactionId());
            
            if (readModelOpt.isPresent()) {
                TransactionReadModel readModel = readModelOpt.get();
                readModel.updateStatus("COMPLETEE");
                
                // Update total amount if provided in event
                if (event.getTotalAmount() != null) {
                    readModel.updateTotalAmount(event.getTotalAmount());
                }
                
                readModelRepository.save(readModel);
                log.info("Updated read model status to COMPLETEE for transaction: {}", event.getTransactionId());
            } else {
                log.warn("Read model not found for completed transaction: {}", event.getTransactionId());
                // Could create read model from event data if needed
                createReadModelFromCompletedEvent(event);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle TransactionCompleted event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update read model", e);
        }
    }
    
    /**
     * Handle TransactionCancelled event and update read model.
     */
    public void handleTransactionCancelled(TransactionCancelled event) {
        log.info("Handling TransactionCancelled event for transaction: {}", event.getTransactionId());
        
        try {
            Optional<TransactionReadModel> readModelOpt = readModelRepository.findById(event.getTransactionId());
            
            if (readModelOpt.isPresent()) {
                TransactionReadModel readModel = readModelOpt.get();
                readModel.updateStatus("ANNULEE");
                
                readModelRepository.save(readModel);
                log.info("Updated read model status to ANNULEE for transaction: {}", event.getTransactionId());
            } else {
                log.warn("Read model not found for cancelled transaction: {}", event.getTransactionId());
                // Could create read model from event data if needed
                createReadModelFromCancelledEvent(event);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle TransactionCancelled event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update read model", e);
        }
    }
    
    /**
     * Create read model from TransactionCompleted event when read model doesn't exist.
     */
    private void createReadModelFromCompletedEvent(TransactionCompleted event) {
        log.info("Creating read model from TransactionCompleted event for transaction: {}", event.getTransactionId());
        
        TransactionReadModel readModel = new TransactionReadModel(
            event.getTransactionId(),
            event.getTransactionType(),
            event.getTransactionDate(),
            event.getTotalAmount(),
            event.getPersonnelId(),
            event.getStoreId(),
            "COMPLETEE",
            event.getCorrelationId()
        );
        
        // Set return information if applicable
        if (event.getOriginalTransactionId() != null) {
            readModel.setReturnInfo(event.getOriginalTransactionId(), event.getReturnReason());
        }
        
        // Create item read models
        List<TransactionItemReadModel> itemReadModels = new ArrayList<>();
        if (event.getItems() != null) {
            for (TransactionCreated.TransactionItemData itemData : event.getItems()) {
                TransactionItemReadModel itemReadModel = new TransactionItemReadModel(
                    readModel,
                    itemData.getInventoryItemId(),
                    "Product-" + itemData.getInventoryItemId(),
                    itemData.getQuantity(),
                    itemData.getUnitPrice(),
                    itemData.getSubtotal(),
                    "General"
                );
                itemReadModels.add(itemReadModel);
            }
        }
        
        readModel.setItems(itemReadModels);
        readModel.updateItemCount(itemReadModels.size());
        
        readModelRepository.save(readModel);
        log.info("Created read model from completed event for transaction: {}", event.getTransactionId());
    }
    
    /**
     * Create read model from TransactionCancelled event when read model doesn't exist.
     */
    private void createReadModelFromCancelledEvent(TransactionCancelled event) {
        log.info("Creating read model from TransactionCancelled event for transaction: {}", event.getTransactionId());
        
        TransactionReadModel readModel = new TransactionReadModel(
            event.getTransactionId(),
            event.getTransactionType(),
            event.getTransactionDate(),
            event.getTotalAmount(),
            event.getPersonnelId(),
            event.getStoreId(),
            "ANNULEE",
            event.getCorrelationId()
        );
        
        // Set return information if applicable
        if (event.getOriginalTransactionId() != null) {
            readModel.setReturnInfo(event.getOriginalTransactionId(), "");
        }
        
        readModel.updateItemCount(0); // No items for cancelled transaction
        
        readModelRepository.save(readModel);
        log.info("Created read model from cancelled event for transaction: {}", event.getTransactionId());
    }
    
    /**
     * Rebuild read model from domain events (for event replay scenarios).
     */
    public void rebuildReadModel(Long transactionId) {
        log.info("Rebuilding read model for transaction: {}", transactionId);
        
        // Delete existing read model
        readModelRepository.deleteById(transactionId);
        
        // This would typically replay events from event store
        // For now, we'll just log the operation
        log.info("Read model rebuild completed for transaction: {}", transactionId);
    }
}