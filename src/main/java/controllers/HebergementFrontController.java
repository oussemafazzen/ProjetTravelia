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
    @FXML private Text txtMoyenTarif;
    @FXML private VBox cardsContainer;
    @FXML private javafx.scene.control.TextField tfSearchNom;
    
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Text txtCurrentPage;
    @FXML private Text txtTotalPages;

    private HebergementService hs = new HebergementService();
    private List<Hebergement> hebergementList;
    private List<Hebergement> currentFilteredList; // Keeps track of currently displayed items
    
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 3;

    @FXML
    public void initialize() {
        loadData();
        
        // Real-time search listener
        tfSearchNom.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0; // Reset pagination on search
            filterAndDisplay(newVal);
        });
    }

    public void loadData() {
        try {
            hebergementList = hs.recupTousHebergements();
            currentFilteredList = hebergementList;
            updateStats(hebergementList);
            populateCards();
        } catch (SQLException e) {
            System.err.println("Erreur chargement données: " + e.getMessage());
        }
    }

    private void filterAndDisplay(String query) {
        if (query == null || query.isEmpty()) {
            currentFilteredList = hebergementList;
            updateStats(hebergementList);
            populateCards();
            return;
        }

        String lowerQuery = query.toLowerCase();
        currentFilteredList = hebergementList.stream()
                .filter(h -> h.getNom() != null && h.getNom().toLowerCase().contains(lowerQuery))
                .toList();

        updateStats(currentFilteredList);
        populateCards();
    }

    private void updateStats(List<Hebergement> list) {
        int total = list.size();
        double avgTarif = list.stream().mapToDouble(Hebergement::getTarifParNuit).average().orElse(0.0);

        txtTotalHebergements.setText(String.valueOf(total));
        txtMoyenTarif.setText(String.format("%.0f DT", avgTarif));
        
        // Update pagination numbers
        int totalPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        txtCurrentPage.setText(String.valueOf(currentPage + 1));
        txtTotalPages.setText(String.valueOf(totalPages));
        
        // Disable/Enable buttons
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= totalPages - 1);
        
        // Hide pagination if only one page or empty
        btnPrev.getParent().setVisible(total > ITEMS_PER_PAGE);
    }

    private void populateCards() {
        cardsContainer.getChildren().clear();
        
        if (currentFilteredList == null || currentFilteredList.isEmpty()) return;
        
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, currentFilteredList.size());
        
        List<Hebergement> pageItems = currentFilteredList.subList(start, end);
        
        for (Hebergement h : pageItems) {
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
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) currentFilteredList.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateStats(currentFilteredList);
            populateCards();
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateStats(currentFilteredList);
            populateCards();
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
            currentPage = 0;
            loadData(); // Reset
            return;
        }
        
        // Filter by country
        currentPage = 0;
        currentFilteredList = hebergementList.stream()
                .filter(h -> country.equalsIgnoreCase(h.getPays()))
                .toList();
        
        updateStats(currentFilteredList);
        populateCards();
    }
}
