package com.log430.tp7.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.log430.tp7.dto.ProductDTO;
import com.log430.tp7.dto.TransactionDTO;
import com.log430.tp7.service.InventoryService;
import com.log430.tp7.service.TransactionService;

/**
 * Web controller for online store interface.
 * Handles both employee and client views using microservices.
 */
@Controller
@RequestMapping("/")
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    
    private final InventoryService inventoryService;
    private final TransactionService transactionService;

    private static final String MONEY_FORMAT = "%.2f $";
    private static final String PRODUCTS_ATTR = "products";
    private static final String CATEGORIES_ATTR = "categories";

    public WebController(InventoryService inventoryService, TransactionService transactionService) {
        this.inventoryService = inventoryService;
        this.transactionService = transactionService;
    }

    /**
     * Landing page - redirects to login.
     */
    @GetMapping("/")
    public String home() {
        logger.info("Access to home page - redirecting to login");
        return "redirect:/login";
    }

    /**
     * Login page.
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        logger.info("Access to login page, error parameter: {}", error);
        if (error != null) {
            logger.warn("Login error displayed to user");
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        }
        return "login";
    }

    /**
     * Products page - main store page after login.
     */
    @GetMapping("/products")
    public String products(Model model) {
        logger.info("Loading products page");
        try {
            List<ProductDTO> products = inventoryService.getAllActiveItems();
            model.addAttribute(PRODUCTS_ATTR, products);
            model.addAttribute(CATEGORIES_ATTR, inventoryService.getDistinctCategories());
            return PRODUCTS_ATTR;
        } catch (Exception e) {
            logger.error("Error loading products", e);
            model.addAttribute("error", "Erreur lors du chargement des produits");
            return "error";
        }
    }

    /**
     * Shopping cart page.
     */
    @GetMapping("/cart")
    public String cart(Model model) {
        logger.info("Access to shopping cart page");
        return "cart";
    }

    /**
     * Returns page for clients.
     */
    @GetMapping("/returns")
    public String returns(Model model) {
        logger.info("Access to returns page");
        
        try {
            // Get returnable transactions (completed sales)
            List<TransactionDTO> returnableTransactions = transactionService.getReturnableTransactions();
            logger.info("Found {} returnable transactions", returnableTransactions.size());
            
            // Add data to model
            model.addAttribute("returnableTransactions", returnableTransactions);
            
            // Add some sample data for demo if no transactions exist
            if (returnableTransactions.isEmpty()) {
                logger.info("No returnable transactions found - page will show empty state");
            }
            
        } catch (Exception e) {
            logger.error("Error loading returnable transactions", e);
            model.addAttribute("error", "Unable to load returnable transactions. Please try again later.");
        }
        
        return "returns";
    }

    /**
     * Sales page (Mes Achats) - shows all client purchases.
     */
    @GetMapping("/sales")
    public String sales(Model model) {
        logger.info("Loading sales page");
        try {
            // For demo: show all sales for the first client user (or all sales if needed)
            Long clientId = 4L; // Assuming client user has id 4
            logger.info("Fetching transactions for client ID: {}", clientId);
            
            // First try to get all transactions to see if API is working
            List<TransactionDTO> allTransactions = transactionService.getAllTransactions();
            logger.info("All transactions count: {}", allTransactions != null ? allTransactions.size() : 0);
            
            List<TransactionDTO> transactions = transactionService.getTransactionsByPersonnelId(clientId);
            logger.info("Retrieved {} transactions from service for client {}", transactions != null ? transactions.size() : 0, clientId);
            
            if (transactions == null) {
                logger.warn("Transactions service returned null");
                transactions = List.of();
            }
            
            if (!transactions.isEmpty()) {
                TransactionDTO firstTransaction = transactions.get(0);
                logger.info("First transaction details: ID={}, Type={}, Status={}, Total={}", 
                    firstTransaction.getId(), 
                    firstTransaction.getType(), 
                    firstTransaction.getStatus(), 
                    firstTransaction.getTotal());
            }
            
            // For debugging, let's show all transactions first without filtering
            List<Map<String, Object>> allSalesData = (allTransactions != null ? allTransactions : List.<TransactionDTO>of()).stream()
                    .map(t -> {
                        List<Map<String, Object>> itemsData = (t.getItems() != null ? t.getItems() : List.<TransactionDTO.TransactionItemDTO>of()).stream()
                                .map(item -> Map.<String, Object>of(
                                        "name", item.getProductName() != null ? item.getProductName() : "Article ID " + item.getId(),
                                        "price", String.format(MONEY_FORMAT, item.getPrice() != null ? item.getPrice() : 0.0),
                                        "quantity", item.getQuantity() != null ? item.getQuantity() : 0,
                                        "total", String.format(MONEY_FORMAT, item.getSousTotal() != null ? item.getSousTotal() : 0.0)
                                ))
                                .toList();
                        
                        return Map.of(
                                "id", t.getId() != null ? t.getId() : 0L,
                                "date", t.getDate() != null ? t.getDate() : "Date inconnue",
                                "total", String.format(MONEY_FORMAT, t.getTotal() != null ? t.getTotal() : 0.0),
                                "items", itemsData,
                                "type", t.getType() != null ? t.getType() : "N/A",
                                "status", t.getStatus() != null ? t.getStatus() : "N/A",
                                "personnelId", t.getPersonnelId() != null ? t.getPersonnelId() : 0L
                        );
                    })
                    .toList();
            
            logger.info("All sales data: {} items", allSalesData.size());
            
            List<Map<String, Object>> salesData = transactions.stream()
                    .filter(t -> {
                        boolean isVente = "VENTE".equals(t.getType());
                        boolean isComplete = "COMPLETEE".equals(t.getStatus());
                        logger.debug("Transaction {}: Type={}, Status={}, isVente={}, isComplete={}", 
                            t.getId(), t.getType(), t.getStatus(), isVente, isComplete);
                        return isVente && isComplete;
                    })
                    .map(t -> {
                        // Map the backend transaction items to frontend format
                        List<Map<String, Object>> itemsData = (t.getItems() != null ? t.getItems() : List.<TransactionDTO.TransactionItemDTO>of()).stream()
                                .map(item -> Map.<String, Object>of(
                                        "name", item.getProductName() != null ? item.getProductName() : "Article ID " + item.getId(),
                                        "price", String.format(MONEY_FORMAT, item.getPrice() != null ? item.getPrice() : 0.0),
                                        "quantity", item.getQuantity() != null ? item.getQuantity() : 0,
                                        "total", String.format(MONEY_FORMAT, item.getSousTotal() != null ? item.getSousTotal() : 0.0)
                                ))
                                .toList();
                        
                        return Map.of(
                                "id", t.getId() != null ? t.getId() : 0L,
                                "date", t.getDate() != null ? t.getDate() : "Date inconnue",
                                "total", String.format(MONEY_FORMAT, t.getTotal() != null ? t.getTotal() : 0.0),
                                "items", itemsData
                        );
                    })
                    .toList();
            
            logger.info("Filtered sales data: {} items", salesData.size());
            
            // For debugging, show all transactions if filtered is empty
            if (salesData.isEmpty() && !allSalesData.isEmpty()) {
                logger.info("No filtered sales found, showing all transactions for debugging");
                model.addAttribute("sales", allSalesData);
                model.addAttribute("debugMode", true);
            } else {
                model.addAttribute("sales", salesData);
                model.addAttribute("debugMode", false);
            }
            
            return "sales";
        } catch (Exception e) {
            logger.error("Error loading sales", e);
            model.addAttribute("error", "Erreur lors du chargement des achats: " + e.getMessage());
            return "sales";
        }
    }

    /**
     * Logging test page for frontend debugging.
     */
    @GetMapping("/logging-test")
    public String loggingTest() {
        logger.info("Access to logging test page");
        return "logging-test";
    }

    /**
     * Test endpoint to debug transactions API
     */
    @GetMapping("/test-transactions")
    public String testTransactions(Model model) {
        logger.info("Testing transactions API");
        try {
            // Test all transactions
            List<TransactionDTO> allTransactions = transactionService.getAllTransactions();
            logger.info("All transactions: {}", allTransactions.size());
            model.addAttribute("allTransactions", allTransactions);

            // Test specific personnel transactions
            List<TransactionDTO> personnelTransactions = transactionService.getTransactionsByPersonnelId(4L);
            logger.info("Personnel 4 transactions: {}", personnelTransactions.size());
            model.addAttribute("personnelTransactions", personnelTransactions);

            return "test-transactions";
        } catch (Exception e) {
            logger.error("Error testing transactions", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
