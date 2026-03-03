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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import models.Client;
import org.json.JSONObject;
import services.ClientService;
import utils.GeoLocationService;
import utils.WeatherService;
import utils.CurrencyService;

import javafx.scene.control.Alert;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;

import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    private Label lblAvatarInitial;

    @FXML
    private Label lblLevelMini;

    @FXML
    private VBox profileDropdown;

    @FXML
    private HBox profileAvatarContainer;

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
    private MenuButton userMenu;

    @FXML
    private Button btnNavReservations;

    @FXML
    private Button btnNavHebergement;

    @FXML
    private Button btnNavActivites;

    @FXML
    private Button btnNavAvis;

    @FXML
    private Button btnTabVols;


    @FXML
    private Button btnTabVoitures;

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

    @FXML
    private VBox flightSearchContainer;

    // vboxHistory removed to prevent NPE since it's removed from layout

    @FXML
    private ComboBox<String> comboFrom;

    @FXML
    private ComboBox<String> comboTo;

    @FXML
    private TextField txtFrom;

    @FXML
    private TextField txtTo;



    @FXML
    private StackPane heroSection;

    @FXML
    private DatePicker dateFlight;

    @FXML
    private Button btnContinuer;

    @FXML
    private Button btnRechercherVols;


    @FXML
    private TextField txtAmount;

    @FXML
    private Label lblConvertResult;
    
    @FXML
    private VBox currencyConverterContainer;

    private Client currentClient;
    private ClientService clientService = new ClientService();

    public void initialize() {
        // Setup DatePicker format to dd/MM/yyyy to prevent parsing errors when typing
        if (dateFlight != null) {
            String pattern = "dd/MM/yyyy";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            dateFlight.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return formatter.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        try {
                            return LocalDate.parse(string, formatter);
                        } catch (Exception e) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            });
            dateFlight.setPromptText(pattern.toLowerCase());
        }
    }

    public void initData(Client client) {
        this.currentClient = client;
        initialize(); // Ensure converter is set
        
        // Initialize SessionContext for other modules (like Reservations) to know who is logged in
        if (client != null) {
            utils.SessionContext.loginAsClient(client.getId());
        }
        
        initCurrencies();
        refreshView();
        switchView(dashboardView, btnNavAccueil);

        // Setup click-away for profile dropdown
        Platform.runLater(() -> {
            if (lblNavUserName.getScene() != null) {
                lblNavUserName.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                    if (profileDropdown != null && profileDropdown.isVisible()) {
                        double x = event.getSceneX();
                        double y = event.getSceneY();
                        
                        Bounds dropdownBounds = profileDropdown.localToScene(profileDropdown.getBoundsInLocal());
                        Bounds avatarBounds = profileAvatarContainer.localToScene(profileAvatarContainer.getBoundsInLocal());
                        
                        if (!dropdownBounds.contains(x, y) && !avatarBounds.contains(x, y)) {
                            hideProfileDropdown();
                        }
                    }
                });
            }
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
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
        switchView(profileView, null);
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
    void showReservations(ActionEvent event) {
        // This is the entry point for the "Réservations" navbar tab
        // It should show the flight search interface
        if (flightSearchContainer != null) {
            flightSearchContainer.setVisible(true);
            flightSearchContainer.setManaged(true);
        }
        // Remove any loaded reservation list or flight results
        ticketsView.getChildren().removeIf(node -> !"heroSection".equals(node.getId()) && !"flightSearchContainer".equals(node.getId()) && ! (node instanceof StackPane && ((StackPane)node).getStyleClass().contains("booking-hero")));
        
        // Ensure hero section is visible
        showSearchInterface();
        
        switchView(ticketsView, btnNavReservations);
    }

    @FXML
    public controllers.ReservationsController showReservationsList(ActionEvent event) {
        controllers.ReservationsController resCtrl = null;
        // This is for "Mes réservations" in the profile dropdown
        try {
            if (ticketsView != null) {
                // Hide flight search and hero background
                if (flightSearchContainer != null) {
                    flightSearchContainer.setVisible(false);
                    flightSearchContainer.setManaged(false);
                }
                if (heroSection != null) {
                    heroSection.setVisible(false);
                    heroSection.setManaged(false);
                }
                
                // Remove custom flight results if any
                ticketsView.getChildren().removeIf(node -> "activeFlightResults".equals(node.getId()));

                // Load the list if not already there or if we want to refresh
                System.out.println("Chargement de ReservationsFrontView.fxml pour la liste...");
                
                URL fxmlUrl = getClass().getResource("/views/ReservationsFrontView.fxml");
                if (fxmlUrl == null) throw new IOException("FXML /views/ReservationsFrontView.fxml introuvable");
                
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent view = loader.load();
                resCtrl = loader.getController();
                resCtrl.setMainController(this); // Set the controller here
                view.setId("reservationsListView");
                
                // Add to ticketsView
                ticketsView.getChildren().add(view);
                System.out.println("ReservationsFrontView chargé.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Module Réservations", e.getMessage());
        }
        switchView(ticketsView, btnNavReservations);
        return resCtrl;
    }

    public void triggerNewReservation(models.Billet flightBillet, String destination) {
        controllers.ReservationsController resCtrl = showReservationsList(null);
        if (resCtrl != null) {
            // Pass the search query to the reservations controller to dynamically show "Billet de X vers Y"
            String route = "Billet";
            if (txtFrom != null && txtTo != null && !txtFrom.getText().isEmpty()) {
                route = "Billet de " + txtFrom.getText() + " vers " + txtTo.getText();
            }
            resCtrl.setCustomBilletTitle(route);
            resCtrl.setPreSelectedDestination(destination);

            javafx.application.Platform.runLater(() -> {
                resCtrl.openNewReservationDialog(flightBillet);
            });
        }
    }

    @FXML
    void handleBookingTabChange(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        // Reset all tabs
        btnTabVols.getStyleClass().remove("booking-tab-active");
        btnTabVoitures.getStyleClass().remove("booking-tab-active");
        
        // Activate clicked tab
        if (!clickedButton.getStyleClass().contains("booking-tab-active")) {
            clickedButton.getStyleClass().add("booking-tab-active");
        }
        
        // Reset search state when tab changes
        btnContinuer.setVisible(true);
        btnRechercherVols.setVisible(false);
        
        System.out.println("Booking Tab Changed: " + clickedButton.getText());
    }

    @FXML
    void handleContinuerClick(ActionEvent event) {
        btnContinuer.setVisible(false);
        btnRechercherVols.setVisible(true);
        System.out.println("Continuer clicked -> Show Rechercher des vols");
    }

    @FXML
    void handleSearchFlightsClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FlightResultsView.fxml"));
            Parent resultsView = loader.load();
            
            FlightResultsController controller = loader.getController();
            controller.setMainController(this);
            
            // Pass real data
            String from = txtFrom != null ? txtFrom.getText() : "Tunis (TUN)";
            String to = txtTo != null ? txtTo.getText() : "Paris (PAR)";
            String date = dateFlight != null && dateFlight.getValue() != null ? dateFlight.getValue().toString() : java.time.LocalDate.now().plusDays(5).toString();
            
            controller.setRouteInfo(from, to, date);
            
            // Set ID for the results node for easy removal
            resultsView.setId("activeFlightResults");
            
            // CRITICAL: Ensure ticketsView is visible
            ticketsView.setVisible(true);
            ticketsView.setManaged(true);
            
            // Hide the ENTIRE search UI but keep ticketsView children clean for results
            if (heroSection != null) {
                heroSection.setVisible(false);
                heroSection.setManaged(false);
            }
            
            // Hide suggestions too if we want a clean standalone view
            // (Assuming we want to hide everything in ticketsView except the results)
            for (Node node : ticketsView.getChildren()) {
                if (node != resultsView) {
                    node.setVisible(false);
                    node.setManaged(false);
                }
            }
            
            ticketsView.getChildren().add(resultsView);
            resultsView.setVisible(true);
            resultsView.setManaged(true);
            
            System.out.println("Search Flights clicked -> FXML Loaded, Swapped, and Visible");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FlightResultsView.fxml");
        }
    }

    public void showFlightResultsIntegrated(String from, String to, String date, java.util.function.Consumer<models.Billet> callback, Runnable onBack) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FlightResultsView.fxml"));
            Parent resultsView = loader.load();
            
            FlightResultsController controller = loader.getController();
            controller.setMainController(this);
            
            if (onBack != null) {
                controller.setOnBackAction(onBack);
            } else {
                controller.hideBackButton();
            }
            
            controller.setOnBilletSelectedCallback(callback);
            controller.setRouteInfo(from, to, date);
            
            resultsView.setId("activeFlightResults");
            ticketsView.setVisible(true);
            ticketsView.setManaged(true);
            
            if (heroSection != null) {
                heroSection.setVisible(false);
                heroSection.setManaged(false);
            }
            
            for (Node node : ticketsView.getChildren()) {
                if (node != resultsView) {
                    node.setVisible(false);
                    node.setManaged(false);
                }
            }
            
            ticketsView.getChildren().add(resultsView);
            resultsView.setVisible(true);
            resultsView.setManaged(true);
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void showSearchInterface() {
        // 1. Remove the results node
        ticketsView.getChildren().removeIf(node -> "activeFlightResults".equals(node.getId()));
        
        // 2. Restore all nodes in ticketsView
        for (Node node : ticketsView.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }
        
        // 3. Specifically ensure heroSection is visible
        if (heroSection != null) {
            heroSection.setVisible(true);
            heroSection.setManaged(true);
        }
        
        // 4. Reset buttons to initial "Continuer" state
        btnContinuer.setVisible(true);
        btnRechercherVols.setVisible(false);
        
        System.out.println("Returned to Search Interface");
    }

    @FXML
    void handleResultCategoryClick(javafx.scene.input.MouseEvent event) {
        // Simple logic to switch active style between result categories
        VBox clickedBox = (VBox) event.getSource();
        HBox parent = (HBox) clickedBox.getParent();
        
        for (javafx.scene.Node node : parent.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("result-option-active");
            }
        }
        
        clickedBox.getStyleClass().add("result-option-active");
        System.out.println("Category selected: " + ((Label)clickedBox.getChildren().get(0)).getText());
    }

    private boolean isShowingPlaceholder(Parent root) {
        if (root instanceof Label && ((Label) root).getText().contains("Liste des réservations")) {
            return true;
        }
        if (root instanceof javafx.scene.layout.Pane) {
            for (Node child : ((javafx.scene.layout.Pane) root).getChildren()) {
                if (child instanceof Parent && isShowingPlaceholder((Parent) child)) {
                    return true;
                }
            }
        }
        return false;
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

    // Dropdown Item Handlers
    @FXML void onDropdownProfileClick(javafx.scene.input.MouseEvent event) { 
        hideProfileDropdown();
        showProfile((ActionEvent) null); 
    }
    
    @FXML void onDropdownTicketsClick(javafx.scene.input.MouseEvent event) { 
        hideProfileDropdown();
        showReservationsList((ActionEvent) null); 
    }
    
    @FXML void onDropdownLogoutClick(javafx.scene.input.MouseEvent event) { 
        hideProfileDropdown();
        handleLogout((ActionEvent) null); 
    }

    // Rename cards handlers...
    @FXML void onModuleCardClickProfile(javafx.scene.input.MouseEvent event) { showProfile((ActionEvent) null); }
    @FXML void toggleProfileDropdown(javafx.scene.input.MouseEvent event) {
        if (profileDropdown.isVisible()) {
            hideProfileDropdown();
        } else {
            showProfileDropdown();
        }
    }

    private void showProfileDropdown() {
        profileDropdown.toFront();
        profileDropdown.setManaged(true);
        profileDropdown.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), profileDropdown);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideProfileDropdown() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), profileDropdown);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            profileDropdown.setVisible(false);
            profileDropdown.setManaged(false);
        });
        ft.play();
    }
    @FXML void onModuleCardClickTickets(javafx.scene.input.MouseEvent event) { showReservationsList((ActionEvent) null); }
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
        Button[] allNavBtns = {btnNavAccueil, btnNavReservations, btnNavHebergement, btnNavActivites, btnNavAvis};
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
            lblAvatarInitial.setText(fullName.substring(0, 1).toUpperCase());
            lblLevelMini.setText(currentClient.getNiveau_fidelite().toString() + " Genius");
            
            lblPoints.setText(String.valueOf(currentClient.getPoints_fidelite()));
            lblNiveau.setText(currentClient.getNiveau_fidelite().toString());
            
            // Progress Bar Logic
            updateProgressBar();
            updateWeather();
        }
    }

    private void updateWeather() {
        new Thread(() -> {
            JSONObject loc = GeoLocationService.getUserLocation();
            if (loc != null && loc.has("city")) {
                String city = loc.getString("city");
                double lat = loc.getDouble("lat");
                double lon = loc.getDouble("lon");
                String weather = WeatherService.getWeather(lat, lon);
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
                double result = CurrencyService.convert(amount, from, to);
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
