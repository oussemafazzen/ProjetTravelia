package org.example.services;

import org.example.models.Billet;
import org.example.models.Reservation;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceBilletTest {

    static final ServiceBillet serviceBillet = new ServiceBillet();
    static final ServiceReservation serviceReservation = new ServiceReservation();

    static int idBilletTest;
    static int idReservationForBillet;

    @Test
    @Order(1)
    void testAddBillet() {
        Reservation r = new Reservation(LocalDateTime.now(), "confirmee", "carte", 1);
        serviceReservation.add(r);

        List<Reservation> reservations = serviceReservation.getAll();
        idReservationForBillet = reservations.get(reservations.size() - 1).getIdReservation();

        Reservation resRef = new Reservation();
        resRef.setIdReservation(idReservationForBillet);

        Billet b = new Billet(
                "avion",
                "TEST-" + System.currentTimeMillis(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                111,
                "confirme",
                resRef
        );

        serviceBillet.add(b);

        List<Billet> billets = serviceBillet.getAll();
        assertFalse(billets.isEmpty());

        Billet last = billets.get(billets.size() - 1);
        idBilletTest = last.getIdBillet();

        assertEquals("avion", last.getTypeTransport());
        assertEquals("confirme", last.getStatut());
        assertNotNull(last.getReservation());
    }

    @Test
    @Order(2)
    void testUpdateBillet() {
        Reservation resRef = new Reservation();
        resRef.setIdReservation(idReservationForBillet);

        Billet b = new Billet();
        b.setIdBillet(idBilletTest);
        b.setTypeTransport("train");
        b.setNumeroBillet("UPD-" + System.currentTimeMillis());
        b.setDateDepart(LocalDateTime.now());
        b.setDateArrivee(LocalDateTime.now().plusHours(1));
        b.setPrix(222);
        b.setStatut("confirme");
        b.setReservation(resRef);

        serviceBillet.update(b);

        List<Billet> billets = serviceBillet.getAll();
        boolean ok = billets.stream().anyMatch(x ->
                x.getIdBillet() == idBilletTest && "train".equals(x.getTypeTransport())
        );

        assertTrue(ok);
    }

    @Test
    @Order(3)
    void testDeleteBillet() {
        Billet b = new Billet();
        b.setIdBillet(idBilletTest);

        serviceBillet.delete(b);

        List<Billet> billets = serviceBillet.getAll();
        boolean exists = billets.stream().anyMatch(x -> x.getIdBillet() == idBilletTest);

        assertFalse(exists);

        Reservation r = new Reservation();
        r.setIdReservation(idReservationForBillet);
        serviceReservation.delete(r);
    }
}
