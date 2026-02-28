package services;

import models.Paiement;
import utils.MyDataBase;

import java.sql.*;

public class PaiementService {
    private Connection cnx;

    public PaiementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS paiement (" +
                     "    id_paiement INT AUTO_INCREMENT PRIMARY KEY," +
                     "    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "    montant DOUBLE NOT NULL," +
                     "    methode_paiement VARCHAR(50) NOT NULL," +
                     "    id_reservation INT NOT NULL," +
                     "    CONSTRAINT fk_paiement_reservation FOREIGN KEY (id_reservation) REFERENCES reservationhebergement(id_reservation_hebergement) ON DELETE CASCADE" +
                     ");";
        try (Statement stm = cnx.createStatement()) {
            stm.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table paiement: " + e.getMessage());
        }
    }

    public void ajouterPaiement(Paiement p) throws SQLException {
        String req = "INSERT INTO paiement (montant, methode_paiement, id_reservation) VALUES (?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setDouble(1, p.getMontant());
            pstm.setString(2, p.getMethodePaiement());
            pstm.setInt(3, p.getIdReservation());
            pstm.executeUpdate();
            
            // On confirm successful payment, update reservation status
            confirmerReservation(p.getIdReservation());
        }
    }

    private void confirmerReservation(int idReservation) throws SQLException {
        String req = "UPDATE reservationhebergement SET statut = 'Confirmé' WHERE id_reservation_hebergement = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, idReservation);
            pstm.executeUpdate();
        }
    }
}
