package com.log430.tp7.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.domain.inventory.StockReservation;
import com.log430.tp7.domain.inventory.events.InventoryReleased;
import com.log430.tp7.domain.inventory.events.InventoryReserved;
import com.log430.tp7.domain.inventory.events.InventoryUnavailable;
import com.log430.tp7.domain.inventory.readmodel.InventoryReadModel;
import com.log430.tp7.domain.inventory.readmodel.InventoryReservationReadModel;
import com.log430.tp7.infrastructure.repository.InventoryItemRepository;
import com.log430.tp7.infrastructure.repository.InventoryReadModelRepository;
import com.log430.tp7.infrastructure.repository.InventoryReservationReadModelRepository;
import com.log430.tp7.infrastructure.repository.StockReservationRepository;

/**
 * Service responsible for updating inventory read models based on domain events.
 * Implements the projection side of CQRS pattern.
 */
@Service
@Transactional
public class InventoryProjectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryProjectionService.class);
    
    private final InventoryReadModelRepository inventoryReadModelRepository;
    private final InventoryReservationReadModelRepository reservationReadModelRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    
    public InventoryProjectionService(InventoryReadModelRepository inventoryReadModelRepository,
                                     InventoryReservationReadModelRepository reservationReadModelRepository,
                                     InventoryItemRepository inventoryItemRepository,
                                     StockReservationRepository stockReservationRepository) {
        this.inventoryReadModelRepository = inventoryReadModelRepository;
        this.reservationReadModelRepository = reservationReadModelRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockReservationRepository = stockReservationRepository;
    }
    
    /**
     * Handle InventoryReserved event by updating read models.
     */
    public void handleInventoryReserved(InventoryReserved event) {
        logger.info("Updating read models for InventoryReserved event: inventoryItemId={}, reservationId={}", 
                   event.getInventoryItemId(), event.getReservationId());
        
        try {
            // Update inventory read model
            updateInventoryReadModel(event.getInventoryItemId());
            
            // Create reservation read model
            InventoryReservationReadModel reservationReadModel = new InventoryReservationReadModel(
                event.getReservationId(),
                event.getInventoryItemId(),
                event.getTransactionId(),
                event.getQuantity(),
                event.getCorrelationId()
            );
            reservationReadModelRepository.save(reservationReadModel);
            
            logger.info("Successfully updated read models for InventoryReserved event: reservationId={}", 
                       event.getReservationId());
            
        } catch (Exception e) {
            logger.error("Failed to update read models for InventoryReserved event: reservationId={}", 
                        event.getReservationId(), e);
        }
    }
    
    /**
     * Handle InventoryReleased event by updating read models.
     */
    public void handleInventoryReleased(InventoryReleased event) {
        logger.info("Updating read models for InventoryReleased event: inventoryItemId={}, reservationId={}", 
                   event.getInventoryItemId(), event.getReservationId());
        
        try {
            // Update inventory read model
            updateInventoryReadModel(event.getInventoryItemId());
            
            // Update reservation read model
            Optional<InventoryReservationReadModel> reservationOpt = 
                reservationReadModelRepository.findById(event.getReservationId());
            
            if (reservationOpt.isPresent()) {
                InventoryReservationReadModel reservation = reservationOpt.get();
                reservation.release(event.getReason());
                reservationReadModelRepository.save(reservation);
            } else {
                logger.warn("Reservation read model not found for release: reservationId={}", event.getReservationId());
            }
            
            logger.info("Successfully updated read models for InventoryReleased event: reservationId={}", 
                       event.getReservationId());
            
        } catch (Exception e) {
            logger.error("Failed to update read models for InventoryReleased event: reservationId={}", 
                        event.getReservationId(), e);
        }
    }
    
    /**
     * Handle InventoryUnavailable event by logging (no read model updates needed).
     */
    public void handleInventoryUnavailable(InventoryUnavailable event) {
        logger.info("Processing InventoryUnavailable event: inventoryItemId={}, transactionId={}, reason={}", 
                   event.getInventoryItemId(), event.getTransactionId(), event.getReason());
        
        // For InventoryUnavailable events, we typically don't need to update read models
        // since no reservation was created. However, we might want to log this for analytics.
        
        logger.info("Processed InventoryUnavailable event: inventoryItemId={}, transactionId={}", 
                   event.getInventoryItemId(), event.getTransactionId());
    }
    
    /**
     * Rebuild inventory read model from current domain state.
     * This is useful for initial population or recovery scenarios.
     */
    public void rebuildInventoryReadModel(Long inventoryItemId) {
        logger.info("Rebuilding inventory read model for item: {}", inventoryItemId);
        
        try {
            Optional<InventoryItem> itemOpt = inventoryItemRepository.findById(inventoryItemId);
            if (itemOpt.isEmpty()) {
                logger.warn("Inventory item not found for read model rebuild: {}", inventoryItemId);
                return;
            }
            
            InventoryItem item = itemOpt.get();
            
            // Calculate current reserved quantity
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantity(
                inventoryItemId.toString(), LocalDateTime.now()
            );
            
            // Create or update read model
            InventoryReadModel readModel = inventoryReadModelRepository.findById(inventoryItemId)
                .orElse(new InventoryReadModel());
            
            readModel.setInventoryItemId(inventoryItemId);
            readModel.setNom(item.getNom());
            readModel.setCategorie(item.getCategorie());
            readModel.setPrix(item.getPrix());
            readModel.setDescription(item.getDescription());
            readModel.setStockMinimum(item.getStockMinimum());
            readModel.setDateDerniereMaj(item.getDateDerniereMaj());
            readModel.setActive(item.isActive());
            
            // Update stock information
            readModel.updateStock(item.getStockCentral(), reservedQuantity);
            
            inventoryReadModelRepository.save(readModel);
            
            logger.info("Successfully rebuilt inventory read model for item: {}", inventoryItemId);
            
        } catch (Exception e) {
            logger.error("Failed to rebuild inventory read model for item: {}", inventoryItemId, e);
        }
    }
    
    /**
     * Rebuild all inventory read models from current domain state.
     */
    public void rebuildAllInventoryReadModels() {
        logger.info("Rebuilding all inventory read models");
        
        try {
            inventoryItemRepository.findAll().forEach(item -> {
                rebuildInventoryReadModel(item.getId());
            });
            
            logger.info("Successfully rebuilt all inventory read models");
            
        } catch (Exception e) {
            logger.error("Failed to rebuild all inventory read models", e);
        }
    }
    
    /**
     * Rebuild reservation read models from current domain state.
     */
    public void rebuildReservationReadModels() {
        logger.info("Rebuilding all reservation read models");
        
        try {
            // Clear existing reservation read models
            reservationReadModelRepository.deleteAll();
            
            // Rebuild from current reservations
            stockReservationRepository.findAll().forEach(reservation -> {
                InventoryReservationReadModel readModel = new InventoryReservationReadModel(
                    reservation.getReservationId(),
                    Long.parseLong(reservation.getProductId()),
                    reservation.getSagaId(),
                    reservation.getQuantity(),
                    null // correlation ID not available in original reservation
                );
                
                readModel.setCreatedAt(reservation.getCreatedAt());
                readModel.setExpiresAt(reservation.getExpiresAt());
                readModel.setUpdatedAt(LocalDateTime.now());
                
                // Set status based on reservation status
                switch (reservation.getStatus()) {
                    case ACTIVE:
                        readModel.setStatus(InventoryReservationReadModel.ReservationStatus.ACTIVE);
                        break;
                    case CONFIRMED:
                        readModel.setStatus(InventoryReservationReadModel.ReservationStatus.CONFIRMED);
                        break;
                    case RELEASED:
                        readModel.setStatus(InventoryReservationReadModel.ReservationStatus.RELEASED);
                        break;
                }
                
                reservationReadModelRepository.save(readModel);
            });
            
            logger.info("Successfully rebuilt all reservation read models");
            
        } catch (Exception e) {
            logger.error("Failed to rebuild reservation read models", e);
        }
    }
    
    /**
     * Update inventory read model with current domain state.
     */
    private void updateInventoryReadModel(Long inventoryItemId) {
        try {
            Optional<InventoryItem> itemOpt = inventoryItemRepository.findById(inventoryItemId);
            if (itemOpt.isEmpty()) {
                logger.warn("Inventory item not found for read model update: {}", inventoryItemId);
                return;
            }
            
            InventoryItem item = itemOpt.get();
            
            // Calculate current reserved quantity
            Integer reservedQuantity = stockReservationRepository.calculateReservedQuantity(
                inventoryItemId.toString(), LocalDateTime.now()
            );
            
            // Update read model
            InventoryReadModel readModel = inventoryReadModelRepository.findById(inventoryItemId)
                .orElse(new InventoryReadModel(
                    inventoryItemId,
                    item.getNom(),
                    item.getCategorie(),
                    item.getPrix(),
                    item.getStockCentral(),
                    item.getStockMinimum(),
                    item.isActive()
                ));
            
            readModel.updateStock(item.getStockCentral(), reservedQuantity);
            inventoryReadModelRepository.save(readModel);
            
        } catch (Exception e) {
            logger.error("Failed to update inventory read model for item: {}", inventoryItemId, e);
        }
    }
}