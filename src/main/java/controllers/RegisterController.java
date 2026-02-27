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
import models.Client;
import models.enums.Role;
import models.enums.Statut;
import services.ClientService;
import utils.EmailService;

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

    private ClientService clientService = new ClientService();
    private EmailService emailService = new EmailService();

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
            String telephone = prefix + " " + telephoneNum;
            
            String nationalite = nationaliteCombo.getValue();
            LocalDate dateNaissanceLocal = dateNaissanceF.getValue();

            // 1. Champs vides
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || 
                telephoneNum.isEmpty() || nationalite == null || dateNaissanceLocal == null) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // 2. Format Email (Regex)
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Format email invalide (ex: test@domaine.com).");
                return;
            }

            // 3. Unicité de l'Email
            if (clientService.emailExists(email)) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Cet email est déjà utilisé.");
                return;
            }

            // 4. Complexité Mot de passe
            String passwordRegex = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
            if (!password.matches(passwordRegex)) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Mot de passe faible (min 8 car., 1 Maj, 1 Chiffre).");
                return;
            }

            // 5. Validité Téléphone (On enlève le controle strict de 8 chiffres, on garde juste presence)
            if (telephoneNum.length() < 4) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Numéro de téléphone trop court.");
                return;
            }

            // 6. Contrôle d'Age (min 18 ans)
            if (java.time.Period.between(dateNaissanceLocal, LocalDate.now()).getYears() < 18) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Vous devez avoir au moins 18 ans.");
                return;
            }

            Date dateNaissance = Date.valueOf(dateNaissanceLocal);

            Client client = new Client(email, password, Role.USER, Statut.ACTIF, nom, prenom, telephone, nationalite, dateNaissance);
            
            clientService.add(client);
            System.out.println("Client ajouté en base : " + email);
            
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Inscription réussie !");
            
            // Send Welcome Email (in background)
            new Thread(() -> emailService.sendWelcomeEmail(email, nom + " " + prenom)).start();
            
            // Clear fields
            nomF.clear(); prenomF.clear(); emailF.clear(); passwordF.clear();
            telephoneF.clear(); nationaliteCombo.setValue(null); dateNaissanceF.setValue(null);
            
        } catch (SQLException e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur Base de Données: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void goToHome(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/Home.fxml"));
            javafx.scene.Parent root = loader.load();
            dateNaissanceF.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}
