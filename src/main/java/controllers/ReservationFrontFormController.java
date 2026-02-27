package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.scene.control.DateCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Hebergement;
import models.ReservationHebergement;
import services.ReservationHebergementService;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReservationFrontFormController {

    @FXML private VBox mainContainer;
    @FXML private Text txtHebergementNom;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private TextField tfNbPersonnes;

    @FXML private Label lblErrorDateDebut;
    @FXML private Label lblErrorDateFin;
    @FXML private Label lblErrorNbPersonnes;

    private Hebergement selectedHebergement;
    private ReservationHebergementService reservationService = new ReservationHebergementService();

    @FXML
    public void initialize() {
        // Simple entry animation
        mainContainer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void setHebergement(Hebergement h) {
        this.selectedHebergement = h;
        txtHebergementNom.setText(h.getNom() + " • " + h.getVille());
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        ReservationHebergement res = new ReservationHebergement();
        res.setDateDebut(Date.valueOf(dpDateDebut.getValue()));
        res.setDateFin(Date.valueOf(dpDateFin.getValue()));
        res.setNombrePersonnes(Integer.parseInt(tfNbPersonnes.getText()));
        res.setHebergement(selectedHebergement);
        res.setStatut("En attente");
        res.setIdClient(1); // Default client ID for testing
        
        try {
            reservationService.ajouterReservation(res);
        } catch (java.sql.SQLException e) {
            Alert alertError = new Alert(Alert.AlertType.ERROR);
            alertError.setTitle("Erreur");
            alertError.setHeaderText("Erreur lors de la réservation");
            alertError.setContentText("Une erreur est survenue : " + e.getMessage());
            alertError.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Réservation effectuée");
        alert.setContentText("Votre séjour à " + selectedHebergement.getNom() + " a bien été enregistré.");
        alert.showAndWait();
        
        handleCancel();
    }

    @FXML
    private void handleCancel() {
        ((Stage) mainContainer.getScene().getWindow()).close();
    }

    private boolean validateFields() {
        boolean valid = true;
        clearErrors();

        LocalDate start = dpDateDebut.getValue();
        LocalDate end = dpDateFin.getValue();

        // 1. Check if dates are selected
        if (start == null) {
            showError(lblErrorDateDebut, "Date de départ obligatoire");
            valid = false;
        }

        if (end == null) {
            showError(lblErrorDateFin, "Date d'arrivée obligatoire");
            valid = false;
        } 
        
        // 2. LOGIC: Départ MUST be before Arrivée
        else if (start != null && (start.isAfter(end) || start.equals(end))) {
            showError(lblErrorDateFin, "La date de départ doit être avant la date d'arrivée");
            valid = false;
        }

        // 3. Travelers check
        try {
            int nb = Integer.parseInt(tfNbPersonnes.getText());
            if (nb <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError(lblErrorNbPersonnes, "Nombre de voyageurs invalide");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        hideError(lblErrorDateDebut);
        hideError(lblErrorDateFin);
        hideError(lblErrorNbPersonnes);
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
