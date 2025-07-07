package com.log430.tp4.presentation.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp4.application.service.InventoryService;
import com.log430.tp4.domain.transaction.Transaction;
import com.log430.tp4.infrastructure.repository.TransactionRepository;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody Map<String, Object> payload) {
        try {
            Long personnelId = Long.valueOf(payload.get("personnelId").toString());
            Long storeId = Long.valueOf(payload.get("storeId").toString());
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            Double montantTotal = Double.valueOf(payload.get("montantTotal").toString());

            Transaction transaction = new Transaction();
            transaction.setTypeTransaction(Transaction.TypeTransaction.VENTE);
            transaction.setDateTransaction(LocalDate.now());
            transaction.setPersonnelId(personnelId);
            transaction.setStoreId(storeId);
            transaction.setMontantTotal(montantTotal);
            transaction.setStatut(Transaction.StatutTransaction.COMPLETEE);

            for (Map<String, Object> item : items) {
                Long inventoryItemId = Long.valueOf(item.get("id").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                Double price = Double.valueOf(item.get("price").toString());
                transaction.addItem(inventoryItemId, quantity, price);
            }

            transactionRepository.save(transaction);
            return ResponseEntity.ok(Map.of("success", true, "transactionId", transaction.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
