package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import models.Hebergement;
import services.HebergementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HebergementFrontController {

    @FXML private Text txtTotalHebergements;
    @FXML private Text txtTotalCapacite;
    @FXML private Text txtMoyenTarif;
    @FXML private VBox cardsContainer;
    @FXML private javafx.scene.control.TextField tfSearchNom;

    private HebergementService hs = new HebergementService();
    private List<Hebergement> hebergementList;
    private List<Hebergement> currentFilteredList; // Keeps track of currently displayed items

    @FXML
    public void initialize() {
        loadData();
        
        // Real-time search listener
        tfSearchNom.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAndDisplay(newVal);
        });
    }

    public void loadData() {
        try {
            hebergementList = hs.recupTousHebergements();
            currentFilteredList = hebergementList;
            updateStats(hebergementList);
            populateCards(hebergementList);
        } catch (SQLException e) {
            System.err.println("Erreur chargement données: " + e.getMessage());
        }
    }

    private void filterAndDisplay(String query) {
        if (query == null || query.isEmpty()) {
            currentFilteredList = hebergementList;
            updateStats(hebergementList);
            populateCards(hebergementList);
            return;
        }

        String lowerQuery = query.toLowerCase();
        currentFilteredList = hebergementList.stream()
                .filter(h -> h.getNom() != null && h.getNom().toLowerCase().contains(lowerQuery))
                .toList();

        updateStats(currentFilteredList);
        populateCards(currentFilteredList);
    }

    private void updateStats(List<Hebergement> list) {
        int total = list.size();
        int capacite = list.stream().mapToInt(Hebergement::getCapacite).sum();
        double avgTarif = list.stream().mapToDouble(Hebergement::getTarifParNuit).average().orElse(0.0);

        txtTotalHebergements.setText(String.valueOf(total));
        txtTotalCapacite.setText(String.valueOf(capacite));
        txtMoyenTarif.setText(String.format("%.0f DT", avgTarif));
    }

    private void populateCards(List<Hebergement> list) {
        cardsContainer.getChildren().clear();
        for (Hebergement h : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HebergementCard.fxml"));
                Parent card = loader.load();
                
                HebergementCardController controller = loader.getController();
                controller.setData(h, this);
                
                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    public void openForm(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HebergementForm.fxml"));
            Parent root = loader.load();

            HebergementFormController controller = loader.getController();
            // We need a way to refresh this view after form submission.
            // HebergementFormController expects a HebergementController (table-based).
            // I should either refactor it to accept an interface or just mock it.
            
            // Let's create a temporary solution or refactor.
            // For now, I'll refresh data after the modal closes.

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(hebergement == null ? "Ajouter un Hébergement" : "Modifier l'Hébergement");

            Scene scene = new Scene(root);
            // Apply current theme
            scene.getStylesheets().add(getClass().getResource("/css/frontoffice-styles.css").toExternalForm());

            stage.setScene(scene);
            
            // Set parent and hebergement
            controller.setParentController(this);
            controller.setHebergement(hebergement);
            
            stage.showAndWait();
            
            // Refresh data after modal is closed
            loadData();

        } catch (IOException e) {
            System.err.println("Erreur chargement modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MapBoxView.fxml"));
            Parent root = loader.load();

            MapBoxController controller = loader.getController();

            // Build JSON manually to avoid Gson dependency explicitly
            StringBuilder jsonBuilder = new StringBuilder("[");
            if (currentFilteredList != null) {
                for (int i = 0; i < currentFilteredList.size(); i++) {
                    Hebergement h = currentFilteredList.get(i);
                    jsonBuilder.append("{")
                        .append("\"nom\":\"").append(escapeJson(h.getNom())).append("\",")
                        .append("\"type\":\"").append(escapeJson(h.getType())).append("\",")
                        .append("\"adresse\":\"").append(escapeJson(h.getAdresse())).append("\",")
                        .append("\"ville\":\"").append(escapeJson(h.getVille())).append("\",")
                        .append("\"pays\":\"").append(escapeJson(h.getPays())).append("\",")
                        .append("\"tarifParNuit\":").append(h.getTarifParNuit())
                        .append("}");
                    if (i < currentFilteredList.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
            }
            jsonBuilder.append("]");
            controller.setHebergementsData(jsonBuilder.toString());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Carte des Hébergements");

            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Add custom animation for showing
            stage.setOpacity(0.0);
            stage.show();
            
            // Fade in animation for the Stage
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300));
            ft.setNode(scene.getRoot());
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            
            // Fade in for the window itself
            javafx.animation.Timeline timeline = new javafx.animation.Timeline();
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), 
                    new javafx.animation.KeyValue(stage.opacityProperty(), 1.0));
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
            
        } catch (IOException e) {
            System.err.println("Erreur chargement carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }

    @FXML
    private void handleOpenCountrySelector() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CountrySelector.fxml"));
            Parent root = loader.load();

            CountrySelectorController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Choisir un Pays");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/frontoffice-styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement sélecteur pays: " + e.getMessage());
        }
    }

    public void onCountrySelected(String country) {
        if (country == null) {
            loadData(); // Reset
            return;
        }
        
        // Filter by country
        currentFilteredList = hebergementList.stream()
                .filter(h -> country.equalsIgnoreCase(h.getPays()))
                .toList();
        
        updateStats(currentFilteredList);
        populateCards(currentFilteredList);
    }
}
