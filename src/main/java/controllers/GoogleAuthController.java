package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import models.Client;
import models.User;
import services.GoogleAuthService;
import services.SecurityLogService;

import org.json.JSONObject;

public class GoogleAuthController {

    @FXML
    private WebView webView;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnCancel;

    private GoogleAuthService googleAuthService = new GoogleAuthService();
    private SecurityLogService securityLogService = new SecurityLogService();

    @FXML
    public void initialize() {
        String authUrl = googleAuthService.getAuthorizationUrl();
        WebEngine engine = webView.getEngine();

        lblStatus.setText("Redirection vers Google...");

        // Listen for URL changes to capture the auth code
        engine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            
            System.out.println("DEBUG Google: current URL = " + newValue);

            // Intercept as soon as it tries to go back to localhost
            if (newValue.startsWith("http://localhost") || newValue.contains("code=")) {
                String code = extractCodeFromUrl(newValue);
                if (code != null) {
                    // IMMEDIATELY stop the engine to prevent XAMPP from loading
                    engine.getLoadWorker().cancel();
                    webView.setVisible(false);
                    
                    lblStatus.setText("Code détecté ! Finalisation de la connexion...");
                    System.out.println("DEBUG Google: Auth code extracted, starting exchange...");
                    handleAuthCode(code);
                }
            }

            // Fallback for desktop flows: authorize app message
            if (newValue.contains("approval?")) {
                lblStatus.setText("Veuillez autoriser l'application dans la fenêtre Google...");
            }
        });

        // Also listen for title changes (for OOB flow, code appears in title)
        engine.titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.startsWith("Success code=")) {
                String code = newValue.replace("Success code=", "").trim();
                handleAuthCode(code);
            }
        });

        engine.load(authUrl);
    }

    private String extractCodeFromUrl(String url) {
        try {
            String[] parts = url.split("code=");
            if (parts.length > 1) {
                String code = parts[1];
                // Remove any additional parameters
                if (code.contains("&")) {
                    code = code.substring(0, code.indexOf("&"));
                }
                
                // CRITICAL FIX: The code in the URL is already percent-encoded (e.g. %2F).
                // We MUST decode it before passing it to the service, which will re-encode it for the POST body.
                String decodedCode = java.net.URLDecoder.decode(code, "UTF-8");
                System.out.println("DEBUG Google: Auth code decoded (" + (code.equals(decodedCode) ? "no change" : "decoded") + ")");
                return decodedCode;
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction code: " + e.getMessage());
        }
        return null;
    }

    private void handleAuthCode(String code) {
        new Thread(() -> {
            try {
                // Exchange code for token
                String accessToken = googleAuthService.exchangeCodeForToken(code);
                if (accessToken == null) {
                    javafx.application.Platform.runLater(() -> 
                        lblStatus.setText("Erreur: impossible d'obtenir le token d'accès.")
                    );
                    return;
                }

                // Get user info
                JSONObject userInfo = googleAuthService.getUserInfo(accessToken);
                if (userInfo == null) {
                    javafx.application.Platform.runLater(() ->
                        lblStatus.setText("Erreur: impossible de récupérer les informations utilisateur.")
                    );
                    return;
                }

                String googleId = userInfo.optString("sub");
                String email = userInfo.optString("email");
                String name = userInfo.optString("name");

                // Find or create user
                Client user = googleAuthService.findOrCreateUser(googleId, email, name);

                if (user != null) {
                    securityLogService.logEvent(user.getId(), "GOOGLE_LOGIN", "Connexion Google pour " + email);

                    javafx.application.Platform.runLater(() -> {
                        lblStatus.setText("Connexion réussie ! Bienvenue " + name);
                        utils.SessionManager.saveSession(user.getEmail());
                        navigateToDashboard(user);
                    });
                } else {
                    javafx.application.Platform.runLater(() ->
                        lblStatus.setText("Erreur: compte bloqué ou erreur de création.")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                    lblStatus.setText("Erreur: " + e.getMessage())
                );
            }
        }).start();
    }

    private void navigateToDashboard(Client user) {
        try {
            String fxmlPath = "/fxml/Dashboard.fxml";
            if (user.getRole() == models.enums.Role.ADMINISTRATEUR) {
                fxmlPath = "/fxml/AdminPanel.fxml";
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            if (fxmlPath.equals("/fxml/Dashboard.fxml")) {
                DashboardController dc = (DashboardController) loader.getController();
                if (dc != null) {
                    dc.initData(user);
                }
            }

            javafx.stage.Stage stage = (javafx.stage.Stage) webView.getScene().getWindow();
            webView.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Erreur navigation: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            webView.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
