package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import models.Client;
import org.json.JSONObject;
import services.ClientService;

import javafx.scene.control.Alert;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;

public class DashboardController {

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblNiveau;

    @FXML
    private Label lblNavUserName;

    @FXML
    private Label lblPoints;

    @FXML
    private ProgressBar progressFidelite;

    @FXML
    private Label lblProgressPercent;

    @FXML
    private Label lblWeather;

    @FXML
    private Label lblCity;

    @FXML
    private StackPane mainStackPane;

    @FXML
    private VBox dashboardView;

    @FXML
    private VBox profileView;

    @FXML
    private VBox historyView;

    @FXML
    private VBox ticketsView;

    @FXML
    private VBox accommodationsView;

    @FXML
    private VBox activitiesView;

    @FXML
    private VBox reviewsView;

    @FXML
    private Button btnNavAccueil;

    @FXML
    private Button btnNavClients;

    @FXML
    private Button btnNavReservations;

    @FXML
    private Button btnNavHebergement;

    @FXML
    private Button btnNavActivites;

    @FXML
    private Button btnNavAvis;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtNationalite;

    @FXML
    private DatePicker dpDateNaissance;

    @FXML
    private Label lblProfileMessage;

    // vboxHistory removed to prevent NPE since it's removed from layout

    @FXML
    private ComboBox<String> comboFrom;

    @FXML
    private ComboBox<String> comboTo;

    @FXML
    private TextField txtAmount;

    @FXML
    private Label lblConvertResult;
    
    @FXML
    private VBox currencyConverterContainer;

    private Client currentClient;
    private ClientService clientService = new ClientService();

    public void initData(Client client) {
        this.currentClient = client;
        initCurrencies();
        refreshView();
        switchView(dashboardView, btnNavAccueil);
    }

    private void initCurrencies() {
        if (comboFrom == null || comboTo == null) return; // Currency converter not in FXML
        javafx.collections.ObservableList<String> codes = javafx.collections.FXCollections.observableArrayList("EUR", "USD", "GBP", "TND", "CAD", "JPY");
        comboFrom.setItems(codes);
        comboTo.setItems(codes);
        comboFrom.setValue("EUR");
        comboTo.setValue("TND");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            utils.SessionManager.cleanSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();
            lblNavUserName.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        switchView(dashboardView, btnNavAccueil);
    }

    @FXML
    void showProfile(ActionEvent event) {
        switchView(profileView, btnNavClients);
        if (currentClient != null) {
            txtNom.setText(currentClient.getNom());
            txtPrenom.setText(currentClient.getPrenom());
            txtEmail.setText(currentClient.getEmail());
            txtTelephone.setText(currentClient.getTelephone());
            txtNationalite.setText(currentClient.getNationalite());
            if (currentClient.getDate_naissance() != null) {
                Date utilDate = currentClient.getDate_naissance();
                dpDateNaissance.setValue(new java.sql.Date(utilDate.getTime()).toLocalDate());
            }
        }
    }

    @FXML
    void showHistory(ActionEvent event) {
        // Obsolete view in new Figma layout, keeping method signature
        switchView(historyView, null);
    }

    @FXML
    void showTickets(ActionEvent event) {
        switchView(ticketsView, btnNavReservations);
    }

