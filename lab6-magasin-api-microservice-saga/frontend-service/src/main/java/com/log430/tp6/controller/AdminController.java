package com.log430.tp6.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.log430.tp6.dto.ProductDTO;
import com.log430.tp6.service.InventoryService;

/**
 * Admin controller for employee/admin interface.
 * Handles admin dashboard and management pages.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final InventoryService inventoryService;

    private static final String PRODUCTS_ATTR = "products";
    private static final String CATEGORIES_ATTR = "categories";
    private static final String ERROR_ATTR = "error";

    public AdminController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Employee dashboard root - show dashboard page directly.
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        logger.info("Loading admin dashboard");
        try {
            List<ProductDTO> products = inventoryService.getAllActiveItems();
            List<ProductDTO> lowStockItems = inventoryService.getItemsNeedingRestock();
            double totalValue = inventoryService.calculateTotalInventoryValue();
            
            model.addAttribute("inventoryItems", products);
            model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
            model.addAttribute("totalItems", products.size());
            model.addAttribute("totalValue", String.format("%.2f", totalValue));
            model.addAttribute("itemsNeedingRestock", lowStockItems.size());
            model.addAttribute("ordersCount", 0); // TODO: Get from transaction service
            
            return "admin/dashboard";
        } catch (Exception e) {
            logger.error("Error loading admin dashboard", e);
            model.addAttribute(ERROR_ATTR, "Erreur lors du chargement du tableau de bord");
            return "error";
        }
    }

    /**
     * Employee sales management.
     */
    @GetMapping("/sales")
    public String adminSales(Model model) {
        return "admin/sales";
    }

    /**
     * Employee reports.
     */
    @GetMapping("/reports")
    public String adminReports(Model model) {
        return "admin/reports";
    }

    /**
     * Employee inventory management.
     */
    @GetMapping("/inventory")
    public String adminInventory(Model model) {
        logger.info("Loading admin inventory page");
        try {
            List<ProductDTO> products = inventoryService.getAllActiveItems();
            List<ProductDTO> lowStockItems = inventoryService.getItemsNeedingRestock();
            
            model.addAttribute("inventoryItems", products);
            model.addAttribute("lowStockItems", lowStockItems);
            return "admin/inventory";
        } catch (Exception e) {
            logger.error("Error loading admin inventory", e);
            model.addAttribute(ERROR_ATTR, "Erreur lors du chargement de l'inventaire");
            return "error";
        }
    }

    /**
     * Employee customers management.
     */
    @GetMapping("/customers")
    public String adminCustomers(Model model) {
        return "admin/customers";
    }

    /**
     * Employee settings management.
     */
    @GetMapping("/settings")
    public String adminSettings(Model model) {
        return "admin/settings";
    }

    /**
     * Admin product management page.
     */
    @GetMapping("/products")
    public String adminProducts(Model model) {
        logger.info("Loading admin products page");
        try {
            List<ProductDTO> products = inventoryService.getAllActiveItems();
            model.addAttribute(PRODUCTS_ATTR, products);
            model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
            return "admin/products";
        } catch (Exception e) {
            logger.error("Error loading admin products", e);
            model.addAttribute(ERROR_ATTR, "Erreur lors du chargement des produits");
            return "error";
        }
    }
}
