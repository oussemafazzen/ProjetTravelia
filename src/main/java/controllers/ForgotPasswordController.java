package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.UserService;
import utils.EmailService;

import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML
    private TextField emailF;

    @FXML
    private Label lblMessage;

    @FXML
    private Button btnSend;

    private UserService userService = new UserService();
    private EmailService emailService = new EmailService();

    @FXML
    void handleSendResetLink(ActionEvent event) {
        String email = emailF.getText();
        if (email.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Veuillez saisir votre email");
            return;
        }

        try {
            if (userService.emailExists(email)) {
                // Generate a random 6-digit code
                String resetCode = String.valueOf((int) (Math.random() * 900000) + 100000);
                
                // Redirecting to ResetPassword immediately
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResetPassword.fxml"));
                    Parent root = loader.load();
                    ResetPasswordController controller = loader.getController();
                    controller.setData(email, resetCode);
                    btnSend.getScene().setRoot(root);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Send Real Email (in background)
                new Thread(() -> {
                    emailService.sendPasswordResetEmail(email, resetCode);
                }).start();
            } else {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Email non trouvé");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            btnSend.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