    @FXML
    void showAccommodations(ActionEvent event) {
        System.out.println("Navigation: showAccommodations called");
        try {
            if (accommodationsView == null) {
                System.err.println("Erreur: accommodationsView non injecté (null)");
                return;
            }
            
            // If it's the first time, load the real front-office view
            // We check if it has the placeholder label or if it's "mostly" empty
            boolean isPlaceholder = false;
            if (accommodationsView.getChildren().isEmpty()) {
                isPlaceholder = true;
            } else if (accommodationsView.getChildren().get(0) instanceof Label) {
                Label first = (Label) accommodationsView.getChildren().get(0);
                if (first.getText().equals("Hébergement & Logement") || first.getText().contains("offre")) {
                    isPlaceholder = true;
                }
            }

            if (isPlaceholder) {
                System.out.println("Chargement de HebergementFrontView.fxml...");
                accommodationsView.getChildren().clear(); 
                
                URL fxmlUrl = getClass().getResource("/views/HebergementFrontView.fxml");
                if (fxmlUrl == null) throw new IOException("FXML /views/HebergementFrontView.fxml introuvable");
                
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent view = loader.load();
                accommodationsView.getChildren().add(view);
                System.out.println("HebergementFrontView chargé.");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement Hébergement: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Module Hébergement", e.getMessage());
        }
        switchView(accommodationsView, btnNavHebergement);
    }

    @FXML
    void showActivities(ActionEvent event) {
        System.out.println("Navigation: showActivities called");
        try {
             if (activitiesView != null && (activitiesView.getChildren().isEmpty() || activitiesView.getChildren().get(0) instanceof Label)) {
                // If it's just the placeholder VBox from FXML, we can load a PlaceholderView or real content
                // For now, let's just make sure the container is visible and has something
                if (activitiesView.getChildren().size() <= 1) { // Title label + maybe placeholder VBox
                     // Keep existing FXML content or load something else
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        switchView(activitiesView, btnNavActivites);
    }

    private void showErrorAlert(String module, String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de Navigation");
        alert.setHeaderText("Impossible de charger le module " + module);
        alert.setContentText("Détail: " + error);
        alert.show();
    }

    @FXML
    void showReviews(ActionEvent event) {
        switchView(reviewsView, btnNavAvis);
    }

    // Rename cards handlers to avoid ambiguity with ActionEvent methods (solves "random behavior" on run)
    @FXML void onModuleCardClickProfile(javafx.scene.input.MouseEvent event) { showProfile((ActionEvent) null); }
    @FXML void onModuleCardClickTickets(javafx.scene.input.MouseEvent event) { showTickets((ActionEvent) null); }
    @FXML void onModuleCardClickAccommodations(javafx.scene.input.MouseEvent event) { showAccommodations((ActionEvent) null); }
    @FXML void onModuleCardClickActivities(javafx.scene.input.MouseEvent event) { showActivities((ActionEvent) null); }
    @FXML void onModuleCardClickReviews(javafx.scene.input.MouseEvent event) { showReviews((ActionEvent) null); }

    private void switchView(VBox view, Button activeButton) {
        if (view == null) return;
        
        System.out.println("SWITCH: Activating view " + view.getId());
        
        // Use a safe list to hide all view containers
        VBox[] allViews = {dashboardView, profileView, historyView, ticketsView, accommodationsView, activitiesView, reviewsView};
        for (VBox v : allViews) {
            if (v != null) {
                v.setVisible(false);
                v.setManaged(false);
            }
        }
        
        view.setVisible(true);
        view.setManaged(true);
        view.toFront(); // Ensure it's on top of the StackPane

        // Move Currency Converter
        if (currencyConverterContainer != null) {
            if (ticketsView != null) ticketsView.getChildren().remove(currencyConverterContainer);
            if (accommodationsView != null) accommodationsView.getChildren().remove(currencyConverterContainer);
            
            if (view == ticketsView) {
                ticketsView.getChildren().add(currencyConverterContainer);
            } else if (view == accommodationsView) {
                accommodationsView.getChildren().add(currencyConverterContainer);
            }
        }

        // Reset all buttons style
        Button[] allNavBtns = {btnNavAccueil, btnNavClients, btnNavReservations, btnNavHebergement, btnNavActivites, btnNavAvis};
        for (Button btn : allNavBtns) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-button-active");
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #334155;");
            }
        }
        
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");
            activeButton.setStyle("-fx-background-color: linear-gradient(to right, #0156ff, #00d1ff); -fx-text-fill: white;");
        }
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        if (currentClient != null) {
            try {
                currentClient.setNom(txtNom.getText());
                currentClient.setPrenom(txtPrenom.getText());
                currentClient.setEmail(txtEmail.getText());
                currentClient.setTelephone(txtTelephone.getText());
                currentClient.setNationalite(txtNationalite.getText());
                if (dpDateNaissance.getValue() != null) {
                    currentClient.setDate_naissance(java.sql.Date.valueOf(dpDateNaissance.getValue()));
                }
                clientService.update(currentClient);
                lblProfileMessage.setStyle("-fx-text-fill: green;");
                lblProfileMessage.setText("Profil mis à jour avec succès !");
                refreshView();
            } catch (SQLException e) {
                lblProfileMessage.setStyle("-fx-text-fill: red;");
                lblProfileMessage.setText("Erreur mise à jour: " + e.getMessage());
            }
        }
    }

    @FXML
    void exportPDF(ActionEvent event) {
        if (currentClient != null) {
            String path = System.getProperty("user.home") + "/Desktop/Fiche_Client_" + currentClient.getNom() + ".pdf";
            clientService.exportClientPdf(currentClient, path);
            lblMessage.setText("PDF exporté sur le bureau.");
        }
    }

    private void refreshView() {
        if (currentClient != null) {
            String nom = capitalize(currentClient.getNom());
            String prenom = capitalize(currentClient.getPrenom());
            String fullName = prenom + " " + nom;
            
            lblNavUserName.setText(fullName);
            lblPoints.setText(String.valueOf(currentClient.getPoints_fidelite()));
            lblNiveau.setText(currentClient.getNiveau_fidelite().toString());
            
            // Progress Bar Logic
            updateProgressBar();
            updateWeather();
        }
    }

    private void updateWeather() {
        new Thread(() -> {
            JSONObject loc = utils.GeoLocationService.getUserLocation();
            if (loc != null && loc.has("city")) {
                String city = loc.getString("city");
                double lat = loc.getDouble("lat");
                double lon = loc.getDouble("lon");
                String weather = utils.WeatherService.getWeather(lat, lon);
                javafx.application.Platform.runLater(() -> {
                    lblCity.setText(city);
                    lblWeather.setText(weather);
                });
            } else {
                javafx.application.Platform.runLater(() -> lblWeather.setText("Localisation off"));
            }
        }).start();
    }

    private void updateProgressBar() {
        int points = currentClient.getPoints_fidelite();
        double progress = 0;
        int nextLevelPoints = 0;
        
        if (points < 1000) {
            nextLevelPoints = 1000;
            progress = (double) points / 1000;
        } else if (points < 5000) {
            nextLevelPoints = 5000;
            progress = (double) (points - 1000) / 4000;
        } else {
            progress = 1.0;
        }
        
        progressFidelite.setProgress(progress);
        lblProgressPercent.setText((int)(progress * 100) + "%");
    }

    @FXML
    void simulerAchat(ActionEvent event) {
        if (currentClient != null) {
            try {
                clientService.addPoints(currentClient.getId(), 100);
                // Refresh from DB
                this.currentClient = clientService.getById(currentClient.getId());
                refreshView();
                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("Achat simulé ! +100 points.");
            } catch (SQLException e) {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleConvert(ActionEvent event) {
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            String from = comboFrom.getValue();
            String to = comboTo.getValue();
            
            lblConvertResult.setText("...");
            
            new Thread(() -> {
                double result = utils.CurrencyService.convert(amount, from, to);
                javafx.application.Platform.runLater(() -> {
                    if (result != -1) {
                        lblConvertResult.setText(String.format("%.2f %s", result, to));
                    } else {
                        lblConvertResult.setText("Erreur API");
                    }
                });
            }).start();
        } catch (NumberFormatException e) {
            lblConvertResult.setText("Montant invalide");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
