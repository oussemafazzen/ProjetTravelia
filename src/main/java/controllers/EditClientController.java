package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Client;
import models.enums.Role;
import java.time.LocalDate;
import java.time.ZoneId;

public class EditClientController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtNationality;
    @FXML private DatePicker dpBirth;
    @FXML private ComboBox<Role> roleCombo;

    private Client client;
    private boolean saved = false;

    public void setClient(Client client) {
        this.client = client;
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtEmail.setText(client.getEmail());
        txtPhone.setText(client.getTelephone());
        txtNationality.setText(client.getNationalite());
        if (client.getDate_naissance() != null) {
            dpBirth.setValue(new java.sql.Date(client.getDate_naissance().getTime()).toLocalDate());
        }
        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.setValue(client.getRole());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            client.setNom(txtNom.getText());
            client.setPrenom(txtPrenom.getText());
            client.setEmail(txtEmail.getText());
            client.setTelephone(txtPhone.getText());
            client.setNationalite(txtNationality.getText());
            if (dpBirth.getValue() != null) {
                client.setDate_naissance(java.sql.Date.valueOf(dpBirth.getValue()));
            }
            client.setRole(roleCombo.getValue());
            saved = true;
            closeStage();
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeStage();
    }

    private boolean validateInput() {
        // Simple validation, can be enhanced
        return !txtNom.getText().isEmpty() && !txtEmail.getText().isEmpty();
    }

    private void closeStage() {
        ((Stage) txtNom.getScene().getWindow()).close();
    }
}
