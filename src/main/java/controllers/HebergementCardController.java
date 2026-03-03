package controllers;

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
import javafx.scene.control.Alert;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;

public class HebergementCardController {

    @FXML private ImageView imgAccommodation;
    @FXML private Text txtType;
    @FXML private Text txtTypeBadge;
    @FXML private Text txtNom;
    @FXML private Text txtLocation;
    @FXML private Text txtTarif;
    @FXML private HBox equipmentsBox;
    @FXML private VBox badgeContainer;

    // Image file lists for each type (only JavaFX-supported formats: jpg, png, gif, bmp)
    // Note: .avif is NOT supported by JavaFX and will fail silently
    private static final String[] HOTEL_IMAGES = {"b.jpg", "c.jpg", "d.jpg", "e.jpg", "f.jpg", "g.jpg", "h.png", "j.jpg"};
    private static final String[] AUBERGE_IMAGES = {"1.jpg", "2.jpg", "3.jpg", "8.jpg", "9.jpg"};
    private static final java.util.Random RANDOM = new java.util.Random();

    private Hebergement hebergement;
    private HebergementFrontController parentController;

    public void setData(Hebergement h, HebergementFrontController parent) {
        this.hebergement = h;
        this.parentController = parent;

        txtNom.setText(h.getNom());
        txtType.setText(h.getType());
        if (txtTypeBadge != null) txtTypeBadge.setText(h.getType().toLowerCase());
        txtLocation.setText(h.getVille() + ", " + h.getPays());
        txtTarif.setText(String.format("%.0f DT", h.getTarifParNuit()));

        // Load a random image based on accommodation type
        loadRandomImage(h.getType());

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

    private void loadRandomImage(String type) {
        try {
            String folder;
            String[] imageFiles;

            if (type != null && type.toLowerCase().contains("auberge")) {
                folder = "/images/heberge/";
                imageFiles = AUBERGE_IMAGES;
            } else {
                folder = "/images/hotel/";
                imageFiles = HOTEL_IMAGES;
            }

            // Try multiple times in case a specific file can't be loaded
            for (int attempt = 0; attempt < 3; attempt++) {
                String randomFile = imageFiles[RANDOM.nextInt(imageFiles.length)];
                String imagePath = folder + randomFile;

                java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
                if (stream != null) {
                    Image image = new Image(stream);
                    if (!image.isError()) {
                        imgAccommodation.setImage(image);
                        return; // Success!
                    }
                }
                System.err.println("Tentative " + (attempt + 1) + " échouée pour: " + imagePath);
            }

            // Ultimate fallback: try the first .jpg from hotel folder
            java.io.InputStream fallback = getClass().getResourceAsStream("/images/hotel/b.jpg");
            if (fallback != null) {
                imgAccommodation.setImage(new Image(fallback));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
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
            URL cssUrl = getClass().getResource("/css/frontoffice-styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Avertissement: Stylesheet /css/frontoffice-styles.css introuvable.");
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Erreur chargement formulaire réservation: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture du formulaire");
            alert.setContentText("Impossible d'ouvrir le formulaire de réservation : " + e.getMessage());
            alert.showAndWait();
        }
    }

    }

