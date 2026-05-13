package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Activite;
import services.ActiviteService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ActiviteFormController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private TextField tfLieu;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private TextField tfCapaciteMax;
    @FXML private ComboBox<String> cbCategorie;

    @FXML private Label lblErrorNom;
    @FXML private Label lblErrorDescription;
    @FXML private Label lblErrorLieu;
    @FXML private Label lblErrorDuree;
    @FXML private Label lblErrorPrix;
    @FXML private Label lblErrorCapaciteMax;
    @FXML private Label lblErrorCategorie;

    private ActiviteService as = new ActiviteService();
    private Activite activite;
    private boolean isUpdate = false;
    private Object parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbCategorie.setItems(FXCollections.observableArrayList(
                "Aventure", "Culture", "Sport", "Détente", "Gastronomie", "Nature", "Autre"
        ));

        // Add input validation filters
        addTextOnlyFilter(tfNom);
        addTextOnlyFilter(tfLieu);
        addIntegerOnlyFilter(tfDuree);
        addDecimalOnlyFilter(tfPrix);
        addIntegerOnlyFilter(tfCapaciteMax);
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
        if (activite != null) {
            this.isUpdate = true;
            this.lblTitle.setText("Modifier l'Activité");
            this.tfNom.setText(activite.getNom());
            this.taDescription.setText(activite.getDescription());
            this.tfLieu.setText(activite.getLieu());
            this.tfDuree.setText(String.valueOf(activite.getDuree()));
            this.tfPrix.setText(String.valueOf(activite.getPrix()));
            this.tfCapaciteMax.setText(String.valueOf(activite.getCapaciteMax()));
            this.cbCategorie.setValue(activite.getCategorie());
        } else {
            this.isUpdate = false;
            this.lblTitle.setText("Ajouter une Activité");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (activite == null) {
            activite = new Activite();
        }

        activite.setNom(tfNom.getText());
        activite.setDescription(taDescription.getText());
        activite.setLieu(tfLieu.getText());
        activite.setDuree(Integer.parseInt(tfDuree.getText()));
        activite.setPrix(Double.parseDouble(tfPrix.getText()));
        activite.setCapaciteMax(Integer.parseInt(tfCapaciteMax.getText()));
        activite.setCategorie(cbCategorie.getValue());

        try {
            if (isUpdate) {
                as.modifierActivite(activite);
            } else {
                as.ajouterActivite(activite);
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

        if (parentController instanceof ActiviteController) {
            ((ActiviteController) parentController).loadData();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        clearErrors();

        // Nom: requis, 3-100 caractères
        String nom = tfNom.getText();
        if (nom == null || nom.trim().isEmpty()) {
            setErrorMessage(lblErrorNom, "Nom requis!");
            isValid = false;
        } else if (nom.trim().length() < 3) {
            setErrorMessage(lblErrorNom, "Le nom doit contenir au moins 3 caractères!");
            isValid = false;
        } else if (nom.trim().length() > 100) {
            setErrorMessage(lblErrorNom, "Le nom ne peut pas dépasser 100 caractères!");
            isValid = false;
        }

        // Description: requis, 10-500 caractères
        String desc = taDescription.getText();
        if (desc == null || desc.trim().isEmpty()) {
            setErrorMessage(lblErrorDescription, "Description requise!");
            isValid = false;
        } else if (desc.trim().length() < 10) {
            setErrorMessage(lblErrorDescription, "La description doit contenir au moins 10 caractères!");
            isValid = false;
        } else if (desc.trim().length() > 500) {
            setErrorMessage(lblErrorDescription, "La description ne peut pas dépasser 500 caractères!");
            isValid = false;
        }

        // Lieu: requis, 2-100 caractères
        String lieu = tfLieu.getText();
        if (lieu == null || lieu.trim().isEmpty()) {
            setErrorMessage(lblErrorLieu, "Lieu requis!");
            isValid = false;
        } else if (lieu.trim().length() < 2) {
            setErrorMessage(lblErrorLieu, "Le lieu doit contenir au moins 2 caractères!");
            isValid = false;
        } else if (lieu.trim().length() > 100) {
            setErrorMessage(lblErrorLieu, "Le lieu ne peut pas dépasser 100 caractères!");
            isValid = false;
        }

        // Catégorie: requis (ComboBox)
        if (cbCategorie.getValue() == null) {
            setErrorMessage(lblErrorCategorie, "Catégorie requise!");
            isValid = false;
        }

        // Durée: requis, entre 1 et 1440 minutes
        if (tfDuree.getText() == null || tfDuree.getText().isEmpty()) {
            setErrorMessage(lblErrorDuree, "Durée requise!");
            isValid = false;
        } else {
            try {
                int duree = Integer.parseInt(tfDuree.getText());
                if (duree < 1 || duree > 1440) {
                    setErrorMessage(lblErrorDuree, "La durée doit être entre 1 et 1440 minutes!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorDuree, "Doit être un nombre entier!");
                isValid = false;
            }
        }

        // Prix: requis, entre 0.01 et 10000 DT
        if (tfPrix.getText() == null || tfPrix.getText().isEmpty()) {
            setErrorMessage(lblErrorPrix, "Prix requis!");
            isValid = false;
        } else {
            try {
                double prix = Double.parseDouble(tfPrix.getText());
                if (prix < 0.01 || prix > 10000) {
                    setErrorMessage(lblErrorPrix, "Le prix doit être entre 0.01 et 10000 DT!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorPrix, "Doit être un nombre valide!");
                isValid = false;
            }
        }

        // Capacité: requis, entre 1 et 1000
        if (tfCapaciteMax.getText() == null || tfCapaciteMax.getText().isEmpty()) {
            setErrorMessage(lblErrorCapaciteMax, "Capacité requise!");
            isValid = false;
        } else {
            try {
                int capacite = Integer.parseInt(tfCapaciteMax.getText());
                if (capacite < 1 || capacite > 1000) {
                    setErrorMessage(lblErrorCapaciteMax, "La capacité doit être entre 1 et 1000!");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                setErrorMessage(lblErrorCapaciteMax, "Doit être un nombre entier!");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        Label[] labels = {lblErrorNom, lblErrorDescription, lblErrorLieu, lblErrorDuree,
                          lblErrorPrix, lblErrorCapaciteMax, lblErrorCategorie};
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

    private void addTextOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s'-]*")) {
                textField.setText(oldValue);
            }
        });
    }

    private void addIntegerOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(oldValue);
            }
        });
    }

    private void addDecimalOnlyFilter(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                textField.setText(oldValue);
            }
        });
    }
}
