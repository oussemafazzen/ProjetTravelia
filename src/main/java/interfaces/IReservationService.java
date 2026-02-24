package interfaces;

import models.ReservationHebergement;
import java.sql.SQLException;
import java.util.List;

public interface IReservationService {
    void ajouterReservation(ReservationHebergement r) throws SQLException;
    void modifierReservation(ReservationHebergement r) throws SQLException;
    void supprimerReservation(ReservationHebergement r) throws SQLException;
    void supprimerReservationParId(int id) throws SQLException;
    List<ReservationHebergement> recupToutesReservations() throws SQLException;
    ReservationHebergement recupParIdReservation(int id) throws SQLException;
}
