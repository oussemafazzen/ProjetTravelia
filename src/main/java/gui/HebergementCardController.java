package gui;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Hebergement;

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

        // Add equipments logic here if needed
    }

    @FXML
    private void handleEdit() {
        parentController.openForm(hebergement);
    }

    @FXML
    private void handleReserve() {
        System.out.println("Reserve clicked for " + hebergement.getNom());
    }

    @FXML
    private void handleDetails() {
        System.out.println("Details clicked for " + hebergement.getNom());
    }
}
