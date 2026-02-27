package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    void goToLogin(ActionEvent event) {
        navigateTo("/views/Login.fxml");
    }

    @FXML
    void goToRegister(ActionEvent event) {
        navigateTo("/views/Register.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            btnLogin.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
