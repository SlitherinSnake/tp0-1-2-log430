package com.log430.tp7.test.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building test data objects.
 */
public class TestDataBuilder {
    
    // Inventory Test Data
    public static class InventoryItemBuilder {
        private Long id = 1L;
        private String nom = "Test Product";
        private String categorie = "Electronics";
        private Double prix = 99.99;
        private Integer stockCentral = 100;
        private String description = "Test product description";
        private Boolean actif = true;
        
        public InventoryItemBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public InventoryItemBuilder withNom(String nom) {
            this.nom = nom;
            return this;
        }
        
        public InventoryItemBuilder withCategorie(String categorie) {
            this.categorie = categorie;
            return this;
        }
        
        public InventoryItemBuilder withPrix(Double prix) {
            this.prix = prix;
            return this;
        }
        
        public InventoryItemBuilder withStock(Integer stock) {
            this.stockCentral = stock;
            return this;
        }
        
        public InventoryItemBuilder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public InventoryItemBuilder inactive() {
            this.actif = false;
            return this;
        }
        
        public Object build() {
            // This would return the actual InventoryItem entity
            // For now, returning a mock object structure
            return new Object() {
                public Long getId() { return id; }
                public String getNom() { return nom; }
                public String getCategorie() { return categorie; }
                public Double getPrix() { return prix; }
                public Integer getStockCentral() { return stockCentral; }
                public String getDescription() { return description; }
                public Boolean getActif() { return actif; }
            };
        }
    }
    
    // Personnel Test Data
    public static class PersonnelBuilder {
        private Long id = 1L;
        private String nom = "John Doe";
        private String identifiant = "EMP001";
        private String username = "johndoe";
        private String password = "password123";
        private Boolean actif = true;
        
        public PersonnelBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public PersonnelBuilder withNom(String nom) {
            this.nom = nom;
            return this;
        }
        
        public PersonnelBuilder withIdentifiant(String identifiant) {
            this.identifiant = identifiant;
            return this;
        }
        
        public PersonnelBuilder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public PersonnelBuilder withPassword(String password) {
            this.password = password;
            return this;
        }
        
        public PersonnelBuilder inactive() {
            this.actif = false;
            return this;
        }
        
        public Object build() {
            return new Object() {
                public Long getId() { return id; }
                public String getNom() { return nom; }
                public String getIdentifiant() { return identifiant; }
                public String getUsername() { return username; }
                public String getPassword() { return password; }
                public Boolean getActif() { return actif; }
            };
        }
    }
    
    // Store Test Data
    public static class StoreBuilder {
        private Long id = 1L;
        private String nom = "Test Store";
        private String quartier = "Downtown";
        private String adresse = "123 Test Street";
        private String telephone = "555-0123";
        private Boolean actif = true;
        
        public StoreBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public StoreBuilder withNom(String nom) {
            this.nom = nom;
            return this;
        }
        
        public StoreBuilder withQuartier(String quartier) {
            this.quartier = quartier;
            return this;
        }
        
        public StoreBuilder withAdresse(String adresse) {
            this.adresse = adresse;
            return this;
        }
        
        public StoreBuilder withTelephone(String telephone) {
            this.telephone = telephone;
            return this;
        }
        
        public StoreBuilder inactive() {
            this.actif = false;
            return this;
        }
        
        public Object build() {
            return new Object() {
                public Long getId() { return id; }
                public String getNom() { return nom; }
                public String getQuartier() { return quartier; }
                public String getAdresse() { return adresse; }
                public String getTelephone() { return telephone; }
                public Boolean getActif() { return actif; }
            };
        }
    }
    
    // Transaction Test Data
    public static class TransactionBuilder {
        private Long id = 1L;
        private String typeTransaction = "VENTE";
        private LocalDate dateTransaction = LocalDate.now();
        private Long personnelId = 1L;
        private Long storeId = 1L;
        private Double montantTotal = 199.98;
        private String statut = "COMPLETEE";
        private List<Object> items = new ArrayList<>();
        
        public TransactionBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public TransactionBuilder withType(String type) {
            this.typeTransaction = type;
            return this;
        }
        
        public TransactionBuilder withDate(LocalDate date) {
            this.dateTransaction = date;
            return this;
        }
        
        public TransactionBuilder withPersonnelId(Long personnelId) {
            this.personnelId = personnelId;
            return this;
        }
        
        public TransactionBuilder withStoreId(Long storeId) {
            this.storeId = storeId;
            return this;
        }
        
        public TransactionBuilder withMontantTotal(Double montant) {
            this.montantTotal = montant;
            return this;
        }
        
        public TransactionBuilder withStatut(String statut) {
            this.statut = statut;
            return this;
        }
        
        public TransactionBuilder addItem(Long itemId, Integer quantity, Double price) {
            items.add(new Object() {
                public Long getId() { return itemId; }
                public Integer getQuantity() { return quantity; }
                public Double getPrice() { return price; }
            });
            return this;
        }
        
        public Object build() {
            return new Object() {
                public Long getId() { return id; }
                public String getTypeTransaction() { return typeTransaction; }
                public LocalDate getDateTransaction() { return dateTransaction; }
                public Long getPersonnelId() { return personnelId; }
                public Long getStoreId() { return storeId; }
                public Double getMontantTotal() { return montantTotal; }
                public String getStatut() { return statut; }
                public List<Object> getItems() { return items; }
            };
        }
    }
    
    // Factory methods
    public static InventoryItemBuilder anInventoryItem() {
        return new InventoryItemBuilder();
    }
    
    public static PersonnelBuilder aPersonnel() {
        return new PersonnelBuilder();
    }
    
    public static StoreBuilder aStore() {
        return new StoreBuilder();
    }
    
    public static TransactionBuilder aTransaction() {
        return new TransactionBuilder();
    }
}