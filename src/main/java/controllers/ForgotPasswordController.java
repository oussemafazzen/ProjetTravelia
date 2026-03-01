package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.PasswordResetToken;
import models.User;
import services.TokenService;
import services.SecurityLogService;
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
    private TokenService tokenService = new TokenService();
    private SecurityLogService securityLogService = new SecurityLogService();

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
                // Get user to get their ID
                User user = userService.getUserByEmail(email);
                if (user == null) {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Compte bloqué ou introuvable.");
                    return;
                }

                // Generate secure token and store in DB
                String resetToken = tokenService.generateToken(user.getId());

                // Log the event
                securityLogService.logEvent(user.getId(), "PASSWORD_RESET", "Token de réinitialisation généré pour " + email);

                // Navigate to ResetPassword with the token
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResetPassword.fxml"));
                    Parent root = loader.load();
                    ResetPasswordController controller = loader.getController();
                    controller.setData(email, resetToken);
                    btnSend.getScene().setRoot(root);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Send email with token (in background)
                new Thread(() -> {
                    emailService.sendPasswordResetEmail(email, resetToken);
                }).start();

                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("Code envoyé par email !");
            } else {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Email non trouvé");
            }
        } catch (SQLException e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur: " + e.getMessage());
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
