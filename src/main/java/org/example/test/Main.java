package org.example.test;

import org.example.models.Billet;
import org.example.models.Reservation;
import org.example.services.ServiceBillet;
import org.example.services.ServiceReservation;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ServiceReservation sr = new ServiceReservation();
        ServiceBillet sb = new ServiceBillet();

        Reservation r = new Reservation();
        r.setDateReservation(LocalDateTime.now());
        r.setStatut("confirmee");
        r.setModalitesPaiement("carte");
        r.setClientId(1);

        sr.add(r);

        List<Reservation> reservations = sr.getAll();
        Reservation lastReservation = reservations.get(reservations.size() - 1);

        Billet b = new Billet();
        b.setTypeTransport("avion");
        b.setNumeroBillet("BIL-" + System.currentTimeMillis());
        b.setDateDepart(LocalDateTime.now());
        b.setDateArrivee(LocalDateTime.now().plusHours(2));
        b.setPrix(450);
        b.setStatut("confirme");
        b.setReservationId(lastReservation.getIdReservation());

        sb.add(b);

        System.out.println("RESERVATIONS:");
        sr.getAll().forEach(System.out::println);

        System.out.println("BILLETS:");
        sb.getAll().forEach(System.out::println);
    }
}