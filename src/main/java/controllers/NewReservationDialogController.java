package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reservation;
import services.ServiceReservation;
import services.ServiceBillet;
import models.Billet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class NewReservationDialogController {

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbPaiement;
    @FXML private Label lblError;

    private final ServiceReservation sr = new ServiceReservation();
    private final ServiceBillet sb = new ServiceBillet();

    private Integer clientId;
    private Billet initialBillet;
    private String paysDestination;
    private Consumer<Reservation> onSaved;

    @FXML
    public void initialize() {
        cbPaiement.getItems().setAll("carte", "espece", "virement");

        dpDate.setValue(LocalDate.now());
        cbPaiement.setValue("carte");
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public void setInitialBillet(Billet b) {
        this.initialBillet = b;
    }

    public void setPaysDestination(String paysDestination) {
        this.paysDestination = paysDestination;
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

        if (date == null) { showError("Veuillez choisir une date."); return; }
        if (paiement == null || paiement.isBlank()) { showError("Veuillez choisir une modalité de paiement."); return; }

        LocalDateTime dt = date.atTime(LocalTime.now().withSecond(0).withNano(0));

        Reservation r = new Reservation();
        r.setDateReservation(dt);
        r.setModalitesPaiement(paiement);
        r.setStatut("en_attente");
        r.setClientId(clientId); // ✅
        
        // Ensure paysDestination is not null to prevent database constraints from failing
        if (paysDestination == null || paysDestination.trim().isEmpty()) {
            r.setPaysdestination("destination inconnue");
        } else {
            r.setPaysdestination(paysDestination);
        }

        try {
            int idRes = sr.add(r); // ✅ ajoute + récupère idReservation
            r.setIdReservation(idRes);

            // Auto-create billet if one was passed in
            if (initialBillet != null) {
                initialBillet.setReservationId(idRes);
                sb.add(initialBillet);
            }

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