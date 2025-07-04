package com.log430.tp4.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EmployeTest {

    @Test
    void testDefaultConstructor() {
        Employe employe = new Employe();
        
        assertEquals(0, employe.getId()); // int primitives default to 0
        assertNull(employe.getNom());
        assertNull(employe.getIdentifiant());
    }

    @Test
    void testParameterizedConstructor() {
        String nom = "Jean Dupont";
        String identifiant = "JD001";
        
        Employe employe = new Employe(nom, identifiant);
        
        assertEquals(0, employe.getId()); // ID not set in constructor
        assertEquals(nom, employe.getNom());
        assertEquals(identifiant, employe.getIdentifiant());
    }

    @Test
    void testSettersAndGetters() {
        Employe employe = new Employe();
        String nom = "Marie Martin";
        String identifiant = "MM002";

        employe.setNom(nom);
        employe.setIdentifiant(identifiant);

        assertEquals(nom, employe.getNom());
        assertEquals(identifiant, employe.getIdentifiant());
    }

    @Test
    void testToString() {
        Employe employe = new Employe("Paul Durand", "PD003");
        
        String result = employe.toString();
        assertTrue(result.contains("Employe{id=0"));
        assertTrue(result.contains("nom='Paul Durand'"));
        assertTrue(result.contains("identifiant='PD003'"));
    }

    @Test
    void testConstructorWithEmptyValues() {
        Employe employe = new Employe("", "");
        
        assertEquals("", employe.getNom());
        assertEquals("", employe.getIdentifiant());
    }

    @Test
    void testConstructorWithNullValues() {
        Employe employe = new Employe(null, null);
        
        assertNull(employe.getNom());
        assertNull(employe.getIdentifiant());
    }

    @Test
    void testSettersWithNullValues() {
        Employe employe = new Employe("Initial Name", "INIT001");
        
        employe.setNom(null);
        employe.setIdentifiant(null);
        
        assertNull(employe.getNom());
        assertNull(employe.getIdentifiant());
    }
}
