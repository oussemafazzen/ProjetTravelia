package org.example.services;

import org.example.models.Reservation;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceReservationTest {

    static final ServiceReservation service = new ServiceReservation();
    static int idReservationTest;

    @Test
    @Order(1)
    void testAddReservation() {
        Reservation r = new Reservation(LocalDateTime.now(), "en_attente", "carte", 1);
        service.add(r);

        List<Reservation> list = service.getAll();
        assertFalse(list.isEmpty());

        Reservation last = list.get(list.size() - 1);
        idReservationTest = last.getIdReservation();

        assertEquals(1, last.getIdClient());
        assertEquals("carte", last.getModalitesPaiement());
    }

    @Test
    @Order(2)
    void testUpdateReservation() {
        Reservation r = new Reservation();
        r.setIdReservation(idReservationTest);
        r.setDateReservation(LocalDateTime.now());
        r.setStatut("confirmee");
        r.setModalitesPaiement("paypal");
        r.setIdClient(1);

        service.update(r);

        List<Reservation> list = service.getAll();
        boolean ok = list.stream().anyMatch(x ->
                x.getIdReservation() == idReservationTest &&
                        "confirmee".equals(x.getStatut()) &&
                        "paypal".equals(x.getModalitesPaiement())
        );

        assertTrue(ok);
    }

    @Test
    @Order(3)
    void testDeleteReservation() {
        Reservation r = new Reservation();
        r.setIdReservation(idReservationTest);

        service.delete(r);

        List<Reservation> list = service.getAll();
        boolean exists = list.stream().anyMatch(x -> x.getIdReservation() == idReservationTest);

        assertFalse(exists);
    }
}
