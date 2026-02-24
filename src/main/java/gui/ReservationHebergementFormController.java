package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Hebergement;
import models.ReservationHebergement;
import services.HebergementService;
import services.ReservationHebergementService;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReservationHebergementFormController implements Initializable {

    @FXML
    private Label lblTitle;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextField tfNbPersonnes;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextField tfIdClient;
    @FXML
    private ComboBox<Hebergement> cbHebergement;

    @FXML
    private Label lblErrorDateDebut;
    @FXML
    private Label lblErrorDateFin;
    @FXML
    private Label lblErrorNbPersonnes;
    @FXML
    private Label lblErrorStatut;
    @FXML
    private Label lblErrorIdClient;
    @FXML
    private Label lblErrorHebergement;

    private ReservationHebergementService rhs = new ReservationHebergementService();
    private HebergementService hs = new HebergementService();
    private ReservationHebergement reservation;
    private boolean isUpdate = false;
    private ReservationHebergementController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbStatut.setItems(FXCollections.observableArrayList("en_attente", "confirmee", "annulee"));
        try {
            cbHebergement.setItems(FXCollections.observableArrayList(hs.recupTousHebergements()));
        } catch (SQLException e) {
            System.err.println("Erreur chargement hébergements: " + e.getMessage());
        }
        
        // Add input validation filters
        addIntegerOnlyFilter(tfNbPersonnes);
        addIntegerOnlyFilter(tfIdClient);
    }

    public void setParentController(ReservationHebergementController parentController) {
        this.parentController = parentController;
    }

    public void setReservation(ReservationHebergement reservation) {
        this.reservation = reservation;
        if (reservation != null) {
            this.isUpdate = true;
            this.lblTitle.setText("Modifier la Réservation");
            this.dpDateDebut.setValue(reservation.getDateDebut().toLocalDate());
            this.dpDateFin.setValue(reservation.getDateFin().toLocalDate());
            this.tfNbPersonnes.setText(String.valueOf(reservation.getNombrePersonnes()));
            this.cbStatut.setValue(reservation.getStatut());
            this.tfIdClient.setText(String.valueOf(reservation.getIdClient()));

            this.cbHebergement.setValue(reservation.getHebergement());
        } else {
            this.isUpdate = false;
            this.lblTitle.setText("Nouvelle Réservation");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (reservation == null) {
            reservation = new ReservationHebergement();
        }

        reservation.setDateDebut(Date.valueOf(dpDateDebut.getValue()));
        reservation.setDateFin(Date.valueOf(dpDateFin.getValue()));
        reservation.setNombrePersonnes(Integer.parseInt(tfNbPersonnes.getText()));
        reservation.setStatut(cbStatut.getValue());
        reservation.setIdClient(Integer.parseInt(tfIdClient.getText()));
        reservation.setHebergement(cbHebergement.getValue());

        try {
            if (isUpdate) {
                rhs.modifierReservation(reservation);
            } else {
                rhs.ajouterReservation(reservation);
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sauvegarde");
            alert.setContentText("Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
            alert.showAndWait();
            return;
        }

        parentController.loadData();
        closeStage();
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        // Reset error messages
        clearErrors();

        if (dpDateDebut.getValue() == null) {
            setErrorMessage(lblErrorDateDebut, "Date début requise!");
            isValid = false;
        }
        if (dpDateFin.getValue() == null) {
            setErrorMessage(lblErrorDateFin, "Date fin requise!");
            isValid = false;
        }
        if (dpDateDebut.getValue() != null && dpDateFin.getValue() != null) {
            if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
                setErrorMessage(lblErrorDateFin, "Doit être après début!");
                isValid = false;
            }
        }

        if (tfNbPersonnes.getText() == null || tfNbPersonnes.getText().isEmpty()) {
            setErrorMessage(lblErrorNbPersonnes, "Nombre requis!");
            isValid = false;
        } else {
            try {
                int nb = Integer.parseInt(tfNbPersonnes.getText());
                if (nb <= 0) {
                    setErrorMessage(lblErrorNbPersonnes, "Doit être > 0!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorNbPersonnes, "Doit être un entier!");
                isValid = false;
            }
        }

        if (cbStatut.getValue() == null) {
            setErrorMessage(lblErrorStatut, "Statut requis!");
            isValid = false;
        }

        if (tfIdClient.getText() == null || tfIdClient.getText().isEmpty()) {
            setErrorMessage(lblErrorIdClient, "ID requis!");
            isValid = false;
        } else {
            try {
                int id = Integer.parseInt(tfIdClient.getText());
                if (id <= 0) {
                    setErrorMessage(lblErrorIdClient, "Doit être > 0!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorIdClient, "Doit être un entier!");
                isValid = false;
            }
        }

        if (cbHebergement.getValue() == null) {
            setErrorMessage(lblErrorHebergement, "Choix requis!");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        Label[] labels = {lblErrorDateDebut, lblErrorDateFin, lblErrorNbPersonnes, 
                          lblErrorStatut, lblErrorIdClient, lblErrorHebergement};
        for (Label label : labels) {
            label.setText("");
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    private void setErrorMessage(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) dpDateDebut.getScene().getWindow()).close();
    }

    /**
     * Adds a filter to allow only integer numbers in the TextField
     */
    private void addIntegerOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(oldValue);
            }
        });
    }
}
