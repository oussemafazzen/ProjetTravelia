package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Paiement;
import models.ReservationHebergement;
import services.PaiementService;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

public class PaymentController {

    @FXML private VBox mainContainer;
    @FXML private Text txtHebergement;
    @FXML private Text txtDates;
    @FXML private Text txtPrixTotal;
    @FXML private TextField tfCardNumber;
    @FXML private TextField tfExpiry;
    @FXML private TextField tfCvc;
    @FXML private Button btnPayer;

    private ReservationHebergement reservation;
    private double totalAmount;
    private PaiementService paiementService = new PaiementService();

    public void setReservation(ReservationHebergement res) {
        this.reservation = res;
        
        // Populate UI
        txtHebergement.setText(res.getHebergement().getNom());
        txtDates.setText(res.getDateDebut().toString() + " au " + res.getDateFin().toString());
        
        // Calculate price
        long nights = ChronoUnit.DAYS.between(res.getDateDebut().toLocalDate(), res.getDateFin().toLocalDate());
        if (nights <= 0) nights = 1; // Minimal stay
        
        this.totalAmount = nights * res.getHebergement().getTarifParNuit();
        txtPrixTotal.setText(String.format("%.0f DT", totalAmount));
    }

    @FXML
    private void handlePayer() {
        if (!validateCard()) return;

        try {
            Paiement p = new Paiement(totalAmount, "CARD", reservation.getIdReservationHebergement());
            paiementService.ajouterPaiement(p);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paiement Réussi");
            alert.setHeaderText("Félicitations !");
            alert.setContentText("Votre paiement de " + totalAmount + " DT a été accepté. Votre réservation est maintenant confirmée.");
            alert.showAndWait();

            handleCancel(); // Close window

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Paiement");
            alert.setHeaderText("Échec de la transaction");
            alert.setContentText("Une erreur est survenue lors du traitement : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) mainContainer.getScene().getWindow()).close();
    }

    private boolean validateCard() {
        if (tfCardNumber.getText().isEmpty() || tfExpiry.getText().isEmpty() || tfCvc.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez remplir tous les champs de la carte.");
            alert.show();
            return false;
        }
        // Simplified validation for demo
        if (tfCardNumber.getText().length() < 16) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Numéro de carte invalide.");
            alert.show();
            return false;
        }
        return true;
    }
}
