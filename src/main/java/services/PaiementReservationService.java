package services;

import utils.MyDataBase;

import java.sql.*;

public class PaiementReservationService {
    private Connection cnx;

    public PaiementReservationService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS paiement_reservation (" +
                     "    id_paiement INT AUTO_INCREMENT PRIMARY KEY," +
                     "    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "    montant DOUBLE NOT NULL," +
                     "    methode_paiement VARCHAR(50) NOT NULL," +
                     "    id_reservation INT NOT NULL" +
                     ");";
        try (Statement stm = cnx.createStatement()) {
            stm.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table paiement_reservation: " + e.getMessage());
        }
    }

    public void ajouterPaiement(double montant, String methode, int idReservation) throws SQLException {
        String req = "INSERT INTO paiement_reservation (montant, methode_paiement, id_reservation) VALUES (?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setDouble(1, montant);
            pstm.setString(2, methode);
            pstm.setInt(3, idReservation);
            pstm.executeUpdate();
        }

        // Update reservation status to CONFIRMÉ
        confirmerReservation(idReservation);
    }

    private void confirmerReservation(int idReservation) throws SQLException {
        String req = "UPDATE reservation SET statut = 'CONFIRMÉ' WHERE id_reservation = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, idReservation);
            pstm.executeUpdate();
        }
    }
}
