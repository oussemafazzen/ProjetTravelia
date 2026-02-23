package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private PasswordField confirmPasswordF;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblUserEmail;

    @FXML
    private PasswordField newPasswordF;

    @FXML
    private Button btnReset;

    private String userEmail;
    private String expectedCode;
    private UserService userService = new UserService();

    public void setData(String email, String code) {
        this.userEmail = email;
        this.expectedCode = code;
        lblUserEmail.setText("Email : " + email);
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPass = newPasswordF.getText();
        String confirmPass = confirmPasswordF.getText();
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            lblMessage.setText("Remplir tous les champs");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            lblMessage.setText("Les mots de passe ne correspondent pas");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            userService.updatePassword(userEmail, newPass);
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Mot de passe mis à jour !");
            
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> goToLogin(null));
            pause.play();
        } catch (SQLException e) {
            lblMessage.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            btnReset.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
