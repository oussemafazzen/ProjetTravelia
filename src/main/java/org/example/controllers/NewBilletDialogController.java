package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Billet;
import org.example.services.ServiceBillet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class NewBilletDialogController {

    @FXML private ComboBox<String> cbTransport;
    @FXML private DatePicker dpDepart;
    @FXML private DatePicker dpArrivee;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;

    private final ServiceBillet sb = new ServiceBillet();

    private Integer reservationId;          // obligatoire
    private Consumer<Billet> onSaved;       // callback

    @FXML
    public void initialize() {
        cbTransport.getItems().setAll("avion", "train", "bus");
        cbStatut.getItems().setAll("en_attente", "confirme", "confirmee", "annulee");

        cbTransport.setValue("avion");
        cbStatut.setValue("confirme");

        dpDepart.setValue(LocalDate.now());
        dpArrivee.setValue(LocalDate.now());
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public void setOnSaved(Consumer<Billet> onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    @FXML
    private void onSave() {
        hideError();

        if (reservationId == null) {
            showError("Réservation introuvable.");
            return;
        }

        String transport = cbTransport.getValue();
        LocalDate d1 = dpDepart.getValue();
        LocalDate d2 = dpArrivee.getValue();
        String statut = cbStatut.getValue();
        String prixTxt = tfPrix.getText();

        if (transport == null || transport.isBlank()) { showError("Choisir un transport."); return; }
        if (d1 == null) { showError("Choisir la date de départ."); return; }
        if (d2 == null) { showError("Choisir la date d'arrivée."); return; }
        if (statut == null || statut.isBlank()) { showError("Choisir un statut."); return; }

        double prix;
        try {
            prix = Double.parseDouble(prixTxt == null ? "" : prixTxt.trim());
            if (prix <= 0) { showError("Prix invalide."); return; }
        } catch (Exception ex) {
            showError("Prix invalide.");
            return;
        }

        // dates -> LocalDateTime (heure simple)
        LocalDateTime depart = d1.atTime(LocalTime.of(10, 0));
        LocalDateTime arrivee = d2.atTime(LocalTime.of(12, 0));

        Billet b = new Billet();
        b.setTypeTransport(transport);
        b.setNumeroBillet("BIL-" + System.currentTimeMillis());
        b.setDateDepart(depart);
        b.setDateArrivee(arrivee);
        b.setPrix(prix);
        b.setStatut(statut);
        b.setReservationId(reservationId);

        try {
            sb.add(b);
            if (onSaved != null) onSaved.accept(b);
            closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur ajout billet: " + ex.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cbTransport.getScene().getWindow();
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