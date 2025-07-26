package com.log430.tp6.presentation.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp6.application.service.TransactionService;
import com.log430.tp6.domain.transaction.Transaction;
import com.log430.tp6.infrastructure.repository.TransactionRepository;
import com.log430.tp6.presentation.api.dto.TransactionDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API controller for transaction management.
 * Provides endpoints for transaction operations.
 */
@Tag(name = "Transactions", description = "Gestion des transactions (ventes)")
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get all transactions.
     */
    @Operation(summary = "Lister toutes les transactions", description = "Retourne toutes les transactions.")
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        log.info("API call: getAllTransactions");
        try {
            List<Transaction> transactions = transactionRepository.findAll();
            List<TransactionDto> transactionDtos = transactions.stream()
                    .map(TransactionDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(transactionDtos);
        } catch (Exception e) {
            log.error("Error in getAllTransactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get transaction by ID.
     */
    @Operation(summary = "Obtenir une transaction par ID", description = "Retourne une transaction par son identifiant.")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        log.info("API call: getTransactionById with id {}", id);
        try {
            return transactionService.getTransactionById(id)
                    .map(transaction -> ResponseEntity.ok(TransactionDto.fromEntity(transaction)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error in getTransactionById: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new transaction (sale).
     */
    @Operation(summary = "Créer une vente", description = "Crée une nouvelle transaction de vente.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSale(@RequestBody CreateSaleRequest request) {
        log.info("API call: createSale for personnel {} store {}", request.personnelId(), request.storeId());
        try {
            // Validate that items list is not empty
            if (request.items() == null || request.items().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Items list cannot be empty"));
            }

            Transaction transaction = new Transaction();
            transaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
            transaction.setDateTransaction(LocalDate.now());
            transaction.setPersonnelId(request.personnelId());
            transaction.setStoreId(request.storeId());
            transaction.setMontantTotal(request.montantTotal());
            transaction.setStatut(Transaction.StatutTransaction.COMPLETEE);

            for (SaleItem item : request.items()) {
                transaction.addItem(item.id(), item.quantity(), item.price());
            }

            Transaction savedTransaction = transactionRepository.save(transaction);
            Long transactionId = savedTransaction.getId();
            if (transactionId == null) {
                return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to save transaction"));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "transactionId", transactionId));
        } catch (Exception e) {
            log.error("Error in createSale: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get transactions by personnel ID.
     */
    @Operation(summary = "Obtenir les transactions par personnel", description = "Retourne les transactions d'un personnel.")
    @GetMapping("/personnel/{personnelId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByPersonnel(@PathVariable Long personnelId) {
        log.info("API call: getTransactionsByPersonnel with personnelId {}", personnelId);
        try {
            List<Transaction> transactions = transactionService.getTransactionsByPersonnel(personnelId);
            List<TransactionDto> transactionDtos = transactions.stream()
                    .map(TransactionDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(transactionDtos);
        } catch (Exception e) {
            log.error("Error in getTransactionsByPersonnel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get transactions by store ID.
     */
    @Operation(summary = "Obtenir les transactions par magasin", description = "Retourne les transactions d'un magasin.")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<TransactionDto>> getTransactionsByStore(@PathVariable Long storeId) {
        log.info("API call: getTransactionsByStore with storeId {}", storeId);
        try {
            List<Transaction> transactions = transactionService.getTransactionsByStore(storeId);
            List<TransactionDto> transactionDtos = transactions.stream()
                    .map(TransactionDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(transactionDtos);
        } catch (Exception e) {
            log.error("Error in getTransactionsByStore: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get transaction statistics.
     */
    @Operation(summary = "Obtenir les statistiques des transactions", description = "Retourne les statistiques des transactions.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTransactionStats() {
        log.info("API call: getTransactionStats");
        try {
            long totalTransactions = transactionService.getTransactionCount();
            double totalSales = transactionService.getTotalSalesAmount();
            
            Map<String, Object> stats = Map.of(
                "totalTransactions", totalTransactions,
                "totalSales", totalSales
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error in getTransactionStats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new return transaction.
     */
    @Operation(summary = "Créer un retour", description = "Crée une nouvelle transaction de retour.")
    @PostMapping("/returns")
    public ResponseEntity<Map<String, Object>> createReturn(@RequestBody CreateReturnRequest request) {
        log.info("API call: createReturn for personnel {} store {} original transaction {}", 
                request.personnelId(), request.storeId(), request.originalTransactionId());
        try {
            // Validate that the original transaction exists
            Transaction originalTransaction = transactionService.getTransactionById(request.originalTransactionId())
                    .orElseThrow(() -> new RuntimeException("Original transaction not found"));
            
            // Check if the original transaction is a sale
            if (!originalTransaction.isSale()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Can only return from sales transactions"));
            }
            
            // Create the return transaction
            Transaction returnTransaction = transactionService.createReturnTransaction(
                    request.personnelId(), 
                    request.storeId(), 
                    request.originalTransactionId(), 
                    request.motifRetour()
            );
            
            // If items are provided, add them to the return
            if (request.items() != null && !request.items().isEmpty()) {
                for (ReturnItem item : request.items()) {
                    returnTransaction.addItem(item.id(), item.quantity(), item.price());
                }
            }
            
            // Complete the return transaction
            returnTransaction.complete();
            Transaction savedTransaction = transactionRepository.save(returnTransaction);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true, 
                "transactionId", savedTransaction.getId(),
                "message", "Return transaction created successfully"
            ));
        } catch (Exception e) {
            log.error("Error in createReturn: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get completed sales transactions that can be returned.
     */
    @Operation(summary = "Obtenir les transactions retournables", description = "Retourne les transactions de vente complétées qui peuvent être retournées.")
    @GetMapping("/returnable")
    public ResponseEntity<List<TransactionDto>> getReturnableTransactions() {
        log.info("API call: getReturnableTransactions");
        try {
            List<Transaction> transactions = transactionRepository.findAll().stream()
                    .filter(t -> t.isSale() && t.isCompleted())
                    .toList();
            List<TransactionDto> transactionDtos = transactions.stream()
                    .map(TransactionDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(transactionDtos);
        } catch (Exception e) {
            log.error("Error in getReturnableTransactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get return transactions for a specific original transaction.
     */
    @Operation(summary = "Obtenir les retours d'une transaction", description = "Retourne les transactions de retour pour une transaction originale.")
    @GetMapping("/returns/{originalTransactionId}")
    public ResponseEntity<List<TransactionDto>> getReturnsByOriginalTransaction(@PathVariable Long originalTransactionId) {
        log.info("API call: getReturnsByOriginalTransaction with originalTransactionId {}", originalTransactionId);
        try {
            List<Transaction> returns = transactionRepository.findByTransactionOriginaleId(originalTransactionId);
            List<TransactionDto> returnDtos = returns.stream()
                    .map(TransactionDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(returnDtos);
        } catch (Exception e) {
            log.error("Error in getReturnsByOriginalTransaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Simple test endpoint to verify API is working.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("API call: test endpoint");
        Map<String, Object> response = Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "Transaction API is working"
        );
        return ResponseEntity.ok(response);
    }

    // Request DTOs
    public record CreateSaleRequest(
            Long personnelId,
            Long storeId,
            List<SaleItem> items,
            Double montantTotal
    ) {}

    public record SaleItem(
            Long id,
            Integer quantity,
            Double price
    ) {}

    public record CreateReturnRequest(
            Long personnelId,
            Long storeId,
            Long originalTransactionId,
            String motifRetour,
            List<ReturnItem> items
    ) {}

    public record ReturnItem(
            Long id,
            Integer quantity,
            Double price
    ) {}
}
