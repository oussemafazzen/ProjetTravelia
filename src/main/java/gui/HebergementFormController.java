package gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Hebergement;
import services.HebergementService;
import java.sql.SQLException;

import java.net.URL;
import java.util.ResourceBundle;

public class HebergementFormController implements Initializable {

    @FXML
    private Label lblTitle;
    @FXML
    private TextField tfNom;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField tfAdresse;
    @FXML
    private TextField tfVille;
    @FXML
    private TextField tfPays;
    @FXML
    private TextField tfCapacite;
    @FXML
    private TextField tfEquipements;
    @FXML
    private TextField tfTarif;

    @FXML
    private Label lblErrorNom;
    @FXML
    private Label lblErrorType;
    @FXML
    private Label lblErrorAdresse;
    @FXML
    private Label lblErrorVille;
    @FXML
    private Label lblErrorPays;
    @FXML
    private Label lblErrorCapacite;
    @FXML
    private Label lblErrorEquipements;
    @FXML
    private Label lblErrorTarif;

    private HebergementService hs = new HebergementService();
    private Hebergement hebergement;
    private boolean isUpdate = false;
    private Object parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbType.setItems(FXCollections.observableArrayList("hotel", "auberge"));
        
        // Add input validation filters
        addTextOnlyFilter(tfNom);
        addTextOnlyFilter(tfVille);
        addTextOnlyFilter(tfPays);
        addIntegerOnlyFilter(tfCapacite);
        addDecimalOnlyFilter(tfTarif);
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setHebergement(Hebergement hebergement) {
        this.hebergement = hebergement;
        if (hebergement != null) {
            this.isUpdate = true;
            this.lblTitle.setText("Modifier l'Hébergement");
            this.tfNom.setText(hebergement.getNom());
            this.cbType.setValue(hebergement.getType());
            this.tfAdresse.setText(hebergement.getAdresse());
            this.tfVille.setText(hebergement.getVille());
            this.tfPays.setText(hebergement.getPays());
            this.tfCapacite.setText(String.valueOf(hebergement.getCapacite()));
            this.tfEquipements.setText(hebergement.getEquipements());
            this.tfTarif.setText(String.valueOf(hebergement.getTarifParNuit()));
        } else {
            this.isUpdate = false;
            this.lblTitle.setText("Ajouter un Hébergement");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (hebergement == null) {
            hebergement = new Hebergement();
        }

        hebergement.setNom(tfNom.getText());
        hebergement.setType(cbType.getValue());
        hebergement.setAdresse(tfAdresse.getText());
        hebergement.setVille(tfVille.getText());
        hebergement.setPays(tfPays.getText());
        hebergement.setCapacite(Integer.parseInt(tfCapacite.getText()));
        hebergement.setEquipements(tfEquipements.getText());
        hebergement.setTarifParNuit(Double.parseDouble(tfTarif.getText()));

        try {
            if (isUpdate) {
                hs.modifierHebergement(hebergement);
            } else {
                hs.ajouterHebergement(hebergement);
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sauvegarde");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
            alert.showAndWait();
            return; // Don't close or refresh if it failed
        }

        // Refresh parent data if possible
        refreshParent();
        
        closeStage();
    }

    private void refreshParent() {
        if (parentController == null) return;
        
        if (parentController instanceof HebergementController) {
            ((HebergementController) parentController).loadData();
        } else if (parentController instanceof HebergementFrontController) {
            ((HebergementFrontController) parentController).loadData();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        // Reset error messages
        clearErrors();

        if (tfNom.getText() == null || tfNom.getText().isEmpty()) {
            setErrorMessage(lblErrorNom, "Nom requis!");
            isValid = false;
        }
        if (cbType.getValue() == null) {
            setErrorMessage(lblErrorType, "Type requis!");
            isValid = false;
        }
        if (tfAdresse.getText() == null || tfAdresse.getText().isEmpty()) {
            setErrorMessage(lblErrorAdresse, "Adresse requise!");
            isValid = false;
        }
        if (tfVille.getText() == null || tfVille.getText().isEmpty()) {
            setErrorMessage(lblErrorVille, "Ville requise!");
            isValid = false;
        }
        if (tfPays.getText() == null || tfPays.getText().isEmpty()) {
            setErrorMessage(lblErrorPays, "Pays requis!");
            isValid = false;
        }

        if (tfCapacite.getText() == null || tfCapacite.getText().isEmpty()) {
            setErrorMessage(lblErrorCapacite, "Capacité requise!");
            isValid = false;
        } else {
            try {
                int capacite = Integer.parseInt(tfCapacite.getText());
                if (capacite <= 0) {
                    setErrorMessage(lblErrorCapacite, "Doit être > 0!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorCapacite, "Doit être un nombre entier!");
                isValid = false;
            }
        }
        if (tfEquipements.getText() == null || tfEquipements.getText().isEmpty()) {
            setErrorMessage(lblErrorEquipements, "Equipements requis!");
            isValid = false;
        }

        if (tfTarif.getText() == null || tfTarif.getText().isEmpty()) {
            setErrorMessage(lblErrorTarif, "Tarif requis!");
            isValid = false;
        } else {
            try {
                double tarif = Double.parseDouble(tfTarif.getText());
                if (tarif <= 0) {
                    setErrorMessage(lblErrorTarif, "Doit être > 0!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorTarif, "Doit être un nombre valide!");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        Label[] labels = {lblErrorNom, lblErrorType, lblErrorAdresse, lblErrorVille, lblErrorPays, 
                          lblErrorCapacite, lblErrorEquipements, lblErrorTarif};
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
        ((Stage) tfNom.getScene().getWindow()).close();
    }

    /**
     * Adds a filter to allow only text (letters, spaces, accents) in the TextField
     */
    private void addTextOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow letters (including accented), spaces, hyphens, and apostrophes
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s'-]*")) {
                textField.setText(oldValue);
            }
        });
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

    /**
     * Adds a filter to allow only decimal numbers in the TextField
     */
    private void addDecimalOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow digits and at most one decimal point
            if (!newValue.matches("\\d*\\.?\\d*")) {
                textField.setText(oldValue);
            }
        });
    }
}
