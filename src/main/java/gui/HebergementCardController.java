package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Hebergement;
import java.io.IOException;

public class HebergementCardController {

    @FXML private Text txtTypeIcon;
    @FXML private Text txtType;
    @FXML private Text txtNom;
    @FXML private Text txtLocation;
    @FXML private Text txtTarif;
    @FXML private HBox equipmentsBox;
    @FXML private VBox badgeContainer;

    private Hebergement hebergement;
    private HebergementFrontController parentController;

    public void setData(Hebergement h, HebergementFrontController parent) {
        this.hebergement = h;
        this.parentController = parent;

        txtNom.setText(h.getNom());
        txtType.setText(h.getType());
        txtLocation.setText(h.getVille() + ", " + h.getPays());
        txtTarif.setText(String.format("%.0f DT", h.getTarifParNuit()));

        // Set icon based on type
        String type = h.getType().toLowerCase();
        if (type.contains("hotel")) {
            txtTypeIcon.setText("🏨");
        } else if (type.contains("auberge")) {
            txtTypeIcon.setText("🏠");
        } else {
            txtTypeIcon.setText("🏨"); // Default
        }

        // Dynamic Equipments
        equipmentsBox.getChildren().clear();
        if (h.getEquipements() != null && !h.getEquipements().isEmpty()) {
            String[] items = h.getEquipements().split("[,;]");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    equipmentsBox.getChildren().add(createEquipmentBadge(trimmed));
                }
            }
        }
    }

    private HBox createEquipmentBadge(String equipment) {
        String icon = getEquipmentIcon(equipment);
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 12px;");
        
        Text labelTxt = new Text(equipment);
        labelTxt.setStyle("-fx-font-size: 11px; -fx-fill: #64748b;");
        
        HBox badge = new HBox(5, iconTxt, labelTxt);
        badge.setAlignment(javafx.geometry.Pos.CENTER);
        badge.setStyle("-fx-background-color: #f8fafc; -fx-padding: 5 10; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        return badge;
    }

    private String getEquipmentIcon(String eq) {
        String lower = eq.toLowerCase();
        if (lower.contains("wifi")) return "📶";
        if (lower.contains("park")) return "🚗";
        if (lower.contains("restau")) return "🍽";
        if (lower.contains("spa")) return "💆";
        if (lower.contains("piscine") || lower.contains("pool")) return "🏊";
        if (lower.contains("gym") || lower.contains("sport")) return "💪";
        if (lower.contains("clim") || lower.contains("ac")) return "❄️";
        if (lower.contains("tv")) return "📺";
        return "🔹"; // Default
    }


    @FXML
    private void handleReserve() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ReservationFrontForm.fxml"));
            Parent root = loader.load();

            ReservationFrontFormController controller = loader.getController();
            controller.setHebergement(hebergement);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Réserver : " + hebergement.getNom());
            
            Scene scene = new Scene(root);
            scene.setFill(null); // Allow rounded corners of VBox to show
            
            // Explicitly load the stylesheet to ensure it's applied
            scene.getStylesheets().add(getClass().getResource("/frontoffice-styles.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur chargement formulaire réservation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDetails() {
        System.out.println("Details clicked for " + hebergement.getNom());
    }
}
