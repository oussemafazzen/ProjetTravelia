package services;

import models.Hebergement;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HebergementServiceTest {
    static HebergementService service;
    static int idHebergementTest;

    @BeforeAll
    static void setup() {
        service = new HebergementService();
    }

    @Test
    @Order(1)
    void testAjouterHebergement() {
        Hebergement h = new Hebergement(
                "Hotel Test",
                "hotel",
                "123 Rue Test",
                "Tunis",
                "Tunisie",
                50,
                "WiFi, Piscine, Restaurant",
                150.0
        );
        service.add(h);
        
        List<Hebergement> hebergements = service.getAll();
        assertFalse(hebergements.isEmpty());
        assertTrue(
                hebergements.stream().anyMatch(heb -> heb.getNom().equals("Hotel Test"))
        );
        
        // Récupérer l'ID de l'hébergement ajouté
        idHebergementTest = hebergements.stream()
                .filter(heb -> heb.getNom().equals("Hotel Test"))
                .findFirst()
                .get()
                .getIdHebergement();
        System.out.println("ID Hebergement Test: " + idHebergementTest);
    }

    @Test
    @Order(2)
    void testModifierHebergement() {
        Hebergement h = new Hebergement();
        h.setIdHebergement(idHebergementTest);
        h.setNom("Hotel Modifie");
        h.setType("auberge");
        h.setAdresse("456 Avenue Modifiee");
        h.setVille("Sousse");
        h.setPays("Tunisie");
        h.setCapacite(30);
        h.setEquipements("WiFi, Parking");
        h.setTarifParNuit(120.0);
        
        service.update(h);
        
        List<Hebergement> hebergements = service.getAll();
        boolean trouve = hebergements.stream()
                .anyMatch(heb -> heb.getNom().equals("Hotel Modifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testGetById() {
        Hebergement h = service.getById(idHebergementTest);
        assertNotNull(h);
        assertEquals("Hotel Modifie", h.getNom());
        assertEquals("auberge", h.getType());
    }

    @Test
    @Order(4)
    void testSupprimerHebergement() {
        Hebergement h = new Hebergement();
        h.setIdHebergement(idHebergementTest);
        
        service.delete(h);
        
        List<Hebergement> hebergements = service.getAll();
        boolean existe = hebergements.stream()
                .anyMatch(heb -> heb.getIdHebergement() == idHebergementTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() {
        // Nettoyage après chaque test si nécessaire
        System.out.println("Test terminé");
    }
}
