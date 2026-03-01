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
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblStatut;
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
        cbStatut.getItems().setAll("en_attente", "confirmee", "annulee");

        dpDate.setValue(LocalDate.now());
        cbPaiement.setValue("carte");
        cbStatut.setValue("en_attente");

        // Hide status field if user is not admin
        if (!utils.SessionContext.isAdmin()) {
            cbStatut.setVisible(false);
            cbStatut.setManaged(false);
            // Also hide the label (assuming it's the preceding sibling in the grid or we can find it)
            // Simpler: find the labels in the GridPane and hide the "Statut" one.
        }

        hideError();
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