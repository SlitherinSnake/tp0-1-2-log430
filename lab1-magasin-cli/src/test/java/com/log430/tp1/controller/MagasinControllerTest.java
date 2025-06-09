package com.log430.tp1.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.log430.tp1.model.*;
import com.log430.tp1.model.dao.*;

/**
 * Unit test for the MagasinController class.
 * * Tests “non interactifs” - on simule les entrées clavier via Scanner(String)
 * et on « mock » tous les DAO afin d’éviter la base de données.
 */
public class MagasinControllerTest {

    /* ---------------------------  DAO simulés (Mockito)  --------------------------- */
    @Mock
    private EmployeDAO employeDAO;
    @Mock
    private ProduitDAO produitDAO;
    @Mock
    private RetourDAO retourDAO;
    @Mock
    private VenteDAO venteDAO;

    /* -------------------------  Fixtures  -------------------------- */
    private MagasinController controller; // SUT (System‑Under‑Test)
    private ByteArrayOutputStream console; // Capture de System.out
    private PrintStream originalOut; // Pour remettre System.out à la fin

    /**
     * Initialisation avant chaque test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Active les @Mock
        controller = new MagasinController(employeDAO, produitDAO, retourDAO, venteDAO);

        // Redirige la sortie console vers un buffer mémoire
        originalOut = System.out;
        console = new ByteArrayOutputStream();
        System.setOut(new PrintStream(console));
    }

    /**
     * Restaure System.out après chaque test.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    /* ---------- rechercherProduit() ---------- */
    /**
     * Cas succès: l’utilisateur choisit « Rechercher par ID »
     * le DAO renvoie un produit. 
     * Le nom du produit doit apparaître dans la console.
     */
    @Test
    void rechercherProduit_idTrouve_afficheProduit() {
        // 1) Préparation des données simulées
        Produit p = new Produit();
        p.setId(123); p.setNom("Pomme");
        when(produitDAO.rechercherParId(123)).thenReturn(p);

        // 2) Exécution : « 1 -> 123  » (1 = recherche par ID)
        Scanner in = new Scanner("1\n123\n");
        controller.rechercherProduit(in);

        // 3) Vérifications
        String output = console.toString();
        assertTrue(output.contains("Pomme")); // texte attendu
        verify(produitDAO).rechercherParId(123); // DAO appelé
    }

    /**
     * Cas d’erreur : l’ID saisi n’existe pas. 
     * On doit afficher le message
     * « Aucun produit trouvé » et ne pas planter.
     */
    @Test
    void rechercherProduit_idInconnu_messageAucunProduit() {
        // 1) Préparation
        when(produitDAO.rechercherParId(999)).thenReturn(null);

        // 2) Exécution : 1 -> 999
        Scanner in = new Scanner("1\n999\n");
        controller.rechercherProduit(in);

        // 3) Vérifications
        assertTrue(console.toString().contains("Aucun produit trouvé"));
        verify(produitDAO).rechercherParId(999);
    }

    /* ---------- consulterStock() ---------- */
    /**
     * Cas d’erreur : Liste vide
     * Lorsque la base retourne une liste vide, la vue doit imprimer
     * « Aucun produit disponible… ».
     */
    @Test
    void consulterStock_aucunProduit_afficheMessage() {
        // 1) Préparation
        when(produitDAO.afficherListeProduits()).thenReturn(List.of());

        // 2) Exécution
        controller.consulterStock(new Scanner(""));       

        // 3) Vérifications
        assertTrue(console.toString().contains("Aucun produit disponible"));
        verify(produitDAO).afficherListeProduits();
    }
    
    /* ---------- enregistrerVente() (chemin « pas d’employé ») ---------- */
    /**
     * Cas d’erreur : L'employé n'existe pas
     * Si l’employé n’est pas trouvé, la vente est annulée : 
     * aucun appel à venteDAO.enregistrerVente() ne doit avoir lieu.
     */
    @Test
    void enregistrerVente_employeInconnu_annuleVente() {
        // 1) Préparation
        when(employeDAO.rechercherParId(42)).thenReturn(null);

        // 2) Exécution : saisie 42 -> résultat: (ID inexistant)
        Scanner in = new Scanner("42\n");                 
        controller.enregistrerVente(in);

        // 3) Vérifications
        assertTrue(console.toString().contains("Aucun employé trouvé"));
        verify(employeDAO).rechercherParId(42);
        verifyNoInteractions(venteDAO); // aucune vente enregistrée                
    }

    /* ---------- gererRetour() ---------- */
    /**
     * Cas succès: l’utilisateur retourne un article d’une vente existante.
     * On prépare une vente (id = 77) avec 1 produit (id = 1, qté = 2)
     * l’utilisateur tape “77” (id vente) puis  “1“ (id produit) puis “1” (quantité à retourner) puis “n”
     * On vérifie : stock incrémenté, DAO appelés, texte “Total remboursé” présent
     */
    @Test
    void gererRetour_unSeulArticle_ok() {
        // --- 1. Prépare une vente déjà existante avec un article --------------------------
        Produit p = new Produit();
        p.setId(1); p.setNom("Jus d'orange"); p.setPrix(3.0f); p.setQuantite(10);

        VenteProduit vp = new VenteProduit();
        vp.setProduit(p);
        vp.setQuantite(2);                    

        Vente vente = new Vente();
        vente.setId(77);
        vente.setEmploye(new Employe());      
        vente.setVenteProduits(List.of(vp));

        when(venteDAO.rechercherParId(77)).thenReturn(vente);

        // --- 2. Exécution : L’utilisateur tape : 77 -> 1 -> 1 -> n
        Scanner in = new Scanner("77\n1\n1\nn\n");
        controller.gererRetour(in);

        // --- 3. Vérifications --------------------------------------------
        // le stock doit avoir été incrémenté
        verify(produitDAO).mettreAJourStock(eq(p), eq(11));

        // un objet Retour doit avoir été persisté
        verify(retourDAO, times(1)).enregistrerRetour(any(Retour.class));

        // un check texte :
        assertTrue(console.toString().contains("Total remboursé"));
    }
}
