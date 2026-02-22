package services;

import models.Hebergement;
import models.ReservationHebergement;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationHebergementServiceTest {
    static ReservationHebergementService service;
    static HebergementService hebergementService;
    static int idReservationTest;
    static int idHebergementTest;

    @BeforeAll
    static void setup() {
        service = new ReservationHebergementService();
        hebergementService = new HebergementService();
        
        // Créer un hébergement pour les tests de réservation
        Hebergement h = new Hebergement(
                "Hotel Pour Reservation",
                "hotel",
                "789 Rue Reservation",
                "Tunis",
                "Tunisie",
                100,
                "WiFi, Spa",
                200.0
        );
        hebergementService.add(h);
        
        // Récupérer l'ID de l'hébergement créé
        List<Hebergement> hebergements = hebergementService.getAll();
        idHebergementTest = hebergements.stream()
                .filter(heb -> heb.getNom().equals("Hotel Pour Reservation"))
                .findFirst()
                .get()
                .getIdHebergement();
        System.out.println("ID Hebergement pour tests: " + idHebergementTest);
    }

    @Test
    @Order(1)
    void testAjouterReservation() {
        Hebergement h = hebergementService.getById(idHebergementTest);
        assertNotNull(h, "L'hébergement doit exister");
        
        ReservationHebergement r = new ReservationHebergement(
                Date.valueOf("2026-03-01"),
                Date.valueOf("2026-03-05"),
                2,
                "confirmée",
                1,
                h
        );
        service.add(r);
        
        List<ReservationHebergement> reservations = service.getAll();
        assertFalse(reservations.isEmpty());
        assertTrue(
                reservations.stream().anyMatch(res -> 
                        res.getNombrePersonnes() == 2 && 
                        res.getStatut().equals("confirmée")
                )
        );
        
        // Récupérer l'ID de la réservation ajoutée
        idReservationTest = reservations.stream()
                .filter(res -> res.getStatut().equals("confirmée") && res.getNombrePersonnes() == 2)
                .findFirst()
                .get()
                .getIdReservationHebergement();
        System.out.println("ID Reservation Test: " + idReservationTest);
    }

    @Test
    @Order(2)
    void testModifierReservation() {
        Hebergement h = hebergementService.getById(idHebergementTest);
        
        ReservationHebergement r = new ReservationHebergement();
        r.setIdReservationHebergement(idReservationTest);
        r.setDateDebut(Date.valueOf("2026-04-01"));
        r.setDateFin(Date.valueOf("2026-04-10"));
        r.setNombrePersonnes(4);
        r.setStatut("modifiée");
        r.setIdClient(2);
        r.setHebergement(h);
        
        service.update(r);
        
        List<ReservationHebergement> reservations = service.getAll();
        boolean trouve = reservations.stream()
                .anyMatch(res -> res.getStatut().equals("modifiée") && res.getNombrePersonnes() == 4);
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerReservation() {
        ReservationHebergement r = new ReservationHebergement();
        r.setIdReservationHebergement(idReservationTest);
        
        service.delete(r);
        
        List<ReservationHebergement> reservations = service.getAll();
        boolean existe = reservations.stream()
                .anyMatch(res -> res.getIdReservationHebergement() == idReservationTest);
        assertFalse(existe);
    }

    @AfterAll
    static void tearDown() {
        // Nettoyer l'hébergement de test après tous les tests
        if (idHebergementTest > 0) {
            Hebergement h = new Hebergement();
            h.setIdHebergement(idHebergementTest);
            hebergementService.delete(h);
            System.out.println("Hébergement de test supprimé");
        }
    }

    @AfterEach
    void cleanUp() {
        System.out.println("Test terminé");
    }
}
