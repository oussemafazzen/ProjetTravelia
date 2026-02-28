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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        
        Integer currentUserId = utils.SessionContext.getCurrentUserId();
        if (currentUserId == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Vous devez être connecté");
            alert.setContentText("Veuillez vous connecter pour effectuer une réservation.");
            alert.showAndWait();
            return;
        }
        res.setIdClient(currentUserId);
        
        try {
            int resId = reservationService.ajouterReservation(res);
            res.setIdReservationHebergement(resId);
            
            // Redirect to Payment View
            openPaymentView(res);
            
        } catch (java.sql.SQLException e) {
            Alert alertError = new Alert(Alert.AlertType.ERROR);
            alertError.setTitle("Erreur");
            alertError.setHeaderText("Erreur lors de la réservation");
            alertError.setContentText("Une erreur est survenue : " + e.getMessage());
            alertError.showAndWait();
            return;
        }
        
        handleCancel();
    }

    private void openPaymentView(ReservationHebergement res) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/PaymentView.fxml"));
            javafx.scene.Parent root = loader.load();

            PaymentController controller = loader.getController();
            controller.setReservation(res);

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Paiement Hôtel : " + res.getHebergement().getNom());
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de l'ouverture du paiement : " + e.getMessage());
            alert.show();
        }
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
