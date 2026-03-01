package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.PasswordResetToken;
import services.TokenService;
import services.SecurityLogService;
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
    private TextField codeF;

    @FXML
    private VBox passwordBox;

    @FXML
    private Button btnVerify;

    @FXML
    private Button btnReset;

    private String userEmail;
    private String expectedToken;
    private UserService userService = new UserService();
    private TokenService tokenService = new TokenService();
    private SecurityLogService securityLogService = new SecurityLogService();

    public void setData(String email, String token) {
        this.userEmail = email;
        this.expectedToken = token;
        lblUserEmail.setText("Email : " + email);
    }

    @FXML
    void handleVerifyCode(ActionEvent event) {
        String enteredCode = codeF.getText();
        if (enteredCode.isEmpty()) {
            lblMessage.setText("Veuillez saisir le code");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            PasswordResetToken validToken = tokenService.validateToken(enteredCode);
            if (validToken == null) {
                lblMessage.setText("Code incorrect ou expiré.");
                lblMessage.setStyle("-fx-text-fill: red;");
            } else {
                // Success: Show password fields
                lblMessage.setText("Code valide ! Saisissez votre nouveau mot de passe.");
                lblMessage.setStyle("-fx-text-fill: green;");
                codeF.setDisable(true);
                btnVerify.setVisible(false);
                btnVerify.setManaged(false);
                passwordBox.setVisible(true);
                passwordBox.setManaged(true);
            }
        } catch (SQLException e) {
            lblMessage.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String enteredCode = codeF.getText();
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

        // Password complexity check
        String passwordRegex = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
        if (!newPass.matches(passwordRegex)) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Mot de passe faible (min 8 car., 1 Maj, 1 Chiffre).");
            return;
        }

        try {
            userService.updatePassword(userEmail, newPass);

            // Invalidate the token after successful use
            tokenService.invalidateToken(enteredCode);

            // Log the event
            models.User user = userService.getUserByEmail(userEmail);
            if (user != null) {
                securityLogService.logEvent(user.getId(), "PASSWORD_RESET_SUCCESS", "Mot de passe réinitialisé pour " + userEmail);
            }

            // Clean up expired tokens
            tokenService.deleteExpiredTokens();

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
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            btnReset.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
