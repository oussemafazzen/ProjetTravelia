package services;

import models.Reservation;
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
    void testAddReservation() throws Exception {
        Reservation r = new Reservation();
        r.setDateReservation(LocalDateTime.now());
        r.setStatut("en_attente");
        r.setModalitesPaiement("carte");
        r.setClientId(1);
        service.add(r);

        List<Reservation> list = service.getAll();
        assertFalse(list.isEmpty());

        Reservation last = list.get(0); // service.getAll() returns list ordered by id DESC
        idReservationTest = last.getIdReservation();

        assertEquals(1, last.getClientId());
        assertEquals("carte", last.getModalitesPaiement());
    }

    @Test
    @Order(2)
    void testUpdateReservation() throws Exception {
        Reservation r = new Reservation();
        r.setIdReservation(idReservationTest);
        r.setDateReservation(LocalDateTime.now());
        r.setStatut("confirmee");
        r.setModalitesPaiement("paypal");
        r.setClientId(1);

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
    void testDeleteReservation() throws Exception {
        Reservation r = new Reservation();
        r.setIdReservation(idReservationTest);

        service.delete(r.getIdReservation());

        List<Reservation> list = service.getAll();
        boolean exists = list.stream().anyMatch(x -> x.getIdReservation() == idReservationTest);

        assertFalse(exists);
    }
}
