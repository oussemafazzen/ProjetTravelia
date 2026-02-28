package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Reservation;
import models.Billet;
import services.PaiementReservationService;
import services.ServiceBillet;

import java.sql.SQLException;
import java.util.List;

public class PaymentFlightController {

    @FXML private VBox mainContainer;
    @FXML private Text txtReservationTitle;
    @FXML private Text txtDetails;
    @FXML private Text txtPrixTotal;
    @FXML private TextField tfCardNumber;
    @FXML private TextField tfExpiry;
    @FXML private TextField tfCvc;
    @FXML private Button btnPayer;

    private Reservation reservation;
    private double totalAmount;
    private PaiementReservationService paiementService = new PaiementReservationService();
    private Runnable onPaymentSuccess;

    public void setReservation(Reservation res) {
        this.reservation = res;

        // Get billets for this reservation to calculate total
        ServiceBillet sb = new ServiceBillet();
        List<Billet> billets = sb.getByReservationId(res.getIdReservation());

        this.totalAmount = 0;
        for (Billet b : billets) {
            totalAmount += b.getPrix();
        }

        // Populate UI
        txtReservationTitle.setText("Réservation #" + res.getIdReservation());
        txtDetails.setText("Billets: " + billets.size() + "  •  " +
                (res.getDateReservation() != null ? res.getDateReservation().toLocalDate().toString() : ""));
        txtPrixTotal.setText(String.format("%.0f DT", totalAmount));
    }

    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    @FXML
    private void handlePayer() {
        if (!validateCard()) return;

        try {
            paiementService.ajouterPaiement(totalAmount, "CARD", reservation.getIdReservation());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paiement Réussi");
            alert.setHeaderText("Félicitations !");
            alert.setContentText("Votre paiement de " + String.format("%.0f", totalAmount) + " DT a été accepté. Votre réservation est maintenant confirmée.");
            alert.showAndWait();

            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }

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
        if (tfCardNumber.getText().length() < 16) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Numéro de carte invalide.");
            alert.show();
            return false;
        }
        return true;
    }
}
