package com.log430.tp3.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp3.model.Produit;
import com.log430.tp3.repository.ProduitRepository;
import com.log430.tp3.api.dto.ProduitDto;

/**
 * Tests unitaires pour ProduitApiController
 * Teste les endpoints API REST pour la gestion des produits :
 * - GET /api/v1/produits (liste)
 * - GET /api/v1/produits/{id} (détail)
 * - POST /api/v1/produits (création)
 * - PUT /api/v1/produits/{id} (mise à jour)
 * - DELETE /api/v1/produits/{id} (suppression)
 */
@WebMvcTest(value = ProduitApiController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class ProduitApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private ProduitRepository produitRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Produit produit1;
    private Produit produit2;
    private ProduitDto produitDto;

    @BeforeEach
    void setUp() {
        // Données de test
        produit1 = new Produit();
        produit1.setId(1);
        produit1.setNom("Ordinateur portable");
        produit1.setCategorie("Électronique");
        produit1.setPrix(999.99f);
        produit1.setQuantite(10);

        produit2 = new Produit();
        produit2.setId(2);
        produit2.setNom("Souris");
        produit2.setCategorie("Électronique");
        produit2.setPrix(29.99f);
        produit2.setQuantite(50);

        produitDto = new ProduitDto(null, "Nouveau produit", "Électronique", 199.99f, 25);
    }

    @Test
    void testGetAllProduits_ShouldReturnListOfProduits() throws Exception {
        // Given
        List<Produit> produits = Arrays.asList(produit1, produit2);
        when(produitRepository.findAll()).thenReturn(produits);

        // When & Then
        mockMvc.perform(get("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nom", is("Ordinateur portable")))
                .andExpect(jsonPath("$[0].categorie", is("Électronique")))
                .andExpect(jsonPath("$[0].prix", is(999.99)))
                .andExpect(jsonPath("$[0].quantite", is(10)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].nom", is("Souris")));

        verify(produitRepository, times(1)).findAll();
    }

    @Test
    void testGetAllProduits_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(produitRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(produitRepository, times(1)).findAll();
    }

    @Test
    void testGetProduitById_WhenExists_ShouldReturnProduit() throws Exception {
        // Given
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit1));

        // When & Then
        mockMvc.perform(get("/api/v1/produits/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("Ordinateur portable")))
                .andExpect(jsonPath("$.categorie", is("Électronique")))
                .andExpect(jsonPath("$.prix", is(999.99)))
                .andExpect(jsonPath("$.quantite", is(10)));

        verify(produitRepository, times(1)).findById(1);
    }

    @Test
    void testGetProduitById_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(produitRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/produits/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(produitRepository, times(1)).findById(999);
    }

    @Test
    void testCreateProduit_WithValidData_ShouldReturnCreatedProduit() throws Exception {
        // Given
        Produit savedProduit = new Produit();
        savedProduit.setId(3);
        savedProduit.setNom(produitDto.getNom());
        savedProduit.setCategorie(produitDto.getCategorie());
        savedProduit.setPrix(produitDto.getPrix());
        savedProduit.setQuantite(produitDto.getQuantite());

        when(produitRepository.save(any(Produit.class))).thenReturn(savedProduit);

        // When & Then
        mockMvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(produitDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nom", is("Nouveau produit")))
                .andExpect(jsonPath("$.categorie", is("Électronique")))
                .andExpect(jsonPath("$.prix", is(199.99)))
                .andExpect(jsonPath("$.quantite", is(25)));

        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testCreateProduit_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - DTO avec des données invalides
        ProduitDto invalidDto = new ProduitDto(null, "", "Électronique", -10.0f, -5);

        // When & Then
        mockMvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateProduit_WithValidData_ShouldReturnUpdatedProduit() throws Exception {
        // Given
        ProduitDto updateDto = new ProduitDto(1, "Ordinateur portable mis à jour", "Électronique", 1199.99f, 8);
        
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit1));
        
        Produit updatedProduit = new Produit();
        updatedProduit.setId(1);
        updatedProduit.setNom(updateDto.getNom());
        updatedProduit.setCategorie(updateDto.getCategorie());
        updatedProduit.setPrix(updateDto.getPrix());
        updatedProduit.setQuantite(updateDto.getQuantite());
        
        when(produitRepository.save(any(Produit.class))).thenReturn(updatedProduit);

        // When & Then
        mockMvc.perform(put("/api/v1/produits/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("Ordinateur portable mis à jour")))
                .andExpect(jsonPath("$.prix", is(1199.99)))
                .andExpect(jsonPath("$.quantite", is(8)));

        verify(produitRepository, times(1)).findById(1);
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testUpdateProduit_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        ProduitDto updateDto = new ProduitDto(999, "Produit inexistant", "Électronique", 99.99f, 5);
        when(produitRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/v1/produits/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(produitRepository, times(1)).findById(999);
    }

    @Test
    void testDeleteProduit_WhenExists_ShouldReturn200() throws Exception {
        // Given
        when(produitRepository.existsById(1)).thenReturn(true);
        doNothing().when(produitRepository).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/api/v1/produits/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Produit supprimé avec succès")));

        verify(produitRepository, times(1)).existsById(1);
        verify(produitRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteProduit_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(produitRepository.existsById(999)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/produits/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(produitRepository, times(1)).existsById(999);
    }
}
