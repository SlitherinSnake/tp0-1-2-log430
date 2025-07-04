package com.log430.tp4.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class MagasinTest {

    @Test
    void testDefaultConstructor() {
        Magasin magasin = new Magasin();
        
        assertEquals(0, magasin.getId()); // int primitives default to 0
        assertNull(magasin.getNom());
        assertNull(magasin.getQuartier());
        assertNull(magasin.getVentes()); // OneToMany relationship not initialized by default
    }

    @Test
    void testSettersAndGetters() {
        Magasin magasin = new Magasin();
        int id = 1;
        String nom = "Magasin Centre-Ville";
        String quartier = "Centre-Ville";
        List<Vente> ventes = new ArrayList<>();

        magasin.setId(id);
        magasin.setNom(nom);
        magasin.setQuartier(quartier);
        magasin.setVentes(ventes);

        assertEquals(id, magasin.getId());
        assertEquals(nom, magasin.getNom());
        assertEquals(quartier, magasin.getQuartier());
        assertEquals(ventes, magasin.getVentes());
        assertTrue(magasin.getVentes().isEmpty());
    }

    @Test
    void testSetVentesWithData() {
        Magasin magasin = new Magasin();
        List<Vente> ventes = new ArrayList<>();
        
        // Create some test ventes
        Vente vente1 = new Vente();
        Vente vente2 = new Vente();
        ventes.add(vente1);
        ventes.add(vente2);

        magasin.setVentes(ventes);

        assertEquals(2, magasin.getVentes().size());
        assertTrue(magasin.getVentes().contains(vente1));
        assertTrue(magasin.getVentes().contains(vente2));
    }

    @Test
    void testSetNullValues() {
        Magasin magasin = new Magasin();
        
        magasin.setNom(null);
        magasin.setQuartier(null);
        magasin.setVentes(null);

        assertNull(magasin.getNom());
        assertNull(magasin.getQuartier());
        assertNull(magasin.getVentes());
    }

    @Test
    void testSetEmptyValues() {
        Magasin magasin = new Magasin();
        
        magasin.setNom("");
        magasin.setQuartier("");
        magasin.setVentes(new ArrayList<>());

        assertEquals("", magasin.getNom());
        assertEquals("", magasin.getQuartier());
        assertNotNull(magasin.getVentes());
        assertTrue(magasin.getVentes().isEmpty());
    }

    @Test
    void testCompleteSetup() {
        Magasin magasin = new Magasin();
        
        magasin.setId(5);
        magasin.setNom("Magasin Test");
        magasin.setQuartier("Quartier Test");
        
        List<Vente> ventes = new ArrayList<>();
        Vente vente = new Vente();
        vente.setMagasin(magasin); // Set bidirectional relationship
        ventes.add(vente);
        magasin.setVentes(ventes);

        assertEquals(5, magasin.getId());
        assertEquals("Magasin Test", magasin.getNom());
        assertEquals("Quartier Test", magasin.getQuartier());
        assertEquals(1, magasin.getVentes().size());
        assertEquals(magasin, magasin.getVentes().get(0).getMagasin());
    }
}
