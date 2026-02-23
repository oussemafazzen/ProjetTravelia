package org.example.test;

import org.example.models.Billet;
import org.example.models.Reservation;
import org.example.services.ServiceBillet;
import org.example.services.ServiceReservation;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        ServiceReservation sr = new ServiceReservation();
        ServiceBillet sb = new ServiceBillet();

        Reservation r = new Reservation(
                LocalDateTime.now(),
                "confirmee",
                "carte",
                1
        );

        sr.add(r);

        Reservation lastReservation = sr.getAll().get(sr.getAll().size() - 1);

        Billet b = new Billet(
                "avion",
                "BIL-" + System.currentTimeMillis(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                450,
                "confirme",
                lastReservation
        );

        sb.add(b);

        System.out.println("RESERVATIONS:");
        sr.getAll().forEach(System.out::println);

        System.out.println("BILLETS:");
        sb.getAll().forEach(System.out::println);
    }
}
