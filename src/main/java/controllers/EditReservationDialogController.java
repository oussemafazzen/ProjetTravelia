package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reservation;
import services.ServiceReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class EditReservationDialogController {

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbPaiement;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;

    private final ServiceReservation sr = new ServiceReservation();

    private Reservation reservation;              // réservation à modifier
    private Consumer<Reservation> onUpdated;      // callback

    @FXML
    public void initialize() {
        cbPaiement.getItems().setAll("carte", "espece", "virement");
        cbStatut.getItems().setAll("en_attente", "confirmee", "annulee");
    }

    public void setReservation(Reservation r) {
        this.reservation = r;

        if (r.getDateReservation() != null) dpDate.setValue(r.getDateReservation().toLocalDate());
        else dpDate.setValue(LocalDate.now());

        cbPaiement.setValue(r.getModalitesPaiement() == null ? "carte" : r.getModalitesPaiement());
        cbStatut.setValue(r.getStatut() == null ? "en_attente" : r.getStatut());
    }

    public void setOnUpdated(Consumer<Reservation> onUpdated) {
        this.onUpdated = onUpdated;
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    @FXML
    private void onSave() {
        hideError();

        if (reservation == null) {
            showError("Réservation introuvable.");
            return;
        }

        LocalDate date = dpDate.getValue();
        String paiement = cbPaiement.getValue();
        String statut = cbStatut.getValue();

        if (date == null) { showError("Veuillez choisir une date."); return; }
        if (paiement == null || paiement.isBlank()) { showError("Veuillez choisir un paiement."); return; }
        if (statut == null || statut.isBlank()) { showError("Veuillez choisir un statut."); return; }

        // on garde l'heure actuelle
        LocalDateTime dt = date.atTime(LocalTime.now().withSecond(0).withNano(0));

        reservation.setDateReservation(dt);
        reservation.setModalitesPaiement(paiement);
        reservation.setStatut(statut);

        try {
            sr.update(reservation); // ✅ méthode ajoutée dans ServiceReservation (voir plus bas)
            if (onUpdated != null) onUpdated.accept(reservation);
            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur update: " + ex.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) dpDate.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setManaged(true);
        lblError.setVisible(true);
    }

    private void hideError() {
        lblError.setText("");
        lblError.setManaged(false);
        lblError.setVisible(false);
    }
}