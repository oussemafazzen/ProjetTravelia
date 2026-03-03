package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Client;
import models.enums.Role;
import models.enums.Statut;
import services.ClientService;
import services.FaceRecognitionService;
import services.SecurityLogService;
import services.TokenService;
import utils.EmailService;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.sql.SQLException;
import java.util.function.UnaryOperator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import java.sql.Date;
import java.time.LocalDate;

public class RegisterController {

    @FXML
    private DatePicker dateNaissanceF;

    @FXML
    private TextField emailF;

    @FXML
    private ComboBox<String> nationaliteCombo;

    @FXML
    private ComboBox<String> countryCodeCombo;

    @FXML
    private TextField nomF;

    @FXML
    private PasswordField passwordF;

    @FXML
    private TextField prenomF;

    @FXML
    private TextField telephoneF;
    
    @FXML
    private Label lblMessage;

    @FXML
    private Label lblFaceStatus;

    @FXML
    private VBox mainForm;

    @FXML
    private VBox verificationBox;

    @FXML
    private TextField codeF;

    @FXML
    private Button btnRegister;

    private ClientService clientService = new ClientService();
    private EmailService emailService = new EmailService();
    private FaceRecognitionService faceRecognitionService = new FaceRecognitionService();
    private SecurityLogService securityLogService = new SecurityLogService();
    private TokenService tokenService = new TokenService();

    private File selectedFaceImage = null;
    private String detectedFaceToken = null;
    private String generatedCode = null;
    private Client pendingUser = null;

    @FXML
    public void initialize() {
        // Initialize Country List - Using Java Locale for all countries
        List<String> countries = Arrays.stream(Locale.getISOCountries())
                .map(code -> new Locale("", code).getDisplayCountry(Locale.FRENCH))
                .sorted()
                .collect(Collectors.toList());
        nationaliteCombo.setItems(FXCollections.observableArrayList(countries));

        // Initialize Common Phone Codes
        ObservableList<String> codes = FXCollections.observableArrayList(
                "+216", "+33", "+1", "+44", "+49", "+39", "+34", "+212", "+213", "+20", "+966", "+971"
        );
        countryCodeCombo.setItems(codes);
        countryCodeCombo.setValue("+216"); // Default value

        // Digits-only filter for telephone field
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        telephoneF.setTextFormatter(new TextFormatter<>(filter));

        // Auto-detect nationality
        new Thread(() -> {
            String country = utils.GeoLocationService.getCountry();
            javafx.application.Platform.runLater(() -> {
                if (nationaliteCombo.getItems().contains(country)) {
                    nationaliteCombo.setValue(country);
                }
            });
        }).start();

        // Initialize face status label
        if (lblFaceStatus != null) {
            lblFaceStatus.setText("");
        }
    }

    /**
     * Permet à l'utilisateur d'enregistrer une image de son visage.
     */
    @FXML
    void handleCaptureFace(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez une photo de votre visage");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(dateNaissanceF.getScene().getWindow());

        if (file != null) {
            selectedFaceImage = file;
            if (lblFaceStatus != null) {
                lblFaceStatus.setStyle("-fx-text-fill: #2196F3;");
                lblFaceStatus.setText("Détection du visage en cours...");
            }

            // Detect face in background
            new Thread(() -> {
                String faceToken = faceRecognitionService.detectFace(file);
                javafx.application.Platform.runLater(() -> {
                    if (faceToken != null) {
                        detectedFaceToken = faceToken;
                        if (lblFaceStatus != null) {
                            lblFaceStatus.setStyle("-fx-text-fill: green;");
                            lblFaceStatus.setText("✓ Visage détecté avec succès !");
                        }
                    } else {
                        detectedFaceToken = null;
                        if (lblFaceStatus != null) {
                            String errorMsg = faceRecognitionService.getLastErrorMessage();
                            lblFaceStatus.setStyle("-fx-text-fill: red;");
                            lblFaceStatus.setText("✗ " + (errorMsg.isEmpty() ? "Aucun visage détecté. Réessayez." : errorMsg));
                        }
                    }
                });
            }).start();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            String nom = nomF.getText();
            String prenom = prenomF.getText();
            String email = emailF.getText();
            String password = passwordF.getText();
            String telephoneNum = telephoneF.getText();
            String prefix = countryCodeCombo.getValue();
            String telephone = (prefix != null ? prefix : "") + " " + (telephoneNum != null ? telephoneNum : "");
            
            String nationalite = nationaliteCombo.getValue();
            LocalDate dateNaissanceLocal = dateNaissanceF.getValue();

            // 1. Validation de base
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || 
                telephoneNum.isEmpty() || nationalite == null || dateNaissanceLocal == null) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Veuillez remplir tous les champs.");
                return;
            }

            // 2. Format Email
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Format email invalide.");
                return;
            }

            // 3. Unicité
            if (clientService.emailExists(email)) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Email déjà utilisé.");
                return;
            }

            Date dateNaissance = Date.valueOf(dateNaissanceLocal);
            pendingUser = new Client(email, password, Role.USER, Statut.ACTIF, nom, prenom, telephone, nationalite, dateNaissance);
            
            // Génération du code
            generatedCode = String.format("%06d", new java.util.Random().nextInt(999999));
            
            lblMessage.setStyle("-fx-text-fill: #2196F3;");
            lblMessage.setText("Envoi du code...");

            new Thread(() -> {
                try {
                    emailService.sendConfirmationEmail(email, nom + " " + prenom, generatedCode);
                    javafx.application.Platform.runLater(() -> {
                        lblMessage.setStyle("-fx-text-fill: green;");
                        lblMessage.setText("Code envoyé à " + email);
                        
                        mainForm.setVisible(false);
                        mainForm.setManaged(false);
                        verificationBox.setVisible(true);
                        verificationBox.setManaged(true);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        lblMessage.setStyle("-fx-text-fill: red;");
                        lblMessage.setText("Erreur d'envoi: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void handleVerifyAndFinalize(ActionEvent event) {
        String enteredCode = codeF.getText();
        if (enteredCode == null || !enteredCode.equals(generatedCode)) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Code incorrect.");
            return;
        }

        try {
            clientService.add(pendingUser);
            
            // Initialiser la session
            utils.SessionManager.saveSession(pendingUser.getEmail());

            if (detectedFaceToken != null && pendingUser.getId() > 0) {
                faceRecognitionService.storeFaceData(pendingUser.getId(), detectedFaceToken, "");
                securityLogService.logEvent(pendingUser.getId(), "FACE_REGISTERED", "Visage enregistré");
            }

            securityLogService.logEvent(pendingUser.getId(), "REGISTRATION_COMPLETE", "Inscription terminée");

            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Inscription réussie ! Connexion...");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
                            Parent root = loader.load();
                            
                            // Passer l'utilisateur au contrôleur du Dashboard
                            DashboardController controller = loader.getController();
                            controller.initData(pendingUser);
                            
                            lblMessage.getScene().setRoot(root);
                        } catch (IOException e) {
                            e.printStackTrace();
                            lblMessage.setText("Erreur redirection: " + e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {}
            }).start();

        } catch (SQLException e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur DB: " + e.getMessage());
        }
    }

    @FXML
    void handleResendCode(ActionEvent event) {
        if (pendingUser != null) {
            handleRegister(null);
        }
    }

    @FXML
    void handleGoogleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GoogleAuth.fxml"));
            Parent root = loader.load();
            dateNaissanceF.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            dateNaissanceF.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
