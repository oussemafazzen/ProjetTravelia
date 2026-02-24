package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import services.UserService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.IOException;

public class LoginController {

    @FXML
    private Button btnLogin;

    @FXML
    private TextField emailF;

    @FXML
    private Label lblMessage;

    @FXML
    private PasswordField passwordF;

    @FXML
    private javafx.scene.control.Hyperlink hlForgotPassword;

    private UserService userService = new UserService();
    private int localFailedAttempts = 0;
    private boolean isLockedOut = false;
    private int remainingCooldown = 30;

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailF.getText();
        String password = passwordF.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Remplir tous les champs");
            return;
        }

        if (isLockedOut) {
            lblMessage.setText("Trop de tentatives. Réessayez dans " + remainingCooldown + "s");
            return;
        }

        try {
            User user = userService.login(email, password);
            
            if (user != null) {
                localFailedAttempts = 0;
                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("Connexion réussie ! Bienvenue " + user.getEmail());
                
                String fxmlPath = "";
                models.enums.Role role = user.getRole();
                
                if (role == models.enums.Role.ADMINISTRATEUR) {
                    fxmlPath = "/fxml/AdminPanel.fxml";
                } else if (role == models.enums.Role.USER || role == models.enums.Role.CLIENT) {
                    fxmlPath = "/fxml/Dashboard.fxml";
                } else {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Rôle inconnu: " + role);
                    return;
                }

                java.net.URL location = getClass().getResource(fxmlPath);
                if (location == null) {
                   lblMessage.setStyle("-fx-text-fill: red;");
                   lblMessage.setText("Fichier introuvable: " + fxmlPath);
                   return;
                }

                javafx.scene.Parent root;
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(location);
                    root = loader.load();
                    
                    if (fxmlPath.equals("/fxml/Dashboard.fxml") && user instanceof models.Client) {
                        controllers.DashboardController dc = loader.getController();
                        if (dc != null) {
                            dc.initData((models.Client) user);
                        }
                    }
                } catch (Exception e) {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Erreur chargement interface: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                
                utils.SessionManager.saveSession(user.getEmail());
                
                // Maximize on redirect
                javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
                btnLogin.getScene().setRoot(root);
                stage.setMaximized(true);
            } else {
                localFailedAttempts++;
                lblMessage.setStyle("-fx-text-fill: red;");
                if (localFailedAttempts >= 3) {
                    startLockout();
                } else {
                    lblMessage.setText("Email ou mot de passe incorrect (" + (3 - localFailedAttempts) + " tentatives restantes)");
                }
            }
        } catch (java.sql.SQLException e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur Base de Données: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur Inattendue: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startLockout() {
        isLockedOut = true;
        btnLogin.setDisable(true);
        remainingCooldown = 30;
        lblMessage.setText("Trop de tentatives. Réessayez dans " + remainingCooldown + "s");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingCooldown--;
            if (remainingCooldown <= 0) {
                isLockedOut = false;
                btnLogin.setDisable(false);
                localFailedAttempts = 0;
                lblMessage.setText("");
            } else {
                lblMessage.setText("Trop de tentatives. Réessayez dans " + remainingCooldown + "s");
            }
        }));
        timeline.setCycleCount(30);
        timeline.play();
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ForgotPassword.fxml"));
            javafx.scene.Parent root = loader.load();
            btnLogin.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToHome(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            javafx.scene.Parent root = loader.load();
            btnLogin.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}
