package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.InscriptionActivite;
import services.InscriptionActiviteService;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class InscriptionActiviteFormController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private DatePicker dpDateActivite;
    @FXML private TextField tfNombreParticipants;

    @FXML private Label lblErrorDate;
    @FXML private Label lblErrorParticipants;

    private InscriptionActiviteService is = new InscriptionActiviteService();
    private InscriptionActivite inscription;
    private boolean isUpdate = false;
    private Object parentController;
    
    // Default IDs for new inscriptions
    private int defaultClientId = -1;
    private int defaultActiviteId = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addIntegerOnlyFilter(tfNombreParticipants);
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setInscription(InscriptionActivite inscription) {
        this.inscription = inscription;
        if (inscription != null) {
            this.isUpdate = true;
            this.lblTitle.setText("Modifier l'Inscription");
            if (inscription.getDateActivite() != null) {
                this.dpDateActivite.setValue(inscription.getDateActivite().toLocalDate());
            }
            this.tfNombreParticipants.setText(String.valueOf(inscription.getNombreParticipants()));
        } else {
            this.isUpdate = false;
            this.lblTitle.setText("Nouvelle Inscription");
        }
    }

    public void prefillClientAndActivite(int clientId, int activiteId) {
        this.defaultClientId = clientId;
        this.defaultActiviteId = activiteId;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (inscription == null) {
            inscription = new InscriptionActivite();
            inscription.setIdClient(defaultClientId);
            inscription.setIdActivite(defaultActiviteId);
        }

        LocalDate localDate = dpDateActivite.getValue();
        inscription.setDateActivite(Date.valueOf(localDate));
        inscription.setNombreParticipants(Integer.parseInt(tfNombreParticipants.getText()));
        // Always save as "Confirmée" based on user requirements
        inscription.setStatut("Confirmée");

        try {
            if (isUpdate) {
                is.modifierInscription(inscription);
            } else {
                is.ajouterInscription(inscription);
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sauvegarde");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
            alert.showAndWait();
            return;
        }

        refreshParent();
        closeStage();
    }

    private void refreshParent() {
        if (parentController == null) return;

        if (parentController instanceof InscriptionActiviteController) {
            ((InscriptionActiviteController) parentController).loadData();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        clearErrors();

        // Date check
        if (dpDateActivite.getValue() == null) {
            setErrorMessage(lblErrorDate, "La date est requise !");
            isValid = false;
        } else if (dpDateActivite.getValue().isBefore(LocalDate.now())) {
            setErrorMessage(lblErrorDate, "La date ne peut pas être dans le passé !");
            isValid = false;
        }

        // Participants check
        if (tfNombreParticipants.getText() == null || tfNombreParticipants.getText().isEmpty()) {
            setErrorMessage(lblErrorParticipants, "Le nombre de participants est requis !");
            isValid = false;
        } else {
            try {
                int nb = Integer.parseInt(tfNombreParticipants.getText());
                if (nb < 1 || nb > 50) {
                    setErrorMessage(lblErrorParticipants, "Veuillez entrer un nombre entre 1 et 50.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorParticipants, "Doit être un nombre valide !");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        if (lblErrorDate != null) {
            lblErrorDate.setText("");
            lblErrorDate.setVisible(false);
            lblErrorDate.setManaged(false);
        }
        if (lblErrorParticipants != null) {
            lblErrorParticipants.setText("");
            lblErrorParticipants.setVisible(false);
            lblErrorParticipants.setManaged(false);
        }
    }

    private void setErrorMessage(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        if (dpDateActivite != null && dpDateActivite.getScene() != null) {
            ((Stage) dpDateActivite.getScene().getWindow()).close();
        }
    }

    private void addIntegerOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(oldValue);
            }
        });
    }
}
