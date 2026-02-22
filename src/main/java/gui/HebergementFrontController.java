package gui;

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
import java.util.List;

public class HebergementFrontController {

    @FXML private Text txtTotalHebergements;
    @FXML private Text txtTotalCapacite;
    @FXML private Text txtMoyenTarif;
    @FXML private VBox cardsContainer;

    private HebergementService hs = new HebergementService();
    private List<Hebergement> hebergementList;

    @FXML
    public void initialize() {
        loadData();
    }

    public void loadData() {
        hebergementList = hs.getAll();
        updateStats();
        populateCards();
    }

    private void updateStats() {
        int total = hebergementList.size();
        int capacite = hebergementList.stream().mapToInt(Hebergement::getCapacite).sum();
        double avgTarif = hebergementList.stream().mapToDouble(Hebergement::getTarifParNuit).average().orElse(0.0);

        txtTotalHebergements.setText(String.valueOf(total));
        txtTotalCapacite.setText(String.valueOf(capacite));
        txtMoyenTarif.setText(String.format("%.0f DT", avgTarif));
    }

    private void populateCards() {
        cardsContainer.getChildren().clear();
        for (Hebergement h : hebergementList) {
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
            scene.getStylesheets().add(getClass().getResource("/frontoffice-styles.css").toExternalForm());

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
}
