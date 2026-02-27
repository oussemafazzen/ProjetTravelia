package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Reservation;
import org.example.services.ServiceReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class NewReservationDialogController {

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbPaiement;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;

    private final ServiceReservation sr = new ServiceReservation();

    private Integer clientId;
    private Consumer<Reservation> onSaved;

    @FXML
    public void initialize() {
        cbPaiement.getItems().setAll("carte", "espece", "virement");
        cbStatut.getItems().setAll("en_attente", "confirmee", "annulee");

        dpDate.setValue(LocalDate.now());
        cbPaiement.setValue("carte");
        cbStatut.setValue("en_attente");

        hideError();
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public void setOnSaved(Consumer<Reservation> onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    @FXML
    private void onSave() {
        hideError();

        if (clientId == null) { showError("Aucun client connecté."); return; }

        LocalDate date = dpDate.getValue();
        String paiement = cbPaiement.getValue();
        String statut = cbStatut.getValue();

        if (date == null) { showError("Veuillez choisir une date."); return; }
        if (paiement == null || paiement.isBlank()) { showError("Veuillez choisir une modalité de paiement."); return; }
        if (statut == null || statut.isBlank()) { showError("Veuillez choisir un statut."); return; }

        LocalDateTime dt = date.atTime(LocalTime.now().withSecond(0).withNano(0));

        Reservation r = new Reservation();
        r.setDateReservation(dt);
        r.setModalitesPaiement(paiement);
        r.setStatut(statut);
        r.setClientId(clientId); // ✅

        try {
            sr.add(r); // ✅ ajoute + récupère idReservation
            if (onSaved != null) onSaved.accept(r);
            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'ajout: " + ex.getMessage());
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