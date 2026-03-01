package services;

import models.Billet;
import models.Reservation;
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
    void testAddBillet() throws Exception {
        Reservation r = new Reservation();
        r.setDateReservation(LocalDateTime.now());
        r.setStatut("confirmee");
        r.setModalitesPaiement("carte");
        r.setClientId(1);
        serviceReservation.add(r);

        List<Reservation> reservations = serviceReservation.getAll();
        idReservationForBillet = reservations.get(0).getIdReservation();

        Billet b = new Billet();
        b.setTypeTransport("avion");
        b.setNumeroBillet("TEST-" + System.currentTimeMillis());
        b.setDateDepart(LocalDateTime.now());
        b.setDateArrivee(LocalDateTime.now().plusHours(2));
        b.setPrix(111);
        b.setStatut("confirme");
        b.setReservationId(idReservationForBillet);

        serviceBillet.add(b);

        List<Billet> billets = serviceBillet.getAll();
        assertFalse(billets.isEmpty());

        Billet last = billets.get(0);
        idBilletTest = last.getIdBillet();

        assertEquals("avion", last.getTypeTransport());
        assertEquals("confirme", last.getStatut());
        assertEquals(idReservationForBillet, last.getReservationId());
    }

    @Test
    @Order(2)
    void testUpdateBillet() throws Exception {
        Billet b = new Billet();
        b.setIdBillet(idBilletTest);
        b.setTypeTransport("train");
        b.setNumeroBillet("UPD-" + System.currentTimeMillis());
        b.setDateDepart(LocalDateTime.now());
        b.setDateArrivee(LocalDateTime.now().plusHours(1));
        b.setPrix(222);
        b.setStatut("confirme");
        b.setReservationId(idReservationForBillet);

        serviceBillet.update(b);

        List<Billet> billets = serviceBillet.getAll();
        boolean ok = billets.stream().anyMatch(x ->
                x.getIdBillet() == idBilletTest && "train".equals(x.getTypeTransport())
        );

        assertTrue(ok);
    }

    @Test
    @Order(3)
    void testDeleteBillet() throws Exception {
        Billet b = new Billet();
        b.setIdBillet(idBilletTest);

        serviceBillet.delete(b.getIdBillet());

        List<Billet> billets = serviceBillet.getAll();
        boolean exists = billets.stream().anyMatch(x -> x.getIdBillet() == idBilletTest);

        assertFalse(exists);

        Reservation r = new Reservation();
        r.setIdReservation(idReservationForBillet);
        serviceReservation.delete(r.getIdReservation());
    }
}
