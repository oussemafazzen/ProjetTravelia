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

import java.io.IOException;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
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
        switchView(accommodationsView, btnNavHebergement);
    }

    @FXML
    void showActivities(ActionEvent event) {
        switchView(activitiesView, btnNavActivites);
    }

    @FXML
    void showReviews(ActionEvent event) {
        switchView(reviewsView, btnNavAvis);
    }

    private void switchView(VBox view, Button activeButton) {
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        profileView.setVisible(false); profileView.setManaged(false);
        historyView.setVisible(false); historyView.setManaged(false);
        ticketsView.setVisible(false); ticketsView.setManaged(false);
        accommodationsView.setVisible(false); accommodationsView.setManaged(false);
        activitiesView.setVisible(false); activitiesView.setManaged(false);
        reviewsView.setVisible(false); reviewsView.setManaged(false);
        
        view.setVisible(true);
        view.setManaged(true);

        // Dynamically move Currency Converter to Tickets or Accommodations views
        if (currencyConverterContainer != null) {
            ticketsView.getChildren().remove(currencyConverterContainer);
            accommodationsView.getChildren().remove(currencyConverterContainer);
            if (view == ticketsView) {
                if (!ticketsView.getChildren().contains(currencyConverterContainer)) {
                    ticketsView.getChildren().add(currencyConverterContainer);
                }
            } else if (view == accommodationsView) {
                if (!accommodationsView.getChildren().contains(currencyConverterContainer)) {
                    accommodationsView.getChildren().add(currencyConverterContainer);
                }
            }
        }

        // Reset all buttons style
        if (btnNavAccueil != null) {
            btnNavAccueil.getStyleClass().remove("nav-button-active");
            btnNavClients.getStyleClass().remove("nav-button-active");
            btnNavReservations.getStyleClass().remove("nav-button-active");
            btnNavHebergement.getStyleClass().remove("nav-button-active");
            btnNavActivites.getStyleClass().remove("nav-button-active");
            btnNavAvis.getStyleClass().remove("nav-button-active");
            
            // Set active class
            if (activeButton != null) {
                activeButton.getStyleClass().add("nav-button-active");
            }
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
