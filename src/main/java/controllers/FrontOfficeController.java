package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;

public class FrontOfficeController {

    @FXML private VBox contentArea;
    @FXML private VBox dashboardVBox;

    @FXML private Button homeBtn;
    @FXML private Button clientsBtn;
    @FXML private Button reservationsBtn;
    @FXML private Button hebergementBtn;
    @FXML private Button activitesBtn;
    @FXML private Button avisBtn;

    @FXML
    private void initialize() {
        // Dashboard is already in FXML, just set internal state
        if (homeBtn != null) {
            setActiveButton(homeBtn);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            
            // For child views that need access to this controller's methods,
            // set the controller before loading to avoid reinitialization issues
            
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            
            if (view instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) view).setMaxWidth(Double.MAX_VALUE);
            }
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        setActiveButton(homeBtn);
        if (dashboardVBox != null) {
            contentArea.getChildren().setAll(dashboardVBox);
        }
    }

    @FXML
    private void handleClients() {
        setActiveButton(clientsBtn);
        loadView("/views/PlaceholderView.fxml");
    }

    @FXML
    private void handleReservations() {
        setActiveButton(reservationsBtn);
        loadView("/views/ReservationsFrontView.fxml");
        // Add the friend's CSS for reservation styling
        if (contentArea.getScene() != null) {
            var css = getClass().getResource("/css/app.css");
            if (css != null && !contentArea.getScene().getStylesheets().contains(css.toExternalForm())) {
                contentArea.getScene().getStylesheets().add(css.toExternalForm());
            }
        }
    }

    @FXML
    private void handleHebergement() {
        setActiveButton(hebergementBtn);
        loadView("/views/HebergementFrontView.fxml");
    }

    @FXML
    private void handleActivites() {
        setActiveButton(activitesBtn);
        loadView("/views/PlaceholderView.fxml");
    }

    @FXML
    private void handleAvis() {
        setActiveButton(avisBtn);
        loadView("/views/PlaceholderView.fxml");
    }

    @FXML
    private void handleGetStarted() {
        handleHebergement(); // Navigate to main feature
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logout clicked");
    }

    private void setActiveButton(Button activeButton) {
        if (homeBtn == null || activeButton == null) return;
        
        // Reset all buttons
        List<Button> buttons = List.of(homeBtn, clientsBtn, reservationsBtn, hebergementBtn, activitesBtn, avisBtn);
        for (Button btn : buttons) {
            if (btn != null) btn.getStyleClass().remove("active");
        }
        // Add active class
        activeButton.getStyleClass().add("active");
    }
}
