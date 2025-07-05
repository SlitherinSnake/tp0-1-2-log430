package com.log430.tp4.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.log430.tp4.application.service.InventoryService;

/**
 * Web controller for online store interface.
 * Handles both employee and client views.
 */
@Controller
@RequestMapping("/")
public class WebController {

    private final InventoryService inventoryService;

    public WebController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
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
        model.addAttribute("products", inventoryService.getAllActiveItems());
        model.addAttribute("categories", inventoryService.getDistinctCategories());
        return "products";
    }

    /**
     * Shopping cart page.
     */
    @GetMapping("/cart")
    public String cart(Model model) {
        return "cart";
    }

    /**
     * Order history for clients.
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        return "orders";
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
     * Employee dashboard.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalItems", inventoryService.getAllActiveItems().size());
        model.addAttribute("totalValue", inventoryService.calculateTotalInventoryValue());
        model.addAttribute("itemsNeedingRestock", inventoryService.getItemsNeedingRestock().size());
        return "admin/dashboard";
    }

    /**
     * Employee product management.
     */
    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute("inventoryItems", inventoryService.getAllActiveItems());
        model.addAttribute("categories", inventoryService.getDistinctCategories());
        return "admin/products";
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
     * Employee orders management.
     */
    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        // In a real app, you would fetch orders from a service
        return "admin/orders";
    }

    /**
     * Employee customers management.
     */
    @GetMapping("/admin/customers")
    public String adminCustomers(Model model) {
        // In a real app, you would fetch customers from a service
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
     * Legacy panier route - redirects to cart.
     */
    @GetMapping("/panier")
    public String panier() {
        return "redirect:/cart";
    }
}
