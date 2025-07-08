package com.log430.tp4.presentation.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.log430.tp4.application.service.InventoryService;
import com.log430.tp4.domain.transaction.Transaction;
import com.log430.tp4.infrastructure.repository.TransactionRepository;

/**
 * Web controller for online store interface.
 * Handles both employee and client views.
 */
@Controller
@RequestMapping("/")
public class WebController {

    private final InventoryService inventoryService;
    private final TransactionRepository transactionRepository;

    private static final String MONEY_FORMAT = "%.2f $";
    private static final String PRODUCTS_ATTR = "products";
    private static final String CATEGORIES_ATTR = "categories";

    public WebController(InventoryService inventoryService, TransactionRepository transactionRepository) {
        this.inventoryService = inventoryService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Landing page - redirects to login.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    /**
     * Login page.
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        }
        return "login";
    }

    /**
     * Products page - main store page after login.
     */
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute(PRODUCTS_ATTR, inventoryService.getAllActiveItems());
        model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
        return PRODUCTS_ATTR;
    }

    /**
     * Shopping cart page.
     */
    @GetMapping("/cart")
    public String cart(Model model) {
        return "cart";
    }

    /**
     * Returns page for clients.
     */
    @GetMapping("/returns")
    public String returns() {
        return "returns";
    }

    // Employee-only pages
    
    /**
     * Employee dashboard root - show dashboard page directly (no redirect).
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboardRoot(Model model) {
        model.addAttribute("inventoryItems", inventoryService.getAllActiveItems());
        model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
        model.addAttribute("totalItems", inventoryService.getAllActiveItems().size());
        model.addAttribute("totalValue", inventoryService.calculateTotalInventoryValue());
        model.addAttribute("itemsNeedingRestock", inventoryService.getItemsNeedingRestock().size());
        return "admin/dashboard";
    }

    /**
     * Employee sales management.
     */
    @GetMapping("/admin/sales")
    public String adminSales(Model model) {
        return "admin/sales";
    }

    /**
     * Employee reports.
     */
    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        return "admin/reports";
    }

    /**
     * Employee inventory management.
     */
    @GetMapping("/admin/inventory")
    public String adminInventory(Model model) {
        model.addAttribute("inventoryItems", inventoryService.getAllActiveItems());
        model.addAttribute("lowStockItems", inventoryService.getItemsNeedingRestock());
        return "admin/inventory";
    }

    /**
     * Employee customers management.
     */
    @GetMapping("/admin/customers")
    public String adminCustomers(Model model) {
        return "admin/customers";
    }

    /**
     * Employee settings management.
     */
    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        return "admin/settings";
    }

    /**
     * Sales page (Mes Achats) - shows all client purchases.
     */
    @GetMapping("/sales")
    public String sales(Model model) {
        // For demo: show all sales for the first client user (or all sales if needed)
        // Remove all authentication checks
        Long clientId = 4L; // Assuming client user has id 4
        List<Transaction> transactions = transactionRepository.findByPersonnelId(clientId);
        List<Map<String, Object>> sales = transactions.stream()
                .filter(t -> t.getTypeTransaction() == Transaction.TypeTransaction.VENTE && t.getStatut() == Transaction.StatutTransaction.COMPLETEE)
                .map(t -> Map.of(
                        "id", t.getId(),
                        "date", t.getDateTransaction(),
                        "total", String.format(MONEY_FORMAT, t.getMontantTotal()),
                        "items", t.getItems().stream().map(item -> Map.of(
                                "name", getItemName(item.getInventoryItemId()),
                                "price", String.format(MONEY_FORMAT, item.getPrixUnitaire()),
                                "quantity", item.getQuantite(),
                                "total", String.format(MONEY_FORMAT, item.getSousTotal())
                        )).toList()
                ))
                .toList();
        model.addAttribute("sales", sales);
        return "sales";
    }

    // Helper to get item name by id (could be optimized with a cache if needed)
    private String getItemName(Long inventoryItemId) {
        return inventoryService.getItemById(inventoryItemId)
                .map(com.log430.tp4.domain.inventory.InventoryItem::getNom)
                .orElse("Article inconnu");
    }

    /**
     * Admin product management page.
     */
    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute(PRODUCTS_ATTR, inventoryService.getAllActiveItems());
        model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
        return "admin/products";
    }
}
